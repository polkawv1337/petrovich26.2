package client_files.ClientikUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Predicate;

public class InventoryUtil {
    private static final Minecraft mc = Minecraft.getInstance();

    private InventoryUtil() {
    }

    public static int searchItemHotbar(Item item) {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem().equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public static int searchItemHotbar(List<Item> items) {
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getItem(i).getItem();
            for (Item candidate : items) {
                if (item.equals(candidate)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int searchItem(Item item) {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).getItem().equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public static int searchItem(Item item, int start, int end) {
        for (int i = start; i < end; i++) {
            if (mc.player.getInventory().getItem(i).getItem().equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public static int searchItem(List<Item> items) {
        for (int i = 0; i < 36; i++) {
            Item item = mc.player.getInventory().getItem(i).getItem();
            for (Item candidate : items) {
                if (item.equals(candidate)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int searchItemStack(Predicate<ItemStack> predicate) {
        for (int i = 0; i < 45; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && predicate.test(stack)) {
                return i;
            }
        }
        return -1;
    }

    public static int searchHotbarStack(Predicate<ItemStack> predicate) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && predicate.test(stack)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean hasEmptyInventorySlot() {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).isEmpty()) return true;
        }
        return false;
    }

    public static void clickSlotNoSync(int syncId, int slotId, int button, ContainerInput action, Player player) {
        if (mc.gameMode == null || player == null) return;
        mc.gameMode.handleContainerInput(syncId, slotId, button, action, player);
    }

    public static int findBestElytraSlot() {
        int bestSlot = -1;
        double bestScore = -1.0;

        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (stack.getItem() != Items.ELYTRA) continue;

            int maxDurability = stack.getMaxDamage();
            if (maxDurability <= 0) continue;
            int currentDamage = stack.getDamageValue();
            double durabilityRatio = (maxDurability - currentDamage) / (double) maxDurability;

            double score = durabilityRatio * 10;
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }

    public static int findBestChestplateSlot() {
        int bestSlot = -1;
        double bestScore = -1.0;

        for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            int priority = getChestplatePriority(stack.getItem());
            if (priority <= 0) continue;

            int maxDurability = stack.getMaxDamage();
            int currentDamage = stack.getDamageValue();
            double durabilityRatio = maxDurability > 0
                    ? (maxDurability - currentDamage) / (double) maxDurability : 1.0;

            double score = priority * 10000 + durabilityRatio * 10;
            if (score > bestScore) {
                bestScore = score;
                bestSlot = slot;
            }
        }
        return bestSlot;
    }

    private static int getChestplatePriority(Item item) {
        if (item == Items.NETHERITE_CHESTPLATE) return 6;
        if (item == Items.DIAMOND_CHESTPLATE) return 5;
        if (item == Items.IRON_CHESTPLATE) return 4;
        if (item == Items.CHAINMAIL_CHESTPLATE) return 3;
        if (item == Items.GOLDEN_CHESTPLATE) return 2;
        if (item == Items.LEATHER_CHESTPLATE) return 1;
        return 0;
    }

    private static final List<Item> HELMETS = List.of(
            Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET,
            Items.GOLDEN_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET);
    private static final List<Item> CHESTPLATES = List.of(
            Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE,
            Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE);
    private static final List<Item> LEGGINGS = List.of(
            Items.LEATHER_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS,
            Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS);
    private static final List<Item> BOOTS = List.of(
            Items.LEATHER_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS,
            Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);

    public static List<Item> getArmorItemsOfSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> HELMETS;
            case CHEST -> CHESTPLATES;
            case LEGS -> LEGGINGS;
            case FEET -> BOOTS;
            default -> List.of();
        };
    }

    public static int getBestArmorSlot(EquipmentSlot slot) {
        int bestSlot = -1;
        double bestScore = 0;
        List<Item> allowed = getArmorItemsOfSlot(slot);
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty() || !allowed.contains(stack.getItem())) continue;
            double score = getArmorScore(stack);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    public static double getArmorScore(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        double defense = getArmorDefense(stack.getItem());
        double max = stack.getMaxDamage();
        double fraction = max > 0 ? (stack.getMaxDamage() - stack.getDamageValue()) / max : 1.0;
        return defense * 1000.0 + fraction * 10.0;
    }

    public static int getArmorDefense(Item item) {
        EquipmentSlot slot = null;
        int tier = 0;

        List<Item>[] sets = new List[]{HELMETS, CHESTPLATES, LEGGINGS, BOOTS};
        EquipmentSlot[] slots = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int s = 0; s < sets.length; s++) {
            if (sets[s].contains(item)) {
                slot = slots[s];
                break;
            }
        }
        if (item == Items.NETHERITE_HELMET || item == Items.NETHERITE_CHESTPLATE
                || item == Items.NETHERITE_LEGGINGS || item == Items.NETHERITE_BOOTS) {
            tier = 6;
        } else if (item == Items.DIAMOND_HELMET || item == Items.DIAMOND_CHESTPLATE
                || item == Items.DIAMOND_LEGGINGS || item == Items.DIAMOND_BOOTS) {
            tier = 4;
        } else if (item == Items.IRON_HELMET || item == Items.IRON_CHESTPLATE
                || item == Items.IRON_LEGGINGS || item == Items.IRON_BOOTS) {
            tier = 3;
        } else if (item == Items.CHAINMAIL_HELMET || item == Items.CHAINMAIL_CHESTPLATE
                || item == Items.CHAINMAIL_LEGGINGS || item == Items.CHAINMAIL_BOOTS
                || item == Items.GOLDEN_HELMET || item == Items.GOLDEN_CHESTPLATE
                || item == Items.GOLDEN_LEGGINGS || item == Items.GOLDEN_BOOTS) {
            tier = 2;
        } else if (item == Items.LEATHER_HELMET || item == Items.LEATHER_CHESTPLATE
                || item == Items.LEATHER_LEGGINGS || item == Items.LEATHER_BOOTS) {
            tier = 1;
        }

        if (slot == null || tier == 0) return 0;

        int netheriteBonus = tier == 6 ? 5 : 0;
        tier = tier == 6 ? 4 : tier;
        return switch (slot) {
            case HEAD -> (tier >= 4 ? 3 : tier == 3 ? 2 : tier == 2 ? 2 : 1) + netheriteBonus;
            case CHEST -> (tier >= 4 ? 8 : tier == 3 ? 6 : tier == 2 ? 5 : 3) + netheriteBonus;
            case LEGS -> (tier >= 4 ? 6 : tier == 3 ? 5 : tier == 2 ? 4 : 2) + netheriteBonus;
            case FEET -> (tier >= 4 ? 3 : tier == 3 ? 2 : tier == 2 ? 1 : 1) + netheriteBonus;
            default -> 0;
        };
    }
}