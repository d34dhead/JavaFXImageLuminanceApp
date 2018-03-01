package sample;
        /*Author: Lubomir Nepil*/

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Main extends Application {
    private final ImageDataBuffer imgDataBuffer = new ImageDataBuffer();
    private final PropertiesManager prop = new PropertiesManager();
    private final ImageView imv = new ImageView();
    private final ImageProcessor processor = imgDataBuffer.getProcessor();
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
        MenuItem formulaItem = new MenuItem("Insert formula...");
        MenuItem generalSettingsItem = new MenuItem("General settings...");
        menuSettings.getItems().addAll(formulaItem, generalSettingsItem);
        menu.getMenus().addAll(menuFile, menuSettings);

        formulaItem.setOnAction(e -> showFormulaWindow());
        generalSettingsItem.setOnAction(e -> showGeneralSettingsWindow());

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
        row1.setPercentHeight(93);
        row2.setPercentHeight(7);
        gridpane.getRowConstraints().addAll(row1, row2);
        gridpane.gridLinesVisibleProperty().setValue(true);
        gridpane.prefHeightProperty().bind(scene.heightProperty());
        gridpane.prefWidthProperty().bind(scene.widthProperty());


        /*imageView setup*/
        imv.setPreserveRatio(true);
        final Label luminance = new Label("Luminance (cd/m^2)");
        final TextField lumTextField = new TextField("0");
        final Label coords = new Label("");

        imv.setOnMouseMoved(e -> {
            if (imgDataBuffer.getlMatrix() != null) {
                coords.setText("x: " + (int) Math.floor(e.getX()) + " y: " + (int) Math.floor(e.getY()));
                lumTextField.setText(String.format("%.2f", imgDataBuffer.getPixelLuminance((int) Math.floor(e.getX()), (int) Math.floor(e.getY()))));
            }
        });

        /*ScrollPane setup*/
        ScrollPane scrollPane = new ScrollPane(imv);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        /*fileChooser setup*/
        FileChooser fileChooser = new FileChooser();
        List<String> extensions = Arrays.asList("*.jpg", "*.jpeg", "*.tif", "*.tiff", "*.png", "*.bmp");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files", extensions));
        openItem.setOnAction(event -> {
            List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);

            if (files != null) {
                try {
                    openImg(files);

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
            if (text.matches("^[+-]?([0-9]*[.])?[0-9]+$")) {
                this.imgDataBuffer.setExposure(Double.parseDouble(text));
                if (this.imgDataBuffer.apertureAndExposureSet()) {
                    this.imgDataBuffer.populateLMatrix();
                }
            }else{
                alertInvalidNumber();
            }
        });

        Label apertureLbl = new Label("Aperture number");
        TextField apertureTextField = new TextField("");
        apertureTextField.setOnAction(e -> {
            String text = apertureTextField.getText();
            if (text.matches("^[+-]?([0-9]*[.])?[0-9]+$")) {
                this.imgDataBuffer.setAperture(Double.parseDouble(text));
                if (this.imgDataBuffer.apertureAndExposureSet()) {
                    this.imgDataBuffer.populateLMatrix();
                }
            }else {
                alertInvalidNumber();
            }
        });

        /*Vbox setup*/
        VBox vertBox = new VBox(10);
        vertBox.setPadding(new Insets(10, 5, 0, 5));
        vertBox.getChildren().addAll(exposureLbl, exposureTextField, apertureLbl, apertureTextField,
                luminance, lumTextField, coords);
        vertBox.setFillWidth(true);
        vertBox.maxHeightProperty().bind(gridpane.maxHeightProperty());

        /*populate grid*/
        gridpane.add(scrollPane, 0, 0);
        gridpane.add(vertBox, 1, 0);
        VBox outerVbox = new VBox(menu, gridpane);
        root.getChildren().add(outerVbox);
        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private void alertInvalidNumber() {
        Alert invalidNumberAlert = new Alert(Alert.AlertType.ERROR);
        invalidNumberAlert.setContentText("Enter a valid real number.\nUse . as the decimal separator");
        invalidNumberAlert.setHeaderText("Invalid number");
        invalidNumberAlert.showAndWait();
    }

    private void showGeneralSettingsWindow() {
        boolean fit = Boolean.parseBoolean(prop.getProperty("fitToWindow"));

        Stage stage = new Stage();
        VBox settingsBox = new VBox();
        settingsBox.setPadding(new Insets(10));
        settingsBox.setAlignment(Pos.CENTER);

        CheckBox fitToWindowCheck = new CheckBox("Fit image to window (image will be downsized accordingly, " +
                "loss of precision may occur.");
        fitToWindowCheck.setSelected(fit);
        fitToWindowCheck.setWrapText(true);
        Button okBtn = new Button("Save");
        settingsBox.getChildren().addAll(fitToWindowCheck, okBtn);

        okBtn.setOnAction(e -> {
            //if checkbox state same as saved property, do nothing
            if (fitToWindowCheck.isSelected() != fit) {

                if (fitToWindowCheck.isSelected()) {
                    prop.setProperty("fitToWindow", "true");
                    if (this.imgDataBuffer.getFullSizedImage() != null) {
                        refreshImage();
                    }
                } else {
                    prop.setProperty("fitToWindow", "false");
                    if (this.imgDataBuffer.getFullSizedImage() != null) {
                        refreshImage();
                    }
                }
            }

            prop.storeProps();
            stage.close();
        });

        Scene scene = new Scene(settingsBox, 250, 150);
        stage.setScene(scene);
        stage.show();
    }

    private void showFormulaWindow() {
        Stage stage = new Stage();
        VBox formulaBox = new VBox();
        formulaBox.setPadding(new Insets(10));
        formulaBox.setAlignment(Pos.CENTER);

        Label label = new Label("Enter formula to calculate luminance(L [cd/m^2]) from lightness(Llab [-]), aperture number(F [-]) and exposure time(t [s])." +
                "Example: (F^2*t)*e^(Llab*10)");
        label.setWrapText(true);
        TextField formulaText = new TextField();
        formulaText.setPromptText("enter formula");
        Button submitBtn = new Button("Submit");
        //TODO:add input validation with alert
        submitBtn.setOnAction(e -> {
            imgDataBuffer.setLuminanceFormula(formulaText.getText());
            if (!(imgDataBuffer.getlLabMatrix() == null)) {
                imgDataBuffer.populateLMatrix();
            }
            stage.close();
        });
        formulaBox.getChildren().addAll(label, formulaText, submitBtn);
        Scene scene = new Scene(formulaBox, 250, 150);
        stage.setScene(scene);
        stage.show();
    }

    private void refreshImage() {
        boolean resize = Boolean.parseBoolean(prop.getProperty("fitToWindow"));

        if (resize) {

            int srcWidth = this.imgDataBuffer.getFullSizedImage().getWidth();
            int srcHeight = this.imgDataBuffer.getFullSizedImage().getHeight();
            double aspectRatio = (double) srcWidth / (double) srcHeight;

            if (srcHeight > 1080) {
                this.imgDataBuffer.setResizedImg(ImageScaler.rescale(this.imgDataBuffer.getFullSizedImage(), (int) (1080 * aspectRatio), 1080));
            }

        }
            this.imgDataBuffer.populateLlabMatrix(resize);
            BufferedImage hueImg = processor.constructHueImage(imgDataBuffer.getlLabMatrix(), imgDataBuffer.getHueImgColors());
            imv.setImage(SwingFXUtils.toFXImage(hueImg, null));

        //refresh luminance matrix if coefficients are set
            this.imgDataBuffer.populateLMatrix();

    }

    private void openImg(List<File> files) throws IOException {
        BufferedImage[] images = new BufferedImage[files.size()];

        for(int i = 0; i < files.size(); i++){
            images[i] = ImageIO.read(files.get(i));
        }

        if(files.size() == 1){
            this.imgDataBuffer.setFullSizedImage(images[0]);
            this.imgDataBuffer.setImages(null);
        }else{
            this.imgDataBuffer.setImages(images);
        }

        refreshImage();
    }

    private HBox createLegend() {
        HBox legend = new HBox();
        legend.setSpacing(5);
        legend.setPadding(new Insets(5, 0, 0, 0));
        DecimalFormat df = new DecimalFormat("#.0");

        java.awt.Color[] awtColors = imgDataBuffer.getHueImgColors();
        int colorCount = awtColors.length;
        double intervalSize = (100.f / colorCount);
        Color[] fxColors = new Color[colorCount];

        for (int i = 0; i < colorCount; i++) {
            java.awt.Color awtColor = awtColors[i];
            fxColors[i] = Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());

            Label interval = new Label("<" + df.format(i * intervalSize) + ", " + df.format((i + 1) * intervalSize) + ")");
            interval.setStyle("-fx-font: italic bold 18px arial, serif ");

            Label coloredsquare = new Label("         ");
            coloredsquare.setStyle("-fx-border-style: solid inside;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-color: black;");
            coloredsquare.setBackground(new Background(new BackgroundFill(fxColors[i], CornerRadii.EMPTY, Insets.EMPTY)));

            legend.getChildren().addAll(coloredsquare, interval);

        }
        return legend;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
