package net.qiujuer.library.clink.frames;

import net.qiujuer.library.clink.core.Frame;
import net.qiujuer.library.clink.core.IoArgsFinal;
import net.qiujuer.library.clink.core.SendPacket;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;

public class SendEntityFrame extends AbsSendPacketFrame{
    private final ReadableByteChannel channel;
    private final long unConsumeEntityLength;

    SendEntityFrame(short identifier,
                    long entityLength,
                    ReadableByteChannel channel,
                    SendPacket packet) {
        super((int) Math.min(entityLength, Frame.MAX_CAPACITY),
                Frame.TYPE_PACKET_ENTITY,
                Frame.FLAG_NONE,
                identifier,
                packet);
        unConsumeEntityLength=entityLength-bodyRemaining;
        this.channel=channel;
    }

    @Override
    public Frame buildNextFrame() {
        if(unConsumeEntityLength==0){
            return null;
        }
       return new SendEntityFrame(getBodyIdentifier(),unConsumeEntityLength,channel,packet);
    }

    @Override
    protected int consumeBody(IoArgsFinal args) throws IOException {
        if(packet==null){
            return args.fillEmpty(bodyRemaining);
        }
        return args.readFrom(channel);
    }
}
