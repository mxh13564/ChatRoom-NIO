package net.qiujuer.library.clink.core;


import java.io.Closeable;

//缓存所有需要发送的数据，通过队列对数据进行发送
//并且在发送数据时，实现对数据的基本包装
public interface SendDispatcher extends Closeable {
    void send(SendPacket packet);

    void cancel(SendPacket packet);

    void sendHeartbeat();
}
