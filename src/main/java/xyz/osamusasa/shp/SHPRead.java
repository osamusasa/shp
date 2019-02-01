package xyz.osamusasa.shp;

import java.io.DataInputStream;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class SHPRead{
    public static int readInt32(DataInputStream ds, ByteOrder endian) throws IOException{
        byte[] buf = new byte[4];
        ds.readFully(buf, 0, 4);

        return ByteBuffer.wrap(buf).order(endian).getInt();
    }

    public static double readDouble(DataInputStream ds, ByteOrder endian) throws IOException{
        byte[] buf = new byte[8];
        ds.readFully(buf, 0, 8);

        return ByteBuffer.wrap(buf).order(endian).getDouble();
    }
}
