package net.qiujuer.library.clink.impl.async;

import net.qiujuer.library.clink.core.*;
import net.qiujuer.library.clink.utils.CloseUtils;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsynReceiveDispatcher implements ReceivedDispatcher ,IoArgsFinal.IoArgsEventProcessor,AsyncPacketWriter.PacketProvider{
    private final AtomicBoolean isClosed =new AtomicBoolean(false);

    private final ReceiverFinal receiver;
    private final ReceivedPacketCallback callback;

    private ReceivedPacket<?,?> packetTemp;
    private final AsyncPacketWriter writer = new AsyncPacketWriter(this) ;

    public AsynReceiveDispatcher(ReceiverFinal receiver, ReceivedPacketCallback callback) throws IOException {
        this.receiver = receiver;
        this.receiver.setReceiveListener(this);
        this.callback = callback;
    }

    @Override
    public void start() {
        registerReceive();
    }

    private void registerReceive() {
        try {
            receiver.postReceiveAsync();
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    private void closeAndNotify() {
       CloseUtils.close((Closeable) this);
    }

    @Override
    public void stop() {

    }

    @Override
    public void close() throws IOException {
        if(isClosed.compareAndSet(false,true)){
            writer.close();
        }
    }

    @Override
    public IoArgsFinal providerIoArgs() {
        IoArgsFinal ioArgs = writer.takeIoArgs();
        ioArgs.startWriting();
        return ioArgs;
    }

    @Override
    public void onConsumeFailed(IoArgsFinal args, Exception e) {
        e.printStackTrace();
    }

    @Override
    public void onConsumeCompleted(IoArgsFinal args) {
        if(isClosed.get()){
            return ;
        }

        args.finishWriting();

        do{
            writer.consumeIoArgs(args);
        }while (args.remained() && !isClosed.get());
        registerReceive();
    }

    @Override
    public ReceivedPacket takePacket(byte type, long length, byte[] headerInfo) {
        packetTemp=callback.onArrivedNewPacket(type,length);
        return packetTemp;
    }

    @Override
    public void completedPacket(ReceivedPacket packet, boolean isSucceed) {
        CloseUtils.close(packet);
        callback.onReceivedPacketComleted(packet);
    }

    @Override
    public void onReceivedHeartbeat() {
        callback.onReceivedHeartbeat();
    }
}
