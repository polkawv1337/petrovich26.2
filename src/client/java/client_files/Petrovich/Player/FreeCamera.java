package client_files.Petrovich.Player;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.PacketUtil;
import client_files.ClientikUtils.SliderSetting;
import client_files.Module;
import client_files.ModuleManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

public class FreeCamera extends Module {

    private final SliderSetting ySpeed = addSetting(new SliderSetting("Скорость по Y", 0.5f, 0.1f, 1.0f, 0.1f));

    private Vec3 frozenPos = Vec3.ZERO;
    private float frozenYaw;
    private float frozenPitch;

    public RemotePlayer fakePlayer;

    private boolean active;

    public FreeCamera() {
        super("FreeCamera", "Свободная камера, игрок остаётся на месте", Category.PLAYER);
    }

    public RemotePlayer getFakePlayer() {
        return fakePlayer;
    }

    @Override
    public void onEnable() {
        active = false;
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;

        frozenPos = mc.player.position();
        frozenYaw = mc.player.getYRot();
        frozenPitch = mc.player.getXRot();

        GameProfile profile = new GameProfile(UUID.randomUUID(), mc.player.getName().getString());
        fakePlayer = new RemotePlayer(mc.level, profile);
        fakePlayer.setPos(frozenPos.x, frozenPos.y, frozenPos.z);
        fakePlayer.setYRot(frozenYaw);
        fakePlayer.setXRot(frozenPitch);
        mc.level.addEntity(fakePlayer);

        mc.player.noPhysics = true;
        mc.player.setDeltaMovement(Vec3.ZERO);
        active = true;
    }

    @Override
    public void onDisable() {
        active = false;

        if (mc.player != null) {
            mc.player.noPhysics = false;
            mc.player.setPos(frozenPos.x, frozenPos.y, frozenPos.z);
            mc.player.setYRot(frozenYaw);
            mc.player.setXRot(frozenPitch);
            mc.player.setDeltaMovement(Vec3.ZERO);
        }

        removeFakePlayer();
    }

    private void removeFakePlayer() {
        if (fakePlayer == null) return;
        if (mc.level != null) {
            mc.level.removeEntity(fakePlayer.getId(), Entity.RemovalReason.DISCARDED);
        }
        fakePlayer = null;
    }

    @Override
    public void onTick() {
        if (!active || mc.player == null) return;

        mc.player.noPhysics = true;
        mc.player.setDeltaMovement(Vec3.ZERO);

        float speed = ySpeed.getValue();
        double motionY = 0.0;
        if (mc.options.keyJump.isDown()) motionY += speed;
        if (mc.options.keyShift.isDown()) motionY -= speed;

        double[] direction = calculateDirection(1.0);
        mc.player.setDeltaMovement(direction[0], motionY, direction[1]);
    }

    private double[] calculateDirection(double distance) {
        Input input = mc.player.getLastSentInput();
        float forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
        float sideways = (input.left() ? 1 : 0) - (input.right() ? 1 : 0);
        float yaw = mc.player.getYRot();

        if (forward != 0.0F) {
            if (sideways > 0.0F) {
                yaw += (forward > 0.0F) ? -45 : 45;
            } else if (sideways < 0.0F) {
                yaw += (forward > 0.0F) ? 45 : -45;
            }
            sideways = 0.0F;
            forward = (forward > 0.0F) ? 1.0F : -1.0F;
        }

        double sinYaw = Math.sin(Math.toRadians(yaw + 90.0F));
        double cosYaw = Math.cos(Math.toRadians(yaw + 90.0F));
        double xMovement = forward * distance * cosYaw + sideways * distance * sinYaw;
        double zMovement = forward * distance * sinYaw - sideways * distance * cosYaw;

        return new double[]{xMovement, zMovement};
    }

    private void applyPosition(PositionMoveRotation change, Set<Relative> relatives) {
        if (fakePlayer == null) return;
        PositionMoveRotation current = PositionMoveRotation.of(fakePlayer);
        PositionMoveRotation applied = PositionMoveRotation.calculateAbsolute(current, change, relatives);
        frozenPos = applied.position();
        frozenYaw = applied.yRot();
        frozenPitch = applied.xRot();
    }

    public static boolean handlePositionPacket(ClientboundPlayerPositionPacket packet) {
        if (!ModuleManager.isReady()) return false;

        FreeCamera module = ModuleManager.getInstance().get(FreeCamera.class);
        if (module == null || !module.isEnabled() || !module.active) return false;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level == null || player == null || !player.isAlive()) return false;

        PacketUtil.sendSilent(new ServerboundAcceptTeleportationPacket(packet.id()));
        if (player.getVehicle() == null) {
            module.applyPosition(packet.change(), packet.relatives());
        }

        PacketUtil.sendSilent(new ServerboundMovePlayerPacket.PosRot(
                new Vec3(module.frozenPos.x, module.frozenPos.y, module.frozenPos.z),
                module.frozenYaw, module.frozenPitch, false, false));
        return true;
    }

    public static boolean handlePacketSend(Packet<?> packet) {
        if (!ModuleManager.isReady()) return false;

        FreeCamera module = ModuleManager.getInstance().get(FreeCamera.class);
        if (module == null || !module.isEnabled() || !module.active) return false;

        if (packet instanceof ServerboundMovePlayerPacket) {
            return true;
        }

        if (packet instanceof ServerboundUseItemOnPacket useItemOn) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                PacketUtil.sendSilent(new ServerboundUseItemPacket(useItemOn.getHand(), useItemOn.getSequence(),
                        mc.player.getYRot(), mc.player.getXRot()));
            }
            return true;
        }

        return false;
    }

    public static void handleWorldGone() {
        if (!ModuleManager.isReady()) return;

        FreeCamera module = ModuleManager.getInstance().get(FreeCamera.class);
        if (module != null && module.isEnabled()) {
            module.setEnabled(false);
        }
    }
}
