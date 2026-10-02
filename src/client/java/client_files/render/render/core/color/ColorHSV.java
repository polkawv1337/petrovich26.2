package client_files.render.render.core.color;

import java.util.Objects;

public final class ColorHSV {
    private final float h;
    private final float s;
    private final float v;

    public ColorHSV(float h, float s, float v) {
        float normalizedH = h % 360f;
        if (normalizedH < 0) normalizedH += 360f;
        this.h = normalizedH;
        this.s = Math.clamp(s, 0f, 1f);
        this.v = Math.clamp(v, 0f, 1f);
    }

    public static ColorHSV of(float h, float s, float v) {
        return new ColorHSV(h, s, v);
    }

    public static ColorHSV fromRGB(int r, int g, int b) {
        float rf = Math.clamp(r, 0, 255) / 255.0f;
        float gf = Math.clamp(g, 0, 255) / 255.0f;
        float bf = Math.clamp(b, 0, 255) / 255.0f;

        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float delta = max - min;

        float h = 0f;
        if (delta > 0.00001f) {
            if (max == rf) {
                h = ((gf - bf) / delta) % 6f;
            } else if (max == gf) {
                h = ((bf - rf) / delta) + 2f;
            } else {
                h = ((rf - gf) / delta) + 4f;
            }
            h *= 60f;
            if (h < 0) h += 360f;
        }

        float s = (max <= 0f) ? 0f : (delta / max);
        float v = max;

        return of(h, s, v);
    }

    public float h() { return h; }
    public float s() { return s; }
    public float v() { return v; }

    public ColorRGBA toRGBA(int alpha) {
        float c = v * s;
        float x = c * (1f - Math.abs((h / 60f) % 2f - 1f));
        float m = v - c;

        float rPrime = 0f, gPrime = 0f, bPrime = 0f;
        int sector = (int) (h / 60f) % 6;
        switch (sector) {
            case 0 -> { rPrime = c; gPrime = x; bPrime = 0; }
            case 1 -> { rPrime = x; gPrime = c; bPrime = 0; }
            case 2 -> { rPrime = 0; gPrime = c; bPrime = x; }
            case 3 -> { rPrime = 0; gPrime = x; bPrime = c; }
            case 4 -> { rPrime = x; gPrime = 0; bPrime = c; }
            case 5 -> { rPrime = c; gPrime = 0; bPrime = x; }
        }

        return ColorRGBA.of(
                Math.round((rPrime + m) * 255f),
                Math.round((gPrime + m) * 255f),
                Math.round((bPrime + m) * 255f),
                alpha
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColorHSV colorHSV)) return false;
        return Float.compare(colorHSV.h, h) == 0 &&
                Float.compare(colorHSV.s, s) == 0 &&
                Float.compare(colorHSV.v, v) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(h, s, v);
    }

    @Override
    public String toString() {
        return String.format("ColorHSV[h=%.1f, s=%.2f, v=%.2f]", h, s, v);
    }
}
