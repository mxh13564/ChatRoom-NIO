package net.qiujuer.lesson.sample.server;

import net.qiujuer.lesson.sample.foo.Foo;
import net.qiujuer.lesson.sample.server.handle.ClientHandlerPulsFinal;
import net.qiujuer.library.clink.box.FileSendPacket;
import net.qiujuer.library.clink.core.ScheduleJob;
import net.qiujuer.library.clink.core.schedule.IdleTimeoutScheduleJob;
import net.qiujuer.library.clink.utils.CloseUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class TCPServerPlusFinal implements ClientHandlerPulsFinal.ClientHandlerCallback {
    private final File cachePath;
    private final int port;
    private ClientListener listener;
    private final List<ClientHandlerPulsFinal> clientHandlerList = new ArrayList<>();
    private final ExecutorService forwardingThreadPoolExecutor;
    private final ExecutorService forwardingThreadPoolExecutorfile;
    private Selector selector;
    private ServerSocketChannel server;

    public TCPServerPlusFinal(File cachePath, int port) {
        this.cachePath = cachePath;
        this.port = port;
        // 转发线程池
        this.forwardingThreadPoolExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("异步提交线程");
                return thread;
            }
        });
        this.forwardingThreadPoolExecutorfile=Executors.newSingleThreadExecutor();
    }

    public boolean start() {
        try {
            selector = Selector.open();
            ServerSocketChannel server = ServerSocketChannel.open();
            // 设置为非阻塞
            server.configureBlocking(false);
            // 绑定本地端口
            server.socket().bind(new InetSocketAddress(port));
            // 注册客户端连接到达监听
            server.register(selector, SelectionKey.OP_ACCEPT);

            this.server = server;

            System.out.println("服务器信息：" + server.getLocalAddress().toString());

            // 启动客户端监听
            ClientListener listener = this.listener = new ClientListener();
            listener.start();
            listener.setName("listener");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (listener != null) {
            listener.exit();
        }

        CloseUtils.close(server);
        CloseUtils.close(selector);
        //在关闭时会自动从数组中移除，所以在遍历该数组时会报错，更该如下，将数组赋值给另一个数组遍历另一个数组，清空原数组;
        ClientHandlerPulsFinal[] clientHandlerPulsFinals;
        synchronized (TCPServerPlusFinal.this) {
            clientHandlerPulsFinals = clientHandlerList.toArray(new ClientHandlerPulsFinal[0]);
            clientHandlerList.clear();
        }

        for (ClientHandlerPulsFinal clientHandler : clientHandlerPulsFinals) {
            clientHandler.exit();
        }

        // 停止线程池
        forwardingThreadPoolExecutor.shutdownNow();
    }

    public synchronized void broadcast(String str) {
        ClientHandlerPulsFinal[] clientHandlerPulsFinals;
        synchronized (clientHandlerList) {
            clientHandlerPulsFinals = clientHandlerList.toArray(new ClientHandlerPulsFinal[0]);
        }

        for (ClientHandlerPulsFinal clientHandler : clientHandlerPulsFinals) {
            clientHandler.send(str);
        }
    }

    @Override
    public void onSelfClosed(ClientHandlerPulsFinal handler) {
        synchronized (clientHandlerList) {
            clientHandlerList.remove(handler);
        }
    }

    @Override
    public void onNewMessageArrived(ClientHandlerPulsFinal handler, String msg) {
        // 异步提交转发任务
        forwardingThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (clientHandlerList) {
                    for (ClientHandlerPulsFinal clientHandler : clientHandlerList) {
                        // 对其他客户端发送消息
                        clientHandler.send(msg);
                    }
                }
            }
        });
    }


    @Override
    public void onNewFileArrived(ClientHandlerPulsFinal handler, String str) {
        forwardingThreadPoolExecutorfile.execute(()->{
            synchronized (clientHandlerList) {
                File file=new File(str);
                if(file.exists() && file.isFile()) {
                    FileSendPacket packet = new FileSendPacket(file);
                    for (ClientHandlerPulsFinal clientHandler : clientHandlerList) {
                        // 对其他客户端发送消息
                        clientHandler.send(packet);
                    }
                }
            }
        });
    }

    private class ClientListener extends Thread {
        private boolean done = false;

        @Override
        public void run() {
            super.run();
            Selector selector = TCPServerPlusFinal.this.selector;
            System.out.println("服务器准备就绪～");
            // 等待客户端连接
            do {
                // 得到客户端
                try {
                    if (selector.select() == 0) {
                        if (done) {
                            break;
                        }
                        continue;
                    }

                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        if (done) {
                            break;
                        }

                        SelectionKey key = iterator.next();
                        iterator.remove();

                        // 检查当前Key的状态是否是我们关注的
                        // 客户端到达状态
                        if (key.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                            // 非阻塞状态拿到客户端连接
                            SocketChannel socketChannel = serverSocketChannel.accept();

                            try {
                                // 客户端构建异步线程
                                ClientHandlerPulsFinal clientHandler = new ClientHandlerPulsFinal(cachePath, socketChannel, TCPServerPlusFinal.this);
                                ScheduleJob scheduleJob=new IdleTimeoutScheduleJob(4*60, TimeUnit.SECONDS,clientHandler);
                                clientHandler.schedule(scheduleJob);
                                // 添加同步处理
                                synchronized (clientHandlerList) {
                                    clientHandlerList.add(clientHandler);
//                                    System.out.println("当前客户端数量：" + clientHandlerList.size());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("客户端连接异常：" + e.getMessage());
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } while (!done);

            System.out.println("服务器已关闭！");
        }

        void exit() {
            done = true;
            // 唤醒当前的阻塞
            selector.wakeup();
        }
    }
}
