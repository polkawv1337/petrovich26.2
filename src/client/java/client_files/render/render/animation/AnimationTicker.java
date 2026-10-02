package client_files.render.render.animation;

public final class AnimationTicker {
    private long lastNanoTime;

    public AnimationTicker() {
        this.lastNanoTime = System.nanoTime();
    }

    public float consumeDeltaSeconds() {
        long now = System.nanoTime();
        long elapsedNanos = now - lastNanoTime;
        lastNanoTime = now;

        float dt = elapsedNanos / 1_000_000_000.0f;
        return Math.clamp(dt, 0f, 0.25f);
    }
}
