package net.qiujuer.library.clink.box;

import net.qiujuer.library.clink.core.ReceivedPacket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FileReceivePacket extends ReceivedPacket<FileOutputStream, File> {
    private File file;

    public FileReceivePacket(long len,File file){
        super(len);
        this.file=file;
    }

    @Override
    protected FileOutputStream createStream() {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    @Override
    public byte type() {
        return TYPE_STREAM_FILE;
    }

    @Override
    protected File buildEntity(FileOutputStream stream) {
        return file;
    }
}
