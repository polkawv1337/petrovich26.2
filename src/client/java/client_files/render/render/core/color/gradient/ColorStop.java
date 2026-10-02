package client_files.render.render.core.color.gradient;

import client_files.render.render.core.color.ColorRGBA;

import java.util.Objects;

public record ColorStop(float position, ColorRGBA color) {
    public ColorStop {
        position = Math.clamp(position, 0f, 1f);
        Objects.requireNonNull(color, "color cannot be null");
    }

    public static ColorStop of(float position, ColorRGBA color) {
        return new ColorStop(position, color);
    }
}
