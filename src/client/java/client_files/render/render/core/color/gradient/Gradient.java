package client_files.render.render.core.color.gradient;

import client_files.render.render.core.color.ColorRGBA;
import client_files.render.render.core.color.ColorUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class Gradient {
    protected final List<ColorStop> stops;

    public Gradient(List<ColorStop> stops) {
        if (stops == null || stops.isEmpty()) {
            throw new IllegalArgumentException("Gradient stops cannot be null or empty");
        }
        List<ColorStop> sorted = new ArrayList<>(stops);
        sorted.sort(Comparator.comparingDouble(ColorStop::position));
        this.stops = List.copyOf(sorted);
    }

    public List<ColorStop> getStops() {
        return stops;
    }

    public abstract ColorRGBA colorAt(float t);

    public ColorRGBA interpolateBetweenStops(float t) {
        float clampedT = Math.clamp(t, 0f, 1f);
        if (clampedT <= stops.getFirst().position()) {
            return stops.getFirst().color();
        }
        if (clampedT >= stops.getLast().position()) {
            return stops.getLast().color();
        }

        for (int i = 0; i < stops.size() - 1; i++) {
            ColorStop start = stops.get(i);
            ColorStop end = stops.get(i + 1);
            if (clampedT >= start.position() && clampedT <= end.position()) {
                float segmentLength = end.position() - start.position();
                float localT = segmentLength > 0.00001f ? (clampedT - start.position()) / segmentLength : 0f;
                return ColorUtils.lerp(start.color(), end.color(), localT);
            }
        }

        return stops.getLast().color();
    }
}
