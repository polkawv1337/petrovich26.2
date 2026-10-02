package client_files.Petrovich.Combat;

import client_files.ClientikUtils.Category;
import client_files.Module;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

import java.util.HashMap;
import java.util.Map;

public class CrystalOptimizer extends Module {

    private static final long PLACEMENT_WINDOW = 2500L;

    private final Map<BlockPos, Long> placements = new HashMap<>();

    public CrystalOptimizer() {
        super("CrystalOptimizer", "Автоматически взрывает кристалл сразу после установки (только свои)", Category.COMBAT);
    }

    private static CrystalOptimizer module() {
        return client_files.ModuleManager.getInstance().get(CrystalOptimizer.class);
    }

    public static boolean handleRightClickBlock(BlockHitResult result) {
        CrystalOptimizer module = module();
        if (module == null || module.mc.player == null || module.mc.level == null) return false;
        if (!module.isEnabled()) return false;

        boolean hasCrystal = module.mc.player.getOffhandItem().getItem() == Items.END_CRYSTAL
                || module.mc.player.getMainHandItem().getItem() == Items.END_CRYSTAL;
        if (!hasCrystal) return false;

        BlockPos pos = result.getBlockPos();
        if (!module.isCrystalBase(pos)) return false;
        if (!module.mc.level.getBlockState(pos.above()).isAir()
                || !module.mc.level.getBlockState(pos.above(2)).isAir()) return false;

        module.placements.put(pos, System.currentTimeMillis());
        if (module.placements.size() > 32) {
            module.placements.entrySet().removeIf(en -> System.currentTimeMillis() - en.getValue() > PLACEMENT_WINDOW);
        }
        return false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        long now = System.currentTimeMillis();
        placements.entrySet().removeIf(en -> now - en.getValue() > PLACEMENT_WINDOW);
        if (placements.isEmpty()) return;

        for (EndCrystal crystal : mc.level.getEntitiesOfClass(EndCrystal.class, mc.player.getBoundingBox().inflate(64.0))) {
            if (!crystal.isAlive()) continue;
            BlockPos base = crystalBase(crystal);
            if (!placements.containsKey(base)) continue;

            mc.player.connection.send(new ServerboundAttackPacket(crystal.getId()));
            mc.player.connection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            placements.remove(base);
        }
    }

    private BlockPos crystalBase(EndCrystal crystal) {
        return new BlockPos(
                Mth.floor(crystal.getX()),
                Mth.floor(crystal.getY() - 1.0),
                Mth.floor(crystal.getZ())
        );
    }

    private boolean isCrystalBase(BlockPos pos) {
        if (mc.level == null) return false;
        Block block = mc.level.getBlockState(pos).getBlock();
        return block == Blocks.OBSIDIAN
                || block == Blocks.BEDROCK
                || block == Blocks.CRYING_OBSIDIAN
                || block == Blocks.RESPAWN_ANCHOR
                || block == Blocks.ANVIL
                || block == Blocks.CHIPPED_ANVIL
                || block == Blocks.DAMAGED_ANVIL
                || block == Blocks.ENCHANTING_TABLE;
    }

    @Override
    public void onDisable() {
        placements.clear();
        super.onDisable();
    }
}