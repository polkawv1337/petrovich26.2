package project.petrovich_26_2.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(targets = "com.viaversion.fabric.ViaFabricClient")
public class ViaFabricButtonMixin {

    @Redirect(method = "lambda$registerGui$0",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private boolean petrovich$cancelViaButton(List<?> list, Object element) {
        return false;
    }
}