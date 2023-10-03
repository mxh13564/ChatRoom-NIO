package net.qiujuer.library.clink.impl;

import net.qiujuer.library.clink.core.IoProvider;
import net.qiujuer.library.clink.impl.stealing.IoTask;
import net.qiujuer.library.clink.impl.stealing.StealingSelectorThread;
import net.qiujuer.library.clink.impl.stealing.StealingService;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class IoStealingSelectorProvider implements IoProvider {
    private IoStealingThread[] threads;
    private StealingService stealingService;

    public IoStealingSelectorProvider(int size) throws IOException {

        IoStealingThread[] threads=new IoStealingThread[size];
        for (int i = 0; i < size; i++) {
            Selector selector=Selector.open();
            threads[i] = new IoStealingThread("IoProvider-Thread-"+(i+1),selector);
        }

        StealingService stealingService=new StealingService(threads,10);
        for (IoStealingThread thread : threads) {
            thread.setStealingService(stealingService);
            thread.start();
        }
        this.threads=threads;
        this.stealingService=stealingService;
    }
    @Override
    public boolean registerInput(SocketChannel channel, HandleProviderCallback callback) {
        StealingSelectorThread thread = stealingService.getNotBusyThread();
        if(thread!=null){
            return thread.register(channel, SelectionKey.OP_READ,callback);
        }
        return false;
    }

    @Override
    public boolean registerOutput(SocketChannel channel, HandleProviderCallback callback) {
        StealingSelectorThread thread = stealingService.getNotBusyThread();
        if(thread!=null){
            return thread.register(channel, SelectionKey.OP_WRITE,callback);
        }
        return false;
    }

    @Override
    public void unRegisterInput(SocketChannel channel) {
        for (IoStealingThread thread : threads) {
            thread.unregister(channel);
        }
    }

    @Override
    public void unRegisterOutput(SocketChannel channel) {

    }

    @Override
    public void close() throws IOException {
        stealingService.shutdown();
    }

    static class IoStealingThread extends StealingSelectorThread{

        protected IoStealingThread(String name,Selector selector) {
            super(selector);
            setName(name);
        }

        @Override
        protected boolean processTask(IoTask task) {
            task.providerCallback.run();
            return false;
        }
    }
}
