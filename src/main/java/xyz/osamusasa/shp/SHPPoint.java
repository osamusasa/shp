package xyz.osamusasa.shp;

import java.io.DataInputStream;
import java.io.IOException;

import java.nio.ByteOrder;

import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

import static xyz.osamusasa.shp.SHPRead.readInt32;
import static xyz.osamusasa.shp.SHPRead.readDouble;

class SHPPoint extends SHPRecode{
    public double	x;
    public double	y;

    SHPPoint(){
        this.shapeType	= 1;
    }
    @Override
    public void read(DataInputStream ds) throws IOException{
        //this.shapeType	= readInt32(ds, ByteOrder.LITTLE_ENDIAN);
        this.x			= readDouble(ds, ByteOrder.LITTLE_ENDIAN);
        this.y			= readDouble(ds, ByteOrder.LITTLE_ENDIAN);
    }
    @Override
    public Shape getPath(){
        Circle c = new Circle(x, y, 0.05, Color.RED);
        return c;
    }
    @Override
    public void print(){
        System.out.println("x->"+x+"    y->"+y);
    }
}