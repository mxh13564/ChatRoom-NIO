package net.qiujuer.library.clink.core;

import net.qiujuer.library.clink.box.ByteReceivePacket;
import net.qiujuer.library.clink.box.FileReceivePacket;
import net.qiujuer.library.clink.box.StringReceivedPacket;
import net.qiujuer.library.clink.box.StringSendPacket;
import net.qiujuer.library.clink.impl.SocketChannelAdapterFinal;
import net.qiujuer.library.clink.impl.async.AsynReceiveDispatcher;
import net.qiujuer.library.clink.impl.async.AsyncSendDispatcher;
import net.qiujuer.library.clink.utils.CloseUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class ConnectorFinal implements Closeable, SocketChannelAdapterFinal.OnChannelStatusChangedListener {
    protected UUID key = UUID.randomUUID();
    public static SocketChannel channel;
    private SenderFinal sender;
    private ReceiverFinal receiver;
    private SendDispatcher sendDispatcher;
    private ReceivedDispatcher receivedDispatcher;
    private final List<ScheduleJob> scheduleJobs=new ArrayList<>(4);

    public void setup(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;

        IoContext context = IoContext.get();
        SocketChannelAdapterFinal adapter = new SocketChannelAdapterFinal(channel, context.getIoProvider(), this);//这个this回调channel异常,实现onChannelClosed方法

        this.sender = adapter;
        this.receiver = adapter;

        sendDispatcher = new AsyncSendDispatcher(sender);
        receivedDispatcher=new AsynReceiveDispatcher(receiver,receivedPacketCallback);
        receivedDispatcher.start();
    }

    public void send(String msg){
        SendPacket packet=new StringSendPacket(msg);
        sendDispatcher.send(packet);
    }

    public void send(SendPacket packet){
        sendDispatcher.send(packet);
    }

    public long getLastActiveTime(){
        return Math.max(sender.getLastWriteTime(),receiver.getLastReadTime());
    }


    @Override
    public void close() throws IOException {
        receivedDispatcher.close();
        sendDispatcher.close();
        sender.close();
        receiver.close();
        channel.close();
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        for (ScheduleJob scheduleJob : scheduleJobs) {
            scheduleJob.unSchedule();
        }
        scheduleJobs.clear();
        CloseUtils.close(this);
    }

    protected void onReceivedPacket(ReceivedPacket packet){
//         System.out.println(key.toString() + "[New Packet]-Type:" + packet.type() + ", length:" + packet.length);
    }

    private ReceivedDispatcher.ReceivedPacketCallback receivedPacketCallback = new ReceivedDispatcher.ReceivedPacketCallback() {
        @Override
        public ReceivedPacket<?, ?> onArrivedNewPacket(byte type, long length) {
            byte a=type;
            switch (type){
                case Packet.TYPE_MEMORY_BYTES:
                    return new ByteReceivePacket(length);
                case Packet.TYPE_MEMORY_STRING:
                    return new StringReceivedPacket(length);
                case Packet.TYPE_STREAM_FILE:
                    return new FileReceivePacket(length,createNewReceiveFile());
                case Packet.TYPE_STREAM_DIRECT:
                    return new ByteReceivePacket(length);
                default:
                    throw new UnsupportedOperationException("Unsupported packet type: " + type);
            }
        }

        @Override
        public void onReceivedPacketComleted(ReceivedPacket packet) {
            onReceivedPacket(packet);
        }

        @Override
        public void onReceivedHeartbeat() {
//            System.out.println(key.toString() + ":[Heartbeat]");
        }
    };

    protected abstract File createNewReceiveFile();

    public void fireIdleTimeoutEvent() {
        sendDispatcher.sendHeartbeat();
    }

    public void fireExceptionCaught(Throwable throwable) {
    }
    public void schedule(ScheduleJob job){
        if(scheduleJobs.contains(job)){
                return;
        }
        IoContext context=IoContext.get();
        Scheduler scheduler=context.getScheduler();
        job.schedule(scheduler);
        scheduleJobs.add(job);
    }
}
