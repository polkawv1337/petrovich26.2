package client_files.render.render.animation.twod;

import client_files.render.render.animation.Animation;
import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationGroup;
import client_files.render.render.animation.EasingType;
import client_files.render.render.baseshape.twod.Rect;
import client_files.render.render.baseshape.twod.Shape2D;
import client_files.render.render.core.color.ColorRGBA;

import java.util.List;

public final class GuiTransition {
    private GuiTransition() {}

    public static Animation fadeIn(Shape2D target, float durationSeconds) {
        ColorRGBA base = target.getColor();
        target.setColor(base.withAlpha(0));
        AnimationCurve curve = AnimationCurve.of(EasingType.EASE_OUT, durationSeconds);

        return new Animation() {
            private float elapsed = 0f;
            private client_files.render.render.animation.AnimationState state = client_files.render.render.animation.AnimationState.PENDING;

            @Override
            public void tick(float deltaSeconds) {
                if (state == client_files.render.render.animation.AnimationState.FINISHED || state == client_files.render.render.animation.AnimationState.PAUSED) return;
                if (state == client_files.render.render.animation.AnimationState.PENDING) {
                    state = client_files.render.render.animation.AnimationState.RUNNING;
                    notifyStart();
                }
                elapsed += deltaSeconds;
                float progress = Math.clamp(elapsed / durationSeconds, 0f, 1f);
                float eased = curve.getInterpolator().apply(progress);
                target.setColor(base.withAlpha(Math.round(base.a() * eased)));
                notifyUpdate(progress);

                if (elapsed >= durationSeconds) {
                    target.setColor(base);
                    state = client_files.render.render.animation.AnimationState.FINISHED;
                    notifyFinish();
                }
            }

            @Override public client_files.render.render.animation.AnimationState state() { return state; }
            @Override public void pause() { state = client_files.render.render.animation.AnimationState.PAUSED; }
            @Override public void resume() { state = client_files.render.render.animation.AnimationState.RUNNING; }
            @Override public void cancel() { state = client_files.render.render.animation.AnimationState.FINISHED; notifyFinish(); }
        };
    }

    public static Animation fadeOut(Shape2D target, float durationSeconds) {
        ColorRGBA base = target.getColor();
        AnimationCurve curve = AnimationCurve.of(EasingType.EASE_IN, durationSeconds);

        return new Animation() {
            private float elapsed = 0f;
            private client_files.render.render.animation.AnimationState state = client_files.render.render.animation.AnimationState.PENDING;

            @Override
            public void tick(float deltaSeconds) {
                if (state == client_files.render.render.animation.AnimationState.FINISHED || state == client_files.render.render.animation.AnimationState.PAUSED) return;
                if (state == client_files.render.render.animation.AnimationState.PENDING) {
                    state = client_files.render.render.animation.AnimationState.RUNNING;
                    notifyStart();
                }
                elapsed += deltaSeconds;
                float progress = Math.clamp(elapsed / durationSeconds, 0f, 1f);
                float eased = curve.getInterpolator().apply(progress);
                target.setColor(base.withAlpha(Math.round(base.a() * (1f - eased))));
                notifyUpdate(progress);

                if (elapsed >= durationSeconds) {
                    target.setColor(base.withAlpha(0));
                    state = client_files.render.render.animation.AnimationState.FINISHED;
                    notifyFinish();
                }
            }

            @Override public client_files.render.render.animation.AnimationState state() { return state; }
            @Override public void pause() { state = client_files.render.render.animation.AnimationState.PAUSED; }
            @Override public void resume() { state = client_files.render.render.animation.AnimationState.RUNNING; }
            @Override public void cancel() { state = client_files.render.render.animation.AnimationState.FINISHED; notifyFinish(); }
        };
    }

    public static Animation slideIn(Shape2D target, float fromX, float fromY, float durationSeconds) {
        float toX = target.getX();
        float toY = target.getY();
        target.setX(fromX);
        target.setY(fromY);
        return new TransformAnimation2D(target, toX, toY, target.getRotationDegrees(), AnimationCurve.of(EasingType.EASE_OUT_CUBIC, durationSeconds));
    }

    public static Animation scaleIn(Shape2D target, float durationSeconds) {
        if (!(target instanceof Rect rect)) {
            return fadeIn(target, durationSeconds);
        }
        float targetW = rect.getWidth();
        float targetH = rect.getHeight();
        rect.setWidth(0f);
        rect.setHeight(0f);

        TransformAnimation2D scaleAnim = new TransformAnimation2D(rect, rect.getX(), rect.getY(), rect.getRotationDegrees(), targetW, targetH, AnimationCurve.of(EasingType.BACK, durationSeconds));
        Animation fadeAnim = fadeIn(rect, durationSeconds * 0.5f);
        return new AnimationGroup(List.of(scaleAnim, fadeAnim));
    }
}
