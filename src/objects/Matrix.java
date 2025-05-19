package objects;

public class Matrix {
    private final float[][] data;

    // Creates a 3x3 matrix initialized to zero
    public Matrix() {
        data = new float[3][3];
    }

    // Creates a 3x3 matrix with provided data
    public Matrix(float[][] data) {
        if (data.length != 3 || data[0].length != 3 || data[1].length != 3 || data[2].length != 3) {
            throw new IllegalArgumentException("Matrix must be 3x3.");
        }
        this.data = new float[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(data[i], 0, this.data[i], 0, 3);
        }
    }

    // Get value at row, col
    public float get(int row, int col) {
        return data[row][col];
    }

    // Set value at row, col
    public void set(int row, int col, float value) {
        data[row][col] = value;
    }

    // Optional: Return the underlying data array (defensive copy)
    public float[][] getData() {
        float[][] copy = new float[3][3];
        for (int i = 0; i < 3; i++) {
            System.arraycopy(data[i], 0, copy[i], 0, 3);
        }
        return copy;
    }
}
