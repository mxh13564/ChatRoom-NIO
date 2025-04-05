import net.qiujuer.lesson.sample.client.TCPClientFinal;
import net.qiujuer.lesson.sample.client.UDPSearcher;
import net.qiujuer.lesson.sample.client.bean.ServerInfo;
import net.qiujuer.lesson.sample.foo.Foo;
import net.qiujuer.library.clink.core.IoContext;
import net.qiujuer.library.clink.impl.IoStealingSelectorProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientTest {
    private static boolean done;

    public static void main(String[] args) throws IOException {

        ServerInfo info = UDPSearcher.searchServer(10000);
        System.out.println("Server:" + info);
        if (info == null) {
            return;
        }

        File cachePath= Foo.getCacheDir("client/test");
        IoContext.setup()
                .ioProvider(new IoStealingSelectorProvider(4))
                .start();

        // 当前连接数量
        int size = 0;
        final List<TCPClientFinal> tcpClients = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            try {
                TCPClientFinal tcpClient = TCPClientFinal.startWith(info,cachePath);
                if (tcpClient == null) {
                    System.out.println("连接异常");
                    continue;
                }

                tcpClients.add(tcpClient);

                System.out.println("连接成功：" + (++size));

            } catch (IOException e) {
                System.out.println("连接异常");
            }

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        System.in.read();

        Runnable runnable = () -> {
            while (!done) {
                TCPClientFinal[] tcpClientFinals=tcpClients.toArray(new TCPClientFinal[0]);
                for (TCPClientFinal tcpClient : tcpClientFinals) {
                    tcpClient.send("Hello~~");
                }
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        List<Thread> threads = new ArrayList<>(4);
        for (int i = 0; i < 4; i ++) {
            Thread thread = new Thread(runnable);
            thread.start();
            threads.add(thread);
        }
        System.in.read();

        // 等待线程完成
        done = true;

        // 客户端结束操作
        for (TCPClientFinal tcpClient : tcpClients) {
            tcpClient.exit();
        }

        IoContext.close();

        for (Thread thread : threads) {
            try {
                thread.interrupt();
            } catch (Exception e) {
            }
        }
    }

}
