package net.qiujuer.library.clink.impl.stealing;

import net.qiujuer.library.clink.core.IoProvider;

import java.nio.channels.SocketChannel;

public class IoTask {
    public final SocketChannel channel;
    public final IoProvider.HandleProviderCallback providerCallback;
    public final int ops;

    public IoTask(SocketChannel channel, int ops,IoProvider.HandleProviderCallback providerCallback){
        this.channel = channel;
        this.providerCallback = providerCallback;
        this.ops = ops;
    }
}
