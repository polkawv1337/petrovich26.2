package client_files.render.render.event.impl;

import client_files.render.render.event.Event;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Render2DEvent extends Event {
    private final GuiGraphicsExtractor guiGraphicsExtractor;
    private final DeltaTracker deltaTracker;

    public Render2DEvent(Minecraft client, GuiGraphicsExtractor guiGraphicsExtractor, DeltaTracker deltaTracker) {
        super(client);
        this.guiGraphicsExtractor = guiGraphicsExtractor;
        this.deltaTracker = deltaTracker;
    }

    public GuiGraphicsExtractor getGuiGraphicsExtractor() {
        return guiGraphicsExtractor;
    }

    public DeltaTracker getDeltaTracker() {
        return deltaTracker;
    }
}
