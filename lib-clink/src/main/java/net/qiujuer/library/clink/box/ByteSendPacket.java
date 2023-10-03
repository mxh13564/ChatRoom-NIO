package net.qiujuer.library.clink.box;

import net.qiujuer.library.clink.core.SendPacket;
import java.io.ByteArrayInputStream;

public class ByteSendPacket extends SendPacket<ByteArrayInputStream> {
    private final byte[] bytes;

    public ByteSendPacket(byte[] bytes) {
        this.bytes = bytes;
        this.length=bytes.length;
    }

    @Override
    protected ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_BYTES;
    }
}
