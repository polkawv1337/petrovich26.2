package client_files.Petrovich.Movement;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.ChatHelper;
import client_files.ClientikUtils.ModeSetting;
import client_files.ClientikUtils.PacketUtil;
import client_files.Module;
import client_files.ModuleManager;
import client_files.Petrovich.Player.ElytraHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class AirStuck extends Module {

    private final ModeSetting mode = addSetting(new ModeSetting("Мод", "Vanilla", "Vanilla", "SvinWorld"));

    private final BooleanSetting autoSwapChest = addSetting(new BooleanSetting("Свап на нагрудник", true));
    private final BooleanSetting backElytra = addSetting((BooleanSetting) new BooleanSetting("Вернуть при выкл", true)
            .setVisible(() -> autoSwapChest.getValue()));
    private final BooleanSetting fallCheck = addSetting(new BooleanSetting("Проверка на падение", true));

    private Vec3 savedVelocity = Vec3.ZERO;
    private Vec3 savedPos = Vec3.ZERO;
    private float savedYaw;
    private float savedPitch;
    private float lastSentYaw;
    private float lastSentPitch;
    private boolean isElytra;
    private boolean active;
    private int critTicks;

    public AirStuck() {
        super("AirStuck", "Даёт зависнуть в воздухе", Category.MOVEMENT);
    }

    public boolean isSvinWorld() {
        return "SvinWorld".equals(mode.getValue());
    }

    @Override
    public void onEnable() {
        active = false;
        if (mc.player == null || mc.level == null) return;

        if (mc.player.fallDistance == 0 && fallCheck.getValue()) {
            ChatHelper.print("Вам нужно падать");
            setEnabled(false);
            return;
        }

        savedPos = mc.player.position();
        savedVelocity = mc.player.getDeltaMovement();
        savedYaw = mc.player.getYRot();
        savedPitch = mc.player.getXRot();
        lastSentYaw = savedYaw;
        lastSentPitch = savedPitch;
        critTicks = 0;

        mc.player.setNoGravity(true);
        mc.player.setDeltaMovement(Vec3.ZERO);

        boolean wearingElytra = mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
        active = true;
        if (!wearingElytra || !autoSwapChest.getValue()) return;
        isElytra = true;

        ElytraHelper helper = ModuleManager.getInstance().get(ElytraHelper.class);
        if (helper != null) {
            helper.swap(true);
        }
    }

    @Override
    public void onDisable() {
        active = false;

        if (mc.player == null) return;

        if (mc.player.fallDistance == 0 && fallCheck.getValue()) return;

        if (savedVelocity != null) {
            mc.player.setDeltaMovement(savedVelocity);
        }

        mc.player.setNoGravity(false);

        ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        boolean wearingChestPlate = !chest.isEmpty() && !chest.is(Items.ELYTRA);
        if (!wearingChestPlate || !(autoSwapChest.getValue() && backElytra.getValue()) || !isElytra) return;
        isElytra = false;

        ElytraHelper helper = ModuleManager.getInstance().get(ElytraHelper.class);
        if (helper != null) {
            helper.swap(false);
        }
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (!active || player == null) return;

        player.setPos(savedPos.x, savedPos.y, savedPos.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
        player.setNoGravity(true);

        player.setPos(savedPos.x, savedPos.y, savedPos.z);
        player.setDeltaMovement(0.0, 0.0, 0.0);

        player.xo = savedPos.x;
        player.yo = savedPos.y;
        player.zo = savedPos.z;
        player.xOld = savedPos.x;
        player.yOld = savedPos.y;
        player.zOld = savedPos.z;

        if (!isSvinWorld()) return;

        float yaw = player.getYRot();
        float pitch = player.getXRot();

        if (Math.abs(yaw - lastSentYaw) > 0.15F || Math.abs(pitch - lastSentPitch) > 0.15F) {
            sendPosRot(savedPos, yaw, pitch);
            lastSentYaw = yaw;
            lastSentPitch = pitch;
        }

        if (++critTicks >= 8) {
            critTicks = 0;
            sendPosRot(new Vec3(savedPos.x, savedPos.y + 0.0625, savedPos.z), yaw, pitch);
            sendPosRot(new Vec3(savedPos.x, savedPos.y + 0.12, savedPos.z), yaw, pitch);
            sendPosRot(savedPos, yaw, pitch);
        }
    }

    private void sendPosRot(Vec3 pos, float yaw, float pitch) {
        PacketUtil.sendSilent(new ServerboundMovePlayerPacket.PosRot(pos, yaw, pitch, false, false));
    }

    private void applySvinPosition(PositionMoveRotation change, Set<Relative> relatives) {
        if (mc.player == null) return;
        PositionMoveRotation current = PositionMoveRotation.of(mc.player);
        PositionMoveRotation applied = PositionMoveRotation.calculateAbsolute(current, change, relatives);
        savedPos = applied.position();
    }

    public static boolean handlePositionPacket(ClientboundPlayerPositionPacket packet) {
        if (!ModuleManager.isReady()) return false;

        AirStuck module = ModuleManager.getInstance().get(AirStuck.class);
        if (module == null || !module.isEnabled() || !module.active) return false;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level == null || player == null || !player.isAlive()) return false;

        if (module.isSvinWorld()) {
            PacketUtil.sendSilent(new ServerboundAcceptTeleportationPacket(packet.id()));

            if (player.getVehicle() == null) {
                module.applySvinPosition(packet.change(), packet.relatives());
            }

            module.sendPosRot(module.savedPos, player.getYRot(), player.getXRot());
            module.lastSentYaw = player.getYRot();
            module.lastSentPitch = player.getXRot();
        } else {
            PositionMoveRotation current = PositionMoveRotation.of(player);
            PositionMoveRotation target = PositionMoveRotation.calculateAbsolute(current, packet.change(), packet.relatives());

            if (target.position().distanceToSqr(module.savedPos) > 4096.0) {
                PacketUtil.sendSilent(new ServerboundAcceptTeleportationPacket(packet.id()));
                module.savedPos = target.position();
                player.setPos(module.savedPos.x, module.savedPos.y, module.savedPos.z);
            } else {
                module.savedPos = player.position();
            }
        }
        return true;
    }

    public static boolean handlePacketSend(Packet<?> packet) {
        if (!ModuleManager.isReady()) return false;

        AirStuck module = ModuleManager.getInstance().get(AirStuck.class);
        if (module == null || !module.isEnabled() || !module.active) return false;

        if (packet instanceof ServerboundMovePlayerPacket) {
            return true;
        }

        if (module.isSvinWorld() && packet instanceof ServerboundUseItemOnPacket useItemOn) {
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

        AirStuck module = ModuleManager.getInstance().get(AirStuck.class);
        if (module != null && module.isEnabled()) {
            module.setEnabled(false);
        }
    }
}
