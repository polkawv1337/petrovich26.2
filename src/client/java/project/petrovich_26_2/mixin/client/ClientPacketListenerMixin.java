package project.petrovich_26_2.mixin.client;

import client_files.ModuleManager;
import client_files.Petrovich.Combat.ElytraLock;
import client_files.Petrovich.Combat.Velocity;
import client_files.Petrovich.Misc.AntiCrash;
import client_files.Petrovich.Movement.AirStuck;
import client_files.Petrovich.Movement.FireworkBoost;
import client_files.Petrovich.Player.Assistant;
import client_files.Petrovich.Player.AutoKitFarm;
import client_files.Petrovich.Player.FreeCamera;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleEntityEvent", at = @At("HEAD"), cancellable = true)
    private void petrovich$onEntityEvent(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        if (ElytraLock.shouldCancelEntityStatus(packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true)
    private void petrovich$onSetEntityMotion(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        if (ElytraLock.shouldCancelEntityMotion(packet)) {
            ci.cancel();
        } else if (Velocity.shouldCancelMotion(packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleExplosion", at = @At("HEAD"), cancellable = true)
    private void petrovich$onExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
        if (AntiCrash.shouldCancelExplosion(packet) || Velocity.shouldCancelExplosion(packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    private void petrovich$onSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        if (packet.overlay()) {
            return;
        }
        Assistant.onGameMessage(packet.content().getString());
        AutoKitFarm.onGameMessage(packet.content().getString());
    }

    @Inject(method = "handlePlayerChat", at = @At("HEAD"))
    private void petrovich$onPlayerChat(ClientboundPlayerChatPacket packet, CallbackInfo ci) {
        String text = packet.body() != null ? packet.body().content() : "";
        if (text.isEmpty() && packet.unsignedContent() != null) {
            text = packet.unsignedContent().getString();
        }
        Assistant.onGameMessage(text);
        AutoKitFarm.onGameMessage(text);
    }

    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void petrovich$onMovePlayer(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        FireworkBoost.notifyFlag();
        if (!ModuleManager.isReady()) return;
        if (FreeCamera.handlePositionPacket(packet) || AirStuck.handlePositionPacket(packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleRespawn", at = @At("HEAD"))
    private void petrovich$onRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        FreeCamera.handleWorldGone();
        AirStuck.handleWorldGone();
    }

    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void petrovich$onLogin(ClientboundLoginPacket packet, CallbackInfo ci) {
        FreeCamera.handleWorldGone();
        AirStuck.handleWorldGone();
    }
}