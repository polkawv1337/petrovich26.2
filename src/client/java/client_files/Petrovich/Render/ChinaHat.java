package client_files.Petrovich.Render;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.Module;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.EntityTypes;

public class ChinaHat extends Module {

    private static ChinaHat instance;

    public static boolean previewMode = false;

    public final BooleanSetting showOnFriends = addSetting(new BooleanSetting("Показывать на друзьях", true));

    @SuppressWarnings("unchecked")
    public ChinaHat() {
        super("China Hat", "Китайская шляпа на голове", Category.RENDER);
        instance = this;

        LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType == EntityTypes.PLAYER) {
                registrationHelper.register(new ChinaHatLayer((RenderLayerParent<AvatarRenderState, PlayerModel>) entityRenderer));
            }
        });
    }

    public static ChinaHat getInstance() {
        return instance;
    }
}