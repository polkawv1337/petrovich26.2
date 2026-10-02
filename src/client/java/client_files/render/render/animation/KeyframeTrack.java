package client_files.render.render.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class KeyframeTrack {
    private final List<Keyframe> keyframes;

    public KeyframeTrack(List<Keyframe> keyframes) {
        if (keyframes == null || keyframes.isEmpty()) {
            throw new IllegalArgumentException("KeyframeTrack requires at least one keyframe");
        }
        List<Keyframe> sorted = new ArrayList<>(keyframes);
        sorted.sort(Comparator.comparingDouble(Keyframe::timeSeconds));
        this.keyframes = Collections.unmodifiableList(sorted);
    }

    public List<Keyframe> getKeyframes() {
        return keyframes;
    }

    public float valueAt(float timeSeconds) {
        if (timeSeconds <= keyframes.getFirst().timeSeconds()) {
            return keyframes.getFirst().value();
        }
        if (timeSeconds >= keyframes.getLast().timeSeconds()) {
            return keyframes.getLast().value();
        }

        for (int i = 0; i < keyframes.size() - 1; i++) {
            Keyframe left = keyframes.get(i);
            Keyframe right = keyframes.get(i + 1);

            if (timeSeconds >= left.timeSeconds() && timeSeconds <= right.timeSeconds()) {
                float segDuration = right.timeSeconds() - left.timeSeconds();
                float localProgress = segDuration > 0.00001f ? (timeSeconds - left.timeSeconds()) / segDuration : 0f;
                float eased = Easing.get(left.easingIntoNext()).apply(localProgress);
                return left.value() + (right.value() - left.value()) * eased;
            }
        }

        return keyframes.getLast().value();
    }
}
