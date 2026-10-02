package project.petrovich_26_2.mixin.client;

import client_files.ClientikUtils.Notifier;
import client_files.Module;
import client_files.ModuleManager;
import client_files.Petrovich.Render.Interface;
import client_files.render.render.core.context.RenderContext;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class HudMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void petrovich$onHudRender(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        // 2D-контекст движка рендера: внутри него доступны Render2D/RenderAPI,
        // батчинг и текст, даже если этот инжект выполнится раньше mixin-а движка
        RenderContext.enter2D(graphics, deltaTracker);
        try {
            Notifier.tick();
            for (Module module : ModuleManager.getInstance().getModules()) {
                if (module.isEnabled()) {
                    module.onRender(graphics);
                }
            }
            if (!Interface.legacyActive()) {
                Notifier.render(graphics);
            }
        } finally {
            RenderContext.exit2D();
        }
    }
}
