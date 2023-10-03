package net.qiujuer.library.clink.core;

import java.io.Closeable;
import java.io.IOException;

public interface ReceiverFinal extends Closeable {
    void setReceiveListener(IoArgsFinal.IoArgsEventProcessor processor) throws IOException;
    boolean postReceiveAsync() throws IOException;
    long getLastReadTime();
}
