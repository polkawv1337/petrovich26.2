package client_files.render.render.animation;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface AnimatableProperty {
    void apply(float value);
    float get();

    static AnimatableProperty of(Supplier<Float> getter, Consumer<Float> setter) {
        return new AnimatableProperty() {
            @Override
            public void apply(float value) {
                if (setter != null) setter.accept(value);
            }

            @Override
            public float get() {
                return getter != null ? getter.get() : 0f;
            }
        };
    }
}
