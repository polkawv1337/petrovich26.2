package client_files.render.render.animation.threed;

import client_files.render.render.animation.Animation;
import client_files.render.render.animation.AnimationState;
import client_files.render.render.baseshape.threed.Shape3D;
import org.joml.Vector3f;

import java.util.Objects;

public class OrbitAnimation extends Animation {
    private final Shape3D target;
    private final Vector3f center;
    private final float radius;
    private final float degreesPerSecond;
    private final float verticalOffset;

    private float currentAngleDegrees = 0f;
    private AnimationState state = AnimationState.PENDING;

    public OrbitAnimation(Shape3D target, Vector3f center, float radius, float degreesPerSecond, float verticalOffset) {
        this.target = Objects.requireNonNull(target, "target cannot be null");
        this.center = Objects.requireNonNull(center, "center cannot be null");
        this.radius = radius;
        this.degreesPerSecond = degreesPerSecond;
        this.verticalOffset = verticalOffset;
    }

    @Override
    public void tick(float deltaSeconds) {
        if (state == AnimationState.FINISHED || state == AnimationState.PAUSED) return;
        if (state == AnimationState.PENDING) {
            state = AnimationState.RUNNING;
            notifyStart();
        }

        currentAngleDegrees += degreesPerSecond * deltaSeconds;
        double rad = Math.toRadians(currentAngleDegrees);

        float ox = center.x + (float) (Math.cos(rad) * radius);
        float oz = center.z + (float) (Math.sin(rad) * radius);
        float oy = center.y + verticalOffset;

        target.setX(ox);
        target.setY(oy);
        target.setZ(oz);

        notifyUpdate(currentAngleDegrees);
    }

    @Override
    public AnimationState state() { return state; }
    @Override
    public void pause() { if (state == AnimationState.RUNNING) state = AnimationState.PAUSED; }
    @Override
    public void resume() { if (state == AnimationState.PAUSED) state = AnimationState.RUNNING; }
    @Override
    public void cancel() { state = AnimationState.FINISHED; notifyFinish(); }
}
