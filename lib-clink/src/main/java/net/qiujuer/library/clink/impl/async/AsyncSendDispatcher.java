package net.qiujuer.library.clink.impl.async;

import net.qiujuer.library.clink.core.*;
import net.qiujuer.library.clink.utils.CloseUtils;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncSendDispatcher implements SendDispatcher ,IoArgsFinal.IoArgsEventProcessor, AsyncPacketReader.PacketProvider {
    private final SenderFinal sender;
    private final Queue<SendPacket> queue=new ConcurrentLinkedDeque<>();
    private final AtomicBoolean isSending=new AtomicBoolean();
    private final AtomicBoolean isClosed=new AtomicBoolean();

    private final AsyncPacketReader reader=new AsyncPacketReader(this);

    public AsyncSendDispatcher(SenderFinal sender) throws IOException {
        this.sender = sender;
        sender.setSendListener(this);
    }


    @Override
    public void send(SendPacket packet) {
        queue.offer(packet);
        requestSend();
    }

    @Override
    public SendPacket takePacket() {
        SendPacket packet;
        packet = queue.poll();
        if(packet==null){
            return null;
        }
        if(packet.isCanceled()){
            //已取消，不用发送
            return takePacket();
        }
        return packet;
    }

    @Override
    public void completedPacket(SendPacket packet, boolean isSucceed) {
        CloseUtils.close(packet);
    }


    private void requestSend() {
        synchronized (isSending){
            if(isSending.get() || isClosed.get()){
                return ;
            }

            if(reader.requestTakePacket()){
                try {
                    isSending.set(true);
                    boolean isSucceed = sender.postSendAsync();
                    if(!isSucceed){
                        isSending.set(false);
                    }
                }catch (IOException e){
                    closeAndNotify();
                }
            }
        }
    }


    private void closeAndNotify() {

    }

    @Override
    public void cancel(SendPacket packet) {
        boolean ret;
        ret = queue.remove(packet);//队列中还有返回true；队列里面没有返回false
        if(ret){
            packet.cancel();
            return ;
        }
        reader.cancel(packet);
    }

    @Override
    public void sendHeartbeat() {
        if(queue.size()>0){
            return ;
        }
        if(reader.requestSendHeartbeatFrame()){
            requestSend();
        }
    }

    @Override
    public void close() throws IOException {
        if(isClosed.compareAndSet(false,true)){
            reader.close();
            queue.clear();
            synchronized (isSending) {
                isSending.set(false);
            }
        }
    }

    @Override
    public IoArgsFinal providerIoArgs() {
        return isClosed.get() ? null : reader.fillData();
    }

    @Override
    public void onConsumeFailed(IoArgsFinal args, Exception e) {
        e.printStackTrace();
        synchronized (isSending){
            isSending.set(false);
        }
        requestSend();
    }

    @Override
    public void onConsumeCompleted(IoArgsFinal args) {
        synchronized (isSending){
            isSending.set(false);
        }
        requestSend();
    }
}
