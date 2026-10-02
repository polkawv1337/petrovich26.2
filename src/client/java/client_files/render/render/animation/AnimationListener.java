package client_files.render.render.animation;

public interface AnimationListener {
    default void onStart(Animation anim) {}
    default void onUpdate(Animation anim, float progress) {}
    default void onFinish(Animation anim) {}
}
