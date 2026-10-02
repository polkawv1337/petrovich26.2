package client_files.render.render.event.impl;

import client_files.render.render.event.Event;
import client_files.render.render.event.util.EventPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class WorldRenderEvent extends Event {
    private final CameraRenderState cameraRenderState;
    private final EventPhase phase;

    public WorldRenderEvent(Minecraft client, CameraRenderState cameraRenderState, EventPhase phase) {
        super(client);
        this.cameraRenderState = cameraRenderState;
        this.phase = phase;
    }

    public CameraRenderState getCameraRenderState() {
        return cameraRenderState;
    }

    public EventPhase getPhase() {
        return phase;
    }
}
