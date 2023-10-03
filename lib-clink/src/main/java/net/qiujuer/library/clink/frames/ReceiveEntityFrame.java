package net.qiujuer.library.clink.frames;

import net.qiujuer.library.clink.core.IoArgsFinal;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

public class ReceiveEntityFrame extends AbsReceiveFrame{
    private WritableByteChannel channel;

    ReceiveEntityFrame(byte[] header) {
        super(header);
    }

    public void bindPacketChannel(WritableByteChannel channel) {
        this.channel = channel;
    }

    @Override
    protected int consumeBody(IoArgsFinal args) throws IOException {
        return channel == null ? args.setEmpty(bodyRemaining) : args.writeTo(channel);
    }
}
