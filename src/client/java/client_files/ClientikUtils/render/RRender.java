package client_files.ClientikUtils.render;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.Theme;
import client_files.render.render.assets.img.TextureAsset;
import client_files.render.render.assets.img.TextureRegistry;
import client_files.render.render.core.SmoothRender;
import client_files.render.render.core.color.ColorRGBA;
import client_files.render.render.core.context.RenderContext;
import client_files.render.render.core.font.FontManager;
import client_files.render.render.core.font.msdf.MsdfFontRenderer;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

/**
 * Тонкий фасад над движком рендера client_files.render.
 * Вся сглаживание/шрифты/закругления делает движок (SmoothRender + MsdfFontRenderer).
 */
public final class RRender {

    public static final int TEXT = 0xFFFFFFFF;
    public static final int TEXT_DIM = 0xFFCCCCCC;
    public static final int TEXT_FAINT = 0xFF999999;
    public static final int BG = 0xE81A1A1A;
    public static final int BG_DEEP = 0xCC101010;
    public static final int LINE = 0xFF2F2F2F;
    public static final int HOVER = 0x33FFFFFF;
    public static final int CYAN = 0xFF4DFFFF;
    public static final int MINT = 0xFF4DFF99;
    public static final int LAVENDER = 0xFFA64DFF;
    public static final int PEACH = 0xFFFFA64D;
    public static final int LEMON = 0xFFFFFF4D;
    public static final int PINK = 0xFFFF4DA6;
    public static final int RED_SOFT = 0xFFFF4D4D;

    public static final Identifier SLOT = Identifier.withDefaultNamespace("container/slot");

    private static final String PANEL_SOURCE = "textures/gui/sprites/widget/panel.png";
    private static final String PANEL_LIGHT_SOURCE = "textures/gui/sprites/widget/panel_light.png";
    private static final int SPRITE_SIZE = 48;
    private static final int SPRITE_BORDER = 16;

    private static final Map<String, Identifier> bakedTextures = new HashMap<>();

    private static DynamicTexture hsTexture;
    private static Identifier hsTextureId;
    private static int hsLastHue = -1;
    private static final int HS_SIZE = 64;

    private RRender() {
    }

    private static ColorRGBA c(int argb) {
        return ColorRGBA.of((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >>> 24) & 0xFF);
    }

    public static int accent() {
        return Theme.accent(Category.COMBAT);
    }

    public static int accent(Category category) {
        return Theme.accent(category);
    }

    // ------------------------------------------------------------------ panels

    public static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        drawNineSlice(g, baked(PANEL_SOURCE), x, y, w, h);
    }

    public static void panelLight(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        drawNineSlice(g, baked(PANEL_LIGHT_SOURCE), x, y, w, h);
    }

    private static Identifier baked(String source) {
        Identifier cached = bakedTextures.get(source);
        if (cached != null) return cached;
        Minecraft mc = Minecraft.getInstance();
        Identifier id = Identifier.fromNamespaceAndPath("petrovich_26_2", source);
        Identifier texId = Identifier.fromNamespaceAndPath("petrovich_26_2", "textures/baked/" + source);
        TextureManager manager = mc.getTextureManager();
        try (InputStream in = mc.getResourceManager().open(id)) {
            manager.release(texId);
            manager.register(texId, new DynamicTexture(() -> source, NativeImage.read(in)));
            bakedTextures.put(source, texId);
            return texId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load panel texture " + source, e);
        }
    }

    private static void drawNineSlice(GuiGraphicsExtractor g, Identifier tex, int x, int y, int w, int h) {
        if (w < SPRITE_BORDER * 2 || h < SPRITE_BORDER * 2) {
            g.blit(tex, x, y, x + w, y + h, 0.0f, 1.0f, 0.0f, 1.0f);
            return;
        }
        int b = SPRITE_BORDER;
        int cSize = SPRITE_SIZE - b;
        int midW = w - b * 2;
        int midH = h - b * 2;
        float inv = 1.0f / SPRITE_SIZE;

        int[][] rows = {{0, b}, {b, cSize}, {cSize, SPRITE_SIZE}};
        int[][] cols = {{0, b}, {b, cSize}, {cSize, SPRITE_SIZE}};

        for (int[] row : rows) {
            int sy0 = row[0];
            int sy1 = row[1];
            int dy = sy0 == 0 ? y : (sy1 == cSize ? y + b : y + b + midH);
            int dh = sy1 == b ? b : (sy1 == cSize ? midH : b);
            for (int[] col : cols) {
                int sx0 = col[0];
                int sx1 = col[1];
                int dx = sx0 == 0 ? x : (sx1 == cSize ? x + b : x + b + midW);
                int dw = sx1 == b ? b : (sx1 == cSize ? midW : b);
                if (dw <= 0 || dh <= 0) continue;
                g.blit(tex, dx, dy, dx + dw, dy + dh, sx0 * inv, sx1 * inv, sy0 * inv, sy1 * inv);
            }
        }
    }

    // ------------------------------------------------------------------ rects

    public static void fill(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        g.fill(x, y, x + w, y + h, color);
    }

    public static void fillGradient(GuiGraphicsExtractor g, int x, int y, int w, int h, int topColor, int bottomColor) {
        if (w <= 0 || h <= 0) return;
        g.fillGradient(x, y, x + w, y + h, topColor, bottomColor);
    }

    public static void bar(GuiGraphicsExtractor g, int x, int y, int w, int h, float ratio, int color) {
        int r = (int) (Math.max(0f, Math.min(1f, ratio)) * w);
        if (r > 0) fill(g, x, y, r, h, color);
    }

    /** Прогресс-бар со скруглёнными концами. */
    public static void barRounded(GuiGraphicsExtractor g, int x, int y, int w, int h, float ratio, int color) {
        if (w <= 0 || h <= 0) return;
        float f = Math.max(0f, Math.min(1f, ratio));
        int filled = Math.round(f * w);
        if (filled <= 0) return;
        if (filled >= w) {
            rounded(g, x, y, w, h, h / 2, color);
            return;
        }
        rounded(g, x, y, filled, h, Math.min(h / 2, filled / 2), color);
    }

    // ------------------------------------------------------------------ shapes (движок)

    public static void rounded(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int color) {
        if (g == null || w <= 0 || h <= 0 || radius <= 0) {
            fill(g, x, y, w, h, color);
            return;
        }
        SmoothRender.get().drawRoundedRect(g, x, y, w, h, radius, c(color));
    }

    public static void roundedCorners(GuiGraphicsExtractor g, int x, int y, int w, int h,
                                      int rTL, int rTR, int rBR, int rBL, int color) {
        if (g == null || w <= 0 || h <= 0) {
            fill(g, x, y, w, h, color);
            return;
        }
        SmoothRender.get().drawRoundedRect(g, x, y, w, h, rTL, rTR, rBR, rBL, c(color));
    }

    /** Вертикальный градиент со скруглением: шапка, градиент, подвал. */
    public static void roundedGradient(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius,
                                       int topColor, int bottomColor) {
        if (g == null || w <= 0 || h <= 0) {
            fillGradient(g, x, y, w, h, topColor, bottomColor);
            return;
        }
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        if (r <= 0 || h <= r * 2) {
            rounded(g, x, y, w, h, r, mix(topColor, bottomColor, 0.5f));
            return;
        }
        roundedCorners(g, x, y, w, h, r, r, r, r, topColor);
        fillGradient(g, x + 1, y + r, w - 2, h - r * 2, topColor, bottomColor);
        fill(g, x + r, y + h - r, w - r * 2, r, bottomColor);
    }

    public static void pill(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        rounded(g, x, y, w, h, h / 2, color);
    }

    public static void accentStrip(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        pill(g, x, y, w, h, color);
    }

    public static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int thickness, int color) {
        if (g == null || w <= 0 || h <= 0 || thickness <= 0) return;
        int t = Math.max(1, Math.min(thickness, Math.min(w, h) / 2));
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        roundedCorners(g, x, y, w, h, r, r, r, r, color);
        if (t * 2 < Math.min(w, h)) {
            roundedCorners(g, x + t, y + t, w - t * 2, h - t * 2,
                    Math.max(0, r - t), Math.max(0, r - t), Math.max(0, r - t), Math.max(0, r - t), 0x00000000);
        }
    }

    public static void circle(GuiGraphicsExtractor g, int centerX, int centerY, int diameter, int color) {
        if (g == null || diameter <= 0) return;
        SmoothRender.get().drawCircle(g, centerX, centerY, diameter / 2f, c(color));
    }

    public static void ring(GuiGraphicsExtractor g, int centerX, int centerY, int diameter, int thickness, int color) {
        if (g == null || diameter <= 0) return;
        float outer = Math.max(1, diameter) / 2f;
        float inner = Math.max(0f, outer - Math.max(1, thickness));
        SmoothRender.get().drawRing(g, centerX, centerY, outer, inner, c(color), c(withAlpha(color, 0)));
    }

    public static void triangle(GuiGraphicsExtractor g, float cx, float cy, float angleDegrees, float size, int color) {
        if (g == null || size <= 0) return;
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().rotate((float) Math.toRadians(angleDegrees));
        int half = Math.max(1, Math.round(size * 0.5f));
        float baseHalf = size * 0.5f;
        for (int row = -half; row <= half; row++) {
            float t = (row + half) / (float) (2 * half);
            int w = Math.max(1, Math.round(baseHalf * t));
            fill(g, -w, row, 2 * w, 1, color);
        }
        g.pose().popMatrix();
    }

    public static void separator(GuiGraphicsExtractor g, int x, int y, int w) {
        fill(g, x, y, w, 1, 0x1AFFFFFF);
    }

    public static void line(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color) {
        if (g == null) return;
        int dx = Math.abs(x2 - x1);
        int dy = -Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx + dy;
        int guard = dx - dy + 4;
        while (guard-- > 0) {
            fill(g, x1, y1, 1, 1, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = err * 2;
            if (e2 >= dy) {
                err += dy;
                x1 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y1 += sy;
            }
        }
    }

    // ------------------------------------------------------------------ colors

    public static int withAlpha(int color, int alpha) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0xFFFFFF);
    }

    public static int alpha(int color) {
        return (color >>> 24) & 0xFF;
    }

    public static int mix(int a, int b, float t) {
        float f = Math.max(0f, Math.min(1f, t));
        int aa = (a >>> 24) & 0xFF;
        int ba = (b >>> 24) & 0xFF;
        int r = Math.round(((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * f);
        int g = Math.round(((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * f);
        int bl = Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * f);
        int al = Math.round(aa + (ba - aa) * f);
        return (al << 24) | (r << 16) | (g << 8) | bl;
    }

    public static int fade(int color, float factor) {
        return withAlpha(color, Math.round(alpha(color) * Math.max(0f, Math.min(1f, factor))));
    }

    // ------------------------------------------------------------------ textures

    private static final int PICTURE_MAX_SIZE = 256;
    private static final Map<String, Identifier> pictureTextures = new HashMap<>();

    public static Identifier picture(String source, int color) {
        int qa = ((color >>> 24) & 0xFF) >= 250 ? 255 : (((color >>> 24) & 0xFF) & 0xF0);
        int rgb = color & 0x00FFFFFF;
        String key = source + "#" + Integer.toHexString((qa << 24) | rgb);
        Identifier cached = pictureTextures.get(key);
        if (cached != null) return cached;

        Minecraft mc = Minecraft.getInstance();
        Identifier id = Identifier.fromNamespaceAndPath("petrovich_26_2", "pictures/" + source);
        Identifier texId = Identifier.fromNamespaceAndPath("petrovich_26_2",
                "textures/pictures/" + source + "_" + Integer.toHexString(key.hashCode()));
        try (InputStream in = mc.getResourceManager().open(id)) {
            NativeImage src = NativeImage.read(in);
            int sw = src.getWidth();
            int sh = src.getHeight();
            float scale = Math.min(1f, (float) PICTURE_MAX_SIZE / Math.max(sw, sh));
            int ow = Math.max(1, Math.round(sw * scale));
            int oh = Math.max(1, Math.round(sh * scale));
            NativeImage out = new NativeImage(ow, oh, true);
            int tr = (rgb >> 16) & 0xFF;
            int tg = (rgb >> 8) & 0xFF;
            int tb = rgb & 0xFF;
            for (int y = 0; y < oh; y++) {
                int sy = Math.min(sh - 1, (int) (y / scale));
                for (int x = 0; x < ow; x++) {
                    int sx = Math.min(sw - 1, (int) (x / scale));
                    int p = src.getPixel(sx, sy);
                    int a = (p >>> 24) & 0xFF;
                    int b = (p >>> 16) & 0xFF;
                    int gch = (p >> 8) & 0xFF;
                    int rch = p & 0xFF;
                    rch = rch * tr / 255;
                    gch = gch * tg / 255;
                    b = b * tb / 255;
                    a = a * qa / 255;
                    out.setPixelABGR(x, y, (a << 24) | (b << 16) | (gch << 8) | rch);
                }
            }
            TextureManager manager = mc.getTextureManager();
            manager.register(texId, new DynamicTexture(() -> key, out));
            pictureTextures.put(key, texId);
            return texId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load picture texture " + source, e);
        }
    }

    public static void picture(GuiGraphicsExtractor g, String source, float cx, float cy, float width, float height,
                               float angleDegrees, int color) {
        Identifier tex = picture(source, color);
        int w = Math.max(1, Math.round(width));
        int h = Math.max(1, Math.round(height));
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().rotate((float) Math.toRadians(angleDegrees));
        g.blit(tex, -w / 2, -h / 2, w, h, 0.0f, 1.0f, 0.0f, 1.0f);
        g.pose().popMatrix();
    }

    // ------------------------------------------------------------------ text (движок MSDF)

    public static void text(GuiGraphicsExtractor g, String text, float x, float y, float size, int color) {
        if (g == null || text == null || text.isEmpty()) return;
        MsdfFontRenderer.get().drawString(g, FontManager.customFontFamily(), text, x, y, size, c(color), false);
    }

    public static void textCenter(GuiGraphicsExtractor g, String text, float centerX, float y, float size, int color) {
        if (g == null || text == null || text.isEmpty()) return;
        float w = textWidth(text, size);
        MsdfFontRenderer.get().drawString(g, FontManager.customFontFamily(), text, centerX - w / 2f, y, size, c(color), false);
    }

    public static void textRight(GuiGraphicsExtractor g, String text, float rightX, float y, float size, int color) {
        if (g == null || text == null || text.isEmpty()) return;
        float w = textWidth(text, size);
        MsdfFontRenderer.get().drawString(g, FontManager.customFontFamily(), text, rightX - w, y, size, c(color), false);
    }

    public static float textWidth(String text, float size) {
        if (text == null || text.isEmpty()) return 0f;
        return MsdfFontRenderer.get().getStringWidth(FontManager.customFontFamily(), text, size);
    }

    /** Текст конкретным начертанием MSDF (inter, roboto, poppins, montserrat, opensans, jetbrains_mono, firacode). */
    public static void textFamily(GuiGraphicsExtractor g, String family, String text, float x, float y, float size, int color) {
        if (g == null || text == null || text.isEmpty()) return;
        MsdfFontRenderer.get().drawString(g, family, text, x, y, size, c(color), false);
    }

    public static void textCenterFamily(GuiGraphicsExtractor g, String family, String text,
                                        float centerX, float y, float size, int color) {
        if (g == null || text == null || text.isEmpty()) return;
        float w = textWidthFamily(family, text, size);
        MsdfFontRenderer.get().drawString(g, family, text, centerX - w / 2f, y, size, c(color), false);
    }

    public static float textWidthFamily(String family, String text, float size) {
        if (text == null || text.isEmpty()) return 0f;
        return MsdfFontRenderer.get().getStringWidth(family, text, size);
    }

    public static float lineHeight(float size) {
        return size * 1.25f;
    }

    // ------------------------------------------------------------------ misc

    public static boolean hovered(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    /** Активен ли сейчас 2D-контекст движка (для кода, который рисует через Render2D). */
    public static boolean contextActive() {
        return RenderContext.is2DActive();
    }

    public static Identifier hsGradient(int hueDeg) {
        int hue = Math.floorMod(hueDeg, 360);
        if (hsTexture != null && hsLastHue == hue) return hsTextureId;
        Minecraft mc = Minecraft.getInstance();
        if (hsTexture == null) {
            hsTexture = new DynamicTexture(() -> "petrovich_hs", HS_SIZE, HS_SIZE, true);
            hsTextureId = Identifier.fromNamespaceAndPath("petrovich_26_2", "textures/hs_gradient");
            mc.getTextureManager().register(hsTextureId, hsTexture);
        }
        NativeImage image = hsTexture.getPixels();
        for (int y = 0; y < HS_SIZE; y++) {
            float value = 1f - (float) y / (HS_SIZE - 1);
            for (int x = 0; x < HS_SIZE; x++) {
                image.setPixelABGR(x, y, hsvToArgb(hue, (float) x / (HS_SIZE - 1), value));
            }
        }
        hsTexture.upload();
        hsLastHue = hue;
        return hsTextureId;
    }

    public static int hsvToArgb(int hue, float sat, float value) {
        float h = hue % 360;
        float s = Math.max(0f, Math.min(1f, sat));
        float v = Math.max(0f, Math.min(1f, value));
        float cVal = v * s;
        float x = cVal * (1f - Math.abs(((h / 60f) % 2f) - 1f));
        float m = v - cVal;
        float r, g, b;
        if (h < 60) {
            r = cVal; g = x; b = 0;
        } else if (h < 120) {
            r = x; g = cVal; b = 0;
        } else if (h < 180) {
            r = 0; g = cVal; b = x;
        } else if (h < 240) {
            r = 0; g = x; b = cVal;
        } else if (h < 300) {
            r = x; g = 0; b = cVal;
        } else {
            r = cVal; g = 0; b = x;
        }
        return 0xFF000000
                | ((int) ((r + m) * 255f) << 16)
                | ((int) ((g + m) * 255f) << 8)
                | ((int) ((b + m) * 255f));
    }
}
