package client_files.render.render.animation;

@FunctionalInterface
public interface Interpolator {
    float apply(float progress);
}
