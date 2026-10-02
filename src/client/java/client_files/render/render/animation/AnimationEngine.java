package client_files.render.render.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AnimationEngine {
    private static final AnimationEngine INSTANCE = new AnimationEngine();

    private final AnimationTicker ticker = new AnimationTicker();
    private final List<Animation> activeAnimations = new ArrayList<>();
    private final List<Animation> pendingAnimations = new ArrayList<>();

    private AnimationEngine() {}

    public static AnimationEngine get() {
        return INSTANCE;
    }

    public synchronized AnimationHandle animate(Supplier<Float> getter, Consumer<Float> setter, float from, float to, AnimationCurve curve) {
        PropertyAnimator animator = new PropertyAnimator(AnimatableProperty.of(getter, setter), from, to, curve);
        return register(animator);
    }

    public synchronized AnimationHandle animate(Timeline timeline, Map<String, Consumer<Float>> propertySetters) {
        Animation anim = new Animation() {
            private float elapsed = 0f;
            private AnimationState state = AnimationState.PENDING;

            @Override
            public void tick(float deltaSeconds) {
                if (state == AnimationState.FINISHED || state == AnimationState.PAUSED) return;
                if (state == AnimationState.PENDING) {
                    state = AnimationState.RUNNING;
                    notifyStart();
                }

                elapsed += deltaSeconds;
                Map<String, Float> samples = timeline.sampleAt(elapsed);
                if (propertySetters != null) {
                    for (Map.Entry<String, Consumer<Float>> entry : propertySetters.entrySet()) {
                        Float val = samples.get(entry.getKey());
                        if (val != null && entry.getValue() != null) {
                            entry.getValue().accept(val);
                        }
                    }
                }

                float duration = timeline.getDurationSeconds();
                float progress = Math.clamp(elapsed / duration, 0f, 1f);
                notifyUpdate(progress);

                if (!timeline.isLooping() && elapsed >= duration) {
                    state = AnimationState.FINISHED;
                    notifyFinish();
                }
            }

            @Override
            public AnimationState state() { return state; }
            @Override
            public void pause() { if (state == AnimationState.RUNNING) state = AnimationState.PAUSED; }
            @Override
            public void resume() { if (state == AnimationState.PAUSED) state = AnimationState.RUNNING; }
            @Override
            public void cancel() { state = AnimationState.FINISHED; notifyFinish(); }
        };

        return register(anim);
    }

    public synchronized AnimationHandle register(Animation animation) {
        if (animation != null) {
            pendingAnimations.add(animation);
        }
        return new AnimationHandle(animation);
    }

    public synchronized void tick() {
        float dt = ticker.consumeDeltaSeconds();

        if (!pendingAnimations.isEmpty()) {
            activeAnimations.addAll(pendingAnimations);
            pendingAnimations.clear();
        }

        for (int i = 0; i < activeAnimations.size(); i++) {
            Animation anim = activeAnimations.get(i);
            if (!anim.isFinished()) {
                anim.tick(dt);
            }
        }

        activeAnimations.removeIf(Animation::isFinished);
    }
}
