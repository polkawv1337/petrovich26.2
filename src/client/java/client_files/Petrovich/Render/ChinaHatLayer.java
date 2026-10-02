package client_files.Petrovich.Render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

import client_files.ClientikUtils.render.RRender;
import client_files.Petrovich.Player.FriendHelper;

public class ChinaHatLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    private static final int SEGMENTS = 60;
    private static final float PI2 = (float) (Math.PI * 2);
    private static final Identifier WHITE = Identifier.withDefaultNamespace("textures/block/white_concrete.png");
    private static final int FULLBRIGHT = 0xF000F0;

    public ChinaHatLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float partialTick, float something) {
        ChinaHat module = ChinaHat.getInstance();
        Minecraft mc = Minecraft.getInstance();
        if (module == null || (!module.isEnabled() && !ChinaHat.previewMode) || mc.player == null || mc.level == null) return;

        boolean isSelf = state.id == mc.player.getId();
        boolean isFriend = false;

        Entity entity = mc.level.getEntity(state.id);
        if (entity != null && module.showOnFriends.getValue()) {
            isFriend = FriendHelper.isFriend(entity.getName().getString());
        }

        if (!ChinaHat.previewMode && !isSelf && !isFriend) return;
        if (!ChinaHat.previewMode && isSelf && mc.options.getCameraType().isFirstPerson()) return;

        poseStack.pushPose();

        this.getParentModel().head.translateAndRotate(poseStack);

        float yOffset = -0.489f;
        if (!state.heldOnHead.isEmpty()) {
            yOffset -= 0.0625f;
        }

        poseStack.translate(0.0f, yOffset, 0.0f);

        int fillColor = getThemeColor(255);

        collector.submitCustomGeometry(poseStack, RenderTypes.entitySolid(WHITE), (pose, consumer) ->
                renderClosedCone(pose, consumer, fillColor));

        collector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, consumer) ->
                renderOutline(pose, consumer, getThemeColor(255)));

        poseStack.popPose();
    }

    private void renderClosedCone(PoseStack.Pose pose, VertexConsumer consumer, int color) {
        renderConeFill(pose, consumer, color);
        renderBaseDisc(pose, consumer, color);
    }

    private void renderBaseDisc(PoseStack.Pose pose, VertexConsumer consumer, int color) {
        float width = 0.62f;

        for (int i = 0; i < SEGMENTS; i++) {
            float angle1 = i * PI2 / SEGMENTS;
            float angle2 = (i + 1) * PI2 / SEGMENTS;

            float x1 = -(float) Math.sin(angle1) * width;
            float z1 = (float) Math.cos(angle1) * width;
            float x2 = -(float) Math.sin(angle2) * width;
            float z2 = (float) Math.cos(angle2) * width;

            vertexFill(consumer, pose, 0, 0, 0, color);
            vertexFill(consumer, pose, x2, 0, z2, color);
            vertexFill(consumer, pose, x1, 0, z1, color);
        }
    }

    private void renderConeFill(PoseStack.Pose pose, VertexConsumer consumer, int color) {
        float width = 0.62f;
        float coneHeight = 0.3f;

        for (int i = 0; i < SEGMENTS; i++) {
            float angle1 = i * PI2 / SEGMENTS;
            float angle2 = (i + 1) * PI2 / SEGMENTS;

            float x1 = -(float) Math.sin(angle1) * width;
            float z1 = (float) Math.cos(angle1) * width;
            float x2 = -(float) Math.sin(angle2) * width;
            float z2 = (float) Math.cos(angle2) * width;

            vertexFill(consumer, pose, 0, -coneHeight, 0, color);
            vertexFill(consumer, pose, x1, 0, z1, color);
            vertexFill(consumer, pose, x2, 0, z2, color);
        }
    }

    private void vertexFill(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z, int color) {
        consumer.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(0.5f, 0.5f)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULLBRIGHT)
                .setNormal(0.0f, 1.0f, 0.0f);
    }

    private void renderOutline(PoseStack.Pose pose, VertexConsumer consumer, int outlineColor) {
        float width = 0.62f;

        for (int i = 0; i < SEGMENTS; i++) {
            float angle1 = i * PI2 / SEGMENTS;
            float angle2 = (i + 1) * PI2 / SEGMENTS;

            float x1 = -(float) Math.sin(angle1) * width;
            float z1 = (float) Math.cos(angle1) * width;
            float x2 = -(float) Math.sin(angle2) * width;
            float z2 = (float) Math.cos(angle2) * width;

            consumer.addVertex(pose, x1, 0, z1)
                    .setColor(outlineColor)
                    .setNormal(0.0f, 1.0f, 0.0f)
                    .setLineWidth(2.0f);
            consumer.addVertex(pose, x2, 0, z2)
                    .setColor(outlineColor)
                    .setNormal(0.0f, 1.0f, 0.0f)
                    .setLineWidth(2.0f);
        }
    }

    private int getThemeColor(int alpha) {
        int themeColor = RRender.accent();
        // Сохраняем альфа-канал из акцентного цвета, но используем переданный alpha если нужно
        int a = (themeColor >> 24) & 0xFF;
        int r = (themeColor >> 16) & 0xFF;
        int g = (themeColor >> 8) & 0xFF;
        int b = themeColor & 0xFF;
        // Если alpha параметр не 255, используем его, иначе используем альфа из themeColor
        int finalAlpha = alpha != 255 ? alpha : a;
        return (finalAlpha << 24) | (r << 16) | (g << 8) | b;
    }
}