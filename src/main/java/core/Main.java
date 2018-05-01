package core;
        /*Author: Lubomir Nepil*/

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class Main extends Application {
    private final ImageDataCache imgDataCache = ImageDataCache.getInstance();
    private final PropertiesManager prop = new PropertiesManager();
    private final ImageView imv = new ImageView();
    private final ImageProcessor processor = imgDataCache.getProcessor();
    private boolean imageButtonsCreated = false;
    private boolean recalculateLuminanceMatrix = false;
    private MyImage displayedImage;

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
        MenuItem generalSettingsItem = new MenuItem("Do not touch...");
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
        final Label llabLabel = new Label("Llab");
        final TextField llabTextField = new TextField("0");
        final Label coords = new Label("");

        imv.setOnMouseMoved(e -> {
            if (this.displayedImage.isInitialized()) {
                coords.setText("x: " + (int) Math.floor(e.getX()) + " y: " + (int) Math.floor(e.getY()));
                llabTextField.setText(String.format("%.2f", this.displayedImage.getlLabMatrix()[(int) Math.floor(e.getY())][(int) Math.floor(e.getX())]));
                lumTextField.setText(String.format("%.2f", this.displayedImage.getLuminanceMatrix()[(int) Math.floor(e.getY())][(int) Math.floor(e.getX())]));
            }
        });

        /*ScrollPane setup*/
        ScrollPane scrollPane = new ScrollPane(imv);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox vertBox = new VBox(10);
        vertBox.setPadding(new Insets(10, 5, 0, 5));
        vertBox.getChildren().addAll(llabLabel, llabTextField, luminance, lumTextField, coords);
        vertBox.setFillWidth(true);
        vertBox.maxHeightProperty().bind(gridpane.maxHeightProperty());

        /*fileChooser setup*/
        FileChooser fileChooser = new FileChooser();
        List<String> extensions = Arrays.asList("*.jpg", "*.jpeg", "*.tif", "*.tiff", "*.png", "*.bmp");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image files", extensions));
        openItem.setOnAction(event -> {
            File file = fileChooser.showOpenDialog(primaryStage);

            if (file != null) {
                try {
                    loadImg(file, vertBox);
                } catch (IOException e) {
                    e.printStackTrace();
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
            if (isValidDouble(coeffA.getText()) && isValidDouble(coeffB.getText())) {
                prop.setProperty("coefficientA", coeffA.getText());
                prop.setProperty("coefficientB", coeffB.getText());
                prop.storeProps();
                stage.close();
                recalculateLuminanceMatrix = true;
                refreshView();
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
                    /*if (this.imgDataCache.getFullSizedImage() != null) {
                        refreshView();
                    }*/
                } else {
                    prop.setProperty("fitToWindow", "false");
                    /*if (this.imgDataCache.getFullSizedImage() != null) {
                        refreshView();
                    }*/
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
        //TODO: add formula input validation
        submitBtn.setOnAction(e -> {
            imgDataCache.setLuminanceFormula(formulaText.getText());
            stage.close();
            recalculateLuminanceMatrix = true;
            refreshView();
        });
        formulaBox.getChildren().addAll(label, formulaText, submitBtn);
        Scene scene = new Scene(formulaBox, 250, 150);
        stage.setScene(scene);
        stage.show();
    }

    private void refreshView() {
        if (!imgDataCache.getImageList().contains(displayedImage) && displayedImage instanceof UnmergedImage) {
            displayedImage = null;
            imv.setImage(null);
        }

        if (displayedImage != null) {
            if (displayedImage instanceof UnmergedImage) {
                this.imgDataCache.initializeImageMatrices(displayedImage, recalculateLuminanceMatrix);
            }
            BufferedImage hueImg = processor.constructHueImage(displayedImage.getlLabMatrix(), imgDataCache.getHueImgColors());
            imv.setImage(SwingFXUtils.toFXImage(hueImg, null));
        }

        recalculateLuminanceMatrix = false;
    }

    private void loadImg(File file, VBox rightMenu) throws IOException {

            final String fileName = file.getName();
            final BufferedImage image = ImageIO.read(file);

            Stage exposureInputStage = new Stage();

            Label exposureLabel = new Label("Enter exposure parameters below:");
            TextField fNumberTextField = new TextField("F-number");
            TextField exposureTimeTextField = new TextField("exposure time [s]");

            Button okBtn = new Button("OK");
            okBtn.setOnAction( e -> {
                String fNumberText = fNumberTextField.getText();
                String exposureTimeText = exposureTimeTextField.getText();

                if (isValidDouble(fNumberText) && isValidDouble(exposureTimeText)) {
                    double fNumber = Double.parseDouble(fNumberText);
                    double exposureTime = Double.parseDouble(exposureTimeText);

                    UnmergedImage toBeAdded = new UnmergedImage(image, exposureTime, fNumber, fileName);
                    imgDataCache.getImageList().add(toBeAdded);


                    exposureInputStage.close();
                    setupImageHandlingButtons(rightMenu);
                    addTooltip(toBeAdded, rightMenu);
                } else {
                    alertInvalidNumber();
                }
            });
            VBox vBox = new VBox(exposureLabel, fNumberTextField, exposureTimeTextField, okBtn);
            Scene scene = new Scene(vBox, 200, 200);
            exposureInputStage.setScene(scene);
            exposureInputStage.show();
    }

    private void setupImageHandlingButtons(VBox rightMenu){
        if(!imageButtonsCreated) {
            Button displayBtn = new Button("View");
            displayBtn.setOnAction(e -> {
                List<UnmergedImage> selectedImgs = imgDataCache.getSelectedImages();
                if (!selectedImgs.isEmpty()) {
                    if (selectedImgs.size() == 1) {
                        this.displayedImage = selectedImgs.get(0);
                    } else {
                        this.displayedImage = imgDataCache.mergeAllImages();
                    }
                    refreshView();
                }
            });

            Button deleteBtn = new Button("Delete");
            deleteBtn.setOnAction(e -> {
                List<UnmergedImage> selectedImgs = imgDataCache.getSelectedImages();
                if (selectedImgs != null) {
                    //get tooltips from rightmenu
                    List<ImageTooltip> tooltips = rightMenu.getChildren().stream()
                            .filter(elem -> elem instanceof ImageTooltip)
                            .map(tip -> (ImageTooltip) tip)
                            .collect(Collectors.toList());
                    //remove selected tooltips from menu
                    tooltips.stream()
                            .forEach(a -> {
                                UnmergedImage img = a.getImageReference();
                                if (selectedImgs.contains(img)) {
                                    rightMenu.getChildren().remove(a);
                                    imgDataCache.getImageList().remove(img);
                                }
                            });
                    refreshView();
                }
            });
            HBox box = new HBox(displayBtn, deleteBtn);
            rightMenu.getChildren().add(box);
            imageButtonsCreated = true;
        }
    }
    private void addTooltip(UnmergedImage addedImage, VBox rightMenu) {

        Label imgName = new Label(addedImage.getImgName());
        imgName.setPadding(new Insets(0,0,0,5));
        imgName.setAlignment(Pos.CENTER);

        CheckBox checkBox = new CheckBox();
        checkBox.setOnAction(e -> {
            addedImage.setSelected(checkBox.isSelected());
        });

        Button detailsBtn = new Button("Details");
        detailsBtn.setOnAction(e -> {
            showImageDetails(addedImage);
        });

        HBox hBox = new HBox(checkBox, detailsBtn);
        hBox.setAlignment(Pos.CENTER);

        ImageTooltip imgTooltip = new ImageTooltip(addedImage, imgName, hBox);
        imgTooltip.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        imgTooltip.setAlignment(Pos.CENTER);
        rightMenu.getChildren().add(imgTooltip);
    }

    private void showImageDetails(UnmergedImage image) {

        Label exposureTime = new Label("Exposure time: " + String.format("%.5f", image.getExposureTime()));
        Label fNumber = new Label("F-number: " + String.format("%.5f", image.getfNumber()));
        Label resolution = new Label(image.getResulution());
        Button closeBtn = new Button("Close");

        VBox vBox = new VBox(resolution, exposureTime, fNumber, closeBtn);
        vBox.setAlignment(Pos.CENTER);
        Stage stage = new Stage();
        stage.setTitle(image.getImgName());
        Scene scene = new Scene(vBox, 300, 100);
        stage.setScene(scene);

        closeBtn.setOnAction(e -> stage.close());
        stage.show();
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
        legend.setStyle("-fx-border-width: 1px;"
                        + "-fx-border-color: black;"
                        + "-fx-border-line: solid;"
                        + "-fx-padding: 5px, 5px, 5px, 5px;"
                        + "-fx-spacing: 5px;"
        );
        legend.setMaxWidth(Double.MAX_VALUE);
        legend.setMaxHeight(Double.MAX_VALUE);
        DecimalFormat df = new DecimalFormat("#");

        java.awt.Color[] awtColors = imgDataCache.getHueImgColors();
        int colorCount = awtColors.length;
        double intervalSize = (100.f / colorCount);
        Color[] fxColors = new Color[colorCount];

        Label titleL = new Label("Lightness");
        titleL.setStyle("-fx-alignment: center;");
        legend.getChildren().add(titleL);

        for (int i = 0; i < colorCount; i++) {
            java.awt.Color awtColor = awtColors[i];
            fxColors[i] = Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue());

            Label interval = new Label(df.format(i * intervalSize));
            interval.setMaxWidth(Double.MAX_VALUE);
            interval.setStyle("-fx-font: bold 15px arial, serif;"
                    + "-fx-padding: 0 0 0 10px;"
                    + "-fx-text-align: center;"
                    + "-fx-spacing: 10px");

            Label coloredsquare = new Label("         ");
            coloredsquare.setStyle("-fx-border-style: solid inside;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-color: black;"
                    + "-fx-spacing: 10px");
            coloredsquare.setBackground(new Background(new BackgroundFill(fxColors[i], CornerRadii.EMPTY, Insets.EMPTY)));
            HBox legendLine = new HBox(coloredsquare, interval);
            legendLine.setStyle("-fx-alignment: center;");

            legend.getChildren().add(legendLine);
        }

        return legend;
    }

    private boolean isValidDouble(String text){
        return text.matches("^[+-]?([0-9]*[.])?[0-9]+$");
    }

    public static void main(String[] args) {
        launch(args);
    }


}
