package client_files.render.render.event.impl;

import client_files.render.render.event.util.Cancellable;
import client_files.render.render.event.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class CameraBobEvent extends Event implements Cancellable {
    private final CameraRenderState cameraRenderState;
    private boolean cancelled = false;

    public CameraBobEvent(CameraRenderState cameraRenderState) {
        super(Minecraft.getInstance());
        this.cameraRenderState = cameraRenderState;
    }

    public CameraRenderState getCameraRenderState() {
        return cameraRenderState;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
