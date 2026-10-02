package client_files.render.render.helper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;

public final class TextureHelper {
    private TextureHelper() {}

    public static AbstractTexture getTexture(Identifier location) {
        if (location == null) return null;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getTextureManager() == null) return null;
        return mc.getTextureManager().getTexture(location);
    }

    public static void bindTexture(Identifier location) {
        if (location != null) {
            getTexture(location);
        }
    }
}
