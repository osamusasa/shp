package xyz.osamusasa.shp;

import java.io.DataInputStream;
import java.io.IOException;

import java.nio.ByteOrder;

import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;

import static xyz.osamusasa.shp.SHPRead.readInt32;

class SHPMultiPoint extends SHPRecode{
    SHPMultiPoint(){
        this.shapeType = 8;
    }
    @Override
    public void read(DataInputStream ds) throws IOException{
        this.shapeType = readInt32(ds, ByteOrder.LITTLE_ENDIAN);
    }
    @Override
    public Shape getPath(){
        Path path = new Path();
        return path;
    }
    @Override
    public void print(){
    }
}