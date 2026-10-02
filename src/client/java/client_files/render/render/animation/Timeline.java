package client_files.render.render.animation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Timeline {
    private final Map<String, KeyframeTrack> tracks;
    private final float durationSeconds;
    private final boolean looping;

    public Timeline(Map<String, KeyframeTrack> tracks, float durationSeconds, boolean looping) {
        this.tracks = new HashMap<>(tracks != null ? tracks : Collections.emptyMap());
        this.durationSeconds = Math.max(0.0001f, durationSeconds);
        this.looping = looping;
    }

    public float getDurationSeconds() {
        return durationSeconds;
    }

    public boolean isLooping() {
        return looping;
    }

    public Map<String, KeyframeTrack> getTracks() {
        return Collections.unmodifiableMap(tracks);
    }

    public Map<String, Float> sampleAt(float timeSeconds) {
        float sampleTime = timeSeconds;
        if (looping && durationSeconds > 0) {
            sampleTime = timeSeconds % durationSeconds;
            if (sampleTime < 0) sampleTime += durationSeconds;
        }

        Map<String, Float> samples = new HashMap<>();
        for (Map.Entry<String, KeyframeTrack> entry : tracks.entrySet()) {
            samples.put(entry.getKey(), entry.getValue().valueAt(sampleTime));
        }
        return samples;
    }
}
