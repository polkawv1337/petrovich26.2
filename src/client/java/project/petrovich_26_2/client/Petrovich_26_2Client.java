package project.petrovich_26_2.client;

import client_files.ModuleManager;
import client_files.Petrovich.Misc.ViaFabricVersionBox;
import client_files.render.Render;
import net.fabricmc.api.ClientModInitializer;

public class Petrovich_26_2Client implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // движок рендера поднимаем первым - от него зависят HUD и меню
        Render.init();
        ModuleManager.getInstance();
        ViaFabricVersionBox.register();
    }
}
