package net.qiujuer.library.clink.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

public class IoArgsFinal {
    private int limit = 256;
    private ByteBuffer buffer = ByteBuffer.allocate(256);

    /**
     * 从bytes数组进行消费
     */
    public int readFrom(byte[] bytes, int offset, int count) {
        int size = Math.min(count, buffer.remaining());
        if (size <= 0) {
            return 0;
        }
        buffer.put(bytes, offset, size);
        return size;
    }

    public int writeTo(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.get(bytes, offset, size);
        return size;
    }


    //从bytes中读取数据
    public int readFrom(ReadableByteChannel channel) throws IOException {
        int bytesProduced=0;
        while(buffer.hasRemaining()){
            int len=channel.read(buffer);
            if(len<0){
                throw new EOFException();
            }
            bytesProduced+=len;
        }
        return bytesProduced;
    }

    //写入数据到channel中
    public int writeTo(WritableByteChannel channel) throws IOException {
        ByteBuffer buffer= this.buffer;
        int bytesProduced=0;
        int len;
        do{
            len=channel.write(buffer);
            if(len<0){
                throw new EOFException("Current write any data with:"+channel);
            }
            bytesProduced+=len;
        }while(buffer.hasRemaining() && len!=0);
        return bytesProduced;
    }

    //从channel读取数据
    public int readFrom(SocketChannel channel) throws IOException {
        ByteBuffer buffer= this.buffer;
        int bytesProduced=0;
        int len;
        do{
            len=channel.read(buffer);
            if(len<0){
                throw new EOFException("Current read any data with:"+channel);
            }
            bytesProduced+=len;
        }while(buffer.hasRemaining() && len!=0);
        return bytesProduced;
    }

    //写数据到channel
    public int writeTo(SocketChannel channel) throws IOException {
        int bytesProduced=0;
        while(buffer.hasRemaining()){
            int len=channel.write(buffer);
            if(len<0){
                throw  new EOFException();
            }
            bytesProduced+=len;
        }
        return bytesProduced;
    }

    public  void startWriting(){
        buffer.clear();
        buffer.limit(limit);
    }

    public void finishWriting(){
        buffer.flip();
    }

    public void limit(int limit){
        this.limit=Math.min(limit,buffer.capacity());
    }

    public boolean remained() {
        return buffer.remaining() > 0;
    }

    public int readLength() {
       return buffer.getInt();
    }

    public int capacity() {
        return buffer.capacity();
    }

    public int fillEmpty(int size) {
        int fillSize=Math.min(size,buffer.remaining());
        buffer.position(buffer.position()+fillSize);
        return fillSize;
    }

    /**
     * 清空部分数据
     *
     * @param size 想要清空的数据长度
     * @return 真实清空的数据长度
     */
    public int setEmpty(int size) {
        int emptySize = Math.min(size, buffer.remaining());
        buffer.position(buffer.position() + emptySize);
        return emptySize;
    }


    public interface IoArgsEventProcessor {
        IoArgsFinal providerIoArgs();
        void onConsumeFailed(IoArgsFinal args,Exception e);

        void onConsumeCompleted(IoArgsFinal args);
    }
}
