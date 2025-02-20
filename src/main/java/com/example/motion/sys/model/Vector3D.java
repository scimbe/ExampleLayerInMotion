package com.example.motion.sys.model;

/**
 * Repräsentiert einen dreidimensionalen Vektor für physikalische Berechnungen.
 */
public class Vector3D {
    private final float x;
    private final float y;
    private final float z;

    public Vector3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }

    public Vector3D add(Vector3D other) {
        return new Vector3D(
            this.x + other.x,
            this.y + other.y,
            this.z + other.z
        );
    }

    public Vector3D subtract(Vector3D other) {
        return new Vector3D(
            this.x - other.x,
            this.y - other.y,
            this.z - other.z
        );
    }

    public Vector3D multiply(float scalar) {
        return new Vector3D(
            this.x * scalar,
            this.y * scalar,
            this.z * scalar
        );
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3D normalize() {
        float len = length();
        if (len > 0) {
            return new Vector3D(x / len, y / len, z / len);
        }
        return this;
    }

    public float dot(Vector3D other) {
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3D cross(Vector3D other) {
        return new Vector3D(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        );
    }

    @Override
    public String toString() {
        return String.format("Vector3D(%.2f, %.2f, %.2f)", x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector3D other = (Vector3D) obj;
        return Float.compare(other.x, x) == 0 &&
               Float.compare(other.y, y) == 0 &&
               Float.compare(other.z, z) == 0;
    }

    @Override
    public int hashCode() {
        int result = Float.floatToIntBits(x);
        result = 31 * result + Float.floatToIntBits(y);
        result = 31 * result + Float.floatToIntBits(z);
        return result;
    }
}
