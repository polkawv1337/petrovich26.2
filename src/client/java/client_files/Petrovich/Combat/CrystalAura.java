package client_files.Petrovich.Combat;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.CrystalDamageCalculator;
import client_files.ClientikUtils.DirectionUtil;
import client_files.ClientikUtils.Easing;
import client_files.ClientikUtils.Animation;
import client_files.ClientikUtils.InventoryUtil;
import client_files.ClientikUtils.ModeSetting;
import client_files.ClientikUtils.MultipointUtils;
import client_files.ClientikUtils.RaycastUtils;
import client_files.ClientikUtils.Rotation;
import client_files.ClientikUtils.RotationComponent;
import client_files.ClientikUtils.WorldUtils;
import client_files.Module;
import client_files.ModuleManager;
import client_files.Petrovich.Player.FriendHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import project.petrovich_26_2.mixin.client.MultiPlayerGameModeAccessor;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CrystalAura extends Module {

    public final BooleanSetting autoKrot = addSetting(new BooleanSetting("Делать подкоп", true));
    public final BooleanSetting digUnderPlayer = addSetting(new BooleanSetting("Подкоп под игроком", true));
    public final ModeSetting slotMode = addSetting(new ModeSetting("Слоты", "Пакетный", "Пакетный", "Обычный"));

    private static final int ROT_PRIORITY_PLACE = 999;
    private static final int ROT_PRIORITY_ATTACK = 1001;
    private static final int ROT_PRIORITY_EXPLODE = 1002;

    // обсидиан - морской синий, кристалл - розовый
    private static final float COLOR_OBSIDIAN_R = 0.13F;
    private static final float COLOR_OBSIDIAN_G = 0.55F;
    private static final float COLOR_OBSIDIAN_B = 0.95F;

    private static final float COLOR_CRYSTAL_R = 1.0F;
    private static final float COLOR_CRYSTAL_G = 0.36F;
    private static final float COLOR_CRYSTAL_B = 0.86F;

    private static final float COLOR_BREAK_R = 1.0F;
    private static final float COLOR_BREAK_G = 0.62F;
    private static final float COLOR_BREAK_B = 0.22F;

    private static final float COLOR_EXPLOSION_R = 1.0F;
    private static final float COLOR_EXPLOSION_G = 0.24F;
    private static final float COLOR_EXPLOSION_B = 0.22F;

    private static java.util.UUID webTrappedTarget;

    public static void setWebTrappedTarget(java.util.UUID uuid) {
        webTrappedTarget = uuid;
    }

    public static void clearWebTrappedTarget(java.util.UUID uuid) {
        if (uuid == null || uuid.equals(webTrappedTarget)) webTrappedTarget = null;
    }

    private static BlockPos lastPlacedCrystalPos;
    private static int lastPlacedCrystalTicks;

    public static boolean isRecentlyPlacedCrystal(BlockPos pos) {
        return lastPlacedCrystalTicks > 0 && lastPlacedCrystalPos != null && pos.equals(lastPlacedCrystalPos);
    }

    private int ticksToBack;
    private int lastSlot = -1;
    private boolean breaking;
    private int actionDelay;
    private int breakCooldown;

    private PlaceData lastData;
    private LivingEntity currentTarget;
    private final ConcurrentLinkedQueue<VisualData> visualDatas = new ConcurrentLinkedQueue<>();

    private static float sentYaw = Float.NaN;
    private static float sentPitch = Float.NaN;

    private BlockPos plannedPos;
    private float plannedR;
    private float plannedG;
    private float plannedB;

    private Rotation rotation;

    public CrystalAura() {
        super("CrystalAura", "Авто бабах мамы Likersyu1 (НОВЕЙШАЯ РАЗРАБОТКА 21 ВЕКА)", Category.COMBAT);
    }

    public PlaceData getLastData() {
        return lastData;
    }

    public ConcurrentLinkedQueue<VisualData> getVisualDatas() {
        return visualDatas;
    }

    public Rotation getRotation() {
        return rotation;
    }

    /** Текущая цель-игрок (для синхронизации с AutoWeb). */
    public LivingEntity getSyncedPlayerTarget() {
        return currentTarget;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        ticksToBack = 0;
        actionDelay = 0;
        breakCooldown = 0;
        rotation = null;
        sentYaw = Float.NaN;
        sentPitch = Float.NaN;
        lastData = null;
        webTrappedTarget = null;
        lastPlacedCrystalPos = null;
        lastPlacedCrystalTicks = 0;
        restoreTool();
    }

    private void restoreTool() {
        breaking = false;
        if (lastSlot != -1 && mc.player != null) {
            syncSlot(lastSlot);
        }
        lastSlot = -1;
    }

    private boolean packetSlots() {
        return "Пакетный".equals(slotMode.getValue());
    }

    private void syncSlot(int slot) {
        mc.player.getInventory().setSelectedSlot(slot);
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        }
    }

    /**
     * Выполняет действие с нужным предметом в руке. В режиме "Пакетный" слот
     * возвращается в том же тике, поэтому хотбар визуально не дёргается.
     */
    private void withSlot(int slot, Runnable action) {
        if (slot < 0 || mc.player == null) {
            action.run();
            return;
        }
        var inventory = mc.player.getInventory();
        int previous = inventory.getSelectedSlot();
        if (previous == slot) {
            action.run();
            return;
        }
        if (lastSlot == -1) lastSlot = previous;
        inventory.setSelectedSlot(slot);
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        }
        try {
            action.run();
        } finally {
            if (packetSlots()) {
                inventory.setSelectedSlot(previous);
                if (mc.getConnection() != null) {
                    mc.getConnection().send(new ServerboundSetCarriedItemPacket(previous));
                }
                lastSlot = -1;
            }
        }
    }

    private void sendRotation(Rotation rot, int priority) {
        rotation = rot;
        updateRotation(rot, priority);
    }

    /**
     * Общая ротация для CrystalAura и AutoWeb: пакет уходит только при реальном
     * изменении угла, поэтому модули не дёргают ротацию друг у друга и не флагуют.
     */
    public static void updateRotation(Rotation rot, int priority) {
        if (rot == null) return;
        if (!Float.isNaN(sentYaw)
                && Math.abs(Mth.wrapDegrees(rot.getYaw() - sentYaw)) < 0.05F
                && Math.abs(rot.getPitch() - sentPitch) < 0.05F) {
            return;
        }
        sentYaw = rot.getYaw();
        sentPitch = rot.getPitch();
        RotationComponent.update(rot, 360, 360, 360, 360, 10, priority, false);
    }

    /** true, если сейчас ротацией владеет CrystalAura (AutoWeb не должен её дублировать). */
    public static boolean rotationOwnedByAura() {
        if (!ModuleManager.isReady()) return false;
        CrystalAura module = ModuleManager.getInstance().get(CrystalAura.class);
        return module != null && module.isEnabled() && module.rotation != null;
    }

    /**
     * Временно берёт предмет в руку, выполняет действие и в режиме "Пакетный"
     * возвращает слот в том же тике - хотбар визуально не прыгает.
     */
    public static void useSlotTemporary(int slot, Runnable action) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            action.run();
            return;
        }
        var inventory = mc.player.getInventory();
        int previous = inventory.getSelectedSlot();
        if (slot < 0 || previous == slot) {
            action.run();
            return;
        }
        boolean packetMode = !ModuleManager.isReady() || packetSlotsEnabled();
        inventory.setSelectedSlot(slot);
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
        }
        try {
            action.run();
        } finally {
            if (packetMode) {
                inventory.setSelectedSlot(previous);
                if (mc.getConnection() != null) {
                    mc.getConnection().send(new ServerboundSetCarriedItemPacket(previous));
                }
            }
        }
    }

    private static boolean packetSlotsEnabled() {
        if (!ModuleManager.isReady()) return true;
        CrystalAura module = ModuleManager.getInstance().get(CrystalAura.class);
        return module == null || "Пакетный".equals(module.slotMode.getValue());
    }

    public static boolean moveFixActive() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return false;
        CrystalAura module = ModuleManager.getInstance().get(CrystalAura.class);
        if (module == null || !module.isEnabled()) return false;
        // правим ввод только пока реально шлём спуфнутую ротацию
        return module.rotation != null;
    }

    /**
     * Правка ввода под серверную ротацию. Ввод НЕ трогаем в воздухе, на элитрах,
     * при падении и без стрейфа - иначе сервер видит "ходьбу не туда" и флагует.
     */
    public static RotationComponent.Vec2Tuple fixInput(float forward, float strafe) {
        RotationComponent.Vec2Tuple unchanged = new RotationComponent.Vec2Tuple(forward, strafe);
        if (forward == 0 && strafe == 0) return unchanged;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return unchanged;
        if (!mc.player.onGround() || mc.player.isFallFlying() || mc.player.isUsingItem()) return unchanged;
        if (strafe == 0) return unchanged;

        if (!ModuleManager.isReady()) return unchanged;
        CrystalAura module = ModuleManager.getInstance().get(CrystalAura.class);
        if (module == null || !module.isEnabled()) return unchanged;

        // сервер считает направление по спуфнутой ротации - выравниваем ввод по ней
        Rotation rot = module.rotation;
        float yaw = rot != null ? rot.getYaw() : Mth.wrapDegrees(mc.gameRenderer.mainCamera().yRot());
        return RotationComponent.fixMovement(forward, strafe, yaw);
    }

    private boolean shouldFastClick() {
        if (mc.level == null || mc.player == null) return false;

        if (webTrappedTarget != null) {
            for (Entity entity : getEntitiesIterable()) {
                if (!(entity instanceof Player p)) continue;
                if (!p.getUUID().equals(webTrappedTarget)) continue;
                if (!p.isAlive()) continue;
                if (mc.player.distanceToSqr(p) <= 25.0) return true;
            }
        }

        for (Entity entity : getEntitiesIterable()) {
            if (!(entity instanceof Player p)) continue;
            if (p == mc.player) continue;
            if (!p.isAlive()) continue;
            if (FriendHelper.isFriend(p.getScoreboardName())) continue;
            if (mc.player.distanceToSqr(p) > 100.0) continue;
            if (p.getHealth() <= 6.0F) return true;
        }

        return mc.player.getHealth() <= 6.0F;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.player.isUsingItem()) return;
        plannedPos = null;

        if (ticksToBack > 0) {
            ticksToBack--;
        }

        if (actionDelay > 0) {
            actionDelay--;
        }

        if (breakCooldown > 0) {
            breakCooldown--;
        }

        if (lastPlacedCrystalTicks > 0) {
            lastPlacedCrystalTicks--;
        }

        if (actionDelay > 0) {
            return;
        }

        if (ticksToBack == 0 && lastSlot != -1 && !breaking) {
            mc.player.getInventory().setSelectedSlot(lastSlot);
            lastSlot = -1;
        }

        LivingEntity target = findTarget();
        currentTarget = target;

        int obsSlot = InventoryUtil.searchItemHotbar(Items.OBSIDIAN);
        int crysSlot = InventoryUtil.searchItemHotbar(Items.END_CRYSTAL);

        if (obsSlot == -1 || crysSlot == -1) {
            lastData = null;
            rotation = null;
            return;
        }

        final var self = mc.player;

        if (target != null) {
            final Player playerTarget = (Player) target;
            final var world = mc.level;
            final List<Entity> entities = getEntitiesIterable();

            PlaceData obsidianData = findBestBlockToPlaceObsidianTheBiggestChinaWall(playerTarget, world, self, entities);
            PlaceData crystalData = findBestBlockToPlaceCrystalTheBiggestChinaWall(playerTarget, world, self, entities);
            PlaceData breakData = findBestBlockToBreakTheBiggestChinaWall(playerTarget, self, entities);

            PlaceData finalData = obsidianData;
            if (crystalData.damage() > finalData.damage()) finalData = crystalData;
            if (breakData.damage() > finalData.damage() && autoKrot.getValue() && breakCooldown == 0) finalData = breakData;
            if (obsidianData.placeResult == PlaceResult.AWAITING) finalData = obsidianData;

            if (lastData == null || lastData.placeResult != PlaceResult.EXPLOSION) lastData = finalData;
        }

        if (lastData == null || lastData.placeResult == PlaceResult.AWAITING) {
            restoreTool();
            rotation = null;
            plannedPos = null;
            return;
        }

        if (lastData.damage() == 0) {
            restoreTool();
            lastData = null;
            rotation = null;
            plannedPos = null;
            return;
        }

        plannedPos = lastData.blockPos();
        switch (lastData.placeResult()) {
            case PLACE_OBSIDIAN -> {
                plannedPos = lastData.blockPos().relative(lastData.face());
                plannedR = COLOR_OBSIDIAN_R; plannedG = COLOR_OBSIDIAN_G; plannedB = COLOR_OBSIDIAN_B;
            }
            case PLACE_CRYSTAL -> {
                plannedPos = lastData.blockPos().above();
                plannedR = COLOR_CRYSTAL_R; plannedG = COLOR_CRYSTAL_G; plannedB = COLOR_CRYSTAL_B;
            }
            case BREAK -> {
                plannedPos = lastData.blockPos();
                plannedR = COLOR_BREAK_R; plannedG = COLOR_BREAK_G; plannedB = COLOR_BREAK_B;
            }
            case EXPLOSION -> {
                if (lastData.entity() != null) plannedPos = lastData.entity().blockPosition();
                plannedR = COLOR_EXPLOSION_R; plannedG = COLOR_EXPLOSION_G; plannedB = COLOR_EXPLOSION_B;
            }
            default -> {
                plannedR = 1.0F; plannedG = 1.0F; plannedB = 1.0F;
            }
        }

        if (lastData.placeResult() == PlaceResult.EXPLOSION) {
            double selfExpDmg = CrystalDamageCalculator.getExplosionDamage(Vec3.atCenterOf(lastData.entity().blockPosition()), self, false);
            double distSq = self.distanceToSqr(lastData.entity());
            if (selfExpDmg > 30 || distSq < 4) {
                lastData = null;
                rotation = null;
                return;
            }

            BlockPos blockPos = lastData.blockPos();
            Direction direction = lastData.face();

            if (blockPos != null && direction != null) {
                Rotation rot = new Rotation(mc.player.getEyePosition(), WorldUtils.getFacePoint(blockPos, direction));

                if (RaycastUtils.raycastBlock(rot, blockPos, 5.5F) != null && RaycastUtils.rayTrace(rot, 3, lastData.entity().getBoundingBox())) {
                    // ротация уходит ДО удара, иначе сервер валидирует удар по старому yaw
                    sendRotation(rot, ROT_PRIORITY_EXPLODE);
                    mc.gameMode.attack(self, lastData.entity());
                    self.swing(InteractionHand.MAIN_HAND);
                    actionDelay = shouldFastClick() ? 0 : 3;
                    breakCooldown = 0;
                    lastData = null;
                    rotation = null;
                    return;
                }
            }

            Rotation rotCrystal = new Rotation(mc.player.getEyePosition(), MultipointUtils.getNearestPoint(lastData.entity(), 0, 0.05F));

            if (RaycastUtils.isLookingAtEntity(rotCrystal, lastData.entity(), 3)) {
                sendRotation(rotCrystal, ROT_PRIORITY_ATTACK);
                mc.gameMode.attack(self, lastData.entity());
                self.swing(InteractionHand.MAIN_HAND);
                actionDelay = shouldFastClick() ? 0 : 3;
                breakCooldown = 0;
            }

            restoreTool();
            lastData = null;
            rotation = null;
            return;
        }

        BlockPos blockPos = lastData.blockPos();
        if (blockPos == null) {
            restoreTool();
            lastData = null;
            rotation = null;
            return;
        }

        Direction direction = lastData.face();
        if (direction == null) {
            restoreTool();
            lastData = null;
            rotation = null;
            return;
        }

        Rotation rot = new Rotation(mc.player.getEyePosition(), WorldUtils.getFacePoint(blockPos, direction));

        if (RaycastUtils.raycastBlock(rot, blockPos, 5.5F) != null) {
            if (lastData.placeResult == PlaceResult.BREAK) {
                int toolSlot = findBestToolSlot(blockPos);
                if (toolSlot == -1) {
                    restoreTool();
                    lastData = null;
                    rotation = null;
                    return;
                }
                if (lastSlot == -1) lastSlot = self.getInventory().getSelectedSlot();
                breaking = true;
                if (self.getInventory().getSelectedSlot() != toolSlot) {
                    syncSlot(toolSlot);
                }
                if (!mc.level.getBlockState(blockPos).isAir()) {
                    if (getDestroyDelay() <= 1) {
                        mc.gameMode.startDestroyBlock(blockPos, direction);
                        mc.player.swing(InteractionHand.MAIN_HAND);
                    }
                    if (!mc.level.getBlockState(blockPos).isAir()) {
                        if (mc.gameMode.continueDestroyBlock(blockPos, direction)) {
                            mc.player.swing(InteractionHand.MAIN_HAND);
                        }
                    }
                } else {
                    mc.gameMode.stopDestroyBlock();
                    restoreTool();
                    breakCooldown = 40;
                    lastData = null;
                    rotation = null;
                    return;
                }
                if (!hasVisual(blockPos)) {
                    var visualData = new VisualData(blockPos, COLOR_OBSIDIAN_R, COLOR_OBSIDIAN_G, COLOR_OBSIDIAN_B);
                    visualData.animation.setValue(1);
                    visualDatas.add(visualData);
                }
            } else {
                breaking = false;
                int slot = InventoryUtil.searchItemHotbar(Items.OBSIDIAN);
                if (lastData.placeResult() == PlaceResult.PLACE_CRYSTAL)
                    slot = InventoryUtil.searchItemHotbar(Items.END_CRYSTAL);

                if (slot != -1) {
                    final BlockPos placeBlock = blockPos;
                    final Direction placeFace = direction;
                    final Rotation placeRot = rot;
                    // ротация ДО useItemOn: сервер проверяет установку блока по yaw
                    sendRotation(placeRot, ROT_PRIORITY_PLACE);
                    ticksToBack = 3;
                    withSlot(slot, () -> {
                        mc.gameMode.useItemOn(self, InteractionHand.MAIN_HAND, new BlockHitResult(
                                Vec3.atCenterOf(placeBlock),
                                DirectionUtil.getFaceDirection(placeRot, self, placeBlock),
                                placeBlock,
                                false
                        ));
                        self.swing(InteractionHand.MAIN_HAND);
                    });
                    if (lastData.placeResult() == PlaceResult.PLACE_CRYSTAL) {
                        lastPlacedCrystalPos = placeBlock;
                        lastPlacedCrystalTicks = 15;
                    }
                    if (!hasVisual(placeBlock)) {
                        VisualData visualData = new VisualData(placeBlock.relative(placeFace), COLOR_CRYSTAL_R, COLOR_CRYSTAL_G, COLOR_CRYSTAL_B);
                        if (lastData.placeResult.equals(PlaceResult.PLACE_CRYSTAL))
                            visualData = new VisualData(placeBlock.above(), COLOR_CRYSTAL_R, COLOR_CRYSTAL_G, COLOR_CRYSTAL_B);
                        visualData.animation.setValue(1);
                        visualDatas.add(visualData);
                    }
                    lastData = null;
                    actionDelay = shouldFastClick() ? 0 : 2;
                }
            }

            sendRotation(rot, ROT_PRIORITY_PLACE);
        } else {
            rotation = null;
            lastData = null;
        }
    }

    private int getDestroyDelay() {
        if (!(mc.gameMode instanceof MultiPlayerGameMode)) return 0;
        return ((MultiPlayerGameModeAccessor) (Object) mc.gameMode).getDestroyDelay();
    }

    private List<Entity> getEntitiesIterable() {
        return mc.level.getEntities(EntityTypeTest.forClass(Entity.class),
                mc.player.getBoundingBox().inflate(64.0), (e) -> true);
    }

    private LivingEntity findTarget() {
        if (mc.level == null) return null;

        if (webTrappedTarget != null) {
            for (Entity entity : getEntitiesIterable()) {
                if (!(entity instanceof Player p)) continue;
                if (!p.getUUID().equals(webTrappedTarget)) continue;
                if (!p.isAlive() || FriendHelper.isFriend(p.getScoreboardName())) {
                    webTrappedTarget = null;
                    break;
                }
                if (mc.player.distanceToSqr(p) <= 64.0) return p;
                webTrappedTarget = null;
                break;
            }
        }

        LivingEntity best = null;
        double bestDist = 8.0 * 8.0;

        for (Entity entity : getEntitiesIterable()) {
            if (!(entity instanceof Player player)) continue;
            if (player == mc.player) continue;
            if (!player.isAlive()) continue;
            if (FriendHelper.isFriend(player.getScoreboardName())) continue;

            double dist = mc.player.distanceToSqr(player);
            if (dist > bestDist) continue;

            bestDist = dist;
            best = player;
        }

        return best;
    }

    private PlaceData findBestBlockToPlaceObsidianTheBiggestChinaWall(
            Player entity,
            Level world,
            Player self,
            List<Entity> entities
    ) {
        BlockPos bestPanPos = null;
        double bestPanDamage = 0;
        Direction bestFace = null;

        BlockPos basePos = self.blockPosition();
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

        for (int x = -5; x <= 5; x++) {
            int px = basePos.getX() + x;

            for (int y = -5; y <= 5; y++) {
                int py = basePos.getY() + y;

                for (int z = -5; z <= 5; z++) {
                    checkPos.set(px, py, basePos.getZ() + z);

                    BlockState state = world.getBlockState(checkPos);
                    if (!state.getBlock().defaultBlockState().isSolid()) continue;

                    Direction face = WorldUtils.getBestVisibleFaceToObsidian(checkPos, self);
                    if (face == null) continue;

                    Vec3 rotatePoint = WorldUtils.getFacePoint(checkPos, face);
                    Rotation rot = new Rotation(mc.player.getEyePosition(), rotatePoint);

                    var raycast = RaycastUtils.raycastBlock(rot, checkPos, 5.5F);

                    if (raycast == null || raycast.getDirection() != face) continue;

                    BlockPos placePos = checkPos.relative(face);

                    if (placePos.getY() > (int) (entity.getY())) continue;

                    BlockPos crystalPos = placePos.above();

                    if (!world.getBlockState(placePos).isAir()) continue;

                    if (isBlockedByEntitiesForObsidian(entities, placePos, entity).equals(ReturnResult.PLAYER)) continue;
                    if (isBlockedByEntitiesForObsidian(entities, crystalPos, entity).equals(ReturnResult.PLAYER)) continue;

                    if (!RaycastUtils.canReachBox(
                            new AABB(
                                    placePos.getX() - 0.5,
                                    placePos.getY(),
                                    placePos.getZ() - 0.5,
                                    placePos.getX() + 2,
                                    placePos.getY() + 1.5,
                                    placePos.getZ() + 1.5
                            ),
                            3
                    )) continue;

                    double damage = CrystalDamageCalculator.getDamageOfGhostBlock(Vec3.atCenterOf(crystalPos), entity, placePos) + 19 - 6;

                    if (damage <= bestPanDamage || damage < 25) continue;

                    double selfDamage = CrystalDamageCalculator.getDamageOfGhostBlock(Vec3.atCenterOf(crystalPos), self, placePos);

                    if (selfDamage > 30) continue;

                    if (isBlockedByEntitiesForObsidian(entities, crystalPos, entity).equals(ReturnResult.ITEM))
                        return new PlaceData(null, null, 0, PlaceResult.AWAITING, null);

                    bestPanDamage = damage;
                    bestPanPos = new BlockPos(checkPos.getX(), checkPos.getY(), checkPos.getZ());
                    bestFace = face;
                }
            }
        }

        BlockPos bestCrystalPos = null;
        Direction bestCrystalFace = null;
        EndCrystal bestCrystal = null;
        double bestCrystalDamage = 0;

        for (Entity entity1 : entities) {
            if (!(entity1 instanceof EndCrystal crystal)) continue;

            var rot = new Rotation(mc.player.getEyePosition(), MultipointUtils.getNearestPoint(crystal, 0, 0.05F));

            double damage = CrystalDamageCalculator.getExplosionDamage(Vec3.atCenterOf(crystal.blockPosition()), entity, false) + 30;
            if (damage <= bestCrystalDamage) continue;

            double selfDamage = CrystalDamageCalculator.getExplosionDamage(Vec3.atCenterOf(crystal.blockPosition()), self, false);
            if (selfDamage > 30) continue;

            BlockPos pos = crystal.blockPosition().below();

            Direction face = Direction.UP;

            Vec3 rotatePoint = WorldUtils.getFacePoint(pos, face);
            Rotation rotBlock = new Rotation(mc.player.getEyePosition(), rotatePoint);

            var raycast = RaycastUtils.raycastBlock(rotBlock, pos, 5.5F);

            if (raycast != null && raycast.getDirection() == face && RaycastUtils.rayTrace(rotBlock, 3, crystal.getBoundingBox())) {
                bestCrystalPos = pos;
                bestCrystalFace = face;
            }

            if (!RaycastUtils.isLookingAtEntity(
                    rot,
                    crystal,
                    3
            ) && bestCrystalPos == null) continue;

            bestCrystalDamage = damage;
            bestCrystal = crystal;
        }

        if (bestCrystalDamage > bestPanDamage) {
            return new PlaceData(bestCrystalPos, bestCrystalFace, bestCrystalDamage, PlaceResult.EXPLOSION, bestCrystal);
        }

        return new PlaceData(bestPanPos, bestFace, bestPanDamage, PlaceResult.PLACE_OBSIDIAN, null);
    }

    private PlaceData findBestBlockToPlaceCrystalTheBiggestChinaWall(
            Player entity,
            Level world,
            Player self,
            List<Entity> entities
    ) {
        BlockPos bestPanPos = null;
        double bestPanDamage = 0;
        Direction bestFace = null;

        BlockPos basePos = self.blockPosition();
        BlockPos targetPos = entity.blockPosition();
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

        int minX = Math.min(basePos.getX(), targetPos.getX()) - 5;
        int maxX = Math.max(basePos.getX(), targetPos.getX()) + 5;
        int minY = Math.min(basePos.getY(), targetPos.getY()) - 5;
        int maxY = Math.max(basePos.getY(), targetPos.getY()) + 5;
        int minZ = Math.min(basePos.getZ(), targetPos.getZ()) - 5;
        int maxZ = Math.max(basePos.getZ(), targetPos.getZ()) + 5;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    checkPos.set(x, y, z);

                    BlockState state = world.getBlockState(checkPos);
                    if (state.getBlock() != Blocks.OBSIDIAN && state.getBlock() != Blocks.BEDROCK) continue;

                    Direction face = WorldUtils.getBestVisibleFace(checkPos, self);
                    if (face == null) continue;

                    BlockPos placePos = checkPos.above();

                    Vec3 rotatePoint = WorldUtils.getFacePoint(checkPos, face);
                    Rotation rot = new Rotation(mc.player.getEyePosition(), rotatePoint);

                    var raycast = RaycastUtils.raycastBlock(rot, checkPos, 5.5F);

                    if (raycast == null || raycast.getDirection() != face) continue;

                    Vec3 rotatePoint2 = WorldUtils.getFacePoint(checkPos, Direction.UP);
                    Rotation rot2 = new Rotation(mc.player.getEyePosition(), rotatePoint2);

                    var raycast2 = RaycastUtils.raycastBlock(rot2, checkPos, 5.5F);

                    if (raycast2 != null && raycast2.getDirection() == Direction.UP) face = Direction.UP;

                    if (isBlockedByEntitiesForCrystal(entities, placePos, entity)) continue;

                    if (!RaycastUtils.canReachBox(
                            new AABB(
                                    placePos.getX() - 0.5,
                                    placePos.getY(),
                                    placePos.getZ() - 0.5,
                                    placePos.getX() + 2,
                                    placePos.getY() + 1.5,
                                    placePos.getZ() + 1.5
                            ),
                            3
                    )) continue;

                    double damage = CrystalDamageCalculator.getExplosionDamage(Vec3.atCenterOf(placePos), entity, false) + 19;
                    if (damage <= bestPanDamage || damage < 31) continue;

                    double selfDamage = CrystalDamageCalculator.getExplosionDamage(Vec3.atCenterOf(placePos), self, false);
                    if (selfDamage > 30) continue;

                    bestPanDamage = damage;
                    bestPanPos = new BlockPos(checkPos.getX(), checkPos.getY(), checkPos.getZ());
                    bestFace = face;
                }
            }
        }

        return new PlaceData(bestPanPos, bestFace, bestPanDamage, PlaceResult.PLACE_CRYSTAL, null);
    }

    private PlaceData findBestBlockToBreakTheBiggestChinaWall(
            Player entity,
            Player self,
            List<Entity> entities
    ) {
        BlockPos bestPanPos = null;
        double bestPanDamage = 0;
        Direction bestFace = null;
        BlockPos bestPanPosBreak = null;
        double bestPanDamageBreak = 0;
        Direction bestFaceBreak = null;

        BlockPos basePos = self.blockPosition();
        BlockPos targetPos = entity.blockPosition();
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos();

        int minX = Math.min(basePos.getX(), targetPos.getX()) - 5;
        int maxX = Math.max(basePos.getX(), targetPos.getX()) + 5;
        int minY = Math.min(basePos.getY(), targetPos.getY()) - 5;
        int maxY = Math.max(basePos.getY(), targetPos.getY()) + 5;
        int minZ = Math.min(basePos.getZ(), targetPos.getZ()) - 5;
        int maxZ = Math.max(basePos.getZ(), targetPos.getZ()) + 5;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    checkPos.set(x, y, z);

                    if (!mc.level.getBlockState(checkPos).getBlock().defaultBlockState().isSolid()) continue;

                    Direction face = WorldUtils.getBestVisibleFace(checkPos, self);
                    if (face == null) continue;

                    Vec3 rotatePoint = WorldUtils.getFacePoint(checkPos, face);
                    Rotation rot = new Rotation(mc.player.getEyePosition(), rotatePoint);

                    var raycast = RaycastUtils.raycastBlock(rot, checkPos, 5.5F);

                    if (raycast == null || raycast.getDirection() != face) continue;

                    BlockPos placePos = checkPos.above();

                    if (isBlockedByEntitiesForBreak(entities, placePos)) continue;

                    if (!RaycastUtils.canReachBox(
                            new AABB(
                                    placePos.getX() - 0.5,
                                    placePos.getY(),
                                    placePos.getZ() - 0.5,
                                    placePos.getX() + 2,
                                    placePos.getY() + 1.5,
                                    placePos.getZ() + 1.5
                            ),
                            3
                    )) continue;

                    if (!mc.level.getBlockState(placePos).isAir()) {
                        int bestSlot = -1;
                        int bestSlot2 = -1;
                        {
                            BlockPos pos = placePos;
                            BlockState state = mc.level.getBlockState(pos);

                            float bestSpeed = 1.0f;

                            for (int slot = 0; slot < 36; slot++) {
                                ItemStack stack = mc.player.getInventory().getItem(slot);
                                float speed = stack.getDestroySpeed(state);

                                if (speed > bestSpeed) {
                                    bestSpeed = speed;
                                    bestSlot = slot;
                                }
                            }
                        }

                        {
                            BlockPos pos = checkPos;
                            BlockState state = mc.level.getBlockState(pos);

                            float bestSpeed = 1.0f;

                            for (int slot = 0; slot < 36; slot++) {
                                ItemStack stack = mc.player.getInventory().getItem(slot);
                                float speed = stack.getDestroySpeed(state);

                                if (speed > bestSpeed) {
                                    bestSpeed = speed;
                                    bestSlot2 = slot;
                                }
                            }
                        }

                        if (bestSlot == -1 || (mc.level.getBlockState(checkPos).getBlock() != Blocks.OBSIDIAN && bestSlot2 == -1)) continue;

                        double damage = CrystalDamageCalculator.getDamageIgnoringBlock(Vec3.atCenterOf(placePos), entity, placePos) + 19 - 12;
                        damage += digUnderPlayerBonus(placePos, entity);

                        if (damage <= bestPanDamageBreak || damage < 19) continue;

                        double selfDamage = CrystalDamageCalculator.getDamageIgnoringBlock(Vec3.atCenterOf(placePos), self, placePos);
                        if (selfDamage > 30) continue;

                        bestPanDamageBreak = damage;
                        bestPanPosBreak = new BlockPos(placePos.getX(), placePos.getY(), placePos.getZ());
                        bestFaceBreak = face;
                        continue;
                    }

                    BlockPos pos = checkPos;
                    BlockState state = mc.level.getBlockState(pos);

                    int bestSlot = -1;
                    float bestSpeed = 1.0f;

                    for (int slot = 0; slot < 36; slot++) {
                        ItemStack stack = mc.player.getInventory().getItem(slot);
                        float speed = stack.getDestroySpeed(state);

                        if (speed > bestSpeed) {
                            bestSpeed = speed;
                            bestSlot = slot;
                        }
                    }

                    if (bestSlot == -1) continue;

                    double damage = CrystalDamageCalculator.getExplosionDamage(Vec3.atCenterOf(placePos), entity, false) + 19 - 12;
                    damage += digUnderPlayerBonus(placePos, entity);

                    if (damage <= bestPanDamage || damage < 19) continue;

                    double selfDamage = CrystalDamageCalculator.getExplosionDamage(Vec3.atCenterOf(placePos), self, false);
                    if (selfDamage > 30) continue;

                    bestPanDamage = damage;
                    bestPanPos = new BlockPos(checkPos.getX(), checkPos.getY(), checkPos.getZ());
                    bestFace = face;
                }
            }
        }

        if (bestPanDamageBreak > bestPanDamage)
            return new PlaceData(bestPanPosBreak, bestFaceBreak, bestPanDamageBreak, PlaceResult.BREAK, null);
        else return new PlaceData(bestPanPos, bestFace, bestPanDamage, PlaceResult.BREAK, null);
    }

    /**
     * Бонус к урону за копку прямо под ногами цели: так подкоп реально пригодится,
     * а не просто выбирает первый попавшийся блок в общем кубе.
     */
    private double digUnderPlayerBonus(BlockPos placePos, Player entity) {
        if (!digUnderPlayer.getValue()) return 0.0;

        double dx = placePos.getX() + 0.5 - entity.getX();
        double dz = placePos.getZ() + 0.5 - entity.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        int feetY = (int) Math.floor(entity.getY());
        int dy = placePos.getY() - feetY;

        double bonus = 0.0;
        if (horizontal <= 1.6) {
            bonus += 6.0;
            if (dy <= 0) bonus += 4.0;
            else if (dy == 1) bonus += 1.5;
        } else if (horizontal <= 2.6) {
            bonus += 2.5;
            if (dy <= 0) bonus += 1.0;
        }
        return bonus;
    }

    private int findBestToolSlot(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);

        int bestSlot = -1;
        float bestSpeed = 1.0f;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            float speed = stack.getDestroySpeed(state);

            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = slot;
            }
        }

        return bestSlot;
    }

    private ReturnResult isBlockedByEntitiesForObsidian(List<Entity> entities, BlockPos crystalPos, Entity ignoreEntity) {
        for (Entity entity : entities) {
            if (entity == ignoreEntity) continue;
            AABB blockBox = new AABB(
                    crystalPos.getX(), crystalPos.getY() - 0.2F, crystalPos.getZ(),
                    crystalPos.getX() + 1.0, crystalPos.getY() + 2.2, crystalPos.getZ() + 1.0
            );
            if (entity instanceof ItemEntity && !entity.onGround() && entity.getBoundingBox().intersects(blockBox)) {
                return ReturnResult.ITEM;
            }
            if ((entity instanceof Player || entity instanceof EndCrystal) && CrystalDamageCalculator.intersectsEntityBox(crystalPos, entity)) {
                return ReturnResult.PLAYER;
            }
        }
        return ReturnResult.NONE;
    }

    private boolean isBlockedByEntitiesForCrystal(List<Entity> entities, BlockPos placePos, Entity ignoreEntity) {
        for (Entity entity : entities) {
            if (entity == ignoreEntity) continue;
            if (CrystalDamageCalculator.intersectsEntityBox(placePos, entity)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlockedByEntitiesForBreak(List<Entity> entities, BlockPos placePos) {
        for (Entity entity : entities) {
            if (CrystalDamageCalculator.intersectsEntityBox(placePos, entity) && !(entity instanceof ItemEntity)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasVisual(BlockPos blockPos) {
        for (VisualData visualData : visualDatas) {
            if (visualData.blockPos.equals(blockPos)) {
                return true;
            }
        }
        return false;
    }

    public static void renderWorld(PoseStack poseStack, SubmitNodeCollector collector) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        CrystalAura module = ModuleManager.getInstance().get(CrystalAura.class);
        if (module == null || !module.isEnabled()) return;
        if (module.visualDatas.isEmpty() && module.plannedPos == null) return;

        Iterator<VisualData> it = module.visualDatas.iterator();
        while (it.hasNext()) {
            VisualData vd = it.next();

            float anim = (float) vd.getAnimation().getValue();
            if (anim <= 0.01f) {
                it.remove();
                continue;
            }

            vd.getAnimation().run(0);

            BlockPos pos = vd.getBlockPos();
            AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

            float age = (System.currentTimeMillis() - vd.getCreatedMs()) / 1000f;
            float pulse = 0.5f + 0.5f * (float) Math.sin(age * 4.0f);
            float alpha = (0.15f + 0.10f * pulse) * anim;

            drawFill(poseStack, collector, box, vd.getR(), vd.getG(), vd.getB(), alpha);
        }

        BlockPos planned = module.plannedPos;
        if (planned != null) {
            AABB box = new AABB(planned.getX(), planned.getY(), planned.getZ(),
                    planned.getX() + 1, planned.getY() + 1, planned.getZ() + 1);
            float pulse = 0.5f + 0.5f * (float) Math.sin((System.currentTimeMillis() / 1000.0f) * 2.4f);
            float alpha = 0.15f + 0.10f * pulse;
            // "обводка" - тонкий слой заливки вокруг блока (RenderTypes.lines() в 26.2
            // требует дополнительный элемент вершины и роняет буфер, поэтому рисуем
            // тем же проверенным debugFilledBox)
            float e = 0.022f;
            AABB shell = new AABB(box.minX - e, box.minY - e, box.minZ - e,
                    box.maxX + e, box.maxY + e, box.maxZ + e);
            drawFill(poseStack, collector, shell, module.plannedR, module.plannedG, module.plannedB,
                    alpha * 0.45f + 0.12f);
            drawFill(poseStack, collector, box, module.plannedR, module.plannedG, module.plannedB, alpha);
        }
    }

    /** Заливка с вертикальным градиентом: снизу темнее, сверху светлее. */
    private static void drawFill(PoseStack poseStack, SubmitNodeCollector collector, AABB box,
                                 float r, float g, float b, float a) {
        if (a <= 0.01f) return;
        collector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(),
                (pose, buffer) -> renderFillBox(pose, buffer, box, r, g, b, a));
    }

    private static void renderFillBox(PoseStack.Pose pose, VertexConsumer buffer, AABB box,
                                      float r, float g, float b, float a) {
        float minX = (float) box.minX;
        float minY = (float) box.minY;
        float minZ = (float) box.minZ;
        float maxX = (float) box.maxX;
        float maxY = (float) box.maxY;
        float maxZ = (float) box.maxZ;

        float br = r * 0.42f;
        float bg = g * 0.42f;
        float bb = b * 0.42f;

        vertexColor(pose, buffer, minX, minY, minZ, br, bg, bb, a);
        vertexColor(pose, buffer, maxX, minY, minZ, br, bg, bb, a);
        vertexColor(pose, buffer, maxX, minY, maxZ, br, bg, bb, a);
        vertexColor(pose, buffer, minX, minY, maxZ, br, bg, bb, a);

        vertexColor(pose, buffer, minX, maxY, minZ, r, g, b, a);
        vertexColor(pose, buffer, maxX, maxY, minZ, r, g, b, a);
        vertexColor(pose, buffer, maxX, maxY, maxZ, r, g, b, a);
        vertexColor(pose, buffer, minX, maxY, maxZ, r, g, b, a);

        side(pose, buffer, minX, minY, minZ, minX, minY, maxZ, minX, maxY, minZ, minX, maxY, maxZ, br, bg, bb, r, g, b, a);
        side(pose, buffer, maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, maxZ, maxX, maxY, minZ, br, bg, bb, r, g, b, a);
        side(pose, buffer, minX, minY, maxZ, maxX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, br, bg, bb, r, g, b, a);
        side(pose, buffer, maxX, minY, minZ, minX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, br, bg, bb, r, g, b, a);
    }

    private static void side(PoseStack.Pose pose, VertexConsumer buffer,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float br, float bg, float bb, float tr, float tg, float tb, float a) {
        vertexColor(pose, buffer, ax, ay, az, br, bg, bb, a);
        vertexColor(pose, buffer, bx, by, bz, br, bg, bb, a);
        vertexColor(pose, buffer, dx, dy, dz, tr, tg, tb, a);
        vertexColor(pose, buffer, cx, cy, cz, tr, tg, tb, a);
    }

    private static void vertexColor(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float r, float g, float b, float a) {
        buffer.addVertex(pose, x, y, z).setColor(r, g, b, a);
    }

    public record PlaceData(BlockPos blockPos, Direction face, double damage, PlaceResult placeResult, Entity entity) {
    }

    public static class VisualData {
        private final BlockPos blockPos;
        private final Animation animation = new Animation(Easing.CUBIC_OUT, 300);
        private final long createdMs = System.currentTimeMillis();
        private float r;
        private float g;
        private float b;

        public VisualData(BlockPos blockPos, float r, float g, float b) {
            this.blockPos = blockPos;
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        public Animation getAnimation() {
            return animation;
        }

        public long getCreatedMs() {
            return createdMs;
        }

        public float getR() {
            return r;
        }

        public float getG() {
            return g;
        }

        public float getB() {
            return b;
        }
    }

    public enum PlaceResult {
        PLACE_OBSIDIAN,
        PLACE_CRYSTAL,
        EXPLOSION,
        BREAK,
        AWAITING
    }

    public enum ReturnResult {
        ITEM,
        PLAYER,
        NONE
    }
}