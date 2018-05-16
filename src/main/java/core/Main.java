package core;
        /*Author: Lubomir Nepil*/

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
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
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
    private ReconstructedImage reconstructedImage;
    private final Tab measure = new Tab("Measure");
    private final Tab colors = new Tab("Color");
    private TabPane tabPane = new TabPane();
    private Button renderBtn = new Button("Render");

    @Override
    public void start(Stage primaryStage) {

        primaryStage.setTitle("HDR Luminance Analyzer");
        VBox root = new VBox();
        Scene scene = new Scene(root, 1200, 1000, Color.WHITE);

        //menu setup
        MenuBar menu = new MenuBar();
        menu.setBackground(new Background(new BackgroundFill(Color.LIGHTGREY, null, null)));
        menu.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, null, BorderWidths.DEFAULT)));
        Menu menuFile = new Menu("File");
        MenuItem openItem = new MenuItem("Open...");
        menuFile.getItems().add(openItem);
        Menu menuSettings = new Menu("Settings");
        MenuItem formulaItem = new MenuItem("Change formula...");
        MenuItem coefficientsItem = new MenuItem("Change coefficients...");
        menuSettings.getItems().addAll(coefficientsItem, formulaItem);
        menu.getMenus().addAll(menuFile, menuSettings);

        coefficientsItem.setOnAction(e -> showCoefficientsWindow());

        /*grid setup*/
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(10, 10, 30, 10));
        gridpane.setHgap(10);
        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setPercentWidth(90);
        col2.setPercentWidth(10);
        gridpane.getColumnConstraints().addAll(col1, col2);
        RowConstraints row1 = new RowConstraints();
        row1.setPercentHeight(100);
        gridpane.getRowConstraints().addAll(row1);
        gridpane.gridLinesVisibleProperty().setValue(false);
        gridpane.prefHeightProperty().bind(scene.heightProperty());
        gridpane.prefWidthProperty().bind(scene.widthProperty());

        /*RHS menu setup*/

        final Label luminance = new Label("Luminance (cd/m^2)");
        luminance.setFont(Font.font("Segoe UI", 12));
        luminance.setMaxWidth(Double.MAX_VALUE);
        luminance.setAlignment(Pos.CENTER);

        final TextField lumTextField = new TextField("0");
        lumTextField.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lumTextField.setAlignment(Pos.CENTER);

        final Label llabLabel = new Label("LLab");
        llabLabel.setFont(Font.font("Segoe UI", 12));
        llabLabel.setMaxWidth(Double.MAX_VALUE);
        llabLabel.setAlignment(Pos.CENTER);

        final TextField llabTextField = new TextField("0");
        llabTextField.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        llabTextField.setAlignment(Pos.CENTER);

        final Label coords = new Label("");
        coords.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        coords.setMaxWidth(Double.MAX_VALUE);
        coords.setAlignment(Pos.CENTER);

        imv.setPreserveRatio(true);
        imv.setOnMouseMoved(e -> {
            if (this.displayedImage.isInitialized()) {
                int x = (int) Math.floor(e.getX());
                int y = (int) Math.floor(e.getY());
                coords.setText(
                        "X: " + x + " Y: " + y);

                llabTextField.setText(String.format("%.3f",
                        this.displayedImage.getlLabMatrix()[y][x]));

                lumTextField.setText(String.format("%.3f",
                        this.displayedImage.getLuminanceMatrix()[y][x]));
            }
        });

        /*ScrollPane setup*/
        ScrollPane scrollPane = new ScrollPane(imv);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        VBox vertBox = new VBox(10);
        vertBox.setPadding(new Insets(10, 10, 10, 10));
        vertBox.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));
        vertBox.getChildren().addAll(llabLabel, llabTextField, luminance, lumTextField,
                coords);
        vertBox.setFillWidth(true);
        vertBox.maxHeightProperty().bind(gridpane.maxHeightProperty());

        /*fileChooser setup*/
        FileChooser fileChooser = new FileChooser();
        List<String> extensions = Arrays.asList("*.jpg", "*.jpeg", "*.tif", "*.tiff",
                "*.png", "*.bmp");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image files", extensions));
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

        /*tabpane setup*/
        measure.setContent(vertBox);
        colors.setContent(createColorVBox(convertAwtColorsToJFX(imgDataCache.getDefaultColors())));
        tabPane.getTabs().addAll(measure, colors);

        /*populate grid*/
        gridpane.add(scrollPane, 0, 0);
        gridpane.add(tabPane, 1, 0);
        root.getChildren().addAll(menu, gridpane);

        primaryStage.setMaximized(true);
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createColorVBox(Color[] colors) {
        Label thresholdLabel = new Label("L (cd/m^2)");
        thresholdLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        thresholdLabel.setAlignment(Pos.CENTER_RIGHT);
        thresholdLabel.setMaxWidth(Double.MAX_VALUE);

        VBox pickers = new VBox();
        pickers.setPadding(new Insets(10, 10, 10, 10));
        pickers.setSpacing(5);
        pickers.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), BorderWidths.DEFAULT)));
        pickers.getChildren().add(thresholdLabel);

        for (int i = 0; i < colors.length; i++) {
            HBox row = new HBox();
            row.setSpacing(5);
            row.setAlignment(Pos.CENTER);

            ColorPicker picker = new ColorPicker(colors[colors.length - 1 - i]);
            picker.setStyle("-fx-color-label-visible: false ;" +
                    "-fx-color-rect-width: 20px ;" +
                    "-fx-color-rect-height: 20px;");
            picker.setMaxWidth(Double.MAX_VALUE);

            TextField luminanceThreshhold = new TextField();
            luminanceThreshhold.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            luminanceThreshhold.setAlignment(Pos.CENTER);
            luminanceThreshhold.setMinWidth(0);

            row.getChildren().addAll(picker, luminanceThreshhold);
            pickers.getChildren().add(row);
        }

        renderBtn.setVisible(false);
        renderBtn.setOnAction(e -> {
            List<TextField> textFields = extractLuminanceThreshholdTextFields();
            double[] threshholds = new double[textFields.size()];

            for (int i = 0; i < textFields.size(); i++) {
                String text = textFields.get(i).getText();
                if (isValidDouble(text)) {
                    threshholds[i] = Double.parseDouble(text);
                } else {
                    alertInvalidNumber();
                    break;
                }
            }
            if (!validateThreshHoldValues(threshholds)) {
                alertInvalidValues();
            } else {
                java.awt.Color[] extractedColors = extractSelectedColors();
                reconstructedImage.setLuminanceThreshholds(threshholds);
                reconstructedImage.setColors(extractedColors);
                reconstructedImage = ColorMapper.reconstructImage(displayedImage, reconstructedImage);
                imv.setImage(SwingFXUtils.toFXImage(reconstructedImage, null));
            }
        });
        renderBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        renderBtn.setAlignment(Pos.CENTER);
        renderBtn.setMaxWidth(Double.MAX_VALUE);

        pickers.getChildren().add(renderBtn);
        return pickers;
    }

    private void alertInvalidValues() {
        Alert invalidValueAlert = new Alert(Alert.AlertType.ERROR);
        invalidValueAlert.setContentText("Luminance threshholds not valid.\n" +
                "Make sure they are in descending order and\n" +
                "within the bounds of max and min L values.");
        invalidValueAlert.setHeaderText("Invalid input");
        invalidValueAlert.showAndWait();
    }

    private java.awt.Color[] extractSelectedColors() {
        VBox box = (VBox) colors.getContent();
        List<HBox> boxes = box.getChildren().stream()
                .filter(e -> e instanceof HBox)
                .map(obj -> (HBox) obj)
                .collect(Collectors.toList());

        //get each color from each box and put into the list
        List<ColorPicker> colorpickers = new ArrayList<>();
        boxes.stream().forEach(hbox -> colorpickers.add((ColorPicker) hbox.getChildren().get(0)));

        List<Color> fxcolors = new ArrayList<>();
        colorpickers.stream().forEach(p -> fxcolors.add(p.getValue()));

        return convertFxColorsToAwt(fxcolors.toArray(new Color[fxcolors.size()]));
    }

    private boolean validateThreshHoldValues(double[] threshholds) {
        if (threshholds[0] > displayedImage.getMaxLuminance()) return false;
        if (threshholds[threshholds.length - 1] < displayedImage.getMinLuminance()) return false;

        for (int i = 0; i < threshholds.length - 1; i++) {
            if (threshholds[i] <= threshholds[i + 1]) return false;
        }
        return true;
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
            } else {
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

    private Color[] convertAwtColorsToJFX(java.awt.Color[] awtColors) {
        Color[] fxColors = new Color[awtColors.length];

        for (int i = 0; i < awtColors.length; i++) {
            java.awt.Color awtColor = awtColors[i];

            int r = awtColor.getRed();
            int g = awtColor.getGreen();
            int b = awtColor.getBlue();
            int a = awtColor.getAlpha();
            double opacity = a / 255.0;

            javafx.scene.paint.Color fxColor = javafx.scene.paint.Color.rgb(r, g, b, opacity);
            fxColors[i] = fxColor;
        }

        return fxColors;
    }

    private java.awt.Color[] convertFxColorsToAwt(Color[] fxColors) {
        java.awt.Color[] awtColors = new java.awt.Color[fxColors.length];

        for (int i = 0; i < awtColors.length; i++) {
            Color fxColor = fxColors[i];
            awtColors[i] = new java.awt.Color(
                    (float) fxColor.getRed(),
                    (float) fxColor.getGreen(),
                    (float) fxColor.getBlue(),
                    (float) fxColor.getOpacity());
        }

        return awtColors;
    }

    private void refreshView() {
        if ((!imgDataCache.getImageList().contains(displayedImage) && displayedImage instanceof UnmergedImage)
                || imgDataCache.getImageList().isEmpty()) {
            displayedImage = null;
            imv.setImage(null);
            renderBtn.setVisible(false);
        }

        if (displayedImage != null) {
            if (displayedImage instanceof UnmergedImage) {
                this.imgDataCache.initializeImageMatrices(displayedImage, recalculateLuminanceMatrix);
            }
            reconstructedImage = ColorMapper.reconstructImage(displayedImage);
            updateColorThreshholds();
            renderBtn.setVisible(true);
            imv.setImage(SwingFXUtils.toFXImage(reconstructedImage, null));

        }

        recalculateLuminanceMatrix = false;
    }

    private void resetColorsToDefault(){
        VBox box = (VBox) colors.getContent();
        List<HBox> boxes = box.getChildren().stream()
                .filter(e -> e instanceof HBox)
                .map(obj -> (HBox) obj)
                .collect(Collectors.toList());

        //reset each colorpicker
        List<ColorPicker> colorpickers = new ArrayList<>();
        boxes.stream().forEach(hbox -> colorpickers.add((ColorPicker) hbox.getChildren().get(0)));

        Color[] colors = convertAwtColorsToJFX(reconstructedImage.DEFAULT_COLORS);
        for(int j = 0; j < colors.length; j++){
            colorpickers.get(j).setValue(colors[j]);
        }
    }
    private void updateColorThreshholds() {
        resetColorsToDefault();
        List<TextField> textFields = extractLuminanceThreshholdTextFields();
        double[] threshholds = reconstructedImage.getLuminanceThreshholds();

        for (int i = 0; i < threshholds.length; i++) {
            textFields.get(i).setText(String.format("%.3f",
                    threshholds[i]));
        }

    }

    private List<TextField> extractLuminanceThreshholdTextFields() {
        VBox box = (VBox) colors.getContent();
        List<HBox> boxes = box.getChildren().stream()
                .filter(e -> e instanceof HBox)
                .map(obj -> (HBox) obj)
                .collect(Collectors.toList());
        //get each textfield from each box and put into the list
        List<TextField> textFields = new ArrayList<>();
        boxes.stream().forEach(hbox -> textFields.add((TextField) hbox.getChildren().get(1)));
        return textFields;
    }

    private void loadImg(File file, VBox rightMenu) throws IOException {

        final String fileName = file.getName();
        final BufferedImage image = ImageIO.read(file);

        Stage exposureInputStage = new Stage();

        Label exposureLabel = new Label("Enter exposure parameters for file:\n"
                + fileName);
        exposureLabel.setFont(Font.font("Segoe UI", 12));

        TextField fNumberTextField = new TextField("F-number");
        fNumberTextField.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        fNumberTextField.setAlignment(Pos.CENTER);

        TextField exposureTimeTextField = new TextField("exposure time [s]");
        exposureTimeTextField.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        exposureTimeTextField.setAlignment(Pos.CENTER);

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            String fNumberText = fNumberTextField.getText();
            String exposureTimeText = exposureTimeTextField.getText();

            if (isValidDouble(fNumberText) && isValidDouble(exposureTimeText)) {
                double fNumber = Double.parseDouble(fNumberText);
                double exposureTime = Double.parseDouble(exposureTimeText);

                UnmergedImage toBeAdded = new UnmergedImage(image, exposureTime, fNumber, fileName);
                imgDataCache.getImageList().add(toBeAdded);

                setupImageHandlingButtons(rightMenu);
                addImageMenuItem(toBeAdded, rightMenu);
                exposureInputStage.close();
            } else {
                alertInvalidNumber();
            }
        });

        VBox vBox = new VBox(exposureLabel, fNumberTextField, exposureTimeTextField, okBtn);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(5));

        Scene scene = new Scene(vBox, 200, 160);
        exposureInputStage.setScene(scene);
        exposureInputStage.show();
    }

    private void setupImageHandlingButtons(VBox rightMenu) {
        if (!imageButtonsCreated) {
            Button displayBtn = new Button("View");
            displayBtn.setFont(Font.font("Segoe UI", 12));
            displayBtn.setOnAction(e -> {
                List<UnmergedImage> selectedImgs = imgDataCache.getSelectedImages();
                if (!selectedImgs.isEmpty()) {
                    if (selectedImgs.size() == 1) {
                        this.displayedImage = selectedImgs.get(0);
                    } else {
                        if (imgDataCache.imgDimensionsEqual()) {
                            this.displayedImage = imgDataCache.mergeSelectedImages();
                        } else {
                            alertDimensionsNotEqual();
                        }
                    }
                    refreshView();
                }
            });

            Button deleteBtn = new Button("Delete");
            deleteBtn.setFont(Font.font("Segoe UI", 12));

            deleteBtn.setOnAction(e -> {
                List<UnmergedImage> selectedImgs = imgDataCache.getSelectedImages();
                if (selectedImgs != null) {
                    //get tooltips from rightmenu
                    List<ImageMenuItem> imgMenuItems = rightMenu.getChildren().stream()
                            .filter(elem -> elem instanceof ImageMenuItem)
                            .map(item -> (ImageMenuItem) item)
                            .collect(Collectors.toList());
                    //remove selected tooltips from menu
                    imgMenuItems.stream()
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
            box.setSpacing(10);
            box.setAlignment(Pos.CENTER);
            rightMenu.getChildren().add(box);
            imageButtonsCreated = true;
        }
    }

    private void alertDimensionsNotEqual() {
        Alert invalidNumberAlert = new Alert(Alert.AlertType.ERROR);
        invalidNumberAlert.setContentText("Selected images do not have the same sizes!\nCannot merge images with different dimensions.");
        invalidNumberAlert.setHeaderText("Size mismatch");
        invalidNumberAlert.showAndWait();
    }

    private void addImageMenuItem(UnmergedImage addedImage, VBox rightMenu) {

        Label imgName = new Label(addedImage.getImgName());
        imgName.setPadding(new Insets(0, 0, 0, 5));
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
        hBox.setPadding(new Insets(0, 0, 2, 0));
        hBox.setAlignment(Pos.CENTER);

        ImageMenuItem imgMenuItem = new ImageMenuItem(addedImage, imgName, hBox);
        imgMenuItem.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        imgMenuItem.setAlignment(Pos.CENTER);
        rightMenu.getChildren().add(imgMenuItem);
    }

    private void showImageDetails(UnmergedImage image) {

        Label exposureTime = new Label("Exposure time: " + String.format("%.5f", image.getExposureTime()));
        exposureTime.setFont(Font.font("Segoe UI", 12));

        Label fNumber = new Label("F-number: " + String.format("%.5f", image.getfNumber()));
        fNumber.setFont(Font.font("Segoe UI", 12));

        Label resolution = new Label(image.getResulution());
        resolution.setFont(Font.font("Segoe UI", 12));

        Button closeBtn = new Button("Close");
        closeBtn.setFont(Font.font("Segoe UI", 12));

        VBox vBox = new VBox(resolution, exposureTime, fNumber, closeBtn);
        vBox.setSpacing(5);
        vBox.setAlignment(Pos.CENTER);

        Stage stage = new Stage();
        stage.setTitle(image.getImgName());
        Scene scene = new Scene(vBox, 300, 100);
        stage.setScene(scene);

        closeBtn.setOnAction(e -> stage.close());
        stage.show();
    }

    private boolean isValidDouble(String text) {
        return text.matches("^[+-]?([0-9]*[.])?[0-9]+$");
    }

    public static void main(String[] args) {
        launch(args);
    }


}
