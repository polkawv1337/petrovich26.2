package project.petrovich_26_2.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

    @Unique
    private static final int FIRST_DROPPABLE_SLOT = 5;

    @Unique
    private static final int LAST_DROPPABLE_SLOT = 45;

    @Inject(method = "init", at = @At("TAIL"))
    private void petrovich$addDropAllButton(CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen) (Object) this;
        AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor) (Object) this;
        Minecraft mc = Minecraft.getInstance();

        int buttonWidth = 100;
        int x = accessor.petrovich$getLeftPos() + (accessor.petrovich$getImageWidth() - buttonWidth) / 2;
        int y = accessor.petrovich$getTopPos() - 26;

        Button button = Button.builder(Component.literal("Выкинуть всё"), btn -> petrovich$dropAll(accessor))
                .bounds(x, y, buttonWidth, 18)
                .build();

        ScreenInvoker invoker = (ScreenInvoker) (Object) this;
        invoker.petrovich$addRenderableWidget(button);
    }

    @Unique
    private static void petrovich$dropAll(AbstractContainerScreenAccessor accessor) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.gameMode == null) {
            return;
        }
        AbstractContainerMenu menu = accessor.petrovich$getMenu();
        if (!menu.stillValid(player)) {
            return;
        }
        int containerId = menu.containerId;
        for (Slot slot : menu.slots) {
            int index = slot.index;
            if (index < FIRST_DROPPABLE_SLOT || index > LAST_DROPPABLE_SLOT) {
                continue;
            }
            if (!slot.hasItem() || !slot.mayPickup(player)) {
                continue;
            }
            mc.gameMode.handleContainerInput(containerId, index, 1, ContainerInput.THROW, player);
        }
    }
}