package utils;

import objects.Vector; // Assuming your custom Vector class is in objects package
import objects.Matrix; // Assuming your custom Matrix class is in objects package

public class MathUtils {

    public static Vector addVectors(Vector a, Vector b) {
        return new Vector(a.getX() + b.getX(), a.getY() + b.getY(), a.getZ() + b.getZ());
    }

    public static Vector multiplyMatrix(Matrix m, Vector v) {
        float x = m.get(0, 0) * v.getX() + m.get(0, 1) * v.getY() + m.get(0, 2) * v.getZ();
        float y = m.get(1, 0) * v.getX() + m.get(1, 1) * v.getY() + m.get(1, 2) * v.getZ();
        float z = m.get(2, 0) * v.getX() + m.get(2, 1) * v.getY() + m.get(2, 2) * v.getZ();
        return new Vector(x, y, z);
    }
}