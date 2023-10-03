package net.qiujuer.library.clink.box;

import net.qiujuer.library.clink.core.ReceivedPacket;

import java.io.ByteArrayOutputStream;

public abstract class AbsByteArrayReceivePacket<Entity> extends ReceivedPacket<ByteArrayOutputStream,Entity> {

    public AbsByteArrayReceivePacket(long len){
        super(len);
    }

    @Override
    protected final ByteArrayOutputStream createStream(){
        return new ByteArrayOutputStream((int)length);
    }

}
