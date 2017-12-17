package sample;
/*Author: Lubomir Nepil*/
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;



public class Main extends Application {
    private final LuminanceImage lumImg = new LuminanceImage();

    @Override
    public void start(Stage primaryStage) {
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        primaryStage.setTitle("LuminanceCalculator");
        Group root = new Group();
        Scene scene = new Scene(root, Color.WHITE);

        /*grid setup*/
        GridPane gridpane = new GridPane();
        //gridpane.setHgap(10);
        gridpane.setVgap(10);
        gridpane.setPadding(new Insets(10, 10, 10, 10));
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setPercentWidth(85);
        col2.setPercentWidth(15);
        gridpane.getColumnConstraints().addAll(col1, col2);
        gridpane.gridLinesVisibleProperty().setValue(true);
        gridpane.prefHeightProperty().bind(scene.heightProperty());
        gridpane.prefWidthProperty().bind(scene.widthProperty());

        /*imageView setup*/
        final ImageView imv = new ImageView();
        final Label luminance = new Label("Luminance (cd/m^2)");
        final TextField lumTextField = new TextField("0");
        imv.setOnMouseMoved(e -> {
            if (lumImg.getlMatrix() != null) {
                lumTextField.setText(String.format("%.2f",lumImg.getPixelLuminance((int)Math.floor(e.getX()), (int)Math.floor(e.getY()))));
            }
        });
        imv.fitWidthProperty().bind(gridpane.widthProperty().multiply(0.83));
        imv.fitHeightProperty().bind(gridpane.heightProperty().multiply(0.9));


        /*fileChooser setup*/
        FileChooser fileChooser = new FileChooser();
        Button openBtn = new Button("Open an image...");
        openBtn.setOnAction(event -> {
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                try {
                    imv.setImage(openImg(file, (int) imv.getFitHeight() + 1, (int) imv.getFitWidth() + 1));


                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        openBtn.prefWidthProperty().bind(vertBox.widthProperty());
        vertBox.getChildren().addAll(openBtn, exposureLbl, exposureTextField, apertureLbl, apertureTextField,
                luminance, lumTextField);
        vertBox.setFillWidth(true);

        /*populate grid*/
        gridpane.add(imv, 0, 0);
        gridpane.add(vertBox, 1, 0);
        root.getChildren().add(gridpane);

        primaryStage.setMaximized(true);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private Image openImg(File file, int height, int width) throws IOException {
        BufferedImage srcImg = ImageIO.read(file);

        BufferedImage scaledImg = ImageScaler.scaleImage(srcImg, width, height);//scale to fit frame

        lumImg.setlLabMatrix(ImageProcessor.constructLlabMatrix(scaledImg));
        BufferedImage hueImg = ImageProcessor.constructHueImage(lumImg.getlLabMatrix());
        File outputFile = new File("outputImg.jpg");
        ImageIO.write(hueImg, "jpg", outputFile);
        return new Image(outputFile.toURI().toString());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
