package client_files.render.render.animation.twod;

import client_files.render.render.animation.Animation;
import client_files.render.render.baseshape.twod.Shape2D;

import java.util.Objects;

public abstract class Animation2D extends Animation {
    protected final Shape2D target;

    public Animation2D(Shape2D target) {
        this.target = Objects.requireNonNull(target, "target shape cannot be null");
    }

    public Shape2D getTarget() {
        return target;
    }
}
