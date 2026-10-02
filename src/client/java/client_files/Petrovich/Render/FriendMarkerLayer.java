package client_files.Petrovich.Render;

import client_files.Petrovich.Player.FriendHelper;
import client_files.ModuleManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class FriendMarkerLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private static final Identifier LEATHER = Identifier.withDefaultNamespace("textures/models/armor/leather_layer_1.png");
    private static final int GREEN = 0xFF00C853;

    public FriendMarkerLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float partialTick, float something) {
        FriendHelper module = ModuleManager.getInstance().get(FriendHelper.class);
        if (module == null || !module.isEnabled() || !module.highlightFriends.getValue()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Entity entity = mc.level.getEntity(state.id);
        if (!(entity instanceof Player player) || entity == mc.player) return;
        if (!FriendHelper.isFriend(player.getName().getString())) return;
        if (state.isInvisible) return;

        PlayerModel model = getParentModel();
        boolean headVisible = model.head.visible;
        boolean hatVisible = model.hat.visible;
        model.head.visible = false;
        model.hat.visible = false;
        try {
            collector.order(0).submitModel(model, state, poseStack,
                    RenderTypes.entityCutout(LEATHER),
                    light,
                    OverlayTexture.NO_OVERLAY,
                    GREEN,
                    null, state.outlineColor, null);
        } finally {
            model.head.visible = headVisible;
            model.hat.visible = hatVisible;
        }
    }
}