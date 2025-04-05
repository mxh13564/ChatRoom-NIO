package net.qiujuer.lesson.sample.server;

import net.qiujuer.lesson.sample.foo.Foo;
import net.qiujuer.lesson.sample.foo.constants.TCPConstants;
import net.qiujuer.library.clink.core.IoContext;
import net.qiujuer.library.clink.impl.IoStealingSelectorProvider;
import net.qiujuer.library.clink.impl.SchedulerImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {
    public static void main(String[] args) throws IOException {
        File cachePath= Foo.getCacheDir("server");
        IoContext.setup()
                .ioProvider(new IoStealingSelectorProvider(4))
                .scheduler(new SchedulerImpl(1))
                .start();

        TCPServerPlusFinal tcpServer = new TCPServerPlusFinal(cachePath, TCPConstants.PORT_SERVER);
        boolean isSucceed = tcpServer.start();
        if (!isSucceed) {
            System.out.println("Start TCP server failed!");
            return;
        }

//        UDPProvider.start(TCPConstants.PORT_SERVER);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str ;
        do {
            str = bufferedReader.readLine();
            if(str == null ||Foo.COMMAND_EXIT.equalsIgnoreCase(str)) break;
            if(str.length() == 0) continue;
            tcpServer.broadcast(str);
        } while (true);

//        UDPProvider.stop();
        tcpServer.stop();

        IoContext.close();
    }
}
