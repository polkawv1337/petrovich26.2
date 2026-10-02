package client_files.render.render.core.color;

import java.util.Objects;

public final class ColorRGBA {
    public static final ColorRGBA WHITE = of(255, 255, 255, 255);
    public static final ColorRGBA BLACK = of(0, 0, 0, 255);
    public static final ColorRGBA TRANSPARENT = of(0, 0, 0, 0);

    private final int r;
    private final int g;
    private final int b;
    private final int a;

    public ColorRGBA(int r, int g, int b, int a) {
        this.r = Math.clamp(r, 0, 255);
        this.g = Math.clamp(g, 0, 255);
        this.b = Math.clamp(b, 0, 255);
        this.a = Math.clamp(a, 0, 255);
    }

    public ColorRGBA(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public ColorRGBA(float r, float g, float b, float a) {
        boolean isNormalized = (r <= 1.0f && g <= 1.0f && b <= 1.0f && a <= 1.0f);
        if (isNormalized) {
            this.r = Math.round(Math.clamp(r, 0f, 1f) * 255f);
            this.g = Math.round(Math.clamp(g, 0f, 1f) * 255f);
            this.b = Math.round(Math.clamp(b, 0f, 1f) * 255f);
            this.a = Math.round(Math.clamp(a, 0f, 1f) * 255f);
        } else {
            this.r = Math.clamp(Math.round(r), 0, 255);
            this.g = Math.clamp(Math.round(g), 0, 255);
            this.b = Math.clamp(Math.round(b), 0, 255);
            this.a = Math.clamp(Math.round(a), 0, 255);
        }
    }

    public ColorRGBA(float r, float g, float b) {
        this(r, g, b, 1.0f);
    }

    public static ColorRGBA of(int r, int g, int b, int a) {
        return new ColorRGBA(r, g, b, a);
    }

    public static ColorRGBA of(float r, float g, float b, float a) {
        return new ColorRGBA(r, g, b, a);
    }

    public static ColorRGBA fromColor(Color color, int alpha) {
        return of(color.r(), color.g(), color.b(), alpha);
    }

    public static ColorRGBA fromHSBA(float h, float s, float b, float a) {
        return ColorHSV.of(h * 360f, s, b).toRGBA(Math.round(Math.clamp(a, 0f, 1f) * 255f));
    }

    public static ColorRGBA ofHex(String hex) {
        if (hex == null) throw new IllegalArgumentException("Hex string cannot be null");
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        if (clean.length() == 6) {
            int rgb = Integer.parseInt(clean, 16);
            return of((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 255);
        } else if (clean.length() == 8) {
            long rgba = Long.parseLong(clean, 16);
            return of((int) ((rgba >> 24) & 0xFF), (int) ((rgba >> 16) & 0xFF), (int) ((rgba >> 8) & 0xFF), (int) (rgba & 0xFF));
        }
        throw new IllegalArgumentException("Invalid hex color format: " + hex);
    }

    public int r() { return r; }
    public int g() { return g; }
    public int b() { return b; }
    public int a() { return a; }

    public int getRed() { return r; }
    public int getGreen() { return g; }
    public int getBlue() { return b; }
    public int getAlpha() { return a; }

    public float redFloat() { return r / 255.0f; }
    public float greenFloat() { return g / 255.0f; }
    public float blueFloat() { return b / 255.0f; }
    public float alphaFloat() { return a / 255.0f; }

    public ColorRGBA withAlpha(int newAlpha) {
        return of(r, g, b, newAlpha);
    }

    public ColorRGBA withAlpha(float newAlpha) {
        return of(r, g, b, Math.round(Math.clamp(newAlpha, 0f, 1f) * 255f));
    }

    public int packed() {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColorRGBA that)) return false;
        return r == that.r && g == that.g && b == that.b && a == that.a;
    }

    @Override
    public int hashCode() {
        return Objects.hash(r, g, b, a);
    }

    @Override
    public String toString() {
        return String.format("ColorRGBA[r=%d, g=%d, b=%d, a=%d]", r, g, b, a);
    }
}
