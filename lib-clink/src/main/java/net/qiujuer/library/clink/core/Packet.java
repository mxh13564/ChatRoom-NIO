package net.qiujuer.library.clink.core;

import java.io.Closeable;
import java.io.IOException;

public abstract class Packet<Stream extends Closeable> implements Closeable {
    public static final byte TYPE_MEMORY_BYTES=1;
    public static final byte TYPE_MEMORY_STRING=2;
    public static final byte TYPE_STREAM_FILE=3;
    public static final byte TYPE_STREAM_DIRECT=4;
    private Stream stream;
    protected long length;

    public long length(){
        return length;
    }

    protected abstract Stream createStream();

    public abstract byte type();

    public final Stream open(){
        if(stream==null){
            stream=createStream();
        }
        return stream;
    }

    protected void closeStream(Stream stream) throws IOException {
        stream.close();
    }

    @Override
    public void close() throws IOException {
        if(stream!=null){
            closeStream(stream);
            stream=null;
        }
    }

    public byte[] headerInfo(){
        return null;
    }
}
