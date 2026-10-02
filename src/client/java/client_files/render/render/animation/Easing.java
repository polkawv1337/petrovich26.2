package client_files.render.render.animation;

public final class Easing {
    public static final Interpolator LINEAR = get(EasingType.LINEAR);
    public static final Interpolator EASE_IN_CUBIC = get(EasingType.EASE_IN_CUBIC);
    public static final Interpolator EASE_OUT_CUBIC = get(EasingType.EASE_OUT_CUBIC);
    public static final Interpolator EASE_IN_OUT_CUBIC = get(EasingType.EASE_IN_OUT_CUBIC);
    public static final Interpolator EASE_OUT_EXPO = t -> t >= 1.0f ? 1.0f : 1.0f - (float) Math.pow(2.0, -10.0 * t);

    private Easing() {}

    public static Interpolator get(EasingType type) {
        if (type == null) return t -> t;
        return switch (type) {
            case LINEAR -> t -> t;
            case EASE_IN, EASE_IN_QUAD -> t -> t * t;
            case EASE_OUT, EASE_OUT_QUAD -> t -> 1f - (1f - t) * (1f - t);
            case EASE_IN_OUT, EASE_IN_OUT_QUAD -> t -> t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2 * t + 2, 2) / 2f;
            case EASE_IN_CUBIC -> t -> t * t * t;
            case EASE_OUT_CUBIC -> t -> 1f - (float) Math.pow(1f - t, 3);
            case EASE_IN_OUT_CUBIC -> t -> t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2 * t + 2, 3) / 2f;
            case BOUNCE -> Easing::bounceOut;
            case ELASTIC -> Easing::elasticOut;
            case BACK -> Easing::backOut;
        };
    }

    private static float bounceOut(float t) {
        float n1 = 7.5625f;
        float d1 = 2.75f;
        if (t < 1f / d1) {
            return n1 * t * t;
        } else if (t < 2f / d1) {
            float t2 = t - 1.5f / d1;
            return n1 * t2 * t2 + 0.75f;
        } else if (t < 2.5f / d1) {
            float t2 = t - 2.25f / d1;
            return n1 * t2 * t2 + 0.9375f;
        } else {
            float t2 = t - 2.625f / d1;
            return n1 * t2 * t2 + 0.984375f;
        }
    }

    private static float elasticOut(float t) {
        if (t <= 0f) return 0f;
        if (t >= 1f) return 1f;
        float c4 = (2f * (float) Math.PI) / 3f;
        return (float) (Math.pow(2, -10 * t) * Math.sin((t * 10f - 0.75f) * c4) + 1f);
    }

    private static float backOut(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float t1 = t - 1f;
        return 1f + c3 * t1 * t1 * t1 + c1 * t1 * t1;
    }
}
