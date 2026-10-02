package client_files.Petrovich.Combat;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.InventoryUtil;
import client_files.ClientikUtils.SliderSetting;
import client_files.Module;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;

public class AutoTotem extends Module {

    private static final int SWAP_COOLDOWN = 2;

    private final SliderSetting health = addSetting(new SliderSetting("Уровень здоровья", 4.0f, 1.0f, 20.0f, 0.5f));
    private final SliderSetting elytraHealth = addSetting(new SliderSetting("Здоровье на элитре", 8.0f, 1.0f, 20.0f, 0.5f));
    private final SliderSetting noArmorHealth = addSetting(new SliderSetting("Здоровье без брони", 6.0f, 1.0f, 20.0f, 0.5f));
    private final BooleanSetting swapBack = addSetting(new BooleanSetting("Возвращать предмет", true));
    private final BooleanSetting noBallSwitch = addSetting(new BooleanSetting("Игнорировать проверки, если в руке шар", false));
    private final BooleanSetting saveEnchanted = addSetting(new BooleanSetting("Сохранять зачарованный", true));
    private final BooleanSetting ignoreDragonidTotem = addSetting(new BooleanSetting("Не брать тотем Драконида", true));

    private final BooleanSetting goldenHearts = addSetting(new BooleanSetting("Золотые сердца", true));
    private final BooleanSetting crystalCheck = addSetting(new BooleanSetting("Кристаллы", true));
    private final BooleanSetting fallCheck = addSetting(new BooleanSetting("Падение", true));
    private final BooleanSetting tntCheck = addSetting(new BooleanSetting("Динамит", true));
    private final BooleanSetting tridentCheck = addSetting(new BooleanSetting("Трезубец", false));
    private final SliderSetting crystalDistance = addSetting(new SliderSetting("Дистанция проверки на кристалл",
            6.0f, 1.0f, 6.0f, 1.0f));

    private int oldItem = -1;
    private ItemStack oldOffhandStack = ItemStack.EMPTY;
    private boolean totemLocked;
    private int ticks;
    private int dangerCooldown;
    private int safeTicks;

    public AutoTotem() {
        super("AutoTotem", "Автоматически берет тотем бессмертия", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        oldItem = -1;
        oldOffhandStack = ItemStack.EMPTY;
        totemLocked = false;
        ticks = 0;
        dangerCooldown = 0;
        safeTicks = 0;
        crystalDistance.setVisible(crystalCheck.getValue());
    }

    @Override
    public void onUpdate() {
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        crystalDistance.setVisible(crystalCheck.getValue());
        ticks++;

        if (dangerCooldown > 0) dangerCooldown--;
        if (isDangerDetected() || getEffectiveHealth() <= getTotemHealthThreshold()) {
            safeTicks = 0;
        } else {
            safeTicks++;
        }

        int totemSlot = findTotemSlot();
        boolean hasTotem = hasTotemInHand();

        boolean urgent = isEmergencyDamage();
        if (!hasTotem && !isSwapBlockedByBall() && dangerCooldown == 0 && !player.isUsingItem()) {
            int pickSlot = urgent ? (findAnyTotemSlot() != -1 ? findAnyTotemSlot() : totemSlot) : totemSlot;
            if (pickSlot != -1 && (urgent || isDangerDetected())) {
                swap(pickSlot);
                ticks = 0;
                dangerCooldown = 10 + (int) (Math.random() * 6);
                safeTicks = 0;
                return;
            }
        }

        if (isTotemLockActive() && !hasTotem && totemSlot != -1 && ticks >= SWAP_COOLDOWN
                && dangerCooldown == 0 && !isSwapBlockedByBall() && !player.isUsingItem()) {
            swap(totemSlot);
            ticks = 0;
            dangerCooldown = 8;
            return;
        }

        if (shouldReturnSavedItem(totemSlot) && !player.isUsingItem()) {
            int returnSlot = findReturnSlot();
            if (returnSlot != -1) {
                swap(returnSlot);
                ticks = 0;
                return;
            }
        }

        if (ticks < SWAP_COOLDOWN) return;

        if (!isSwapBlockedByBall() && !hasTotem && !player.isUsingItem()) {
            if (needTotem() && totemSlot != -1) {
                swap(totemSlot);
                ticks = 0;
                return;
            }
            if (getEffectiveHealth() <= 2 && findAnyTotemSlot() != -1) {
                swap(findAnyTotemSlot());
                ticks = 0;
                return;
            }
        }

        if (!needTotem() && canSwapBack()) {
            int returnSlot = findReturnSlot();
            if (returnSlot == -1) {
                oldItem = -1;
                oldOffhandStack = ItemStack.EMPTY;
                totemLocked = false;
                return;
            }
            swap(returnSlot);
            ticks = 0;
        }
    }

    private boolean hasTotemInHand() {
        Player player = mc.player;
        if (player == null) return false;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return main.getItem() == Items.TOTEM_OF_UNDYING || off.getItem() == Items.TOTEM_OF_UNDYING;
    }

    private boolean isUsableTotem(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.TOTEM_OF_UNDYING || isDragonidTotem(stack)) return false;
        return !saveEnchanted.getValue() || stack.getEnchantments().isEmpty();
    }

    private boolean isDragonidTotem(ItemStack stack) {
        if (!ignoreDragonidTotem.getValue() || stack.isEmpty()) return false;
        return stack.getHoverName().getString().contains("Тотем Драконида");
    }

    private int findTotemSlot() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING
                    && !isDragonidTotem(stack)
                    && (!saveEnchanted.getValue() || stack.getEnchantments().isEmpty())) {
                return i;
            }
        }
        return -1;
    }

    private int findAnyTotemSlot() {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }

    private boolean needTotem() {
        boolean blockByBall = isSwapBlockedByBall();
        if (!blockByBall && fallCheck.getValue() && !mc.player.isInWater() && !mc.player.isFallFlying()
                && mc.player.fallDistance > 10) {
            return true;
        }
        return getEffectiveHealth() <= getTotemHealthThreshold();
    }

    private boolean shouldReturnSavedItem(int totemSlot) {
        if (!swapBack.getValue() || oldItem == -1) return false;
        if (isSameReturnItem(mc.player.getOffhandItem(), oldOffhandStack)) {
            oldItem = -1;
            oldOffhandStack = ItemStack.EMPTY;
            totemLocked = false;
            return false;
        }
        if (isTotemLockActive()) return false;
        return totemSlot == -1 && !isUsableTotem(mc.player.getOffhandItem());
    }

    private boolean canSwapBack() {
        if (oldItem == -1 || !swapBack.getValue()) return false;
        if (mc.player.getHealth() <= 1.0F) return false;
        if (mc.player.isUsingItem()) return false;
        if (isSameReturnItem(mc.player.getOffhandItem(), oldOffhandStack)) {
            oldItem = -1;
            oldOffhandStack = ItemStack.EMPTY;
            totemLocked = false;
            return false;
        }
        if (isDangerDetected() || getEffectiveHealth() <= getTotemHealthThreshold()) return false;
        return safeTicks >= 20;
    }

    private boolean isTotemLockActive() {
        return totemLocked && getEffectiveHealth() < getTotemHealthThreshold();
    }

    private float getEffectiveHealth() {
        float currentHealth = mc.player.getHealth();
        if (goldenHearts.getValue() && !mc.player.isFallFlying()
                && mc.player.hasEffect(MobEffects.ABSORPTION)) {
            currentHealth += mc.player.getAbsorptionAmount();
        }
        return currentHealth;
    }

    private float getTotemHealthThreshold() {
        Player player = mc.player;
        if (player == null) return health.getValue();
        if (player.isFallFlying()) return elytraHealth.getValue();
        if (!hasArmor()) return noArmorHealth.getValue();
        return health.getValue();
    }

    private boolean hasArmor() {
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!mc.player.getItemBySlot(slot).isEmpty()) return true;
        }
        return false;
    }

    private int findReturnSlot() {
        if (oldItem >= 0 && oldItem < 36 && isSameReturnItem(mc.player.getInventory().getItem(oldItem), oldOffhandStack)) {
            return oldItem;
        }
        if (oldOffhandStack.isEmpty()) {
            return (oldItem >= 0 && oldItem < 36) ? oldItem : -1;
        }
        for (int i = 0; i < 36; i++) {
            if (isSameReturnItem(mc.player.getInventory().getItem(i), oldOffhandStack)) {
                oldItem = i;
                return i;
            }
        }
        return -1;
    }

    private boolean isSameReturnItem(ItemStack current, ItemStack expected) {
        if (current.isEmpty() || expected.isEmpty()) return false;
        return current.getItem() == expected.getItem()
                && ItemStack.isSameItemSameComponents(current, expected);
    }

    private boolean isDangerDetected() {
        if (mc.level == null || mc.player == null) return false;

        if (crystalCheck.getValue()) {
            AABB box = mc.player.getBoundingBox().inflate(crystalDistance.getValue());
            for (Entity entity : getEntities(box)) {
                if (entity instanceof EndCrystal) return true;
            }
        }
        if (tntCheck.getValue()) {
            AABB box = mc.player.getBoundingBox().inflate(8);
            for (Entity entity : getEntities(box)) {
                if (entity instanceof PrimedTnt) return true;
            }
        }
        if (tridentCheck.getValue()) {
            AABB box = mc.player.getBoundingBox().inflate(10);
            for (Entity entity : getEntities(box)) {
                if (entity instanceof ThrownTrident trident && !trident.getDeltaMovement().equals(net.minecraft.world.phys.Vec3.ZERO)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSwapBlockedByBall() {
        return noBallSwitch.getValue()
                && mc.player.getOffhandItem().getItem() == Items.PLAYER_HEAD
                && !(fallCheck.getValue() && mc.player.fallDistance > 5);
    }

    private boolean isEmergencyDamage() {
        if (mc.level == null || mc.player == null) return false;

        if (getEffectiveHealth() <= 3.0F) return true;
        if (fallCheck.getValue() && !mc.player.isInWater() && !mc.player.isFallFlying()
                && mc.player.fallDistance > 14) return true;
        if (crystalCheck.getValue()) {
            AABB box = mc.player.getBoundingBox().inflate(3.5);
            for (Entity entity : getEntities(box)) {
                if (entity instanceof EndCrystal) return true;
            }
        }
        if (tntCheck.getValue()) {
            AABB box = mc.player.getBoundingBox().inflate(6);
            for (Entity entity : getEntities(box)) {
                if (entity instanceof PrimedTnt tnt && tnt.getFuse() < 40) return true;
            }
        }
        if (tridentCheck.getValue()) {
            AABB box = mc.player.getBoundingBox().inflate(6);
            for (Entity entity : getEntities(box)) {
                if (entity instanceof ThrownTrident trident
                        && trident.getDeltaMovement().horizontalDistanceSqr() > 0.02) return true;
            }
        }
        return false;
    }

    private java.util.List<Entity> getEntities(AABB box) {
        return mc.level.getEntities(EntityTypeTest.forClass(Entity.class), box, e -> true);
    }

    private void swap(int slot) {
        Player player = mc.player;
        if (player == null || mc.gameMode == null) return;
        if (slot < 0 || slot >= 36) return;

        ItemStack fromStack = player.getInventory().getItem(slot);
        boolean swappingTotemToOffhand = fromStack.getItem() == Items.TOTEM_OF_UNDYING;
        boolean swappingSavedItemBack = oldItem != -1 && isSameReturnItem(fromStack, oldOffhandStack);

        ItemStack currentOffhand = player.getOffhandItem().copy();

        if (slot >= 0 && slot <= 8) {
            InventoryUtil.clickSlotNoSync(0, 45, slot, ContainerInput.SWAP, player);
        } else {
            InventoryUtil.clickSlotNoSync(0, slot, 40, ContainerInput.SWAP, player);
        }
        mc.getConnection().send(new net.minecraft.network.protocol.game.ServerboundContainerClosePacket(0));

        if (oldItem != -1 && oldItem == slot) {
            oldItem = -1;
            oldOffhandStack = ItemStack.EMPTY;
            totemLocked = false;
        } else if (!currentOffhand.isEmpty() && oldItem == -1) {
            oldItem = slot;
            oldOffhandStack = currentOffhand;
        }

        if (swappingTotemToOffhand) {
            totemLocked = true;
        } else if (swappingSavedItemBack) {
            totemLocked = false;
        }

        ticks = 0;
    }

    @Override
    public void onDisable() {
        oldItem = -1;
        oldOffhandStack = ItemStack.EMPTY;
        totemLocked = false;
        ticks = 0;
        dangerCooldown = 0;
        safeTicks = 0;
    }
}