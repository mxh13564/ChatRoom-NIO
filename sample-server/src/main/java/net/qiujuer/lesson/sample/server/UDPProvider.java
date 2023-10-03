package net.qiujuer.lesson.sample.server;

import net.qiujuer.lesson.sample.foo.constants.UDPConstants;
import net.qiujuer.library.clink.utils.ByteUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

class UDPProvider {
    private static Provider PROVIDER_INSTANCE;

    static void start(int port) {
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, port);
        provider.start();
        PROVIDER_INSTANCE = provider;
    }

    static void stop() {
        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    private static class Provider extends Thread {
        private final byte[] sn;
        private final int port;
        private boolean done = false;
        private DatagramSocket ds = null;
        // 存储消息的Buffer
        final byte[] buffer = new byte[128];

        Provider(String sn, int port) {
            super("Server-UDPProvider-Thread");
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run() {
            super.run();

            System.out.println("UDPProvider Started.");

            try {
                // 监听端口
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);
                // 接收消息的Packet
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while (!done) {

                    // 接收
                    ds.receive(receivePack);

                    // 打印接收到的信息与发送者的信息
                    // 发送者的IP地址
                    String clientIp = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();
                    int clientDataLen = receivePack.getLength();
                    byte[] clientData = receivePack.getData();
                    boolean isValid = clientDataLen >= (UDPConstants.HEADER.length + 2 + 4)
                            && ByteUtils.startsWith(clientData, UDPConstants.HEADER);

                    System.out.println("UDPProvider receive form ip:" + clientIp
                            + "\tport:" + clientPort + "\tdataValid:" + isValid);

                    if (!isValid) {
                        // 无效继续
                        continue;
                    }
                    // 解析命令与回送端口
                    ByteBuffer byteBuffer = ByteBuffer.wrap(clientData, UDPConstants.HEADER.length, clientDataLen);
                    final short cmd = byteBuffer.getShort();
                    final int responsePort = byteBuffer.getInt();
                    // 判断合法性
                    if (cmd == 1 && responsePort > 0) {
                        // 构建一份回送数据
                        ByteBuffer Buffer = ByteBuffer.wrap(buffer);
                        Buffer.put(UDPConstants.HEADER);
                        Buffer.putShort((short) 2);
                        Buffer.putInt(port);
                        Buffer.put(sn);
                        int len = Buffer.position();
                        // 直接根据发送者构建一份回送信息
                        DatagramPacket responsePacket = new DatagramPacket(buffer,
                                len,
                                receivePack.getAddress(),
                                responsePort);
                        ds.send(responsePacket);
                        System.out.println("UDPProvider response to:" + clientIp + "\tport:" + responsePort + "\tdataLen:" + len);
                    } else {
                        System.out.println("UDPProvider receive cmd nonsupport; cmd:" + cmd + "\tport:" + port);
                    }
                }
            } catch (Exception ignored) {
            } finally {
                close();
            }

            // 完成
            System.out.println("UDPProvider Finished.");
        }

        private void close() {
            if (ds != null) {
                ds.close();
                ds = null;
            }
        }

        /**
         * 提供结束
         */
        void exit() {
            done = true;
            close();
        }
    }
}
