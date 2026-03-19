package ru.peacecraft.core.islands.model;

public final class IslandCoordinate {

    private final double x;
    private final double y;
    private final double z;

    public IslandCoordinate(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public String toString() {
        return "IslandCoordinate{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
    }
}
