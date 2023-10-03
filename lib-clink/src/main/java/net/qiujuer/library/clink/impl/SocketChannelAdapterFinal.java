package net.qiujuer.library.clink.impl;

import net.qiujuer.library.clink.core.*;
import net.qiujuer.library.clink.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketChannelAdapterFinal implements SenderFinal, ReceiverFinal, Cloneable {
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final SocketChannel channel;
    private final IoProvider ioProvider;
    private final OnChannelStatusChangedListener listener;
    private IoArgsFinal.IoArgsEventProcessor receiveIoEventProcessor;
    private IoArgsFinal.IoArgsEventProcessor sendIoEventProcessor;

    private volatile long lastReadTime=System.currentTimeMillis();
    private volatile long lastWriteTime=System.currentTimeMillis();

    public SocketChannelAdapterFinal(SocketChannel channel, IoProvider ioProvider,
                                     OnChannelStatusChangedListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;

        channel.configureBlocking(false);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            // 解除注册回调
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);
            // 关闭
            CloseUtils.close(channel);
            // 回调当前Channel已关闭
            listener.onChannelClosed(channel);
        }
    }

    private final IoProvider.HandleProviderCallback inputCallback = new IoProvider.HandleProviderCallback() {
        @Override
        protected void canProviderOutput(IoArgsFinal args) {
            if (isClosed.get()) {
                return;
            }
            lastReadTime=System.currentTimeMillis();

            IoArgsFinal.IoArgsEventProcessor processor=receiveIoEventProcessor;
            if(args==null){
                args = processor.providerIoArgs();
            }
            try {
                // 具体的读取操作
                if(args==null){
                    processor.onConsumeFailed(null,new IOException("providerIoArgs is null！"));
                } else {
                    int count = args.readFrom(channel);
                    if (count == 0) {
                        System.out.println("Current read zero data!");
                    }

                    if (args.remained()) {
                        attach = args;
                        ioProvider.registerInput(channel, this);
                    } else {
                        attach = null;
                        // 写入完成回调
                        processor.onConsumeCompleted(args);
                    }
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapterFinal.this);
            }
        }
    };


    private final IoProvider.HandleProviderCallback outputCallback = new IoProvider.HandleProviderCallback() {
        @Override
        protected void canProviderOutput(IoArgsFinal args) {
            if (isClosed.get()) {
                return;
            }

            lastWriteTime=System.currentTimeMillis();

            IoArgsFinal.IoArgsEventProcessor processor=sendIoEventProcessor;
            if(args==null){
                args = processor.providerIoArgs();
            }
            try {
                if(args==null){
                    processor.onConsumeFailed(null,new IOException("providerIoArgs is null！"));
                }else {
                    int count = args.writeTo(channel);
                    if (count == 0) {
                        System.out.println("Current write zero data!");
                    }

                    if (args.remained()) {
                        attach = args;
                        ioProvider.registerOutput(channel, this);
                    } else {
                        attach = null;
                        // 写入完成回调
                        processor.onConsumeCompleted(args);
                    }
                }
            } catch (IOException ignored) {
                CloseUtils.close(SocketChannelAdapterFinal.this);
            }
        }

    };

    @Override
    public void setReceiveListener(IoArgsFinal.IoArgsEventProcessor processor) throws IOException {
        receiveIoEventProcessor=processor;
    }

    @Override
    public boolean postReceiveAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }
        inputCallback.checkAttachNull();
        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public long getLastReadTime() {
        return lastReadTime;
    }

    @Override
    public void setSendListener(IoArgsFinal.IoArgsEventProcessor processor) throws IOException {
        sendIoEventProcessor=processor;
    }

    @Override
    public boolean postSendAsync() throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current channel is closed!");
        }
        outputCallback.checkAttachNull();

        return ioProvider.registerOutput(channel, outputCallback);
//        return true;outputCallback.run();
    }

    @Override
    public long getLastWriteTime() {
        return lastWriteTime;
    }


    //回调channel异常
    public interface OnChannelStatusChangedListener {
        void onChannelClosed(SocketChannel channel);
    }
}
