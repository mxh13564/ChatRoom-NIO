package net.qiujuer.library.clink.core;

import java.io.Closeable;

//把一份或者多份IoArgs组合成一份Packet
public interface ReceivedDispatcher extends Closeable{
    void start();

    void stop();

    interface ReceivedPacketCallback{
        ReceivedPacket<?,?> onArrivedNewPacket(byte type,long length);

        void onReceivedPacketComleted(ReceivedPacket packet);

        void onReceivedHeartbeat();
    }
}
