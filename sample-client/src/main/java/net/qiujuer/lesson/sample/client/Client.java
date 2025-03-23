package net.qiujuer.lesson.sample.client;


import net.qiujuer.lesson.sample.client.bean.ServerInfo;
import net.qiujuer.lesson.sample.foo.Foo;
import net.qiujuer.library.clink.box.FileSendPacket;
import net.qiujuer.library.clink.core.IoContext;
import net.qiujuer.library.clink.impl.IoSelectorProvider;
import net.qiujuer.library.clink.impl.IoStealingSelectorProvider;

import java.io.*;

public class Client {
    public static void main(String[] args) throws IOException {
        File cachePath= Foo.getCacheDir("client");
        IoContext.setup()
                .ioProvider(new IoStealingSelectorProvider(1))
                .start();
        ServerInfo info = UDPSearcher.searchServer(100000);
        System.out.println("Server:" + info);
        if (info != null) {
            TCPClientFinal tcpClient = null;
            try {
                tcpClient = TCPClientFinal.startWith(info,cachePath);
                if (tcpClient == null) {
                    return;
                }
                write(tcpClient);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (tcpClient != null) {
                    tcpClient.exit();

                }
            }
        }
        IoContext.close();
    }


    private static void write(TCPClientFinal tcpClient) throws IOException {
        // 构建键盘输入流
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        do {
            // 键盘读取一行
            String str = input.readLine();
            if (str == null ||Foo.COMMAND_EXIT.equalsIgnoreCase(str)) {
                continue;
            }
            if(str.length() == 0) continue;
            if(str.startsWith("--f")){
                String[] array=str.split(" ");
                if(array.length>=2){
                    String filePath=array[1];
                    File file=new File(filePath);
                    if(file.exists() && file.isFile()){
                        FileSendPacket packet=new FileSendPacket(file);
                        tcpClient.send(packet);
                    }
                }
            }
            // 发送到服务器
            tcpClient.send(str);
        } while (true);
    }

}
