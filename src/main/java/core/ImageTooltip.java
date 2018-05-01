package core;

import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class ImageTooltip extends VBox {
    private UnmergedImage imageReference;

    public ImageTooltip(UnmergedImage image, Node... children){
        super(children);
        this.imageReference = image;
    }

    public UnmergedImage getImageReference() {
        return imageReference;
    }
}
