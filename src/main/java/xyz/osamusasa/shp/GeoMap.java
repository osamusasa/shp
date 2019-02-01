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
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class GeoMap extends Application implements Initializable{
    public Stage					stage;
    @FXML
    private Pane					mainPanel;
    @FXML
    private Pane					mapPanel;
    @FXML
    private VBox					treeView;
    @FXML
    private TreeItem<String>		mapTreeRoot;
    @FXML
    private SplitPane				splitPane;

    private OpenedSHPFileProperty	osp;

    private boolean beingPressed;

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        this.stage				= primaryStage;

        URL			location	= getClass().getResource("../../../fxml/GeoMap.fxml");
        FXMLLoader	fxmlLoader 	= new FXMLLoader(location);

        fxmlLoader.setRoot(new VBox());
        Pane		root		= (Pane) fxmlLoader.load();

        primaryStage.setTitle("地図");
        Scene		scene		= new Scene(root, 1100, 500, Color.LIGHTBLUE);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){
        osp						= new OpenedSHPFileProperty();
        beingPressed			= false;

        mapPanel.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::mapDragging);
        mapPanel.addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        mapPanel.addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        mapPanel.addEventHandler(ScrollEvent.SCROLL, this::mapScroll);

        mapPanel.setBorder(new javafx.scene.layout.Border(new javafx.scene.layout.BorderStroke(null, javafx.scene.layout.BorderStrokeStyle.SOLID, null, null)));
    }

    protected Group createMap(File shpFilePath) throws IOException{
        Group	map					= new Group();
        Group	mapShape			= new Group();

        SHPFile	sfile				= new SHPFile(shpFilePath);
        ArrayList<SHPRecode> list	= sfile.getResource();

        for(SHPRecode r:list){
            Shape shape	= r.getPath();

            mapShape.getChildren().add(shape);
        }

        map.getChildren().add(mapShape);

        osp.openedSHPFile(sfile);

        return map;
    }

    private void applyScale(){
        mapPanel.getChildren().forEach(t->{
            Double[] align	= osp.getAlignment(t.getUserData());
            t.translateXProperty().set(osp.scaleProperty[0] + align[0]);
            t.translateYProperty().set(osp.scaleProperty[1] + align[1]);

            System.out.println(t.translateXProperty() + "   x:" + align[0]);
        });
        mapPanel.scaleXProperty().set(osp.scaleX());
        mapPanel.scaleYProperty().set(osp.scaleY());
    }

    public void fileOpen() throws IOException{
        FileChooser fc	= new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.dir")));
        fc.setSelectedExtensionFilter(new ExtensionFilter("shp file", "*.shp"));

        File f			= fc.showOpenDialog(this.stage);
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

            TreeItem<String> treeitem	= new TreeItem<String>(f.getName());
            mapTreeRoot.getChildren().add(treeitem);
            mapTreeRoot.setExpanded(true);
        }

        osp.setScale(mainPanel.getBoundsInLocal());
        //applyScale();
    }

    public void mousePressed(MouseEvent e){
        osp.pressX(e.getX());
        osp.pressY(e.getY());
        this.beingPressed	= true;
    }

    public void mouseReleased(MouseEvent e){
        osp.setTranX(e.getX());
        osp.setTranY(e.getY());
        this.beingPressed	= false;
        //System.out.println(mapPanel.getBoundsInLocal());
    }

    public void mapDragging(MouseEvent e){
        //System.out.println(mapPanel.getBoundsInParent());
        mapPanel.translateXProperty().set(osp.translateX(e.getX()));
        mapPanel.translateYProperty().set(osp.translateY(e.getY()));
    }

    public void mapScroll(ScrollEvent e){
        osp.scroll(e.getDeltaY());
        mapPanel.scaleXProperty().set(osp.scaleX());
        mapPanel.scaleYProperty().set(osp.scaleY());
        //System.out.println(mapPanel.scaleXProperty());
        //System.out.println(mapPanel.getChildren().get(0).translateXProperty());
    }
}