package project.petrovich_26_2.mixin.client;

import client_files.Petrovich.Misc.EditBoxTrimLeftPad;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
public abstract class EditBoxMixin implements EditBoxTrimLeftPad {

    @Shadow
    private int textX;

    @Unique
    private boolean petrovich$trimLeftPad;

    @Override
    public void petrovich$setTrimLeftPad(boolean trim) {
        this.petrovich$trimLeftPad = trim;
    }

    @Inject(method = "updateTextPosition", at = @At("TAIL"))
    private void petrovich$trimLeftPadding(CallbackInfo ci) {
        if (this.petrovich$trimLeftPad) {
            this.textX = ((EditBox) (Object) this).getX() + 1;
        }
    }
}