package client_files.ClientikUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public final class CrystalDamageCalculator {
    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean terrainIgnore = false;

    private CrystalDamageCalculator() {
    }

    public static float getExplosionDamage(Vec3 explosionPos, Player target, boolean optimized) {
        if (mc.level.getDifficulty() == Difficulty.PEACEFUL || target == null) return 0f;

        if (!new AABB(Mth.floor(explosionPos.x - 11), Mth.floor(explosionPos.y - 11), Mth.floor(explosionPos.z - 11), Mth.floor(explosionPos.x + 13), Mth.floor(explosionPos.y + 13), Mth.floor(explosionPos.z + 13)).intersects(target.getBoundingBox()))
            return 0f;

        if (!target.isInvulnerable()) {
            double distExposure = (float) target.distanceToSqr(explosionPos) / 144.;
            if (distExposure <= 1.0) {
                terrainIgnore = false;
                double exposure = getExposure(explosionPos, target.getBoundingBox(), optimized);
                terrainIgnore = false;
                double finalExposure = (1.0 - distExposure) * exposure;

                float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2. * 7. * 12. + 1.);

                if (mc.level.getDifficulty() == Difficulty.EASY) toDamage = Math.min(toDamage / 2f + 1f, toDamage);
                else if (mc.level.getDifficulty() == Difficulty.HARD) toDamage = toDamage * 3f / 2f;

                DamageSource source = mc.level.damageSources().explosion(null, null);

                toDamage = CombatRules.getDamageAfterAbsorb(target, toDamage, source, target.getArmorValue(), (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS));

                if (target.hasEffect(MobEffects.RESISTANCE)) {
                    int resistance = 25 - (target.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
                    float resistance_1 = toDamage * resistance;
                    toDamage = Math.max(resistance_1 / 25f, 0f);
                }

                if (toDamage <= 0f) toDamage = 0f;
                else {
                    float protAmount = getProtectionAmount(List.of(target.getItemBySlot(EquipmentSlot.HEAD), target.getItemBySlot(EquipmentSlot.CHEST), target.getItemBySlot(EquipmentSlot.LEGS), target.getItemBySlot(EquipmentSlot.FEET)));

                    if (protAmount > 0)
                        toDamage = CombatRules.getDamageAfterMagicAbsorb(toDamage, protAmount);
                }
                return toDamage;
            }
        }
        return 0f;
    }

    public static float getDamageOfGhostBlock(Vec3 explosionPos, Player target, BlockPos bp) {
        if (mc.level.getDifficulty() == Difficulty.PEACEFUL) return 0f;

        double maxDist = 12;
        if (!new AABB(Mth.floor(explosionPos.x - maxDist - 1.0), Mth.floor(explosionPos.y - maxDist - 1.0), Mth.floor(explosionPos.z - maxDist - 1.0), Mth.floor(explosionPos.x + maxDist + 1.0), Mth.floor(explosionPos.y + maxDist + 1.0), Mth.floor(explosionPos.z + maxDist + 1.0)).intersects(target.getBoundingBox())) {
            return 0f;
        }

        if (!target.isInvulnerable()) {
            double distExposure = target.distanceToSqr(explosionPos) / 144.;
            if (distExposure <= 1.0) {
                terrainIgnore = true;
                double exposure = getExposureGhost(explosionPos, target, bp);
                terrainIgnore = false;
                double finalExposure = (1.0 - distExposure) * exposure;

                float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * maxDist + 1.0);

                if (mc.level.getDifficulty() == Difficulty.EASY) {
                    toDamage = Math.min(toDamage / 2f + 1f, toDamage);
                } else if (mc.level.getDifficulty() == Difficulty.HARD) {
                    toDamage = toDamage * 3f / 2f;
                }

                DamageSource source = mc.level.damageSources().explosion(null, null);

                toDamage = CombatRules.getDamageAfterAbsorb(target, toDamage, source, target.getArmorValue(), (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS));

                if (target.hasEffect(MobEffects.RESISTANCE)) {
                    int resistance = 25 - (target.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
                    float resistance_1 = toDamage * resistance;
                    toDamage = Math.max(resistance_1 / 25f, 0f);
                }

                if (toDamage <= 0f) toDamage = 0f;
                else {
                    float protAmount = getProtectionAmount(List.of(target.getItemBySlot(EquipmentSlot.HEAD), target.getItemBySlot(EquipmentSlot.CHEST), target.getItemBySlot(EquipmentSlot.LEGS), target.getItemBySlot(EquipmentSlot.FEET)));

                    if (protAmount > 0) toDamage = CombatRules.getDamageAfterMagicAbsorb(toDamage, protAmount);
                }
                return toDamage;
            }
        }
        return 0f;
    }

    public static float getDamageIgnoringBlock(Vec3 explosionPos, Player target, BlockPos airPos) {
        if (mc.level.getDifficulty() == Difficulty.PEACEFUL || target == null || airPos == null) return 0f;

        double maxDist = 12.0;
        if (!new AABB(
                Mth.floor(explosionPos.x - maxDist - 1.0),
                Mth.floor(explosionPos.y - maxDist - 1.0),
                Mth.floor(explosionPos.z - maxDist - 1.0),
                Mth.floor(explosionPos.x + maxDist + 1.0),
                Mth.floor(explosionPos.y + maxDist + 1.0),
                Mth.floor(explosionPos.z + maxDist + 1.0)
        ).intersects(target.getBoundingBox())) {
            return 0f;
        }

        if (!target.isInvulnerable()) {
            double distExposure = target.distanceToSqr(explosionPos) / 144.0;
            if (distExposure <= 1.0) {
                terrainIgnore = true;
                double exposure = getExposureIgnoreBlock(explosionPos, target, airPos);
                terrainIgnore = false;

                double finalExposure = (1.0 - distExposure) * exposure;
                float toDamage = (float) Math.floor((finalExposure * finalExposure + finalExposure) / 2.0 * 7.0 * maxDist + 1.0);

                if (mc.level.getDifficulty() == Difficulty.EASY) {
                    toDamage = Math.min(toDamage / 2f + 1f, toDamage);
                } else if (mc.level.getDifficulty() == Difficulty.HARD) {
                    toDamage = toDamage * 3f / 2f;
                }

                DamageSource source = mc.level.damageSources().explosion(null, null);
                toDamage = CombatRules.getDamageAfterAbsorb(
                        target,
                        toDamage,
                        source,
                        target.getArmorValue(),
                        (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS)
                );

                if (target.hasEffect(MobEffects.RESISTANCE)) {
                    int resistance = 25 - (target.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
                    float resistance1 = toDamage * resistance;
                    toDamage = Math.max(resistance1 / 25f, 0f);
                }

                if (toDamage <= 0f) {
                    toDamage = 0f;
                } else {
                    float protAmount = getProtectionAmount(List.of(
                            target.getItemBySlot(EquipmentSlot.HEAD),
                            target.getItemBySlot(EquipmentSlot.CHEST),
                            target.getItemBySlot(EquipmentSlot.LEGS),
                            target.getItemBySlot(EquipmentSlot.FEET)
                    ));

                    if (protAmount > 0) {
                        toDamage = CombatRules.getDamageAfterMagicAbsorb(toDamage, protAmount);
                    }
                }

                return toDamage;
            }
        }

        return 0f;
    }

    public static float getExposure(Vec3 source, AABB box, boolean optimized) {
        if (!optimized) return getExposure(source, box);

        int miss = 0;
        int hit = 0;

        for (int k = 0; k <= 1; k += 1) {
            for (int l = 0; l <= 1; l += 1) {
                for (int m = 0; m <= 1; m += 1) {
                    double n = Mth.lerp(k, box.minX, box.maxX);
                    double o = Mth.lerp(l, box.minY, box.maxY);
                    double p = Mth.lerp(m, box.minZ, box.maxZ);
                    Vec3 vec3d = new Vec3(n, o, p);
                    if (raycast(vec3d, source, true) == HitResult.Type.MISS)
                        ++miss;
                    ++hit;
                }
            }
        }
        return (float) miss / (float) hit;
    }

    public static float getExposure(Vec3 source, AABB box) {
        double d = 0.4545454446934474;
        double e = 0.21739130885479366;
        double f = 0.4545454446934474;

        int i = 0;
        int j = 0;

        for (double k = 0.0; k <= 1.0; k += d)
            for (double l = 0.0; l <= 1.0; l += e)
                for (double m = 0.0; m <= 1.0; m += f) {
                    double n = Mth.lerp(k, box.minX, box.maxX);
                    double o = Mth.lerp(l, box.minY, box.maxY);
                    double p = Mth.lerp(m, box.minZ, box.maxZ);
                    Vec3 vec3d = new Vec3(n + 0.045454555306552624, o, p + 0.045454555306552624);
                    if (raycast(vec3d, source, true) == HitResult.Type.MISS)
                        ++i;
                    ++j;
                }

        return (float) i / (float) j;
    }

    private static float getExposureGhost(Vec3 source, Entity entity, BlockPos pos) {
        return getExposureGhost(source, entity.getBoundingBox(), entity, pos);
    }

    private static float getExposureGhost(Vec3 source, AABB box, Entity entity, BlockPos pos) {
        double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;

        if (d < 0.0 || e < 0.0 || f < 0.0) {
            return 0.0f;
        }

        int i = 0;
        int j = 0;

        for (double k = 0.0; k <= 1.0; k += d) {
            for (double l = 0.0; l <= 1.0; l += e) {
                for (double m = 0.0; m <= 1.0; m += f) {
                    double n = Mth.lerp(k, box.minX, box.maxX);
                    double o = Mth.lerp(l, box.minY, box.maxY);
                    double p = Mth.lerp(m, box.minZ, box.maxZ);
                    Vec3 vec3d = new Vec3(n + g, o, p + h);
                    if (raycastGhost(vec3d, source, pos).getType() == HitResult.Type.MISS)
                        ++i;
                    ++j;
                }
            }
        }

        return (float) i / (float) j;
    }

    private static float getExposureIgnoreBlock(Vec3 source, Entity entity, BlockPos airPos) {
        return getExposureIgnoreBlock(source, entity.getBoundingBox(), entity, airPos);
    }

    private static float getExposureIgnoreBlock(Vec3 source, AABB box, Entity entity, BlockPos airPos) {
        double d = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double e = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double f = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);
        double g = (1.0 - Math.floor(1.0 / d) * d) / 2.0;
        double h = (1.0 - Math.floor(1.0 / f) * f) / 2.0;

        if (d < 0.0 || e < 0.0 || f < 0.0) {
            return 0.0f;
        }

        int i = 0;
        int j = 0;

        for (double k = 0.0; k <= 1.0; k += d) {
            for (double l = 0.0; l <= 1.0; l += e) {
                for (double m = 0.0; m <= 1.0; m += f) {
                    double n = Mth.lerp(k, box.minX, box.maxX);
                    double o = Mth.lerp(l, box.minY, box.maxY);
                    double p = Mth.lerp(m, box.minZ, box.maxZ);
                    Vec3 vec3d = new Vec3(n + g, o, p + h);

                    if (raycastIgnoreBlock(vec3d, source, airPos).getType() == HitResult.Type.MISS) {
                        ++i;
                    }

                    ++j;
                }
            }
        }

        return (float) i / (float) j;
    }

    private static BlockHitResult raycastGhost(Vec3 start, Vec3 end, BlockPos ghostPos) {
        if (voxelHit(start, end, ghostPos, true, false)) {
            BlockPos hitPos = new BlockPos(Mth.floor(end.x), Mth.floor(end.y), Mth.floor(end.z));
            return new BlockHitResult(end, directionFromDelta(end.x - start.x, end.y - start.y, end.z - start.z), hitPos, false);
        }
        return BlockHitResult.miss(end, directionFromDelta(end.x - start.x, end.y - start.y, end.z - start.z), BlockPos.containing(end));
    }

    private static BlockHitResult raycastIgnoreBlock(Vec3 start, Vec3 end, BlockPos airPos) {
        Direction missDir = directionFromDelta(end.x - start.x, end.y - start.y, end.z - start.z);
        if (voxelHit(start, end, airPos, false, true)) {
            BlockPos hitPos = new BlockPos(Mth.floor(end.x), Mth.floor(end.y), Mth.floor(end.z));
            return new BlockHitResult(end, missDir, hitPos, false);
        }
        return BlockHitResult.miss(end, missDir, BlockPos.containing(end));
    }

    private static Direction directionFromDelta(double dx, double dy, double dz) {
        double ax = Math.abs(dx);
        double ay = Math.abs(dy);
        double az = Math.abs(dz);
        if (ax >= ay && ax >= az) return dx > 0 ? Direction.EAST : Direction.WEST;
        if (ay >= ax && ay >= az) return dy > 0 ? Direction.UP : Direction.DOWN;
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    public static HitResult.Type raycast(Vec3 start, Vec3 end, boolean ignoreTerrain) {
        return voxelHit(start, end, null, false, false) ? HitResult.Type.BLOCK : HitResult.Type.MISS;
    }

    private static boolean voxelHit(Vec3 start, Vec3 end, BlockPos customPos, boolean ghostReplace, boolean bleedAir) {
        if (mc.level == null) return false;

        double dx = end.x - start.x;
        double dy = end.y - start.y;
        double dz = end.z - start.z;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length < 1.0E-8) return false;

        int x = Mth.floor(start.x);
        int y = Mth.floor(start.y);
        int z = Mth.floor(start.z);

        int stepX = dx > 0 ? 1 : (dx < 0 ? -1 : 0);
        int stepY = dy > 0 ? 1 : (dy < 0 ? -1 : 0);
        int stepZ = dz > 0 ? 1 : (dz < 0 ? -1 : 0);

        double absDx = Math.abs(dx);
        double absDy = Math.abs(dy);
        double absDz = Math.abs(dz);

        double tMaxX = stepX == 0 ? Double.POSITIVE_INFINITY : (stepX > 0 ? (x + 1 - start.x) : (start.x - x)) / absDx;
        double tMaxY = stepY == 0 ? Double.POSITIVE_INFINITY : (stepY > 0 ? (y + 1 - start.y) : (start.y - y)) / absDy;
        double tMaxZ = stepZ == 0 ? Double.POSITIVE_INFINITY : (stepZ > 0 ? (z + 1 - start.z) : (start.z - z)) / absDz;

        double tDeltaX = stepX == 0 ? Double.POSITIVE_INFINITY : 1.0 / absDx;
        double tDeltaY = stepY == 0 ? Double.POSITIVE_INFINITY : 1.0 / absDy;
        double tDeltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : 1.0 / absDz;

        int guard = 0;
        while (guard++ < 2048) {
            BlockPos pos = new BlockPos(x, y, z);
            if (blockedAt(pos, customPos, ghostReplace, bleedAir, start, end)) {
                return true;
            }

            double nextT;
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    nextT = tMaxX;
                    tMaxX += tDeltaX;
                    x += stepX;
                } else {
                    nextT = tMaxZ;
                    tMaxZ += tDeltaZ;
                    z += stepZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    nextT = tMaxY;
                    tMaxY += tDeltaY;
                    y += stepY;
                } else {
                    nextT = tMaxZ;
                    tMaxZ += tDeltaZ;
                    z += stepZ;
                }
            }

            if (nextT >= 1.0) break;
        }
        return false;
    }

    private static boolean blockedAt(BlockPos pos, BlockPos customPos, boolean ghostReplace, boolean bleedAir, Vec3 start, Vec3 end) {
        BlockState state;
        if (ghostReplace && customPos != null && pos.equals(customPos)) {
            state = Blocks.OBSIDIAN.defaultBlockState();
        } else if (bleedAir && customPos != null && pos.equals(customPos)) {
            state = Blocks.AIR.defaultBlockState();
        } else {
            state = mc.level.getBlockState(pos);
        }

        VoxelShape shape = state.getCollisionShape(mc.level, pos);
        if (shape.isEmpty()) return false;

        BlockHitResult hit = shape.clip(start, end, pos);
        return hit != null && hit.getType() == HitResult.Type.BLOCK;
    }

    public static boolean intersectsEntityBox(BlockPos pos, Entity entity) {
        AABB blockBox = new AABB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1.0, pos.getY() + 2.0, pos.getZ() + 1.0
        );
        return entity.getBoundingBox().intersects(blockBox);
    }

    public static int getProtectionAmount(Iterable<ItemStack> equipment) {
        int amount = 0;
        for (ItemStack stack : equipment) {
            amount += getProtectionAmount(stack);
        }
        return amount;
    }

    public static int getProtectionAmount(ItemStack stack) {
        int modifierBlast = EnchantmentHelper.getItemEnchantmentLevel(getHolder(Enchantments.BLAST_PROTECTION), stack);
        int modifier = EnchantmentHelper.getItemEnchantmentLevel(getHolder(Enchantments.PROTECTION), stack);
        return modifierBlast * 2 + modifier;
    }

    private static Holder<Enchantment> getHolder(net.minecraft.resources.ResourceKey<Enchantment> key) {
        return mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }
}