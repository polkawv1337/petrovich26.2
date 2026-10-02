package client_files.Petrovich.Combat;

import client_files.ClientikUtils.BestPoint;
import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.GCDFixer;
import client_files.ClientikUtils.IdealHitUtils;
import client_files.ClientikUtils.ModeListSetting;
import client_files.ClientikUtils.ModeSetting;
import client_files.ClientikUtils.PredictUtils;
import client_files.ClientikUtils.RaytraceUtil;
import client_files.ClientikUtils.Rotation;
import client_files.ClientikUtils.RotationComponent;
import client_files.ClientikUtils.RotationUtil;
import client_files.ClientikUtils.SliderSetting;
import client_files.Module;
import client_files.ModuleManager;
import client_files.Petrovich.Player.FreeCamera;
import client_files.Petrovich.Player.FriendHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import project.petrovich_26_2.mixin.client.MultiPlayerGameModeAccessor;

public class Aura extends Module {

    public final ModeSetting rotation = addSetting(new ModeSetting("Ротация", "Smooth", "Smooth", "Sloth", "FunTime", "Aim Assist"));
    public final ModeSetting rotationBehavior = addSetting(new ModeSetting("Поведение ротации", "Плавная", "Плавная", "Снапы"));
    private final ModeListSetting targets = addSetting(new ModeListSetting("Таргеты",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Голые", true),
            new BooleanSetting("Монстры", true),
            new BooleanSetting("Животные", true)
    ));

    public final SliderSetting distance = addSetting(new SliderSetting("Дистанция", 3.0f, 2.0f, 6.0f, 0.1f));
    private final SliderSetting preRotation = addSetting(new SliderSetting("Пре дистанция", 1.5f, 0.0f, 3.0f, 0.1f));
    public final BooleanSetting raycastCheck = addSetting(new BooleanSetting("Проверка на наведение", true));
    public final SliderSetting predictValue = addSetting(new SliderSetting("Предикт значение", 3.0f, 1.0f, 5.0f, 0.1f));
    public final BooleanSetting shieldBreak = addSetting(new BooleanSetting("Ломать щит", true));

    public final SliderSetting assistRadius = addSetting((SliderSetting) new SliderSetting("Assist радиус", 0.0f, -0.5f, 1.0f, 0.01f)
            .setVisible(() -> "Aim Assist".equals(rotation.getValue())));
    public final SliderSetting assistSpeedAimed = addSetting((SliderSetting) new SliderSetting("Assist скорость в цели", 0.25f, 0.0f, 5.0f, 0.01f)
            .setVisible(() -> "Aim Assist".equals(rotation.getValue())));
    public final SliderSetting assistSpeedGround = addSetting((SliderSetting) new SliderSetting("Assist скорость земля", 2.0f, 0.0f, 5.0f, 0.01f)
            .setVisible(() -> "Aim Assist".equals(rotation.getValue())));
    public final SliderSetting assistSpeedAir = addSetting((SliderSetting) new SliderSetting("Assist скорость воздух", 2.0f, 0.0f, 5.0f, 0.01f)
            .setVisible(() -> "Aim Assist".equals(rotation.getValue())));
    public final SliderSetting assistSpeedElytra = addSetting((SliderSetting) new SliderSetting("Assist скорость элитры", 2.0f, 0.0f, 10.0f, 0.01f)
            .setVisible(() -> "Aim Assist".equals(rotation.getValue())));

    public final ModeSetting moveFix = addSetting(new ModeSetting("Коррекция движения", "Сфокусированная", "Нет", "Сфокусированная", "Таргетированная"));

    public final BooleanSetting onlySpace = addSetting(new BooleanSetting("Только с пробелом", true));
    public final BooleanSetting clientLook = addSetting(new BooleanSetting("Клиент лук", true));
    public final BooleanSetting visualElytraRotation = addSetting(new BooleanSetting("Визуал. ротка Элитры", true));

    public static LivingEntity target;
    public static LivingEntity lastTarget;
    public static int ticksToAttack;
    public static boolean isSlowdownActive;
    public static long lastPhysicalMoveTime;

    public float speedAcceleration;
    public float obhod;
    public float lastYaw;
    public float lastPitch;

    private int razvorotikTicks;
    private float lastMouseYaw = Float.NaN;
    private float lastMousePitch = Float.NaN;

    private LivingEntity slothTrackedTarget;
    private float slothCurrentYaw;
    private float slothCurrentPitch;
    private float slothVelocityYaw;
    private float slothVelocityPitch;
    private double slothAimPointX;
    private double slothAimPointY;
    private double slothAimPointZ;
    private int slothAimRegion;
    private float slothPatternSpeed;
    private float slothStiffness;
    private float slothDamping;
    private float slothMaxDeg;
    private int slothMicroTimer;
    private boolean slothPostHitActive;
    private int slothPostHitTicks;
    private float slothStrayYaw;
    private float slothStrayPitch;
    private float slothLastSentYaw;
    private float slothLastSentPitch;
    private float slothSmoothYaw;
    private float slothSmoothPitch;
    private float slothStarAngle;
    private float slothStarSpeed;
    private int slothStarPetals;
    private float slothStarAmp;

    public Aura() {
        super("Aura", "Атакует выбранных целей с обходом античитов", Category.COMBAT);
    }

    public float getPredictValue() {
        return predictValue.getValue();
    }

    public static boolean onlySpaceActive() {
        Aura aura = ModuleManager.getInstance().get(Aura.class);
        return aura != null && aura.onlySpace.getValue();
    }

    public static boolean moveFixTargeted() {
        Aura aura = ModuleManager.getInstance().get(Aura.class);
        return aura != null && aura.isEnabled() && aura.isTargetedMoveFix();
    }

    public boolean isTargetedMoveFix() {
        return "Таргетированная".equals(moveFix.getValue());
    }

    @Override
    public void onTick() {
        LocalPlayer player = mc.player;
        if (player == null || target == null) {
            if (player != null) {
                trackPhysicalMove(player);
            }
            RotationComponent.resetLastRotation();
            return;
        }

        trackPhysicalMove(player);

        if ("Снапы".equals(rotationBehavior.getValue())) {
            boolean isReadyToAttack = player.getAttackStrengthScale(1.0F) >= 0.95F && ticksToAttack <= 1;
            if (!isReadyToAttack) {
                return;
            }
        }

        if (canStopSprinting()) {
            mc.getConnection().send(new ServerboundPlayerInputPacket(
                    new Input(false, false, false, false, false, false, false)));
        }

        switch (rotation.getValue()) {
            case "Smooth" -> updateSmoothRotation(target);
            case "Sloth" -> updateSlothRotation(target);
            case "FunTime" -> updateFunTimeRotation(target);
            case "Aim Assist" -> updateAssistRotation(target);
        }
    }

    @Override
    public void onUpdate() {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        if (ticksToAttack > 0) ticksToAttack--;
        if (razvorotikTicks > 0) razvorotikTicks--;

        updateTarget();

        if (target != null) {
            lastTarget = target;

            ElytraTarget elytraTarget = et();
            Vec3 predict = PredictUtils.predict(target,
                    elytraTarget != null ? elytraTarget.getPredictValue() : predictValue.getValue());
            double distToPredict = player.getEyePosition().distanceTo(predict);

            isSlowdownActive = false;
            if (elytraTarget != null && elytraTarget.isElytraSlowdown() && player.isFallFlying()) {
                if (elytraTarget.isSlowdownBeforeHit()) {
                    isSlowdownActive = ticksToAttack <= elytraTarget.getPreHitTicks();
                } else {
                    isSlowdownActive = distToPredict < 2.7 && ticksToAttack <= 2;
                }
            }

            if (canStopSprinting()) player.setSprinting(false);

            if (canAttack()) {
                if ("Sloth".equals(rotation.getValue())) {
                    slOnAttack();
                }

                int prevSlot = bypassShieldSlot();

                mc.getConnection().send(new ServerboundPlayerInputPacket(
                        new Input(false, false, false, false, false, false, false)));

                mc.gameMode.attack(mc.player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);

                mc.getConnection().send(new ServerboundPlayerInputPacket(mc.player.input.keyPresses));

                if (prevSlot != -1) {
                    mc.player.getInventory().setSelectedSlot(prevSlot);
                    ((MultiPlayerGameModeAccessor) mc.gameMode).petrovich$syncCarriedItem();
                }

                ticksToAttack = 10;
            }
        } else {
            speedAcceleration = 0;
            razvorotikTicks = 0;
        }
    }

    private void trackPhysicalMove(LocalPlayer player) {
        float yaw = player.getYRot();
        float pitch = player.getXRot();
        if (Float.isNaN(lastMouseYaw) || Mth.wrapDegrees(yaw - lastMouseYaw) != 0.0F
                || pitch - lastMousePitch != 0.0F) {
            lastPhysicalMoveTime = System.currentTimeMillis();
        }
        lastMouseYaw = yaw;
        lastMousePitch = pitch;
    }

    private ElytraTarget et() {
        return ModuleManager.getInstance().get(ElytraTarget.class);
    }

    private Player activePlayer() {
        if (ModuleManager.isReady()) {
            FreeCamera freeCamera = ModuleManager.getInstance().get(FreeCamera.class);
            if (freeCamera != null && freeCamera.fakePlayer != null) {
                return freeCamera.fakePlayer;
            }
        }
        return mc.player;
    }

    private boolean isValidEntity(Entity entity) {
        if (!entity.isAlive()) return false;

        Player player = activePlayer();
        if (entity == player) return false;
        if (entity instanceof LocalPlayer) return false;
        if (entity instanceof ArmorStand) return false;

        if (entity instanceof Player p) {
            boolean armored = !p.getItemBySlot(EquipmentSlot.HEAD).isEmpty()
                    || !p.getItemBySlot(EquipmentSlot.CHEST).isEmpty()
                    || !p.getItemBySlot(EquipmentSlot.LEGS).isEmpty()
                    || !p.getItemBySlot(EquipmentSlot.FEET).isEmpty();
            if (armored && !targets.isEnabled("Игроки")) return false;
            if (!armored && !targets.isEnabled("Голые")) return false;
            if (FriendHelper.isFriend(p.getGameProfile().name())) return false;
        }

        if ((entity instanceof Enemy || entity instanceof AmbientCreature) && !targets.isEnabled("Монстры")) {
            return false;
        }
        if ((entity instanceof Animal || entity instanceof AbstractFish) && !targets.isEnabled("Животные")) {
            return false;
        }

        if (player == null) return false;
        double range = player.isFallFlying() ? 50.0 : distance.getValue() + preRotation.getValue();
        return player.getEyePosition().distanceTo(BestPoint.getNearestPoint(entity)) <= range;
    }

    public boolean canAttack() {
        if (target == null) return false;

        Player player = activePlayer();
        if (player == null) return false;

        if (!IdealHitUtils.cooldownIsReached(false)) return false;
        if (ticksToAttack > 0) return false;

        if (target.isFallFlying()) {
            if (shouldBypassPredict() || !predictEnabled()) {
                double distToEye = player.getEyePosition().distanceTo(target.getEyePosition());
                if (distToEye > 4.0F) return false;
            } else {
                double distToPredict = player.getEyePosition().distanceTo(
                        PredictUtils.predict(target, predictValue.getValue()));
                if (distToPredict > 4.0F) return false;
            }
        } else {
            if (!RaytraceUtil.rayTrace(player.getViewVector(1.0F), distance.getValue(), target.getBoundingBox())
                    && raycastCheck.getValue()) {
                return false;
            }

            if (player.getEyePosition().distanceTo(BestPoint.getNearestPoint(target)) > (distance.getValue() - 0.2F)) {
                return false;
            }
        }

        return IdealHitUtils.canCritical();
    }

    private int bypassShieldSlot() {
        if (mc.player == null || target == null) return -1;
        if (!shieldBreak.getValue()) return -1;
        if (!(target instanceof Player p) || !p.isBlocking()) return -1;

        int axeSlot = findBestAxeSlot();
        if (axeSlot == -1 || axeSlot == mc.player.getInventory().getSelectedSlot()) return -1;

        int prevSlot = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(axeSlot);
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(axeSlot));
        return prevSlot;
    }

    private int findBestAxeSlot() {
        int best = -1;
        double bestDamage = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof AxeItem)) continue;
            double dmg = getAttackDamage(stack);
            if (dmg > bestDamage) {
                bestDamage = dmg;
                best = i;
            }
        }
        return best;
    }

    private double getAttackDamage(ItemStack stack) {
        ItemAttributeModifiers modifiers = stack.getComponents().get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) return 0.0;
        double totalDamage = 0.0;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE)) {
                totalDamage += entry.modifier().amount();
            }
        }
        return totalDamage;
    }

    public boolean canStopSprinting() {
        if (target == null) return false;
        if (!IdealHitUtils.cooldownIsReached(true)) return false;
        if (ticksToAttack > 1) return false;
        if (mc.player == null) return false;
        if (!mc.player.onGround() && mc.player.getDeltaMovement().y < -0.0001 && !mc.options.keyJump.isDown()) {
            return false;
        }
        return true;
    }

    private boolean predictEnabled() {
        ElytraTarget elytraTarget = ModuleManager.getInstance().get(ElytraTarget.class);
        return elytraTarget != null && elytraTarget.getPredictValue() > 0;
    }

    private boolean isGroundVsElytra() {
        if (mc.player == null || target == null) return false;
        return !mc.player.isFallFlying() && target.isFallFlying();
    }

    private boolean shouldBypassPredict() {
        return isGroundVsElytra();
    }

    private void updateTarget() {
        LivingEntity best = null;
        double bestFovDot = -1;

        if (mc.player == null || mc.level == null) return;

        Vec3 eyePos = mc.player.getEyePosition();
        Vec3 lookVec = mc.player.getViewVector(1.0F);

        double range = mc.player.isFallFlying() ? 52.0 : distance.getValue() + preRotation.getValue() + 1.0;
        AABB box = mc.player.getBoundingBox().inflate(range);

        for (Object obj : mc.level.getEntities(EntityTypeTest.forClass(LivingEntity.class), box, e -> true)) {
            if (!(obj instanceof LivingEntity living)) continue;
            if (!isValidEntity(living)) continue;

            Vec3 targetVec = BestPoint.getNearestPoint(living).subtract(eyePos).normalize();
            double dot = lookVec.dot(targetVec);

            if (dot > bestFovDot) {
                bestFovDot = dot;
                best = living;
            }
        }

        if (target == null || !isValidEntity(target)) {
            if (target != best && "Sloth".equals(rotation.getValue())) {
                slReset();
            }
            target = best;
        }
    }

    private Vec3 resolveMultipoint(LivingEntity target, Vec3 point, double range) {
        return point;
    }

    private void slReset() {
        slothTrackedTarget = null;
        slothVelocityYaw = slothVelocityPitch = 0.0F;
        slothAimPointX = slothAimPointY = slothAimPointZ = 0.0;
        slothAimRegion = 0;
        slothPatternSpeed = 0.0F;
        slothStiffness = 0.0F;
        slothDamping = 0.0F;
        slothMaxDeg = 0.0F;
        slothMicroTimer = 0;
        slothPostHitActive = false;
        slothPostHitTicks = 0;
        slothStrayYaw = slothStrayPitch = 0.0F;
        slothStarAngle = 0.0F;
        slothStarSpeed = 0.0F;
        slothStarPetals = 1;
        slothStarAmp = 0.0F;

        if (mc.player != null) {
            slothCurrentYaw = mc.player.getYRot();
            slothCurrentPitch = mc.player.getXRot();
            slothLastSentYaw = slothCurrentYaw;
            slothLastSentPitch = slothCurrentPitch;
            slothSmoothYaw = slothCurrentYaw;
            slothSmoothPitch = slothCurrentPitch;
        } else {
            slothCurrentYaw = slothCurrentPitch = 0.0F;
            slothLastSentYaw = slothLastSentPitch = 0.0F;
            slothSmoothYaw = slothSmoothPitch = 0.0F;
        }
    }

    private float slCalcGcd() {
        double s = mc.options.sensitivity().get() * 0.6 + 0.2;
        return (float) (s * s * s * 1.2);
    }

    private void slPickPattern(LivingEntity e) {
        slothAimRegion = (int) (Math.random() * 5.0);
        slothPatternSpeed = 0.9F + (float) Math.random() * 0.8F;
        slothStiffness = 0.12F + (float) Math.random() * 0.22F;
        slothDamping = 0.55F + (float) Math.random() * 0.55F;
        slothMaxDeg = 4.5F + (float) Math.random() * 6.0F;
        slothMicroTimer = 6 + (int) (Math.random() * 9);
        slothStarAngle = (float) (Math.random() * Math.PI * 2.0);
        slothStarSpeed = 0.10F + (float) Math.random() * 0.20F;
        slothStarPetals = 3 + (int) (Math.random() * 4);
        slothStarAmp = 2.4F + (float) Math.random() * 3.2F;
        slPickAimPoint(e);
    }

    private void slPickAimPoint(LivingEntity e) {
        AABB bb = e.getBoundingBox();
        double w = bb.maxX - bb.minX;
        double d = bb.maxZ - bb.minZ;
        double h = bb.maxY - bb.minY;

        double cx = bb.minX + w / 2.0;
        double cz = bb.minZ + d / 2.0;

        double baseY;
        switch (slothAimRegion) {
            case 0 -> baseY = bb.maxY - h * 0.04;
            case 1 -> baseY = bb.maxY - h * 0.30;
            case 2 -> baseY = bb.maxY - h * 0.55;
            case 3 -> baseY = bb.maxY - h * 0.82;
            default -> baseY = bb.minY + Math.random() * h;
        }

        slothAimPointX = cx + (Math.random() - 0.5) * w * 0.9;
        slothAimPointY = baseY + (Math.random() - 0.5) * h * 0.16;
        slothAimPointZ = cz + (Math.random() - 0.5) * d * 0.9;
    }

    public void slOnAttack() {
        if (slothTrackedTarget != null) {
            slPickPattern(slothTrackedTarget);
        }
        slothPostHitActive = true;
        slothPostHitTicks = 3 + (int) (Math.random() * 5);
        slothStrayYaw = ((float) Math.random() - 0.5F) * (44.0F + (float) Math.random() * 24.0F);
        slothStrayPitch = ((float) Math.random() - 0.5F) * (16.0F + (float) Math.random() * 12.0F);
        slothVelocityYaw = slothVelocityPitch = 0.0F;
    }

    private Vec3 slGetAimPoint(LivingEntity target) {
        Vec3 point = new Vec3(slothAimPointX, slothAimPointY, slothAimPointZ);
        if (target.isFallFlying() && predictEnabled() && !shouldBypassPredict()) {
            Vec3 predicted = PredictUtils.predict(target, predictValue.getValue());
            point = point.add(predicted.subtract(target.position()));
        }
        return point;
    }

    private float[] slComputeWant(LivingEntity target) {
        Vec3 dir = slGetAimPoint(target).subtract(mc.player.getEyePosition());
        float wantYaw = (float) Mth.wrapDegrees(Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0);
        float wantPitch = (float) -Math.toDegrees(Math.atan2(dir.y, dir.horizontalDistance()));
        return new float[]{wantYaw, wantPitch};
    }

    private float slSmoothLerp(float from, float to, float alpha) {
        alpha = Mth.clamp(alpha, 0.0F, 1.0F);
        float delta = Mth.wrapDegrees(to - from);
        return from + delta * alpha;
    }

    private void updateSlothRotation(LivingEntity target) {
        if (mc.player == null || target == null) return;

        float gcd = slCalcGcd();

        if (slothTrackedTarget != target) {
            slothTrackedTarget = target;
            slothCurrentYaw = mc.player.getYRot();
            slothCurrentPitch = mc.player.getXRot();
            slothLastSentYaw = slothCurrentYaw;
            slothLastSentPitch = slothCurrentPitch;
            slothSmoothYaw = slothCurrentYaw;
            slothSmoothPitch = slothCurrentPitch;
            slothVelocityYaw = slothVelocityPitch = 0.0F;
            slothPostHitActive = false;
            slPickPattern(target);
            return;
        }

        boolean aboutToStrike = mc.player.getAttackStrengthScale(0.5F) > 0.9F || ticksToAttack <= 1;

        if (!aboutToStrike && --slothMicroTimer <= 0) {
            slothMicroTimer = 5 + (int) (Math.random() * 9);
            slPickAimPoint(target);
        }

        float[] want = slComputeWant(target);
        float wantYaw = want[0];
        float wantPitch = want[1];

        float stiffness = aboutToStrike ? 0.6F : slothPatternSpeed * slothStiffness;
        float damping = aboutToStrike ? 0.95F : slothDamping;
        float maxDeg = aboutToStrike ? 40.0F : slothMaxDeg;

        if (slothPostHitActive) {
            slothPostHitTicks--;
            if (slothPostHitTicks <= 0) {
                slothPostHitActive = false;
            } else {
                wantYaw = slothCurrentYaw + slothStrayYaw;
                wantPitch = Mth.clamp(slothCurrentPitch + slothStrayPitch, -89.0F, 89.0F);
                stiffness *= 1.5F;
                maxDeg = Math.max(maxDeg, 15.0F);
            }
        }

        float diffYaw = Mth.wrapDegrees(wantYaw - slothCurrentYaw);
        float diffPitch = wantPitch - slothCurrentPitch;

        float accY = diffYaw * stiffness - slothVelocityYaw * damping * 0.45F;
        float accP = diffPitch * stiffness * 0.92F - slothVelocityPitch * damping * 0.45F;

        slothVelocityYaw = Mth.clamp(slothVelocityYaw + accY, -maxDeg, maxDeg);
        slothVelocityPitch = Mth.clamp(slothVelocityPitch + accP, -maxDeg, maxDeg);

        slothCurrentYaw += slothVelocityYaw;
        slothCurrentPitch += slothVelocityPitch;
        slothCurrentPitch = Mth.clamp(slothCurrentPitch, -89.0F, 89.0F);

        float smoothFactor = aboutToStrike ? 1.0F : 0.72F + (float) Math.random() * 0.26F;
        slothSmoothYaw = slSmoothLerp(slothSmoothYaw, slothCurrentYaw, smoothFactor);
        slothSmoothPitch = slSmoothLerp(slothSmoothPitch, slothCurrentPitch, smoothFactor * 0.96F);

        slothStarAngle += slothStarSpeed;

        float t = slothStarAngle;
        float starRadius = slothStarAmp * (float) Math.cos(slothStarPetals * t);
        float starYaw = starRadius * (float) Math.cos(t);
        float starPitch = starRadius * (float) Math.sin(t);

        float microY = ((float) Math.random() - 0.5F) * 0.7F;
        float microP = ((float) Math.random() - 0.5F) * 0.5F;

        float ampScale = aboutToStrike ? 0.2F : 1.0F;

        float outY = slothSmoothYaw + (starYaw + microY) * ampScale;
        float outP = Mth.clamp(slothSmoothPitch + (starPitch * 0.8F + microP) * ampScale, -89.0F, 89.0F);

        outY -= (outY - slothLastSentYaw) % gcd;
        outP -= (outP - slothLastSentPitch) % gcd;

        slothLastSentYaw = outY;
        slothLastSentPitch = outP;

        RotationComponent.update(new Rotation(outY, outP), 360, 360, 360, 360, 0, 1, clientLook.getValue());
    }

    private void updateSmoothRotation(LivingEntity target) {
        if (target == null) return;

        Vec3 targetPoint;
        if (target.isFallFlying() && predictEnabled() && !shouldBypassPredict()) {
            Vec3 predicted = PredictUtils.predict(target, predictValue.getValue());
            double boxHeight = target.getBoundingBox().maxY - target.getBoundingBox().minY;
            targetPoint = new Vec3(predicted.x, predicted.y + boxHeight * 0.8, predicted.z);
        } else {
            targetPoint = target.getEyePosition();
        }

        Rotation angle = RotationUtil.to(mc.player.getEyePosition(), targetPoint);
        float targetYaw = angle.getYaw();
        float targetPitch = angle.getPitch();

        float deltaYaw = Mth.wrapDegrees(targetYaw - lastYaw);
        float deltaPitch = targetPitch - lastPitch;

        float speed = 1.0F;

        float newYaw = lastYaw + deltaYaw * speed;
        float newPitch = lastPitch + deltaPitch * speed;

        float gcd = GCDFixer.getGCDValue();
        newYaw -= (newYaw - lastYaw) % gcd;
        newPitch -= (newPitch - lastPitch) % gcd;

        newPitch = Mth.clamp(newPitch, -90.0F, 90.0F);

        Rotation smoothRot = new Rotation(newYaw, newPitch);
        RotationComponent.update(smoothRot, 360, 360, 360, 360, 0, 1, clientLook.getValue());

        lastYaw = smoothRot.getYaw();
        lastPitch = smoothRot.getPitch();
    }

    private void updateFunTimeRotation(LivingEntity target) {
        if (target == null) return;

        Vec3 targetPoint;
        if (target.isFallFlying() && predictEnabled() && !shouldBypassPredict()) {
            Vec3 predicted = PredictUtils.predict(target, predictValue.getValue());
            double boxHeight = target.getBoundingBox().maxY - target.getBoundingBox().minY;
            targetPoint = new Vec3(predicted.x, predicted.y + boxHeight * 0.8, predicted.z);
        } else {
            targetPoint = target.getEyePosition();
        }

        Rotation angle = RotationUtil.to(mc.player.getEyePosition(), targetPoint);
        float targetYaw = angle.getYaw();
        float targetPitch = angle.getPitch();

        float deltaYaw = Mth.wrapDegrees(targetYaw - lastYaw);
        float deltaPitch = targetPitch - lastPitch;

        float dist = (float) Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);
        boolean aimed = dist < 4.0F;

        float speed = aimed ? 2.4F : 0.18F;

        float newYaw = lastYaw + deltaYaw * speed;
        float newPitch = lastPitch + deltaPitch * speed;

        double time = System.nanoTime() * 1e-9;

        float snapYaw = (float) (Math.sin(time * 85.0) * 1.8 + Math.sin(time * 149.0 + 1.7) * 1.1 + (Math.random() - 0.5) * 4.5);
        float snapPitch = (float) (Math.sin(time * 63.0 + 0.6) * 1.2 + (Math.random() - 0.5) * 2.2);

        if (aimed) {
            newYaw += snapYaw;
            newPitch += snapPitch;
            if (Math.random() < 0.25) {
                newYaw += (float) ((Math.random() - 0.5) * 9.0);
            }
            if (Math.random() < 0.08) {
                newYaw += (Math.signum(deltaYaw) * dist + (Math.random() - 0.5) * 8.0F) * (0.3F + (float) Math.random() * 1.1F);
                newPitch += (Math.signum(deltaPitch) * 4.0F + (Math.random() - 0.5) * 6.0F) * 0.4F;
            }
        } else {
            float jerkBoost = Math.min(1.0F, dist / 14.0F);
            newYaw += (float) Math.signum(deltaYaw) * jerkBoost * 2.4F + snapYaw * 0.9F;
            newPitch += (float) Math.signum(deltaPitch) * jerkBoost * 1.4F + snapPitch * 0.9F;
            if (Math.random() < 0.12) {
                newYaw += (Math.random() - 0.5) * 14.0F;
            }
        }

        float gcd = GCDFixer.getGCDValue();
        newYaw -= (newYaw - lastYaw) % gcd;
        newPitch -= (newPitch - lastPitch) % gcd;

        newPitch = Mth.clamp(newPitch, -90.0F, 90.0F);

        Rotation smoothRot = new Rotation(newYaw, newPitch);
        RotationComponent.update(smoothRot, 360, 360, 360, 360, 0, 1, clientLook.getValue());

        lastYaw = smoothRot.getYaw();
        lastPitch = smoothRot.getPitch();
    }

    private void updateAssistRotation(LivingEntity target) {
        if (target == null) return;

        boolean elytraDuel = mc.player.isFallFlying();

        if (!elytraDuel && System.currentTimeMillis() - lastPhysicalMoveTime > 100) {
            return;
        } else if (System.currentTimeMillis() - lastPhysicalMoveTime < 100) {
            this.lastYaw = mc.player.getYRot();
            this.lastPitch = mc.player.getXRot();
        }

        Vec3 point = resolveMultipoint(target, BestPoint.getPoint(target), 6);

        if (elytraDuel && target.isFallFlying() && predictEnabled() && !shouldBypassPredict()) {
            point = PredictUtils.predict(target, predictValue.getValue());
        }

        Vec3 eyePos = mc.player.getEyePosition();
        double deltaX = point.x - eyePos.x;
        double deltaY = point.y - eyePos.y;
        double deltaZ = point.z - eyePos.z;
        double d1 = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        float targetYaw = (float) (Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0);
        float targetPitch = (float) (-Math.toDegrees(Math.atan2(deltaY, d1)));

        float radius = assistRadius.getValue();
        AABB box = target.getBoundingBox();
        boolean isAimed = RaytraceUtil.rayTrace(mc.player.getViewVector(1.0F), 6, box.inflate(radius));
        float speed;
        if (isAimed) {
            speed = assistSpeedAimed.getValue() * 0.25F;
        } else if (mc.player.isFallFlying()) {
            speed = assistSpeedElytra.getValue() * 4.0F;
        } else if (mc.player.onGround()) {
            speed = assistSpeedGround.getValue() * 0.25F;
        } else {
            speed = assistSpeedAir.getValue() * 0.25F;
        }

        float cooldownMultiplier = (mc.player.getAttackStrengthScale(0.5F) > 0.85F) ? 1.2F : 0.4F;
        speed *= cooldownMultiplier;

        if (!RaytraceUtil.rayTrace(mc.player.getViewVector(1.0F), 6, box.inflate(radius + 0.1F))) {
            speedAcceleration += 0.0005F * cooldownMultiplier;
        } else if (speedAcceleration >= -0.01F) {
            speedAcceleration -= 0.0004F;
        }

        float smooth = Math.max(speedAcceleration, 0);
        speed += smooth + (float) ((Math.random() - 0.5) * 0.02);

        float yawDelta = Mth.wrapDegrees(targetYaw - lastYaw);
        float pitchDelta = targetPitch - lastPitch;

        float clampedYawDelta = Mth.clamp(yawDelta, -speed, speed);
        float clampedPitchDelta = Mth.clamp(pitchDelta, -speed, speed);

        float newYaw = lastYaw + clampedYawDelta;
        float newPitch = Mth.clamp(lastPitch + clampedPitchDelta, -89.9F, 89.9F);

        float gcd = GCDFixer.getGCDValue();
        if (gcd > 0.0F) {
            newYaw = lastYaw + (float) Math.round((newYaw - lastYaw) / gcd) * gcd;
            newPitch = lastPitch + (float) Math.round((newPitch - lastPitch) / gcd) * gcd;
        }

        Rotation smoothRot = new Rotation(newYaw, newPitch);
        RotationComponent.update(smoothRot, 360, 360, 360, 360, 0, 1, clientLook.getValue());

        this.lastYaw = smoothRot.getYaw();
        this.lastPitch = smoothRot.getPitch();
    }

    @Override
    public void onEnable() {
        target = null;
        lastTarget = null;
        ticksToAttack = 0;
        isSlowdownActive = false;
        razvorotikTicks = 0;
        speedAcceleration = 0;
        lastMouseYaw = Float.NaN;
        lastMousePitch = Float.NaN;
        slReset();

        if (mc.player != null) {
            lastYaw = mc.player.getYRot();
            lastPitch = mc.player.getXRot();
        }
    }

    @Override
    public void onDisable() {
        target = null;
        lastTarget = null;
        ticksToAttack = 0;
        isSlowdownActive = false;
        razvorotikTicks = 0;
        speedAcceleration = 0;
        slReset();
        RotationComponent.resetLastRotation();
    }
}
