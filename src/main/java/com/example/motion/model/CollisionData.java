package com.example.motion.model;

public class CollisionData {
    private double x;
    private double y;
    private double z;
    private double radius;

    public CollisionData() {}

    public CollisionData(double x, double y, double z, double radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
    }

    // Getters and setters
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }

    public double getRadius() { return radius; }
    public void setRadius(double radius) { this.radius = radius; }
}
