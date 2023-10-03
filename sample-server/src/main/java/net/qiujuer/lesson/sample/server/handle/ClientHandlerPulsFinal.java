package net.qiujuer.lesson.sample.server.handle;


import net.qiujuer.lesson.sample.foo.Foo;
import net.qiujuer.library.clink.core.ConnectorFinal;
import net.qiujuer.library.clink.core.Packet;
import net.qiujuer.library.clink.core.ReceivedPacket;
import net.qiujuer.library.clink.utils.CloseUtils;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;


public class ClientHandlerPulsFinal extends ConnectorFinal {
    private final File cachePath;

    @Override
    protected void onReceivedPacket(ReceivedPacket packet) {
        super.onReceivedPacket(packet);
        if(packet.type() == Packet.TYPE_MEMORY_STRING){
            String string = (String) packet.entity();
//            System.out.println(key.toString() + ":" + string);
            clientHandlerCallback.onNewMessageArrived(this,string);
        } else if(packet.type()==Packet.TYPE_STREAM_FILE){
            File file = (File) packet.entity();
            clientHandlerCallback.onNewFileArrived(this, file.getAbsolutePath());
        }
    }

    private final ClientHandlerCallback clientHandlerCallback;
    private final String clientInfo;

    public ClientHandlerPulsFinal(File cachePath, SocketChannel socketChannel, ClientHandlerCallback clientHandlerCallback) throws IOException {
        this.cachePath = cachePath;
        this.clientHandlerCallback = clientHandlerCallback;
        this.clientInfo = socketChannel.getRemoteAddress().toString();
        System.out.println("新客户端连接：" + clientInfo);
        setup(socketChannel);
    }


    public void exit() {
        CloseUtils.close(this);
        System.out.println("客户端已退出：" + clientInfo);
    }

    private void exitBySelf() {
        exit();
        clientHandlerCallback.onSelfClosed(this);
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {
        super.onChannelClosed(channel);
        exitBySelf();
    }

    @Override
    protected File createNewReceiveFile() {
        return Foo.createRandomTemp(cachePath);
    }


    public interface ClientHandlerCallback {
        // 自身关闭通知
        void onSelfClosed(ClientHandlerPulsFinal handler);

        // 收到string消息时通知
        void onNewMessageArrived(ClientHandlerPulsFinal handler, String msg);

        //收到文件时通知
        void onNewFileArrived(ClientHandlerPulsFinal handler, String str);
    }
}
