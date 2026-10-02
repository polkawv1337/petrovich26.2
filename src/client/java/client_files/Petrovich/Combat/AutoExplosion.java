package client_files.Petrovich.Combat;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.InventoryUtil;
import client_files.ClientikUtils.MultipointUtils;
import client_files.Module;
import client_files.ModuleManager;
import client_files.Petrovich.Player.FriendHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.EggItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;

public class AutoExplosion extends Module {

    private final BooleanSetting placeObsidian = addSetting(new BooleanSetting("Независимо от слота", false));
    private final BooleanSetting saveSelf = addSetting(new BooleanSetting("Не взрывать себя", false));
    private final BooleanSetting saveFriends = addSetting(new BooleanSetting("Не взрывать друзей", false));

    private BlockPos obsidianPos;
    private BlockPos crystalPos;
    private EndCrystal crystal;
    private int lastSlot = -1;
    private int tick;

    public AutoExplosion() {
        super("AutoExplosion", "Автоматически взрывает кристалл сразу после установки", Category.COMBAT);
    }

    private static AutoExplosion module() {
        return ModuleManager.getInstance().get(AutoExplosion.class);
    }

    private boolean busyWithAura() {
        CrystalAura aura = ModuleManager.getInstance().get(CrystalAura.class);
        return aura == null || aura.getLastData() != null;
    }

    private boolean isRightClickItem(ItemStack stack) {
        Item item = stack.getItem();
        if (stack.isEmpty()) return false;

        InteractionResult result = item.use(mc.level, mc.player, InteractionHand.MAIN_HAND);

        return item instanceof BlockItem
                || item instanceof PotionItem
                || item instanceof BucketItem
                || item instanceof SpawnEggItem
                || item instanceof EnderpearlItem
                || item instanceof SnowballItem
                || item instanceof EggItem
                || item instanceof FireworkRocketItem
                || item instanceof BoatItem
                || item instanceof MinecartItem
                || result.consumesAction();
    }

    public static boolean handleRightClickBlock(Player player, BlockHitResult result) {
        AutoExplosion module = module();
        if (module == null || module.mc.player == null || module.mc.level == null) return false;
        if (!module.isEnabled() || module.busyWithAura()) return false;

        if (module.tick > 0) return true;

        BlockPos pos = result.getBlockPos();

        boolean friend = false;
        for (Player p : module.mc.level.players()) {
            if (p == module.mc.player || !FriendHelper.isFriend(p.getGameProfile().name())) continue;
            double dx = Math.abs(p.blockPosition().getX() - pos.getX());
            double dz = Math.abs(p.blockPosition().getZ() - pos.getZ());
            if (pos.getY() < p.getY() && (dx < 6 && dz < 6)) friend = true;
        }

        boolean falsePosition = false;
        for (int i = 0; i < 5; i++) {
            if (pos.equals(module.mc.player.blockPosition().below(i))
                    || !module.mc.level.getBlockState(pos.above()).isAir()
                    || (module.saveSelf.getValue() && pos.getY() < module.mc.player.blockPosition().getY())
                    || (module.saveFriends.getValue() && friend)) {
                falsePosition = true;
                break;
            }
        }

        int crystalSlot = InventoryUtil.searchItem(net.minecraft.world.item.Items.END_CRYSTAL, 0, 9);
        int obsidianSlot = InventoryUtil.searchItem(net.minecraft.world.item.Items.OBSIDIAN, 0, 9);

        Block block = module.mc.level.getBlockState(pos).getBlock();
        if (block == Blocks.OBSIDIAN) {
            if (module.mc.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == net.minecraft.world.item.Items.END_CRYSTAL) {
                if (!falsePosition) module.crystalPos = pos.above();
            }
        } else if (module.placeObsidian.getValue() && crystalSlot != -1 && obsidianSlot != -1) {
            if (!module.isRightClickItem(module.mc.player.getItemInHand(InteractionHand.MAIN_HAND))) {
                if (module.lastSlot == -1) module.lastSlot = module.mc.player.getInventory().getSelectedSlot();
                module.mc.player.getInventory().setSelectedSlot(obsidianSlot);
            }
        }
        return false;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;
        if (busyWithAura()) return;

        if (tick > 0) tick--;

        if (obsidianPos != null) {
            if (mc.player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == net.minecraft.world.item.Items.END_CRYSTAL) {
                mc.gameMode.useItemOn(
                        mc.player,
                        InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(obsidianPos), Direction.UP, obsidianPos, false)
                );
                tick = 2;
                crystalPos = obsidianPos.above();
            }
            obsidianPos = null;
        }

        if (crystal != null && crystal.isAlive()) {
            double dx = Math.abs(mc.player.getX() - crystal.getX());
            double dy = Math.abs(mc.player.getY() - crystal.getY());
            double dz = Math.abs(mc.player.getZ() - crystal.getZ());

            if (!saveSelf.getValue() || !(dx < 6 && dy < 6 && dz < 6 && mc.player.getY() > crystal.getY() - 0.25f)) {
                mc.player.connection.send(new ServerboundAttackPacket(crystal.getId()));
                mc.player.connection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
                tick = 2;
            }
            crystalPos = null;
            crystal = null;
        }

        if (crystalPos != null && crystal == null) {
            for (EndCrystal entity : mc.level.getEntitiesOfClass(EndCrystal.class, new AABB(crystalPos).inflate(2.0))) {
                if (!entity.isAlive()) continue;
                if (mc.player.getEyePosition().distanceTo(MultipointUtils.getNearestPoint(entity, 0)) <= 3) {
                    crystal = entity;
                    break;
                }
            }
        }

        if (tick == 0 && lastSlot != -1) {
            mc.player.getInventory().setSelectedSlot(lastSlot);
            lastSlot = -1;
        }
    }

    @Override
    public void onDisable() {
        obsidianPos = null;
        crystalPos = null;
        crystal = null;
        lastSlot = -1;
        tick = 0;
        super.onDisable();
    }
}