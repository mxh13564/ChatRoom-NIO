package net.qiujuer.library.clink.box;

public class StringSendPacket extends ByteSendPacket {


    public StringSendPacket(String msg) {
        super(msg.getBytes());
    }

   @Override
    public byte type(){
        return TYPE_MEMORY_STRING;
   }
}
