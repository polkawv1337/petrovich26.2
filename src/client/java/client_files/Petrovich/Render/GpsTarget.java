package client_files.Petrovich.Render;

public final class GpsTarget {

    private static final GpsTarget INSTANCE = new GpsTarget();

    private double targetX;
    private double targetY;
    private double targetZ;
    private boolean enabled;

    public static GpsTarget get() {
        return INSTANCE;
    }

    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.enabled = true;
    }

    public void clear() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getTargetX() {
        return targetX;
    }

    public double getTargetY() {
        return targetY;
    }

    public double getTargetZ() {
        return targetZ;
    }
}