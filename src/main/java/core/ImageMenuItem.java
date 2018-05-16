package core;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class ImageMenuItem extends VBox {
    private UnmergedImage imageReference;

    public ImageMenuItem(UnmergedImage image, Node... children){
        super(children);
        this.imageReference = image;
    }

    public UnmergedImage getImageReference() {
        return imageReference;
    }
}
