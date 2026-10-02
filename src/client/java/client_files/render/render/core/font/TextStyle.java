package client_files.render.render.core.font;

import client_files.render.render.core.color.ColorRGBA;

public final class TextStyle {
    public static final TextStyle DEFAULT = new TextStyle(false, false, false, false, false, null);
    public static final TextStyle SHADOW = new TextStyle(false, false, false, false, true, null);

    private final boolean bold;
    private final boolean italic;
    private final boolean underline;
    private final boolean strikethrough;
    private final boolean shadow;
    private final ColorRGBA outlineColor;
    private ColorRGBA color = ColorRGBA.WHITE;
    private float size = 9.0f;

    public TextStyle(boolean bold, boolean italic, boolean underline, boolean strikethrough, boolean shadow, ColorRGBA outlineColor) {
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.shadow = shadow;
        this.outlineColor = outlineColor;
    }

    public TextStyle(ColorRGBA color, float size, boolean bold, boolean italic, boolean underline, boolean strikethrough, float shadow, ColorRGBA outlineColor) {
        this(bold, italic, underline, strikethrough, shadow > 0.0f, outlineColor);
        this.color = color != null ? color : ColorRGBA.WHITE;
        this.size = size > 0 ? size : 9.0f;
    }

    public boolean isBold() { return bold; }
    public boolean isItalic() { return italic; }
    public boolean isUnderline() { return underline; }
    public boolean isStrikethrough() { return strikethrough; }
    public boolean hasShadow() { return shadow; }
    public ColorRGBA getOutlineColor() { return outlineColor; }
    public ColorRGBA getColor() { return color; }
    public float getSize() { return size; }

    public TextStyle withBold(boolean bold) {
        return new TextStyle(bold, italic, underline, strikethrough, shadow, outlineColor);
    }

    public TextStyle withItalic(boolean italic) {
        return new TextStyle(bold, italic, underline, strikethrough, shadow, outlineColor);
    }

    public TextStyle withUnderline(boolean underline) {
        return new TextStyle(bold, italic, underline, strikethrough, shadow, outlineColor);
    }

    public TextStyle withStrikethrough(boolean strikethrough) {
        return new TextStyle(bold, italic, underline, strikethrough, shadow, outlineColor);
    }

    public TextStyle withShadow(boolean shadow) {
        return new TextStyle(bold, italic, underline, strikethrough, shadow, outlineColor);
    }

    public TextStyle withOutlineColor(ColorRGBA outlineColor) {
        return new TextStyle(bold, italic, underline, strikethrough, shadow, outlineColor);
    }
}
