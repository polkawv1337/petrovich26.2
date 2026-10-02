package client_files.render.render.event.impl;

import client_files.render.render.event.Event;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class Render3DEvent extends Event {
    private final CameraRenderState cameraRenderState;
    private final DeltaTracker deltaTracker;

    public Render3DEvent(Minecraft client, CameraRenderState cameraRenderState, DeltaTracker deltaTracker) {
        super(client);
        this.cameraRenderState = cameraRenderState;
        this.deltaTracker = deltaTracker;
    }

    public CameraRenderState getCameraRenderState() {
        return cameraRenderState;
    }

    public DeltaTracker getDeltaTracker() {
        return deltaTracker;
    }
}
