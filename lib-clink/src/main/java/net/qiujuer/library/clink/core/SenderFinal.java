package net.qiujuer.library.clink.core;

import java.io.Closeable;
import java.io.IOException;

public interface SenderFinal extends Closeable {
    void setSendListener(IoArgsFinal.IoArgsEventProcessor processor) throws IOException;
    boolean postSendAsync() throws IOException;
    long getLastWriteTime();
}
