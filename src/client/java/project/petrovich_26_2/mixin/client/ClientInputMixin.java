package project.petrovich_26_2.mixin.client;

import client_files.ClientikUtils.RotationComponent;
import client_files.ModuleManager;
import client_files.Petrovich.Combat.Aura;
import client_files.Petrovich.Combat.CrystalAura;
import client_files.Petrovich.Movement.AirStuck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientInput.class)
public class ClientInputMixin {

    @Shadow
    public Input keyPresses;

    @Shadow
    protected Vec2 moveVector;

    @Inject(method = "tick", at = @At("TAIL"))
    private void petrovich$airStuckInput(CallbackInfo ci) {
        if (!ModuleManager.isReady()) return;

        AirStuck airStuck = ModuleManager.getInstance().get(AirStuck.class);
        if (airStuck == null || !airStuck.isEnabled()) return;

        Input old = this.keyPresses;
        this.keyPresses = new Input(false, false, false, false, old.jump(), old.shift(), old.sprint());
        this.moveVector = new Vec2(0, 0);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void petrovich$targetedMoveFix(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (ModuleManager.isReady()) {
            AirStuck airStuck = ModuleManager.getInstance().get(AirStuck.class);
            if (airStuck != null && airStuck.isEnabled()) return;
        }

        Input old = this.keyPresses;

        if (CrystalAura.moveFixActive()) {
            // ввод не обнуляем: на элитрах и в полёте это выглядит как флаг
            if (player.isFallFlying()) {
                return;
            }

            float f = (old.forward() ? 1 : 0) - (old.backward() ? 1 : 0);
            float s = (old.left() ? 1 : 0) - (old.right() ? 1 : 0);

            RotationComponent.Vec2Tuple fix = CrystalAura.fixInput(f, s);

            if (fix.forward() == f && fix.strafe() == s) {
                return;
            }

            boolean nf = fix.forward() > 0;
            boolean nb = fix.forward() < 0;
            boolean nl = fix.strafe() > 0;
            boolean nr = fix.strafe() < 0;
            this.keyPresses = new Input(nf, nb, nl, nr, old.jump(), old.shift(), old.sprint());
            this.moveVector = new Vec2(fix.strafe(), fix.forward()).normalized();
            return;
        }

        if (!Aura.moveFixTargeted()) return;

        LivingEntity target = Aura.target;
        if (target == null) return;

        if (player.isFallFlying()) {
            this.keyPresses = new Input(false, false, false, false, old.jump(), old.shift(), old.sprint());
            this.moveVector = new Vec2(0, 0);
            return;
        }

        float forward = (old.forward() ? 1 : 0) - (old.backward() ? 1 : 0);
        float strafe = (old.left() ? 1 : 0) - (old.right() ? 1 : 0);
        if (forward == 0 && strafe == 0) return;

        float yaw = Mth.wrapDegrees(mc.gameRenderer.mainCamera().yRot());

        double dx = target.getX() - player.getX();
        double dz = target.getZ() - player.getZ();
        double targetAngle = Mth.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);

        float bestForward = 0, bestStrafe = 0;
        float smallestDiff = Float.MAX_VALUE;
        for (float f = -1f; f <= 1f; f++) {
            for (float s = -1f; s <= 1f; s++) {
                if (f == 0 && s == 0) continue;
                double predictedAngle = Mth.wrapDegrees(Math.toDegrees(RotationComponent.direction(yaw, f, s)));
                float diff = (float) Math.abs(Mth.wrapDegrees((float) (targetAngle - predictedAngle)));
                if (diff < smallestDiff) {
                    smallestDiff = diff;
                    bestForward = f;
                    bestStrafe = s;
                }
            }
        }

        boolean nf = bestForward > 0;
        boolean nb = bestForward < 0;
        boolean nl = bestStrafe > 0;
        boolean nr = bestStrafe < 0;
        this.keyPresses = new Input(nf, nb, nl, nr, old.jump(), old.shift(), old.sprint());
        this.moveVector = new Vec2(bestStrafe, bestForward).normalized();
    }
}