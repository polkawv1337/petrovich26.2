package client_files.render.render.animation;

import java.util.ArrayList;
import java.util.List;

public class AnimationGroup extends Animation {
    private final List<Animation> animations = new ArrayList<>();
    private AnimationState state = AnimationState.PENDING;

    public AnimationGroup(List<Animation> animations) {
        if (animations != null) {
            this.animations.addAll(animations);
        }
    }

    public void add(Animation animation) {
        if (animation != null) {
            animations.add(animation);
        }
    }

    @Override
    public void tick(float deltaSeconds) {
        if (state == AnimationState.FINISHED || state == AnimationState.PAUSED) return;

        if (state == AnimationState.PENDING) {
            state = AnimationState.RUNNING;
            notifyStart();
        }

        boolean allFinished = true;
        for (int i = 0; i < animations.size(); i++) {
            Animation anim = animations.get(i);
            if (!anim.isFinished()) {
                anim.tick(deltaSeconds);
                if (!anim.isFinished()) {
                    allFinished = false;
                }
            }
        }

        if (allFinished) {
            state = AnimationState.FINISHED;
            notifyFinish();
        }
    }

    @Override
    public AnimationState state() {
        return state;
    }

    @Override
    public void pause() {
        state = AnimationState.PAUSED;
        animations.forEach(Animation::pause);
    }

    @Override
    public void resume() {
        state = AnimationState.RUNNING;
        animations.forEach(Animation::resume);
    }

    @Override
    public void cancel() {
        state = AnimationState.FINISHED;
        animations.forEach(Animation::cancel);
        notifyFinish();
    }
}
