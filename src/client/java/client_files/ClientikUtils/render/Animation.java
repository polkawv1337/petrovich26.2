package client_files.ClientikUtils.render;

public final class Animation {

    private float value;
    private float target;
    private float speed;
    private long lastMs;

    public Animation(float initial, float speed) {
        this.value = initial;
        this.target = initial;
        this.speed = speed;
        this.lastMs = System.currentTimeMillis();
    }

    public void update() {
        long now = System.currentTimeMillis();
        float delta = Math.max(1, now - lastMs);
        lastMs = now;
        if (value != target) {
            float factor = 1.0f - (float) Math.exp(-delta / 1000.0f * speed);
            value += (target - value) * factor;
            if (Math.abs(target - value) < 0.001f) {
                value = target;
            }
        }
    }

    public void settleTo(float target) {
        this.target = target;
    }

    public float getValue() {
        return value;
    }

    public float getTarget() {
        return target;
    }

    public void snapTo(float value) {
        this.value = value;
        this.target = value;
    }
}