package net.qiujuer.library.clink.core;

import java.io.InputStream;

public abstract class SendPacket<T extends InputStream> extends Packet<T>{
        private boolean isCanceled;

        public boolean isCanceled(){
            return isCanceled;
        }

        public void cancel(){
            isCanceled=true;
        }
}
