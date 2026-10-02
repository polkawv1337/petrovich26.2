package client_files.render.api;

import client_files.render.render.animation.Animation;
import client_files.render.render.animation.AnimationCurve;
import client_files.render.render.animation.AnimationEngine;
import client_files.render.render.animation.AnimationHandle;
import client_files.render.render.animation.Timeline;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class AnimationApi {
    AnimationApi() {}

    public AnimationHandle animate(Supplier<Float> getter, Consumer<Float> setter, float from, float to, AnimationCurve curve) {
        return AnimationEngine.get().animate(getter, setter, from, to, curve);
    }

    public AnimationHandle animate(Timeline timeline, Map<String, Consumer<Float>> propertySetters) {
        return AnimationEngine.get().animate(timeline, propertySetters);
    }

    public AnimationHandle register(Animation animation) {
        return AnimationEngine.get().register(animation);
    }
}
