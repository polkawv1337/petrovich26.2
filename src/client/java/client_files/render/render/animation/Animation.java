package client_files.render.render.animation;

import java.util.ArrayList;
import java.util.List;

public abstract class Animation {
    protected final List<AnimationListener> listeners = new ArrayList<>();

    public abstract void tick(float deltaSeconds);
    public abstract AnimationState state();
    public abstract void pause();
    public abstract void resume();
    public abstract void cancel();

    public boolean isFinished() {
        return state() == AnimationState.FINISHED;
    }

    public Animation withListener(AnimationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }

    protected void notifyStart() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onStart(this);
        }
    }

    protected void notifyUpdate(float progress) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onUpdate(this, progress);
        }
    }

    protected void notifyFinish() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onFinish(this);
        }
    }
}
