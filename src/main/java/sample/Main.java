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
        MenuItem formulaItem = new MenuItem("Change formula...");
        MenuItem generalSettingsItem = new MenuItem("General settings...");
        MenuItem coefficientsItem = new MenuItem("Change coefficients...");
        menuSettings.getItems().addAll(coefficientsItem, formulaItem, generalSettingsItem);
        menu.getMenus().addAll(menuFile, menuSettings);

        coefficientsItem.setOnAction(e -> showCoefficientsWindow());
        formulaItem.setOnAction(e -> showFormulaWindow());
        generalSettingsItem.setOnAction(e -> showGeneralSettingsWindow());

        /*grid setup*/
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(10, 10, 30, 10));
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setPercentWidth(90);
        col2.setPercentWidth(10);
        gridpane.getColumnConstraints().addAll(col1, col2);
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(100);
        gridpane.getRowConstraints().addAll(row1);
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
            } else {
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
            } else {
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
                if (imgDataBuffer.getImages() != null || imgDataBuffer.getFullSizedImage() != null) {
                    //add legend
                    VBox legend = createLegend();
                    vertBox.getChildren().add(legend);
                    root.getChildren().remove(gridpane);
                    root.getChildren().add(gridpane);
                }
            }
        });

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

    private void showCoefficientsWindow() {
        Stage stage = new Stage();
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(15));
        vBox.setAlignment(Pos.CENTER);

        Label label = new Label("The default formula for Luminance calculations is as follows:\n" +
                "L = (F^2/t) * A * exp(B * Llab)\nL ... luminance[cd * m^(-2)]\nF ... aperture number[-]\nt ... exposure[s]\nLlab ... L coordinate in Lab color system[-]" +
                "\nA and B are real number coefficients.\n");
        label.setWrapText(false);
        HBox aHBox = new HBox();
        HBox bHBox = new HBox();
        Label aLbl = new Label("Coefficient A:");
        Label bLbl = new Label("Coefficient B:");
        TextField coeffA = new TextField(prop.getProperty("coefficientA"));
        TextField coeffB = new TextField(prop.getProperty("coefficientB"));
        Button submitBtn = new Button("Submit");

        submitBtn.setOnAction(e -> {
            if (coeffA.getText().matches("^[+-]?([0-9]*[.])?[0-9]+$") && coeffB.getText().matches("^[+-]?([0-9]*[.])?[0-9]+$")) {
                prop.setProperty("coefficientA", coeffA.getText());
                prop.setProperty("coefficientB", coeffB.getText());
                prop.storeProps();
                if (!(imgDataBuffer.getlLabMatrix() == null)) {
                    imgDataBuffer.populateLMatrix();
                }
                stage.close();
            }else{
                alertInvalidNumber();
            }
        });

        aHBox.getChildren().addAll(aLbl, coeffA);
        bHBox.getChildren().addAll(bLbl, coeffB);
        vBox.getChildren().addAll(label, aHBox, bHBox, submitBtn);
        Scene scene = new Scene(vBox, 350, 450);
        stage.setScene(scene);
        stage.show();
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

        for (int i = 0; i < files.size(); i++) {
            images[i] = ImageIO.read(files.get(i));
        }

        if (files.size() == 1) {
            this.imgDataBuffer.setFullSizedImage(images[0]);
            this.imgDataBuffer.setImages(null);
        } else if (imgDimensionsEqual(images)) {
            this.imgDataBuffer.setImages(images);
        } else {
            Alert sizeAlert = new Alert(Alert.AlertType.ERROR);
            sizeAlert.setHeaderText("Image dimensions do not match");
            sizeAlert.setContentText("When loading multiple images, the dimensions of all images must be identical");
            sizeAlert.showAndWait();
            return;
        }

        refreshImage();

    }

    private boolean imgDimensionsEqual(BufferedImage[] images) {
        boolean allEqual = true;
        for (int i = 0; i < images.length - 1; i++) {
            if (images[i].getWidth() != images[i + 1].getWidth() || images[i].getHeight() != images[i + 1].getHeight()) {
                allEqual = false;
            }
        }
        return allEqual;
    }

    private VBox createLegend() {
        VBox legend = new VBox();
        legend.setSpacing(5);
        legend.setPadding(new Insets(0, 0, 0, 0));
        DecimalFormat df = new DecimalFormat("#.0");

        java.awt.Color[] awtColors = imgDataBuffer.getHueImgColors();
        int colorCount = awtColors.length;
        double intervalSize = (100.f / colorCount);
        Color[] fxColors = new Color[colorCount];

        for (int i = 0; i < colorCount; i++) {
            java.awt.Color awtColor = awtColors[i];
            fxColors[i] = Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());

            Label interval = new Label("<" + df.format(i * intervalSize) + ", " + df.format((i + 1) * intervalSize) + ")");
            interval.setStyle("-fx-font: bold 18px arial, serif ");

            Label coloredsquare = new Label("         ");
            coloredsquare.setStyle("-fx-border-style: solid inside;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-color: black;");
            coloredsquare.setBackground(new Background(new BackgroundFill(fxColors[i], CornerRadii.EMPTY, Insets.EMPTY)));

            legend.getChildren().addAll(new HBox(coloredsquare, interval));
        }
        return legend;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
