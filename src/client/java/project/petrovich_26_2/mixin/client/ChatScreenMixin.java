package project.petrovich_26_2.mixin.client;

import client_files.ClientikUtils.ChatCommands;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {

    @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
    private void petrovich$onChatInput(String input, boolean addToHistory, CallbackInfo ci) {
        if (ChatCommands.dispatch(input)) {
            ci.cancel();
        }
    }
}