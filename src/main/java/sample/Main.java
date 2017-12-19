package sample;
/*Author: Lubomir Nepil*/
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;


public class Main extends Application {
    private final LuminanceImage lumImg = new LuminanceImage();

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("Luminance Calculator");
        Group root = new Group();
        Scene scene = new Scene(root, 1200, 1000, Color.WHITE);
        //menu setup
        MenuBar menu = new MenuBar();
        menu.setBackground(new Background(new BackgroundFill(Color.LIGHTGREY, null, null)));
        Menu menuFile = new Menu("File");
        MenuItem openItem = new MenuItem("Open...");
        menuFile.getItems().add(openItem);
        Menu menuSettings = new Menu("Settings");
        menu.getMenus().addAll(menuFile, menuSettings);

        /*grid setup*/
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(10, 10, 30, 10));
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setPercentWidth(85);
        col2.setPercentWidth(15);
        gridpane.getColumnConstraints().addAll(col1, col2);
        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        row1.setPercentHeight(90);
        row2.setPercentHeight(10);
        gridpane.getRowConstraints().addAll(row1, row2);
        gridpane.gridLinesVisibleProperty().setValue(true);
        gridpane.prefHeightProperty().bind(scene.heightProperty());
        gridpane.prefWidthProperty().bind(scene.widthProperty());


        /*imageView setup*/
        final ImageView imv = new ImageView();
        imv.setPreserveRatio(true);
        final Label luminance = new Label("Luminance (cd/m^2)");
        final TextField lumTextField = new TextField("0");
        final Label coords = new Label("");
        imv.setOnMouseMoved(e -> {
            if (lumImg.getlMatrix() != null) {
                coords.setText("x: " + (int)Math.floor(e.getX()) + " y: " + (int)Math.floor(e.getY()));
                lumTextField.setText(String.format("%.2f",lumImg.getPixelLuminance((int)Math.floor(e.getX()), (int)Math.floor(e.getY()))));
            }
        });

        /*ScrollPane setup*/
        ScrollPane scrollPane = new ScrollPane(imv);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        //scrollPane.setPrefViewportHeight(gridpane.getPrefHeight());
        //scrollPane.setPrefViewportWidth(gridpane.getPrefWidth());

        /*fileChooser setup*/
        FileChooser fileChooser = new FileChooser();
        openItem.setOnAction(event -> {
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    imv.setImage(openImg(file));

                } catch (IOException e) {
                    e.printStackTrace();
                }

                //add legend
                HBox legend = createLegend();
                legend.setAlignment(Pos.TOP_CENTER);
                gridpane.add(legend, 0, 1);
                root.getChildren().remove(gridpane);
                root.getChildren().add(gridpane);
            }
        });

        /*textbox setup*/
        Label exposureLbl = new Label("Exposure time (seconds)");
        TextField exposureTextField = new TextField("");
        exposureTextField.setOnAction(e -> {
            String text = exposureTextField.getText();
            if(text.matches("^[+-]?([0-9]*[.])?[0-9]+$")){
                this.lumImg.setExposure(Double.parseDouble(text));
                if(this.lumImg.apertureAndExposureSet()){
                    this.lumImg.populateLMatrix();
                }
            }
        });

        Label apertureLbl = new Label("Aperture number");
        TextField apertureTextField = new TextField("");

        apertureTextField.setOnAction(e -> {
            String text = apertureTextField.getText();
            if(text.matches("^[+-]?([0-9]*[.])?[0-9]+$")){
                this.lumImg.setAperture(Double.parseDouble(text));
                if(this.lumImg.apertureAndExposureSet()){
                    this.lumImg.populateLMatrix();
                }
            }
        });

        /*Vbox setup*/
        VBox vertBox = new VBox(10);
        vertBox.setPadding(new Insets(10, 5, 0, 5));
        //openBtn.prefWidthProperty().bind(vertBox.widthProperty());
        vertBox.getChildren().addAll(exposureLbl, exposureTextField, apertureLbl, apertureTextField,
                luminance, lumTextField,coords);
        vertBox.setFillWidth(true);
        vertBox.maxHeightProperty().bind(gridpane.maxHeightProperty());

        /*populate grid*/
        gridpane.add(scrollPane, 0, 0);
        gridpane.add(vertBox, 1, 0);
        VBox outerVbox =  new VBox(menu,gridpane);
        root.getChildren().add(outerVbox);
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private Image openImg(File file) throws IOException {
        BufferedImage srcImg = ImageIO.read(file);
        lumImg.setlLabMatrix(ImageProcessor.constructLlabMatrix(srcImg));
        BufferedImage hueImg = ImageProcessor.constructHueImage(lumImg.getlLabMatrix(), lumImg.getHueImgColors());
        return SwingFXUtils.toFXImage(hueImg, null);
    }

    private HBox createLegend(){
        HBox legend = new HBox();
        legend.setSpacing(5);
        legend.setPadding(new Insets(5,0,0,0));
        DecimalFormat df = new DecimalFormat("#.0");
        java.awt.Color[] awtColors = lumImg.getHueImgColors();
        int colorCount = awtColors.length;
        double intervalSize = (100.f/colorCount);
        Color[] fxColors = new Color[colorCount];

        for(int i = 0; i < colorCount; i++){
            java.awt.Color awtColor = awtColors[i];
            fxColors[i] = Color.rgb(awtColor.getRed(),awtColor.getGreen(),awtColor.getBlue());
            double elementWidth = legend.getMaxWidth();
            double elementHeight = legend.getMaxHeight();

            Label interval = new Label("<" + df.format(i * intervalSize) + ", " + df.format((i + 1)* intervalSize) + ")");
            interval.setFont(Font.font(12));
            Label coloredsquare = new Label("         ");
            coloredsquare.setStyle("-fx-border-style: solid inside;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-color: black;");
            coloredsquare.setBackground(new Background( new BackgroundFill(fxColors[i], CornerRadii.EMPTY, Insets.EMPTY )));
            legend.getChildren().addAll(coloredsquare, interval);

        }
    return legend;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
