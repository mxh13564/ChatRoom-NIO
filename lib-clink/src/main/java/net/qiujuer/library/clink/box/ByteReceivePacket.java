package net.qiujuer.library.clink.box;

import java.io.ByteArrayOutputStream;

public class ByteReceivePacket extends AbsByteArrayReceivePacket<byte[]>{

    public ByteReceivePacket(long len){
        super(len);
    }

    @Override
    public byte type(){
        return TYPE_MEMORY_BYTES;
    }

    @Override
    protected byte[] buildEntity(ByteArrayOutputStream stream){
        return stream.toByteArray();
    }
}
