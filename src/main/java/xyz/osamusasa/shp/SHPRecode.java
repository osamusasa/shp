package xyz.osamusasa.shp;

import java.io.DataInputStream;
import java.io.IOException;

import javafx.scene.shape.Shape;

abstract class SHPRecode{
    private int serialNumber;
    private int len;
    int shapeType;

    public static SHPRecode get(int shapeType){
        switch(shapeType){
            case 0:return new SHPNullShape();
            case 1:return new SHPPoint();
            case 3:return new SHPPolyline();
            case 5:return new SHPPolygon();
            case 8:return new SHPMultiPoint();
            case 10:return new SHPPoint();
        }
        System.out.println("not supported shape type:" + shapeType);
        return null;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public int getLen() {
        return len;
    }

    public int getShapeType() {
        return shapeType;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public void setShapeType(int shapeType) {
        this.shapeType = shapeType;
    }

    abstract public void read(DataInputStream ds) throws IOException;
    abstract public Shape getPath();
    abstract public void print();
}
