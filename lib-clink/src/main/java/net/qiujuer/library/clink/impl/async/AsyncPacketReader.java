package net.qiujuer.library.clink.impl.async;

import net.qiujuer.library.clink.core.Frame;
import net.qiujuer.library.clink.core.IoArgsFinal;
import net.qiujuer.library.clink.core.SendPacket;
import net.qiujuer.library.clink.core.ds.BytePriorityNode;
import net.qiujuer.library.clink.frames.*;

import java.io.Closeable;
import java.io.IOException;

public class AsyncPacketReader implements Closeable {
    private volatile IoArgsFinal args=new IoArgsFinal();
    private final PacketProvider provider;

    private volatile BytePriorityNode<Frame> node;
    private volatile int nodeSize=0;

    private short lastIdentifier=0;

    public short generateIdenttifier(){
        short identifier=++lastIdentifier;
        if(identifier==255){
            lastIdentifier=0;
        }
        return identifier;
    }

    AsyncPacketReader(PacketProvider provider) {
        this.provider = provider;
    }

    synchronized void cancel(SendPacket packet) {
            if (nodeSize == 0) {
                return;
            }
        for (BytePriorityNode<Frame> x = node, before = null; x != null; before = x, x = x.next) {
            Frame frame = x.item;
            if (frame instanceof AbsSendPacketFrame) {
                AbsSendPacketFrame packetFrame = (AbsSendPacketFrame) frame;
                if (packetFrame.getPacket() == packet) {
                    boolean removable = packetFrame.abort();
                    //removable是false说明帧已经发送则构建取消发送的帧，如果为true说明改帧没有被发送将其从队列移除，并进行判断其是否为头帧
                    if (removable) {
                        // A B C
                        removeFrame(x, before);
                        if (packetFrame instanceof SendHeaderFrame) {
                            // 头帧，并且未被发送任何数据，直接取消后不需要添加取消发送帧
                            break;
                        }
                    }

                    // 添加终止帧，通知到接收方
                    CancelSendFrame cancelSendFrame = new CancelSendFrame(packetFrame.getBodyIdentifier());
                    appendNewFrame(cancelSendFrame);

                    // 意外终止，返回失败
                    provider.completedPacket(packet, false);
                    break;
                }
            }
        }
    }

    private synchronized void removeFrame(BytePriorityNode<Frame> removeNode, BytePriorityNode<Frame> before) {
        if (before == null) {
            // A B C
            // B C
            node = removeNode.next;
        } else {
            // A B C
            // A C
            before.next = removeNode.next;
        }
        nodeSize--;
        if (node == null) {
            requestTakePacket();
        }
    }

    boolean requestTakePacket() {
        synchronized (this) {
            if (nodeSize >= 1) {
                return true;
            }
        }
        SendPacket packet= provider.takePacket();
        if(packet!=null){
            //生成头帧
            short identifier = generateIdenttifier();
            SendHeaderFrame frame= new SendHeaderFrame(identifier,packet);
            appendNewFrame(frame);
        }
        synchronized (this) {
            return nodeSize != 0;
        }
    }

    boolean requestSendHeartbeatFrame() {
        for (BytePriorityNode<Frame> x = node; x != null; x = x.next) {
            Frame frame = x.item;
            if (frame.getBodyType() == Frame.TYPE_COMMAND_HEARTBEAT) ;
                return false;
        }
        appendNewFrame(new HeartbeatSendFrame());
        return true;
    }

    private synchronized void appendNewFrame(Frame frame) {
        BytePriorityNode<Frame> newNode = new BytePriorityNode<>(frame);
        if (node != null) {
            // 使用优先级别添加到链表
            node.appendWithPriority(newNode);
        } else {
            node = newNode;
        }
        nodeSize++;
    }

    @Override
    public synchronized void close(){
        while (node != null) {
            Frame frame = node.item;
            if (frame instanceof AbsSendPacketFrame) {
                SendPacket packet = ((AbsSendPacketFrame) frame).getPacket();
                provider.completedPacket(packet, false);
            }
            //关闭了当前的node,应该将指向下一个node
            node=node.next;
        }
        nodeSize = 0;
        node = null;
    }

    IoArgsFinal fillData() {
        Frame frame = getCurrentFrame();
        if(frame==null){
            return null;
        }
        try {
            if(frame.handle(args)){
                Frame nextFrame=frame.nextFrame();
                if(nextFrame!=null){
                    appendNewFrame(nextFrame);
                }else if(frame instanceof SendEntityFrame){
                    provider.completedPacket(((SendEntityFrame) frame).getPacket(),true);
                }
                popCurrentFrame();
            }
            return args;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private synchronized void popCurrentFrame() {
        node = node.next;
        nodeSize--;
        if (node == null) {
            requestTakePacket();
        }
    }

    private synchronized Frame getCurrentFrame() {
        if (node == null) {
            return null;
        }
        return node.item;
    }

    interface PacketProvider{
        SendPacket takePacket();

        void completedPacket(SendPacket packet,boolean isSucceed);
    }
}
