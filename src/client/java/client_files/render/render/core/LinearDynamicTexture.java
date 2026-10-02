package client_files.render.render.core;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.util.function.Supplier;

public class LinearDynamicTexture extends DynamicTexture {
    public LinearDynamicTexture(Supplier<String> label, NativeImage image) {
        super(label, image);
        try {
            this.sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        } catch (Throwable ignored) {}
    }
}
