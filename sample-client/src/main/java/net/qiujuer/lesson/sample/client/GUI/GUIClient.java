package net.qiujuer.lesson.sample.client.GUI;

import net.qiujuer.lesson.sample.foo.Foo;
import net.qiujuer.library.clink.core.IoContext;
import net.qiujuer.library.clink.impl.IoStealingSelectorProvider;

import java.io.File;
import java.io.IOException;

public class GUIClient {
    public static void main(String[] args) throws IOException {
        File cachePath= Foo.getCacheDir("client");
        IoContext.setup()
                .ioProvider(new IoStealingSelectorProvider(1))
                .start();
        TCPClientFinalForGUI.chatRoomClient = new TCPClientFinalForGUI();
        TCPClientFinalForGUI.login = new Login();
    }
}
