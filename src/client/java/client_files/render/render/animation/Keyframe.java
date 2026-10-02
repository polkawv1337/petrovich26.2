package client_files.render.render.animation;

import java.util.Objects;

public record Keyframe(float timeSeconds, float value, EasingType easingIntoNext) {
    public Keyframe {
        if (timeSeconds < 0) timeSeconds = 0;
        if (easingIntoNext == null) easingIntoNext = EasingType.LINEAR;
    }

    public static Keyframe of(float timeSeconds, float value) {
        return new Keyframe(timeSeconds, value, EasingType.LINEAR);
    }

    public static Keyframe of(float timeSeconds, float value, EasingType easing) {
        return new Keyframe(timeSeconds, value, easing);
    }
}
