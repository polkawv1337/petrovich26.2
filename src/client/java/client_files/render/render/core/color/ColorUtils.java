package client_files.render.render.core.color;

public final class ColorUtils {
    private ColorUtils() {}

    public static ColorRGBA lerp(ColorRGBA a, ColorRGBA b, float t) {
        float clampedT = Math.clamp(t, 0f, 1f);
        float r = a.r() + (b.r() - a.r()) * clampedT;
        float g = a.g() + (b.g() - a.g()) * clampedT;
        float bl = a.b() + (b.b() - a.b()) * clampedT;
        float alpha = a.a() + (b.a() - a.a()) * clampedT;
        return ColorRGBA.of(Math.round(r), Math.round(g), Math.round(bl), Math.round(alpha));
    }

    public static ColorRGBA multiplyAlpha(ColorRGBA c, float factor) {
        float clampedFactor = Math.clamp(factor, 0f, 1f);
        int newAlpha = Math.round(c.a() * clampedFactor);
        return c.withAlpha(newAlpha);
    }

    public static int packRGBA(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int[] unpackRGBA(int packed) {
        int a = (packed >> 24) & 0xFF;
        int r = (packed >> 16) & 0xFF;
        int g = (packed >> 8) & 0xFF;
        int b = packed & 0xFF;
        return new int[]{r, g, b, a};
    }
}
