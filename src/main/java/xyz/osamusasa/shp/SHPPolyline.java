package xyz.osamusasa.shp;

import java.io.DataInputStream;
import java.io.IOException;

import java.nio.ByteOrder;

import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;

import static xyz.osamusasa.shp.SHPRead.readInt32;
import static xyz.osamusasa.shp.SHPRead.readDouble;

class SHPPolyline extends SHPRecode{
    private double		minX;
    private double		minY;
    private double		maxX;
    private double		maxY;
    private int			numParts;
    private int			numPoints;
    private int[]		parts;
    private SHPPoint[]	points;

    SHPPolyline(){
        this.shapeType	= 3;
    }

    @Override
    public void read(DataInputStream ds) throws IOException{
        minX		= readDouble(ds, ByteOrder.LITTLE_ENDIAN);
        minY		= readDouble(ds, ByteOrder.LITTLE_ENDIAN);
        maxX		= readDouble(ds, ByteOrder.LITTLE_ENDIAN);
        maxY		= readDouble(ds, ByteOrder.LITTLE_ENDIAN);
        numParts	= readInt32(ds, ByteOrder.LITTLE_ENDIAN);
        numPoints	= readInt32(ds, ByteOrder.LITTLE_ENDIAN);

        parts 	= new int[numParts];
        points	= new SHPPoint[numPoints];

        for(int i=0;i<numParts;i++){
            parts[i]	= readInt32(ds, ByteOrder.LITTLE_ENDIAN);
        }

        for(int i=0;i<numPoints;i++){
            points[i]	= new SHPPoint();
            points[i].read(ds);
        }
    }

    @Override
    public Shape getPath(){
        Path path = new Path();

        int nparts		= (numParts > 1) ? 1 : 0 ;
        int part		= (nparts==0) ? Integer.MAX_VALUE : parts[nparts];

        path.setStrokeWidth(0.05);

        path.getElements().add(new MoveTo(getX(0), getY(0)));

        for(int i=1;i<numPoints;i++){
            if(i<part){
                path.getElements().add(new LineTo(getX(i), getY(i)));
            }else{
                path.getElements().add(new MoveTo(getX(i), getY(i)));
                nparts	= (numParts>nparts+1) ? nparts + 1 : 0;
                part	= (nparts==0) ? Integer.MAX_VALUE : parts[nparts];
            }
        }

        return path;
    }

    public double getX(int pos){
        return points[pos].x;
    }

    public double getY(int pos){
        return points[pos].y;
    }

    @Override
    public void print(){
        System.out.println("minX:"+minX);
        System.out.println("minY:"+minY);
        System.out.println("maxX:"+maxX);
        System.out.println("maxY:"+maxY);
        System.out.println("numParts:"+numParts);
        System.out.println("numPoints:"+numPoints);
        System.out.println("parts");
        for(int i=0;i<numParts;i++){
            System.out.println("\t["+i+"]:"+parts[i]);
        }
        System.out.println("points");
        for(int i=0;i<numPoints;i++){
            System.out.print("\t["+i+"]:");
            points[i].print();
        }
    }
}
