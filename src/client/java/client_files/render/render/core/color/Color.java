package client_files.render.render.core.color;

import java.util.Objects;

public final class Color {
    private final int r;
    private final int g;
    private final int b;

    private Color(int r, int g, int b) {
        this.r = Math.clamp(r, 0, 255);
        this.g = Math.clamp(g, 0, 255);
        this.b = Math.clamp(b, 0, 255);
    }

    public static Color of(int r, int g, int b) {
        return new Color(r, g, b);
    }

    public static Color ofHex(String hex) {
        if (hex == null) throw new IllegalArgumentException("Hex string cannot be null");
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        if (clean.length() != 6) {
            throw new IllegalArgumentException("Invalid hex color format: " + hex);
        }
        int rgb = Integer.parseInt(clean, 16);
        return of((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    public int r() { return r; }
    public int g() { return g; }
    public int b() { return b; }

    public int getRed() { return r; }
    public int getGreen() { return g; }
    public int getBlue() { return b; }

    public float redFloat() { return r / 255.0f; }
    public float greenFloat() { return g / 255.0f; }
    public float blueFloat() { return b / 255.0f; }

    public int rgb() {
        return (r << 16) | (g << 8) | b;
    }

    public int argb() {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Color color)) return false;
        return r == color.r && g == color.g && b == color.b;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, g, b);
    }

    @Override
    public String toString() {
        return String.format("Color[r=%d, g=%d, b=%d]", r, g, b);
    }
}
