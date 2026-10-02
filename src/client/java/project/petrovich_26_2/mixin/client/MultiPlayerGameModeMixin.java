package project.petrovich_26_2.mixin.client;

import client_files.Petrovich.Combat.AutoExplosion;
import client_files.Petrovich.Combat.CrystalOptimizer;
import client_files.Petrovich.Combat.ElytraLock;
import client_files.Petrovich.Combat.PacketCriticals;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void petrovich$onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult result,
                                       CallbackInfoReturnable<InteractionResult> cir) {
        if (hand != InteractionHand.MAIN_HAND) return;
        CrystalOptimizer.handleRightClickBlock(result);
        if (AutoExplosion.handleRightClickBlock(player, result)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void petrovich$onAttack(Player player, Entity target, CallbackInfo ci) {
        PacketCriticals.handlePreAttack(player, target);
        ElytraLock.onPlayerAttack(target);
    }
}