package project.petrovich_26_2.mixin.client;

import client_files.Petrovich.Movement.Timer;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(DeltaTracker.Timer.class)
public class DeltaTrackerTimerMixin {

    @ModifyVariable(method = "advanceGameTime", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private long petrovich$timerSpeed(long gameTime) {
        float multiplier = Timer.getMultiplier();
        return multiplier <= 0.0f ? gameTime : (long) (gameTime * multiplier);
    }
}