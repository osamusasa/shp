package xyz.osamusasa.shp;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class GeoMap extends Application implements Initializable{
    public Stage                    stage;
    @FXML
    private Pane                    mainPanel;
    @FXML
    private Pane                    mapPanel;
    @FXML
    private VBox                    treeView;
    @FXML
    private TreeItem<String>        mapTreeRoot;
    @FXML
    private SplitPane               splitPane;

    private OpenedSHPFileProperty   osp;

    private boolean beingPressed;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        this.stage = primaryStage;

        URL location = getClass().getResource("/fxml/GeoMap.fxml");
        System.out.println(location);
        FXMLLoader fxmlLoader = new FXMLLoader(location);

        fxmlLoader.setRoot(new VBox());
        Pane root = fxmlLoader.load();

        primaryStage.setTitle("地図");
        Scene scene = new Scene(root, 1100, 500, Color.LIGHTBLUE);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){
        osp = new OpenedSHPFileProperty();
        beingPressed = false;

        mapPanel.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::mapDragging);
        mapPanel.addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        mapPanel.addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        mapPanel.addEventHandler(ScrollEvent.SCROLL, this::mapScroll);

//        mapPanel.setBorder(new javafx.scene.layout.Border(new javafx.scene.layout.BorderStroke(null, javafx.scene.layout.BorderStrokeStyle.SOLID, null, null)));
    }

    protected Group createMap(File shpFilePath) throws IOException{
        Group map = new Group();
        Group mapShape = new Group();

        SHPFile sfile = new SHPFile(shpFilePath);
        ArrayList<SHPRecode> list = sfile.getResource();

        for(SHPRecode r:list){
            Shape shape = r.getPath();

            mapShape.getChildren().add(shape);
        }

        map.getChildren().add(mapShape);

        Rectangle r = new Rectangle();
        r.setX(sfile.getMinX());
        r.setY(sfile.getMinY());
        r.setWidth(sfile.getMaxX()-sfile.getMinX());
        r.setHeight(sfile.getMaxY()-sfile.getMinY());
        r.setFill(Color.RED);
//        map.getChildren().add(r);
//        mapShape.getChildren().add(r);

        System.out.println(mapPanel.getBoundsInParent());

//        setScale(4);
//        translate(-300, 0);

        System.out.println(mapPanel.scaleXProperty());
        System.out.println(mapPanel.scaleYProperty());
        System.out.println(mapPanel.translateXProperty());
        System.out.println(mapPanel.translateYProperty());

        osp.openedSHPFile(sfile);

        return map;
    }

    private void applyScale(){
//        mapPanel.getChildren().forEach(t->{
//            Double[] align = osp.getAlignment(t.getUserData());
//            t.translateXProperty().set(osp.scaleProperty[0] + align[0]);
//            t.translateYProperty().set(osp.scaleProperty[1] + align[1]);
//
//            System.out.println(t.translateXProperty() + "   x:" + align[0]);
//        });
//        mapPanel.scaleXProperty().set(osp.scaleX());
//        mapPanel.scaleYProperty().set(osp.scaleY());
    }

    public void fileOpen() throws IOException{
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        fc.setSelectedExtensionFilter(new ExtensionFilter("shp file", "*.shp"));

        File f = fc.showOpenDialog(this.stage);
        if(f!=null){
            Group map   = createMap(f);
            map.setUserData(f.getName());
            osp.setAlignment(f.getName(), map.getBoundsInLocal());
/*
			map.scaleXProperty().set(88);
			map.scaleYProperty().set(-88);
			map.translateXProperty().set(257);
			map.translateYProperty().set(180);
*/

            map.scaleYProperty().set(-1);
            map.translateXProperty().set(180);
            if(map.getBoundsInParent().getMinY()<0){
                map.translateYProperty().set(90);
            }



            mapPanel.getChildren().addAll(map);
            System.out.println("map,parent:"+map.getBoundsInParent());
            System.out.println("map,local:"+map.getBoundsInLocal());
            System.out.println("mapP,local:"+mapPanel.getBoundsInLocal());
            System.out.println("mapP,parent:"+mapPanel.getBoundsInParent());
            System.out.println("main,local:"+mainPanel.getBoundsInLocal());

            TreeItem<String> treeitem   = new TreeItem<>(f.getName());
            mapTreeRoot.getChildren().add(treeitem);
            mapTreeRoot.setExpanded(true);

            setExactlyBounds(map.getBoundsInLocal());

            System.out.println("map,parent:"+map.getBoundsInParent());
            System.out.println("map,local:"+map.getBoundsInLocal());
            System.out.println("mapP,local:"+mapPanel.getBoundsInLocal());
            System.out.println("mapP,parent:"+mapPanel.getBoundsInParent());
            System.out.println("main,local:"+mainPanel.getBoundsInLocal());
        }

        osp.setScale(mainPanel.getBoundsInLocal());
//        applyScale();
    }

    private double pressedX = 0.0;
    private double pressedY = 0.0;
    private double prevX = 0.0;
    private double prevY = 0.0;
    public void mousePressed(MouseEvent e){
        osp.pressX(e.getX());
        osp.pressY(e.getY());
        pressedX = prevX = e.getX();
        pressedY = prevY = e.getY();
        this.beingPressed = true;
    }

    public void mouseReleased(MouseEvent e){
        osp.setTranX(e.getX());
        osp.setTranY(e.getY());
        this.beingPressed = false;
        //System.out.println(mapPanel.getBoundsInLocal());
    }

    public void mapDragging(MouseEvent e){
//        System.out.println(mapPanel.getBoundsInParent());
//        mapPanel.translateXProperty().set(osp.translateX(e.getX()));
//        mapPanel.translateYProperty().set(osp.translateY(e.getY()));
        translate(
                (e.getX() - 2 * pressedX + prevX),
                (e.getY() - 2 * pressedY + prevY)
        );
        prevX = e.getX();
        prevY = e.getY();
    }

    public void mapScroll(ScrollEvent e){
        osp.scroll(e.getDeltaY());
        mapPanel.scaleXProperty().set(osp.scaleX());
        mapPanel.scaleYProperty().set(osp.scaleY());
        //System.out.println(mapPanel.scaleXProperty());
        //System.out.println(mapPanel.getChildren().get(0).translateXProperty());
    }

    private void setScale(double scale) {
        mapPanel.scaleXProperty().set(scale);
        mapPanel.scaleYProperty().set(scale);
    }

    private void translateTo(double x, double y) {
        mapPanel.translateXProperty().set(x);
        mapPanel.translateYProperty().set(y);
    }

    private void translate(double diffX, double diffY) {
        System.out.println("X:" + (mapPanel.getTranslateX() + diffX) + " Y:" + (mapPanel.getTranslateY() + diffY));
        translateTo(
                mapPanel.translateXProperty().get() + diffX,
                mapPanel.translateYProperty().get() + diffY
        );
    }

    private void setExactlyBounds(Bounds mapLocalBounds) {
        Bounds mainLocalBounds = mainPanel.getBoundsInLocal();
        Bounds mapPanelLocalBounds = mapPanel.getBoundsInLocal();
//        Bounds mapLocalBounds = mapPanel.getBoundsInLocal();

        double scaleWidth = mainLocalBounds.getWidth() / mapLocalBounds.getWidth();
        double scaleHeight = mainLocalBounds.getHeight() / mapLocalBounds.getHeight();

        double exactlyScale = Math.min(scaleWidth, scaleHeight);

//        double exactlyScale = 40.0;

//        double transX = - (exactlyScale - 1.0) * mapPanelLocalBounds.getWidth();
//        double transY = - (exactlyScale - 1.0) * mapPanelLocalBounds.getHeight() + 400.0;
        double transX = - (exactlyScale - 1.0) * (187.5 + 50.0 + 15.0);
        double transY = - (exactlyScale - 1.0) * (5.2 + 5.0 + 3.0 + 4.0);

//        double transX = - mapPanelLocalBounds.getWidth();
//        double transY = - mapPanelLocalBounds.getHeight();

        System.out.println("exactly scale: " + exactlyScale);
        System.out.println("transX: " + transX);
        System.out.println("transY: " + transY);

        setScale(exactlyScale);
        translateTo(transX, transY);
    }
}