package objects;

import java.awt.Color;

public class GameObject {
    protected float x, y, z;
    protected Color color;
    protected float velocityX = 0f, velocityY = 0f, velocityZ = 0f;
    public GameObject pairedWith = null;

    public GameObject(float x, float y, float z, Color color) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = color;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setZ(float z) { this.z = z; }
    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public float getVelocityX() { return velocityX; }
    public void setVelocityX(float vx) { this.velocityX = vx; }
    public float getVelocityY() { return velocityY; }
    public void setVelocityY(float vy) { this.velocityY = vy; }
    public float getVelocityZ() { return velocityZ; }
    public void setVelocityZ(float vz) { this.velocityZ = vz; }
}