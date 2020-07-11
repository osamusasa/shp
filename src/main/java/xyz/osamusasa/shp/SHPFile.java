/**
 * Object of SHP file.
 *
 * shp file agreement is refer to https://www.esrij.com/cgi-bin/wp/wp-content/uploads/documents/shapefile_j.pdf
 */

package xyz.osamusasa.shp;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.nio.ByteOrder;

import java.util.ArrayList;

import static xyz.osamusasa.shp.SHPRead.readInt32;
import static xyz.osamusasa.shp.SHPRead.readDouble;

public class SHPFile{
    private int len;
    private int version;
    private int shapeType;
    private double minX;
    private double minY;
    private double maxX;
    private double maxY;
    private double minZ;
    private double maxZ;
    private double minM;
    private double maxM;
    private ArrayList<SHPRecode> recodes;
    private DataInputStream ds;
    private String fileName;

    final private int FILE_SYMBOL = 0x0000270a;

    public SHPFile(File shpPath) throws IOException{
        fileName = shpPath.getName();
        ds = new DataInputStream(new FileInputStream(shpPath));
        readHeader();

        recodes = new ArrayList<>();
        readRecode();
    }

    /**
     * Read SHP file header.
     *
     * header consist on this.
     * +---------+--------+------+-------+-----+-----+-----+-----+-----+-----+-----+-----+-----+
     * |   0-3   |  4-23  |24-27 | 28-31 |32-35|36-43|44-51|52-59|60-67|68-75|76-83|84-91|92-99|
     * +---------+--------+------+-------+-----+-----+-----+-----+-----+-----+-----+-----+-----+
     * |file code|not used|length|version|shape|Xmin |Ymin |Xmax |Ymax |Zmin |Zmax |Mmin |Mmax |
     * | (9994)  |        |      |(1000) |type |     |     |     |     |     |     |     |     |
     * +---------+--------+------+-------+-----+-----+-----+-----+-----+-----+-----+-----+-----+
     *
     * @throws IOException if not shp file.
     */
    private void readHeader() throws IOException{
        if(readInt32(ds, ByteOrder.BIG_ENDIAN)!=FILE_SYMBOL){
            throw new IOException("this file may not be SHP file.");
        }

        for(int i=0;i<5;i++){
            readInt32(ds, ByteOrder.BIG_ENDIAN);
        }

        len = readInt32(ds, ByteOrder.BIG_ENDIAN);
        version = readInt32(ds, ByteOrder.LITTLE_ENDIAN);
        shapeType = readInt32(ds,ByteOrder.LITTLE_ENDIAN);
        minX = readDouble(ds, ByteOrder.LITTLE_ENDIAN);
        minY = readDouble(ds, ByteOrder.LITTLE_ENDIAN);
        maxX = readDouble(ds, ByteOrder.LITTLE_ENDIAN);
        maxY = readDouble(ds, ByteOrder.LITTLE_ENDIAN);
        minZ = readDouble(ds, ByteOrder.LITTLE_ENDIAN);
        maxZ = readDouble(ds, ByteOrder.LITTLE_ENDIAN);
        minM = readDouble(ds, ByteOrder.LITTLE_ENDIAN);
        maxM = readDouble(ds, ByteOrder.LITTLE_ENDIAN);
    }

    private void readRecode() throws IOException{
        while(true){
            try{
                int serialNum = readInt32(ds, ByteOrder.BIG_ENDIAN);
                int len = readInt32(ds, ByteOrder.BIG_ENDIAN);
                int type = readInt32(ds, ByteOrder.LITTLE_ENDIAN);
                SHPRecode r = SHPRecode.get(type);

                r.setSerialNumber(serialNum);
                r.setLen(len);

                r.read(ds);

                recodes.add(r);
            }catch(EOFException e){
                return;
            }catch(IOException e){
                throw new IOException(e);
            }
        }
    }

    public ArrayList<SHPRecode> getResource(){
        return this.recodes;
    }

    public Double[] getBounds(){
        Double[] p = new Double[4];
        p[0] = this.minX;
        p[1] = this.minY;
        p[2] = this.maxX;
        p[3] = this.maxY;
        return p;
    }

    public String getPath(){
        return this.fileName;
    }

    public void print(){
        System.out.println("len:"+len);
        System.out.println("version:"+version);
        System.out.println("shapeType:"+shapeType);
        System.out.println("minX:"+minX);
        System.out.println("minY:"+minY);
        System.out.println("maxX:"+maxX);
        System.out.println("maxY:"+maxY);
        System.out.println("minZ:"+minZ);
        System.out.println("maxZ:"+maxZ);
        System.out.println("minM:"+minM);
        System.out.println("maxM:"+maxM);
    }

    public int getLen() {
        return len;
    }

    public int getVersion() {
        return version;
    }

    public int getShapeType() {
        return shapeType;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getMinZ() {
        return minZ;
    }

    public double getMaxZ() {
        return maxZ;
    }

    public double getMinM() {
        return minM;
    }

    public double getMaxM() {
        return maxM;
    }

    public String getFileName() {
        return fileName;
    }
}