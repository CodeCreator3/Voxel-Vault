package objects;

import java.awt.Color;

public class Player extends GameObject {
    public static final float SIZE = 20f;
    private float velocityY = 0f;

    public Player(float x, float y, float z) {
        super(x, y, z, Color.BLUE);
    }

    public float getHalfSize() { return SIZE / 2f; }
    public float getBottomY() { return getY() - getHalfSize(); }
    public float getVelocityY() { return velocityY; }
    public void setVelocityY(float v) { velocityY = v; }
    public void addVelocityY(float v) { velocityY += v; }
}
