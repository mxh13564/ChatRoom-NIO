package net.qiujuer.library.clink.frames;

import net.qiujuer.library.clink.core.Frame;
import net.qiujuer.library.clink.core.IoArgsFinal;
import net.qiujuer.library.clink.core.SendPacket;

import java.io.IOException;


public abstract class AbsSendPacketFrame extends AbsSendFrame {
    protected volatile SendPacket<?> packet;

    public AbsSendPacketFrame(int length, byte type, byte flag, short identifier, SendPacket packet) {
        super(length, type, flag, identifier);
        this.packet=packet;
    }
    
    @Override
    public final synchronized Frame nextFrame(){
        return packet==null?null:buildNextFrame();
    }

    @Override
    public synchronized boolean handle(IoArgsFinal args) throws IOException {
        if(packet==null && !isSending()){
            return true;
        }
        return super.handle(args);
    }

    public synchronized SendPacket getPacket(){return packet;}

    protected abstract Frame buildNextFrame();

    public final synchronized boolean abort(){
         boolean isSending=isSending();
         if(isSending){
             fillDirtyDataOnAbort();
         }
         packet=null;
         return !isSending;
    }

    protected void fillDirtyDataOnAbort() {
    }

}
