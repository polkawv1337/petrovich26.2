package client_files.render;


import client_files.render.api.RenderAPI;
import client_files.render.api.RenderBuilder;
import client_files.render.render.assets.AssetsLoader;
import client_files.render.render.core.font.FontManager;
import client_files.render.render.core.shader.ShaderManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Render implements ClientModInitializer {
    public static final String MOD_ID = "petrovich_26_2";
    public static final Logger LOGGER = LoggerFactory.getLogger("Render");

    private static boolean initialized;

    @Override
    public void onInitializeClient() {
        init();
    }

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        LOGGER.info("[Render] Starting initialization for Minecraft 26.2...");

        ShaderManager.get().init();

        FontManager.get();

        registerReloadListener();

        RenderAPI.init(new RenderBuilder().build());

        LOGGER.info("[Render] Initialization completed successfully.");
    }

    private static void registerReloadListener() {
        try {
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                @Override
                public Identifier getFabricId() {
                    return Identifier.fromNamespaceAndPath(MOD_ID, "asset_reload");
                }

                @Override
                public void onResourceManagerReload(ResourceManager resourceManager) {
                    LOGGER.info("[Render] Reloading resource pack - purging asset caches...");
                    AssetsLoader.unloadAll();
                }
            });
        } catch (Throwable t) {
            LOGGER.debug("[Render] Could not register resource reload listener: {}", t.getMessage());
        }
    }
}
