package net.qiujuer.library.clink.box;

import java.io.ByteArrayOutputStream;

public class StringReceivedPacket extends AbsByteArrayReceivePacket<String> {

    public StringReceivedPacket(long len){
        super(len);
    }

    @Override
    protected String buildEntity(ByteArrayOutputStream stream) {
        return new String(stream.toByteArray());
    }

    @Override
    public byte type() {
        return TYPE_MEMORY_STRING;
    }

}
