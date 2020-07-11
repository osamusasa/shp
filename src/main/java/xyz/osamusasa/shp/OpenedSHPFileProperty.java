package xyz.osamusasa.shp;

import java.util.ArrayList;

import javafx.geometry.Bounds;

class OpenedSHPFileProperty{
    private ArrayList<String> openedFile;
    // 0:tranX - 1:tranY - 2:scaleX - 3:scaleY
    public double[] scaleProperty = new double[4];
    // 0:minX - 1:minY - 2:maxX - 3:maxY
    private ArrayList<Double[]> openedScale;
    private ArrayList<Double[]> alignment;

    private double mapPaneScale = 1;
    private double mapPanediffX = 0;
    private double mapPanediffY = 0;

    private double mousePressedX;
    private double mousePressedY;

    private double currTranX = 0;
    private double currTranY = 0;

    OpenedSHPFileProperty(){
        openedFile = new ArrayList<>();
        openedScale = new ArrayList<>();
        alignment = new ArrayList<>();
        scaleProperty[2] = Double.MAX_VALUE;
    }

    public void openedSHPFile(SHPFile sfile){
        //openedFile.add(sfile.getPath());
        openedScale.add(sfile.getBounds());
    }

    public void setScale(Bounds b){
        for(int i=0;i<openedScale.size();i++){
            Double[] sf = openedScale.get(i);
            double width = sf[2]-sf[0];
            double height = sf[3]-sf[1];
            double scale = Math.min(b.getWidth()/width, b.getHeight()/height) * 0.9;
            double alignX = width * ( scale - 1 ) / 2;
            double alignY = height * ( scale - 1 ) / 2;
            alignment.add(new Double[]{alignX, alignY});

            System.out.println(scale+":"+alignX+":"+alignY);
            System.out.println(sf[0] + ":" + sf[1] + ":" + sf[2] + ":" + sf[3] + ":" + b.getWidth() + ":" + b.getHeight());

            if(scaleProperty[2] > scale){
                double tranX = (sf[2]-sf[0])/scale+b.getWidth()/2;
                double tranY = (sf[3]-sf[1])/scale+b.getHeight()/2;
                scaleProperty[0] = 30;
                scaleProperty[1] = tranY;
                scaleProperty[2] = scale;
                scaleProperty[3] = -scale;

                mapPaneScale = scale;
            }
        }
    }

    public void setAlignment(String name, Bounds b){
        openedFile.add(name);

        if(alignment.size()==0){
            Double[] basis = {0.0, 0.0};
            alignment.add(basis);
            return;
        }

        Double[] basis = openedScale.get(0);
        Double[] a = new Double[2];
        a[0] = basis[0] - b.getMinX();
        a[1] = basis[1] - b.getMinY();
        alignment.add(a);
    }

    public Double[] getAlignment(Object name){
        for(int i=0;i<openedFile.size();i++){
            if(name.equals(openedFile.get(i))){
                return alignment.get(i);
            }
        }
        Double[] dumy = {0.0, 0.0};
        return dumy;
    }

    public void scroll(double r){
        if(mapPaneScale > r/10){
            mapPaneScale -= r / 10;
        }
    }

    public void drag(double x, double y){
        mapPanediffX += x;
        mapPanediffY += y;
    }

    public double scaleX(){
        return mapPaneScale;
    }

    public double scaleY(){
        return mapPaneScale;
    }

    public void pressX(double x){
        mousePressedX = x;
    }

    public void pressY(double y){
        mousePressedY = y;
    }

    public double translateX(double x){
        return x - mousePressedX + currTranX;
    }

    public double translateY(double y){
        return y - mousePressedY + currTranY;
    }

    public void setTranX(Double x){
        currTranX += x - mousePressedX;
    }

    public void setTranY(double y){
        currTranY += y - mousePressedY;
    }
}