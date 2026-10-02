package project.petrovich_26_2.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int petrovich$getLeftPos();

    @Accessor("topPos")
    int petrovich$getTopPos();

    @Accessor("imageWidth")
    int petrovich$getImageWidth();

    @Accessor("imageHeight")
    int petrovich$getImageHeight();

    @Accessor("menu")
    AbstractContainerMenu petrovich$getMenu();
}