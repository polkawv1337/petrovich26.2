package client_files.render.render.animation;

import java.util.Objects;

public final class AnimationCurve {
    private final Interpolator interpolator;
    private final float durationSeconds;

    public AnimationCurve(Interpolator interpolator, float durationSeconds) {
        this.interpolator = Objects.requireNonNull(interpolator, "interpolator cannot be null");
        this.durationSeconds = Math.max(0.0001f, durationSeconds);
    }

    public static AnimationCurve of(Interpolator interpolator, float durationSeconds) {
        return new AnimationCurve(interpolator, durationSeconds);
    }

    public static AnimationCurve of(EasingType type, float durationSeconds) {
        return new AnimationCurve(Easing.get(type), durationSeconds);
    }

    public static AnimationCurve linear(float durationSeconds) {
        return of(EasingType.LINEAR, durationSeconds);
    }

    public static AnimationCurve easeInOut(float durationSeconds) {
        return of(EasingType.EASE_IN_OUT, durationSeconds);
    }

    public Interpolator getInterpolator() {
        return interpolator;
    }

    public float getDurationSeconds() {
        return durationSeconds;
    }
}
