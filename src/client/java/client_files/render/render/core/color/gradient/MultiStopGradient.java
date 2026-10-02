package client_files.render.render.core.color.gradient;

import client_files.render.render.core.color.ColorRGBA;

import java.util.List;

public class MultiStopGradient extends Gradient {
    public MultiStopGradient(List<ColorStop> stops) {
        super(stops);
    }

    @Override
    public ColorRGBA colorAt(float t) {
        return interpolateBetweenStops(t);
    }
}
