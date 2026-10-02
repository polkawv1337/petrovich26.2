package client_files.render.render.core;

import client_files.render.render.core.color.ColorRGBA;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Аналитический сглаженный рендер примитивов.
 *
 * На каждую строку примитива точно вычисляется интервал покрытия и рисуется
 * один сплошной спан + не более двух полупрозрачных AA-пикселей по краям.
 * Никаких текстур и билиннинга, поэтому у кругов и скруглений не остаётся
 * "ползущих" пикселей ни при каком масштабе и радиусе.
 */
public final class SmoothRender {
    private static final SmoothRender INSTANCE = new SmoothRender();

    private SmoothRender() {}

    public static SmoothRender get() {
        return INSTANCE;
    }

    public void drawCircle(GuiGraphicsExtractor extractor, float cx, float cy, float radius, ColorRGBA color) {
        if (extractor == null || color == null || radius <= 0.01f) return;
        disc(extractor, cx, cy, radius, 0f, color);
    }

    /** Кольцо: заливка между внешним и внутренним радиусом. */
    public void drawRing(GuiGraphicsExtractor extractor, float cx, float cy,
                         float outerRadius, float innerRadius, ColorRGBA outerColor, ColorRGBA innerColor) {
        if (extractor == null || outerRadius <= 0.01f) return;
        disc(extractor, cx, cy, outerRadius, Math.max(0f, innerRadius), outerColor);
    }

    /**
     * Заливка диска с необязательной круглой дыркой.
     * Для каждой строки определяется внешний интервал; если строка проходит
     * через дырку - берутся оба оставшихся участка (левый и правый).
     */
    private void disc(GuiGraphicsExtractor extractor, float cx, float cy,
                      float outerRadius, float innerRadius, ColorRGBA color) {
        int col = color.packed();
        int baseAlpha = (col >>> 24) & 0xFF;
        if (baseAlpha <= 0) return;

        float ro = outerRadius + 0.5f;
        float ri = Math.max(0f, innerRadius - 0.5f);

        int minY = (int) Math.ceil(cy - ro);
        int maxY = (int) Math.floor(cy + ro);

        for (int py = minY; py <= maxY; py++) {
            float dy = (py + 0.5f) - cy;

            float ho2 = ro * ro - dy * dy;
            if (ho2 <= 0f) continue;
            float ho = (float) Math.sqrt(ho2);
            float outerLo = cx - ho;
            float outerHi = cx + ho;

            float hi2 = ri * ri - dy * dy;
            if (hi2 <= 0f) {
                // строка вне дырки - сплошной диск
                row(extractor, outerLo, outerHi, py, col, baseAlpha);
                continue;
            }

            float hole = (float) Math.sqrt(hi2);
            row(extractor, outerLo, cx - hole, py, col, baseAlpha);
            row(extractor, cx + hole, outerHi, py, col, baseAlpha);
        }
    }

    /**
     * Строка [lo, hi] со сглаженными краями. Левая граница всегда сглаживается,
     * правая - если строка не упирается в дырку (вызывающий код рисует
     * соседний участок с другой стороны).
     */
    private void row(GuiGraphicsExtractor extractor, float lo, float hi, int py, int col, int baseAlpha) {
        if (hi - lo <= 0.002f) return;

        int x0 = (int) Math.floor(lo);
        int x1 = (int) Math.floor(hi);

        aaPixel(extractor, x0, py, Math.min(1f, x0 + 1f - lo), col, baseAlpha);
        if (x1 > x0 + 1) {
            extractor.fill(x0 + 1, py, x1, py + 1, col);
        }
        aaPixel(extractor, x1, py, Math.min(1f, hi - x1), col, baseAlpha);
    }

    private void aaPixel(GuiGraphicsExtractor extractor, int x, int y, float coverage, int col, int baseAlpha) {
        if (coverage <= 0.004f) return;
        int a = Math.round(baseAlpha * Math.min(1f, coverage));
        if (a <= 0) return;
        extractor.fill(x, y, x + 1, y + 1,
                a >= baseAlpha ? col : (a << 24) | (col & 0x00FFFFFF));
    }

    public void drawRoundedRect(GuiGraphicsExtractor extractor, float x, float y, float width, float height,
                                float radius, ColorRGBA color) {
        drawRoundedRect(extractor, x, y, width, height, radius, radius, radius, radius, color);
    }

    /**
     * Закруглённый прямоугольник с независимым радиусом каждого угла.
     * Углы считаются построчно: для каждой строки точная граница дуги,
     * поэтому радиус 1-3 px тоже скругляется, а не превращается в квадрат.
     */
    public void drawRoundedRect(GuiGraphicsExtractor extractor, float x, float y, float width, float height,
                                float tl, float tr, float br, float bl, ColorRGBA color) {
        if (extractor == null || color == null || width <= 0.01f || height <= 0.01f) return;

        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = Math.round(width);
        int ih = Math.round(height);
        int col = color.packed();
        int baseAlpha = (col >>> 24) & 0xFF;
        if (baseAlpha <= 0) return;

        float maxR = Math.min(iw, ih) * 0.5f;
        int rTL = clamp(tl, maxR);
        int rTR = clamp(tr, maxR);
        int rBR = clamp(br, maxR);
        int rBL = clamp(bl, maxR);

        if (rTL <= 0 && rTR <= 0 && rBR <= 0 && rBL <= 0) {
            extractor.fill(ix, iy, ix + iw, iy + ih, col);
            return;
        }

        // Граница на строке py: inset слева и справа (0 = прямой край).
        for (int py = iy; py < iy + ih; py++) {
            float left = inset(py, iy, ih, rTL, rBL);
            float right = inset(py, iy, ih, rTR, rBR);
            if (left + right >= iw) continue;
            row(extractor, ix + left, ix + iw - right, py, col, baseAlpha);
        }
    }

    /**
     * Насколько строка py уходит внутрь прямоугольника по левой/правой стороне.
     * Внутри прямого участка inset = 0, в углу - точная граница дуги.
     */
    private float inset(int py, int iy, int ih, int rTop, int rBottom) {
        if (rTop > 0 && py < iy + rTop) {
            return arcInset(py, iy + rTop, rTop);
        }
        if (rBottom > 0 && py >= iy + ih - rBottom) {
            return arcInset(py, iy + ih - rBottom, rBottom);
        }
        return 0f;
    }

    /** Горизонтальное смещение от края до дуги радиуса r с центром на строке cy. */
    private float arcInset(int py, int cy, int r) {
        float rr = r + 0.5f;
        float dy = (py + 0.5f) - cy;
        float inside = rr * rr - dy * dy;
        if (inside <= 0f) return r;
        float half = (float) Math.sqrt(inside);
        return Math.max(0f, r - half);
    }

    private int clamp(float value, float maxR) {
        if (value <= 0f) return 0;
        return (int) Math.min(Math.round(value), Math.round(maxR));
    }
}