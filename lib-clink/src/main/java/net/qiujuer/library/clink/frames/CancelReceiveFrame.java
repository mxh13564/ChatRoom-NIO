package net.qiujuer.library.clink.frames;

import net.qiujuer.library.clink.core.IoArgsFinal;
import java.io.IOException;

public class CancelReceiveFrame extends AbsReceiveFrame{

    CancelReceiveFrame(byte[] header) {
        super(header);
    }

    @Override
    protected int consumeBody(IoArgsFinal args) throws IOException {
        return 0;
    }
}
