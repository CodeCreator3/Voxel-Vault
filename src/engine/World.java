package engine;

import objects.GameObject;
import objects.RectangularPrismWithWireframe;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class World {
    public final float groundY = 0f;
    public final RectangularPrismWithWireframe platform =
        new RectangularPrismWithWireframe(0, 300f, 0, 240, 40, 240, Color.GREEN);

    public final List<GameObject> movingCubes = new ArrayList<>();
    public final List<GameObject> allObjects = new ArrayList<>();
}
