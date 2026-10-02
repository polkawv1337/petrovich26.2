package project.petrovich_26_2.mixin.client;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {

    @Accessor("destroyDelay")
    int getDestroyDelay();

    @Invoker("ensureHasSentCarriedItem")
    void petrovich$syncCarriedItem();
}