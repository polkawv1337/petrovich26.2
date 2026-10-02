package client_files.Petrovich.Render;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.SliderSetting;
import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class Coords extends Module {

    private final SliderSetting offset = addSetting(new SliderSetting("Высота над прицелом", 110.0f, 40.0f, 250.0f, 1.0f));
    private final SliderSetting arrowSize = addSetting(new SliderSetting("Размер стрелки", 22.0f, 8.0f, 48.0f, 1.0f));
    private final BooleanSetting showDistance = addSetting(new BooleanSetting("Показывать дистанцию", true));
    private final BooleanSetting showCoords = addSetting(new BooleanSetting("Показывать координаты метки", false));

    public Coords() {
        super("Coords", "Координаты игрока", Category.RENDER);
        setHud(4, 130);
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
        GpsTarget gps = GpsTarget.get();
        if (!gps.isEnabled() || mc.level == null || mc.player == null) {
            return;
        }

        Player player = mc.player;
        double dx = gps.getTargetX() - player.getX();
        double dy = gps  .getTargetY() - player.getY();
        double dz = gps.getTargetZ() - player.getZ();
        int dist = (int) Math.sqrt(dx * dx + dz * dz);

        float angle = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
        float rot = Mth.wrapDegrees(angle - player.getYRot());

        float size = arrowSize.getValue();
        float cx = graphics.guiWidth() / 2.0f;
        float cy = graphics.guiHeight() / 2.0f - offset.getValue();

        RRender.picture(graphics, "triangle.png", cx, cy, size, size, rot - 45, RRender.withAlpha(RRender.accent(), 220));

        float textY = cy + size / 2.0f + 3.0f;
        if (showDistance.getValue()) {
            RRender.textCenter(graphics, dist + "m", cx, textY, 7, RRender.TEXT);
            textY += 8.0f;
        }
        if (showCoords.getValue()) {
            RRender.textCenter(graphics,
                    String.format("%.0f / %.0f / %.0f", gps.getTargetX(), gps.getTargetY(), gps.getTargetZ()),
                    cx, textY, 7, RRender.TEXT_DIM);
        }
        if (dy > 8.0 || dy < -8.0) {
            RRender.textCenter(graphics, (int) dy + "Y", cx, cy - size / 2.0f - 10.0f, 7, RRender.TEXT_DIM);
        }
    }
}