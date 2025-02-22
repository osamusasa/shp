package xyz.osamusasa.shp;

import java.io.File;
import java.io.IOException;

import java.math.BigDecimal;
import java.net.URL;

import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linuxense.javadbf.*;

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

    private boolean debug = true;

    private Map<String, Object> initTranslate;

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

        ObjectMapper mapper = new ObjectMapper();
        try {
            initTranslate = mapper.readValue(new File("src/main/resources/initTranslate.json"), Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mapPanel.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::mapDragging);
        mapPanel.addEventHandler(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        mapPanel.addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        mapPanel.addEventHandler(ScrollEvent.SCROLL, this::mapScroll);
        mapPanel.addEventHandler(KeyEvent.KEY_TYPED, this::mapKeyTyped);

        // 左側のファイルツリー作成
        // [src/main/resources/shp]のXMLファイルから読み込む
        createFileTree();

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

    private Bounds mapLocalBounds = null;
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

//            map.scaleYProperty().set(-1);
//            map.translateXProperty().set(180);
//            if(map.getBoundsInParent().getMinY()<0){
//                map.translateYProperty().set(90);
//            }



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
            mapLocalBounds = map.getBoundsInLocal();

            System.out.println("map,parent:"+map.getBoundsInParent());
            System.out.println("map,local:"+map.getBoundsInLocal());
            System.out.println("mapP,local:"+mapPanel.getBoundsInLocal());
            System.out.println("mapP,parent:"+mapPanel.getBoundsInParent());
            System.out.println("main,local:"+mainPanel.getBoundsInLocal());

            // mapPanelでKeyEventを受け取れるようにする
            mapPanel.requestFocus();
        }

        osp.setScale(mainPanel.getBoundsInLocal());
//        applyScale();
    }

    private void fileOpen(File f) {
        Group map   = null;
        try {
            map = createMap(f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        map.setUserData(f.getName());
        osp.setAlignment(f.getName(), map.getBoundsInLocal());

        mapPanel.getChildren().addAll(map);

        setExactlyBounds(map.getBoundsInLocal());
        mapLocalBounds = map.getBoundsInLocal();

        // mapPanelでKeyEventを受け取れるようにする
        mapPanel.requestFocus();

        osp.setScale(mainPanel.getBoundsInLocal());
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
//        System.out.println("mouse X:"+e.getX()+" Y:"+e.getY());
        translate(
                (e.getX() - 2 * pressedX + prevX) * mapPanel.getScaleX(),
                (e.getY() - 2 * pressedY + prevY) * mapPanel.getScaleY()
        );
        prevX = e.getX();
        prevY = e.getY();
    }

    public void mapKeyTyped(KeyEvent e){
        if(debug)System.out.println(e.getCharacter());

        double unitTran = 1;
        double unitScrl = 1;
        switch (e.getCharacter()) {
            case "w": {
                translate(0, -unitTran);
                break;
            }
            case "a": {
                translate(-unitTran,0);
                break;
            }
            case "s": {
                translate(0, unitTran);
                break;
            }
            case "S": {
                translate(0, unitTran*10);
                break;
            }
            case "d": {
                translate(unitTran, 0);
                break;
            }
            case "+": {
                scale(unitScrl);
                System.out.println(mapPanel.getScaleX());
                System.out.println(mapPanel.getScaleY());
                System.out.println("W:"+mapPanel.getWidth()+"/H:"+mapPanel.getHeight());
                System.out.println("main:"+mainPanel.getBoundsInLocal());
                System.out.println("map:"+mapPanel.getBoundsInLocal());
                System.out.println(mapLocalBounds);
                translate(
                        -mapPanel.getBoundsInParent().getMaxX()/2 - mapLocalBounds.getMaxX()/2 + 90,
                        -mapPanel.getBoundsInLocal().getMaxY()/2 - mapLocalBounds.getMaxY()/2
                );
//                translateTo(0,0);
                break;
            }
            case "-": {
                scale(-unitScrl);
                translate(
                        mapPanel.getBoundsInParent().getMaxX()/2 + mapLocalBounds.getMaxX()/2 - 90,
                        mapPanel.getBoundsInLocal().getMaxY()/2 + mapLocalBounds.getMaxY()/2
                );
            }
            case "t": {
                setScaleX(2);
                setScaleY(-2);
            }
        }
    }

    public void mapScroll(ScrollEvent e){
//        osp.scroll(e.getDeltaY());
//        mapPanel.scaleXProperty().set(osp.scaleX());
//        mapPanel.scaleYProperty().set(osp.scaleY());
////        System.out.println(mapPanel.scaleXProperty());
//        System.out.println(e.getDeltaY());
        //System.out.println(mapPanel.getChildren().get(0).translateXProperty());
    }

    private void setScale(double scale) {
        setScaleX(scale);
        setScaleY(scale);
    }

    private void setScaleX(double scale) {
        mapPanel.scaleXProperty().set(scale);
    }

    private void setScaleY(double scale) {
        mapPanel.scaleYProperty().set(scale);
    }

    private void scale(double scale) {
        scaleX(scale);
        scaleY(-scale);
    }

    private void scaleX(double diff) {
        mapPanel.setScaleX(mapPanel.getScaleX() + diff);
    }

    private void scaleY(double diff) {
        mapPanel.setScaleY(mapPanel.getScaleY() + diff);
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
//        double transX = - (exactlyScale - 1.0) * (187.5 + 50.0 + 15.0);
//        double transY = - (exactlyScale - 1.0) * (5.2 + 5.0 + 3.0 + 4.0);
        double transX = - (exactlyScale+1.0) * mapLocalBounds.getMaxX();
        double transY = - (exactlyScale) * mapLocalBounds.getMaxY();

//        double transX = - mapPanelLocalBounds.getWidth();
//        double transY = - mapPanelLocalBounds.getHeight();

        System.out.println("exactly scale: " + exactlyScale);
        System.out.println("transX: " + transX);
        System.out.println("transY: " + transY);

//        setScale(exactlyScale);
//        translateTo(transX, transY);
    }

    private final Path shpRootDir = Paths.get("src/main/resources/shp");

    /**
     * SHPフォルダを読み込んで、県のチェックボックス付きリストを作成
     */
    private void createFileTree(){
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.xml");
        PathMatcher matcherDbf = FileSystems.getDefault().getPathMatcher("glob:**.dbf");
        List<Path> paths;
        try (Stream<Path> pathStream = Files.list(shpRootDir)) {
            paths = pathStream
                    .sorted(shpFolderSorter)
                    .collect(Collectors.toList());

            for (Path value : paths) {
                try (Stream<Path> fileStream = Files.list(value)) {
                    List<Path> files = fileStream.collect(Collectors.toList());

                    String prefectureName = files.stream()
                            .filter(matcher::matches)
                            .filter(path -> path.getFileName().toString().startsWith("KS-META"))
                            .map(roadMetaXMLFileFunction)
                            .findFirst()
                            .orElse("");
                    List<String> cityNames = files.stream()
                            .filter(matcherDbf::matches)
                            .findFirst()
                            .map(DBFFileAdministrativeArea::new)
                            .map(DBFFileAdministrativeArea::getCityName)
                            .orElse(List.of());

                    // チェックボックス作成
                    CheckBoxTreeItem<String> item = new CheckBoxTreeItem<>(prefectureName);
                    cityNames.forEach(s -> item.getChildren().add(new CheckBoxTreeItem<>(s)));
                    mapTreeRoot.getChildren().add(item);
                    item.selectedProperty().addListener(prefectureSelectedListener(value));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * メタファイルを読み込んで、県名を取得するFunction
     */
    private final Function<Path, String> roadMetaXMLFileFunction = (file) -> {
        try {
            Document document = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(file.toFile());
            XPathExpression expression = XPathFactory
                    .newInstance()
                    .newXPath()
                    .compile("/MD_Metadata/identificationInfo/MD_DataIdentification/extent/geographicElement/EX_GeographicDescription/geographicIdentifier/code");
            NodeList nodeList = (NodeList) expression.evaluate(document, XPathConstants.NODESET);

            // 左側のTreeに追加
            for (int i = 0; i < nodeList.getLength(); i++) {
                System.out.println("Title: " + nodeList.item(i).getTextContent());
                return nodeList.item(i).getTextContent();
            }
        } catch (SAXException | IOException | ParserConfigurationException |
                 XPathExpressionException e) {
            throw new RuntimeException(e);
        }
        return "error";
    };


    /**
     * 県名のCheckBoxTreeItemをクリックした時のリスナーを作成する
     *
     * @param folder 対象のフォルダ
     * @return CheckBoxTreeItemをクリックした時のリスナー
     */
    private ChangeListener<Boolean> prefectureSelectedListener(Path folder) {
        PathMatcher matcherShp = FileSystems.getDefault().getPathMatcher("glob:**.shp");
        return (v, o, n)->{
            try(Stream<Path> fileList = Files.list(folder)) {
                fileList.filter(matcherShp::matches).forEach(path -> {
                    System.out.println(path);
                    fileOpen(path.toFile());

                    // 位置、スケール設定
                    Map<String, Object> prefMap = (Map<String, Object>) initTranslate.get(folder.getFileName().toString());
                    if (prefMap != null) {
                        Integer x = (Integer) prefMap.get("X");
                        Integer y = (Integer) prefMap.get("Y");
                        Integer sx = (Integer) prefMap.get("ScaleX");
                        Integer sy = (Integer) prefMap.get("ScaleY");
                        translateTo(x.doubleValue(), y.doubleValue());
                        setScaleX(sx);
                        setScaleY(sy);
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * shpフォルダ配下のフォルダをソートする関数
     * ファイル名の「N03-20230101_」に続く文字列から判断する。
     */
    private final Comparator<Path> shpFolderSorter = (p1, p2) -> {
        if (!p1.getFileName().toString().startsWith("N03-20230101_")) {
            if (!p2.getFileName().toString().startsWith("N03-20230101_")) {
                return 0;
            } else {
                return Integer.MAX_VALUE;
            }
        } else if (!p2.getFileName().toString().startsWith("N03-20230101_")) {
            return Integer.MIN_VALUE;
        }

        int p1_num = Integer.parseInt(p1.getFileName().toString().substring(13, 15));
        int p2_num = Integer.parseInt(p2.getFileName().toString().substring(13, 15));

        return p1_num - p2_num;
    };
}