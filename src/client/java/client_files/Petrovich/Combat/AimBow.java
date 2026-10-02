package client_files.Petrovich.Combat;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.PredictUtils;
import client_files.ClientikUtils.Rotation;
import client_files.ClientikUtils.RotationUtil;
import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import client_files.Petrovich.Player.FriendHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.phys.Vec3;

public class AimBow extends Module {

    private static final double RANGE = 60.0;
    private static final double ARROW_SPEED = 3.0;
    private static final double ARROW_GRAVITY = 0.05;

    public AimBow() {
        super("AimBow", "Аим лука на игроков", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;
        if (!player.isUsingItem() || player.getUsedItemHand() != InteractionHand.MAIN_HAND) return;
        if (!(player.getMainHandItem().getItem() instanceof BowItem)) return;

        Player target = findTarget();
        if (target == null) return;

        Vec3 eye = player.getEyePosition();
        Vec3 targetPos = target.getBoundingBox().getCenter();
        double dist = eye.distanceTo(targetPos);
        if (dist < 1.0) return;

        double flightTicks = dist / ARROW_SPEED;
        Vec3 predicted = PredictUtils.predict(target, flightTicks);
        double drop = ARROW_GRAVITY * flightTicks * flightTicks * 0.5;
        predicted = predicted.add(0.0, drop, 0.0);

        Rotation rotation = RotationUtil.to(eye, predicted);
        player.setYRot(rotation.getYaw());
        player.setXRot(Mth.clamp(rotation.getPitch(), -90.0f, 90.0f));
        player.yHeadRot = player.getYRot();
    }

    private Player findTarget() {
        LocalPlayer self = mc.player;
        Vec3 eye = self.getEyePosition();
        Player best = null;
        double bestDist = Double.MAX_VALUE;
        for (Player player : mc.level.players()) {
            if (player == self || !player.isAlive()) continue;
            if (FriendHelper.isFriend(player.getName().getString())) continue;
            double dist = eye.distanceTo(player.getBoundingBox().getCenter());
            if (dist > RANGE || dist < 1.0) continue;
            if (dist < bestDist) {
                bestDist = dist;
                best = player;
            }
        }
        return best;
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!player.isUsingItem() || player.getUsedItemHand() != InteractionHand.MAIN_HAND) return;
        if (!(player.getMainHandItem().getItem() instanceof BowItem)) return;
        if (!mc.options.getCameraType().isFirstPerson()) return;
        int diameter = (int) (graphics.guiHeight() * 0.6);
        RRender.ring(graphics, graphics.guiWidth() / 2, graphics.guiHeight() / 2, diameter, 1, 0xFFFFFFFF);
    }
}