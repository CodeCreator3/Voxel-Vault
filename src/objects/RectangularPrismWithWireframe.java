package objects;

import java.awt.Color;

public class RectangularPrismWithWireframe extends GameObject {
    public float width, height, depth;

    public RectangularPrismWithWireframe(float centerX, float centerY, float centerZ,
                                         float width, float height, float depth, Color color) {
        super(centerX, centerY, centerZ, color);
        this.width = width;
        this.height = height;
        this.depth = depth;
    }
}