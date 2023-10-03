package net.qiujuer.library.clink.frames;

import net.qiujuer.library.clink.core.IoArgsFinal;

import java.io.IOException;

public class HeartbeatReceiveFrame extends AbsReceiveFrame{
    static final HeartbeatReceiveFrame INSTANCE=new HeartbeatReceiveFrame();

    private HeartbeatReceiveFrame() {
        super(HeartbeatSendFrame.HEARTBEAT_DATA);
    }

    @Override
    protected int consumeBody(IoArgsFinal args) throws IOException {
        return 0;
    }
}
