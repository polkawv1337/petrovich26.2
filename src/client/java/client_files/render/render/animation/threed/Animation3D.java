package client_files.render.render.animation.threed;

import client_files.render.render.animation.Animation;
import client_files.render.render.baseshape.threed.Shape3D;

public abstract class Animation3D extends Animation {
    protected final Shape3D target;

    public Animation3D(Shape3D target) {
        this.target = target;
    }

    public Shape3D getTarget() {
        return target;
    }
}
