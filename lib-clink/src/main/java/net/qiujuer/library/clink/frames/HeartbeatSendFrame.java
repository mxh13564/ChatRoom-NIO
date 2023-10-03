package net.qiujuer.library.clink.frames;

import net.qiujuer.library.clink.core.Frame;
import net.qiujuer.library.clink.core.IoArgsFinal;

import java.io.IOException;

public class HeartbeatSendFrame extends AbsSendFrame{
    static final byte[] HEARTBEAT_DATA=new byte[]{0,0,Frame.TYPE_COMMAND_HEARTBEAT,0,0,0};

    public HeartbeatSendFrame() {
        super(HEARTBEAT_DATA);
    }

    @Override
    public Frame nextFrame() {
        return null;
    }

    @Override
    protected int consumeBody(IoArgsFinal args) throws IOException {
        return 0;
    }
}
