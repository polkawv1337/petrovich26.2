package client_files.render.render.core.font.msdf;

public record MsdfGlyph(
        int u,
        int v,
        int w,
        int h,
        float advance,
        float offsetX,
        float offsetY
) {}
