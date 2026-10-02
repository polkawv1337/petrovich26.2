package client_files.Petrovich.Combat;

import client_files.ClientikUtils.BestPoint;
import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.GCDFixer;
import client_files.ClientikUtils.IdealHitUtils;
import client_files.ClientikUtils.ModeSetting;
import client_files.ClientikUtils.Rotation;
import client_files.ClientikUtils.RotationComponent;
import client_files.ClientikUtils.RotationUtil;
import client_files.ClientikUtils.SliderSetting;
import client_files.Module;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AutoMace extends Module {

    private final ModeSetting rotation = addSetting(new ModeSetting("Ротация", "Snap", "Snap", "Smooth"));
    private final BooleanSetting players = addSetting(new BooleanSetting("Игроки", true));
    private final BooleanSetting monsters = addSetting(new BooleanSetting("Монстры", true));
    private final BooleanSetting animals = addSetting(new BooleanSetting("Животные", false));
    private final SliderSetting range = addSetting(new SliderSetting("Дистанция", 4.0f, 2.0f, 6.0f, 0.1f));
    private final BooleanSetting onlySmash = addSetting(new BooleanSetting("Только при смэше", true));
    private final BooleanSetting clientLook = addSetting(new BooleanSetting("Клиент лук", true));

    private LivingEntity target;
    private float lastYaw;
    private float lastPitch;

    public AutoMace() {
        super("AutoMace", "Автоматическая атака булавой с высоты", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        Player player = mc.player;
        if (player == null || mc.level == null) return;
        if (player.getMainHandItem().getItem() != Items.MACE) {
            target = null;
            return;
        }

        updateTarget(player);

        if (target == null) {
            return;
        }

        boolean smash = net.minecraft.world.item.MaceItem.canSmashAttack(player)
                || player.fallDistance > net.minecraft.world.item.MaceItem.SMASH_ATTACK_FALL_THRESHOLD;
        if (onlySmash.getValue() && !smash) {
            return;
        }

        rotate(player, target);

        if (IdealHitUtils.cooldownIsReached(false)) {
            mc.gameMode.attack(player, target);
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private void updateTarget(Player player) {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        AABB box = player.getBoundingBox().inflate(range.getValue() + 2.0);

        for (Object obj : mc.level.getEntities(EntityTypeTest.forClass(Entity.class), box, e -> true)) {
            if (!(obj instanceof LivingEntity living)) continue;
            if (!isValidEntity(living)) continue;
            double dist = player.getEyePosition().distanceTo(BestPoint.getNearestPoint(living));
            if (dist < bestDist) {
                bestDist = dist;
                best = living;
            }
        }
        target = best;
    }

    private boolean isValidEntity(LivingEntity living) {
        if (!living.isAlive()) return false;
        if (living == mc.player) return false;
        if (living instanceof ArmorStand) return false;

        if (living instanceof Player p) {
            if (client_files.Petrovich.Player.FriendHelper.isFriend(p.getGameProfile().name())) return false;
            if (!players.getValue()) return false;
        } else if (living instanceof Enemy || living instanceof AmbientCreature) {
            if (!monsters.getValue()) return false;
        } else if (living instanceof Animal || living instanceof AbstractFish || living instanceof AbstractVillager) {
            if (!animals.getValue()) return false;
        }
        return true;
    }

    private void rotate(Player player, LivingEntity target) {
        Vec3 point = target.getBoundingBox().getCenter();
        Rotation angle = RotationUtil.to(player.getEyePosition(), point);

        if ("Smooth".equals(rotation.getValue())) {
            float deltaYaw = Mth.wrapDegrees(angle.getYaw() - lastYaw);
            float deltaPitch = angle.getPitch() - lastPitch;
            float moveYaw = Mth.clamp(deltaYaw, -45f, 45f);
            float movePitch = Mth.clamp(deltaPitch, -45f, 45f);
            float newYaw = lastYaw + moveYaw;
            float newPitch = lastPitch + movePitch;

            float gcd = GCDFixer.getGCDValue();
            newYaw -= (newYaw - lastYaw) % gcd;
            newPitch -= (newPitch - lastPitch) % gcd;
            newPitch = Mth.clamp(newPitch, -90f, 90f);

            Rotation smoothRot = new Rotation(newYaw, newPitch);
            RotationComponent.update(smoothRot, 360, 360, 360, 360, 0, 1, clientLook.getValue());
            lastYaw = smoothRot.getYaw();
            lastPitch = smoothRot.getPitch();
        } else {
            RotationComponent.update(angle, 360, 360, 360, 360, 0, 1, clientLook.getValue());
            lastYaw = angle.getYaw();
            lastPitch = angle.getPitch();
        }
    }

    @Override
    public void onEnable() {
        target = null;
        lastYaw = mc.player != null ? mc.player.getYRot() : 0f;
        lastPitch = mc.player != null ? mc.player.getXRot() : 0f;
    }

    @Override
    public void onDisable() {
        target = null;
    }
}