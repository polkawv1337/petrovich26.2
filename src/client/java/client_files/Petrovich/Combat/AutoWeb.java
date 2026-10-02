package client_files.Petrovich.Combat;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.DirectionUtil;
import client_files.ClientikUtils.PredictUtils;
import client_files.ClientikUtils.RaycastUtils;
import client_files.ClientikUtils.Rotation;
import client_files.ClientikUtils.RotationUtil;
import client_files.Module;
import client_files.ModuleManager;
import client_files.Petrovich.Player.FriendHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class AutoWeb extends Module {

    private static final float RANGE = 4.5f;

    private UUID trappedTarget;
    private int slotToRestore = -1;
    private int ticksToReturnSlot = -1;

    private boolean pendingUpperWeb = false;
    private BlockPos pendingUpperPos = null;

    public AutoWeb() {
        super("AutoWeb", "Ставит паутину под противника с предсказанием и вращением (синк с CrystalAura)", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (ticksToReturnSlot == 0 && slotToRestore >= 0 && slotToRestore < 9) {
            mc.player.getInventory().setSelectedSlot(slotToRestore);
            if (mc.getConnection() != null) {
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(slotToRestore));
            }
            slotToRestore = -1;
            ticksToReturnSlot = -1;
        }

        int webSlot = getWebSlot();
        if (webSlot == -1) {
            tickReturnSlot();
            return;
        }

        if (pendingUpperWeb && pendingUpperPos != null) {
            tryPlaceWebAt(pendingUpperPos, webSlot);

            pendingUpperWeb = false;
            pendingUpperPos = null;

            if (slotToRestore != -1) {
                ticksToReturnSlot = 1;
            }

            tickReturnSlot();
            return;
        }

        LivingEntity target = getSyncedTarget();
        if (target == null || mc.player.distanceToSqr(target) > RANGE * RANGE) {
            tickReturnSlot();
            return;
        }

        Vec3 predictVec = PredictUtils.predict(target, 2);
        BlockPos predictPos = BlockPos.containing(predictVec);

        int prevSlot = mc.player.getInventory().getSelectedSlot();

        boolean placedLower = tryPlaceWebAt(predictPos, webSlot);
        if (placedLower) {
            if (trappedTarget == null) {
                trappedTarget = target.getUUID();
                CrystalAura.setWebTrappedTarget(trappedTarget);
            }

            pendingUpperWeb = true;
            pendingUpperPos = predictPos.above();

            slotToRestore = prevSlot;
            ticksToReturnSlot = 2;
        }

        tickReturnSlot();
    }

    private void tickReturnSlot() {
        if (ticksToReturnSlot > 0) {
            ticksToReturnSlot--;
        }
    }

    private boolean tryPlaceWebAt(BlockPos placePos, int webSlot) {
        if (!canPlaceAt(placePos)) return false;
        if (intersectsPlayer(placePos)) return false;

        PlaceData placeData = findPlaceData(placePos);
        if (placeData == null) return false;

        Rotation rotation = RotationUtil.to(mc.player.getEyePosition(), placeData.hitVec);
        rotation.setYaw(rotation.getYaw() + (float) randomRange(-2, 2));
        rotation.setPitch(rotation.getPitch() + (float) randomRange(-2, 2));

        if (RaycastUtils.raycastBlock(rotation, placeData.neighbourPos, RANGE) == null) return false;

        Direction lookedFace = DirectionUtil.getFaceDirection(rotation, mc.player, placeData.neighbourPos);
        if (lookedFace != placeData.side) return false;

        // если ротацией уже владеет CrystalAura - не шлём свою, иначе они конфликтуют
        if (!CrystalAura.rotationOwnedByAura()) {
            CrystalAura.updateRotation(rotation, 99999999);
        }

        BlockHitResult hitResult = new BlockHitResult(
                placeData.hitVec,
                placeData.side,
                placeData.neighbourPos,
                false
        );

        boolean[] placed = new boolean[1];
        // слот берётся только на время действия и сразу возвращается
        CrystalAura.useSlotTemporary(webSlot, () -> {
            InteractionResult result = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
            if (result.consumesAction()) {
                mc.player.swing(InteractionHand.MAIN_HAND);
                placed[0] = true;
            }
        });

        return placed[0];
    }

    private int getWebSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == Items.COBWEB) {
                return i;
            }
        }
        return -1;
    }

    private boolean canPlaceAt(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        if (!state.canBeReplaced()) return false;
        return state.getFluidState().isEmpty();
    }

    private boolean intersectsPlayer(BlockPos pos) {
        AABB placeBox = new AABB(pos);
        return mc.player.getBoundingBox().intersects(placeBox);
    }

    private Vec3 resolveHitVecForFace(BlockPos neighbour, Direction side) {
        Vec3 center = Vec3.atCenterOf(neighbour);
        org.joml.Vector3f normalStep = side.step();
        Vec3 normal = new Vec3(normalStep.x, normalStep.y, normalStep.z);

        Vec3 base = center.add(normal.scale(0.5 - 0.01));

        Vec3[] offsets = new Vec3[]{
                Vec3.ZERO,
                new Vec3(0.1, 0.0, 0.1),
                new Vec3(-0.1, 0.0, 0.1),
                new Vec3(0.1, 0.0, -0.1),
                new Vec3(-0.1, 0.0, -0.1),
                new Vec3(0.0, 0.1, 0.0),
                new Vec3(0.0, -0.1, 0.0)
        };

        for (Vec3 offset : offsets) {
            Vec3 candidate = adjustOffsetForFace(base, offset, side);

            Rotation current = RotationUtil.to(mc.player.getEyePosition(), candidate);

            Direction lookedFace = DirectionUtil.getFaceDirection(current, mc.player, neighbour);

            if (lookedFace == side) {
                return candidate;
            }
        }

        return base;
    }

    private Vec3 adjustOffsetForFace(Vec3 base, Vec3 offset, Direction side) {
        return switch (side) {
            case UP, DOWN -> base.add(offset.x, 0.0, offset.z);
            case NORTH, SOUTH -> base.add(offset.x, offset.y, 0.0);
            case EAST, WEST -> base.add(0.0, offset.y, offset.z);
        };
    }

    private PlaceData findPlaceData(BlockPos placePos) {
        for (Direction side : Direction.values()) {
            BlockPos neighbour = placePos.relative(side);
            Direction hitSide = side.getOpposite();

            BlockState neighbourState = mc.level.getBlockState(neighbour);
            if (!isValidSupportBlock(neighbourState, neighbour)) continue;

            Vec3 hitVec = resolveHitVecForFace(neighbour, hitSide);
            return new PlaceData(neighbour, hitSide, hitVec);
        }
        return null;
    }

    private boolean isValidSupportBlock(BlockState state, BlockPos pos) {
        if (state.isAir()) return false;
        if (!state.getFluidState().isEmpty()) return false;

        Block block = state.getBlock();

        if (block == Blocks.COBWEB) return true;

        if (!state.isSolid()) return false;

        if (block instanceof DoublePlantBlock) return false;
        if (block instanceof BushBlock) return false;
        if (block instanceof ButtonBlock) return false;
        if (block instanceof LeverBlock) return false;
        if (block instanceof TorchBlock) return false;
        if (block instanceof WallTorchBlock) return false;
        if (block instanceof RedstoneTorchBlock) return false;
        if (block instanceof WallBlock) return false;
        if (block instanceof FenceBlock) return false;
        if (block instanceof FenceGateBlock) return false;
        if (block instanceof DoorBlock) return false;
        if (block instanceof TrapDoorBlock) return false;
        if (block instanceof CarpetBlock) return false;
        if (block instanceof VineBlock) return false;
        if (block instanceof LadderBlock) return false;

        return true;
    }

    private LivingEntity getSyncedTarget() {
        CrystalAura aura = ModuleManager.getInstance().get(CrystalAura.class);
        if (aura != null && aura.isEnabled()) {
            // синхронизируемся только с целью-игроком, кристалл паутиной не ловим
            LivingEntity synced = aura.getSyncedPlayerTarget();
            if (synced != null && synced.isAlive() && synced != mc.player) {
                return synced;
            }
        }

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity entity : getEntitiesIterable()) {
            if (!(entity instanceof Player player)) continue;
            if (player == mc.player) continue;
            if (!player.isAlive()) continue;
            if (FriendHelper.isFriend(player.getScoreboardName())) continue;

            double dist = mc.player.distanceToSqr(player);
            if (dist > RANGE * RANGE) continue;

            if (dist < bestDist) {
                bestDist = dist;
                best = player;
            }
        }
        return best;
    }

    private List<Entity> getEntitiesIterable() {
        return mc.level.getEntities(EntityTypeTest.forClass(Entity.class),
                mc.player.getBoundingBox().inflate(64.0), (e) -> true);
    }

    private double randomRange(double min, double max) {
        return min + Math.random() * (max - min);
    }

    @Override
    public void onDisable() {
        release();
        if (slotToRestore >= 0 && slotToRestore < 9 && mc.player != null) {
            mc.player.getInventory().setSelectedSlot(slotToRestore);
            if (mc.getConnection() != null) {
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(slotToRestore));
            }
        }
        pendingUpperWeb = false;
        pendingUpperPos = null;
        slotToRestore = -1;
        ticksToReturnSlot = -1;
        super.onDisable();
    }

    private void release() {
        if (trappedTarget != null) {
            CrystalAura.clearWebTrappedTarget(trappedTarget);
        }
        trappedTarget = null;
    }

    private record PlaceData(BlockPos neighbourPos, Direction side, Vec3 hitVec) {
    }
}