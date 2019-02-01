package xyz.osamusasa.shp;

import java.io.DataInputStream;
import java.io.IOException;

import javafx.scene.shape.Shape;

abstract class SHPRecode{
    public int	serialNumber;
    public int	len;
    public int	shapeType;

    public static SHPRecode get(int shapeType){
        switch(shapeType){
            case 0:	return	new SHPNullShape();
            case 1:	return	new SHPPoint();
            case 3:	return	new SHPPolyline();
            case 5:	return	new SHPPolygon();
            case 8:	return	new SHPMultiPoint();
            case 10:return	new SHPPoint();
        }
        System.out.println("not supported shape type:" + shapeType);
        return null;
    }

    abstract public void read(DataInputStream ds) throws IOException;
    abstract public Shape getPath();
    abstract public void print();
}
