package client_files.render.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(GuiGraphicsExtractor.class)
public abstract class MixinGuiGraphicsExtractor {

    @Unique
    private int Render$scissorDepth = 0;

    @Unique
    public int Render$getScissorDepth() {
        return this.Render$scissorDepth;
    }

    @Unique
    public void Render$pushScissor() {
        this.Render$scissorDepth++;
    }

    @Unique
    public void Render$popScissor() {
        this.Render$scissorDepth = Math.max(0, this.Render$scissorDepth - 1);
    }
}
