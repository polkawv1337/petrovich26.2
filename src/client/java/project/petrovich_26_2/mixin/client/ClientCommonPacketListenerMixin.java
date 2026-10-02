package project.petrovich_26_2.mixin.client;

import client_files.ClientikUtils.PacketUtil;
import client_files.ModuleManager;
import client_files.Petrovich.Movement.AirStuck;
import client_files.Petrovich.Player.FreeCamera;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonPacketListenerMixin {

    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void petrovich$onSend(Packet<?> packet, CallbackInfo ci) {
        if (PacketUtil.isSilent()) return;
        if (!ModuleManager.isReady()) return;

        if (FreeCamera.handlePacketSend(packet) || AirStuck.handlePacketSend(packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleDisconnect", at = @At("HEAD"))
    private void petrovich$onDisconnect(ClientboundDisconnectPacket packet, CallbackInfo ci) {
        FreeCamera.handleWorldGone();
        AirStuck.handleWorldGone();
    }
}
