package client_files.render.render.animation.threed;

import client_files.render.render.animation.Animation;
import client_files.render.render.animation.AnimationState;
import client_files.render.render.animation.Timeline;
import org.joml.Quaternionf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BoneAnimation extends Animation {
    private final Skeleton skeleton;
    private final Map<String, Timeline> timelines;

    private float elapsed = 0f;
    private AnimationState state = AnimationState.PENDING;

    public BoneAnimation(Skeleton skeleton, Map<String, Timeline> timelines) {
        this.skeleton = Objects.requireNonNull(skeleton, "skeleton cannot be null");
        this.timelines = new HashMap<>(timelines != null ? timelines : Collections.emptyMap());
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    @Override
    public void tick(float deltaSeconds) {
        if (state == AnimationState.FINISHED || state == AnimationState.PAUSED) return;
        if (state == AnimationState.PENDING) {
            state = AnimationState.RUNNING;
            notifyStart();
        }

        elapsed += deltaSeconds;

        for (Map.Entry<String, Timeline> entry : timelines.entrySet()) {
            String boneName = entry.getKey();
            Timeline timeline = entry.getValue();
            Bone bone = skeleton.findBone(boneName);
            if (bone == null) continue;

            Map<String, Float> samples = timeline.sampleAt(elapsed);

            Float px = samples.get("posX");
            Float py = samples.get("posY");
            Float pz = samples.get("posZ");
            if (px != null || py != null || pz != null) {
                bone.getLocalPosition().set(
                        px != null ? px : bone.getLocalPosition().x,
                        py != null ? py : bone.getLocalPosition().y,
                        pz != null ? pz : bone.getLocalPosition().z
                );
            }

            Float rotX = samples.get("rotX");
            Float rotY = samples.get("rotY");
            Float rotZ = samples.get("rotZ");
            if (rotX != null || rotY != null || rotZ != null) {
                bone.getLocalRotation().identity()
                        .rotateX((float) Math.toRadians(rotX != null ? rotX : 0f))
                        .rotateY((float) Math.toRadians(rotY != null ? rotY : 0f))
                        .rotateZ((float) Math.toRadians(rotZ != null ? rotZ : 0f));
            }
        }

        notifyUpdate(elapsed);
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
