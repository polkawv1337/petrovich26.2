package client_files.Petrovich.Render;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.HudEditor;
import client_files.ClientikUtils.ModeSetting;
import client_files.ClientikUtils.SliderSetting;
import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import client_files.render.render.core.SmoothRender;
import client_files.render.render.core.color.ColorRGBA;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Donut extends Module {

    private final SliderSetting size = addSetting(new SliderSetting("Диаметр", 28, 8, 120, 1));
    private final SliderSetting hole = addSetting(new SliderSetting("Дырка", 12, 2, 60, 1));
    private final ModeSetting corner = addSetting(new ModeSetting("Угол", "Правый верхний",
            "Правый верхний", "Левый верхний", "Правый нижний", "Левый нижний"));
    private final SliderSetting offset = addSetting(new SliderSetting("Отступ", 8, 0, 120, 1));

    public Donut() {
        super("Donut", "Белый круг с дыркой в углу экрана", Category.RENDER);
        setHud(0, 0);
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
        int diameter = (int) size.getValue();
        int holeSize = Math.min((int) hole.getValue(), diameter - 2);
        if (diameter <= 2 || holeSize <= 0) {
            return;
        }

        float margin = offset.getValue();
        int x = switch (corner.getValue()) {
            case "Левый верхний" -> (int) margin;
            case "Правый нижний" -> graphics.guiWidth() - (int) margin - diameter;
            case "Левый нижний" -> (int) margin;
            default -> graphics.guiWidth() - (int) margin - diameter;
        };
        int y = switch (corner.getValue()) {
            case "Левый верхний" -> (int) margin;
            case "Правый нижний" -> graphics.guiHeight() - (int) margin - diameter;
            case "Левый нижний" -> graphics.guiHeight() - (int) margin - diameter;
            default -> (int) margin;
        };

        float cx = x + diameter / 2.0f;
        float cy = y + diameter / 2.0f;
        float outer = diameter / 2.0f;
        float inner = holeSize / 2.0f;

        ColorRGBA white = ColorRGBA.of(255, 255, 255, 255);
        ColorRGBA clear = ColorRGBA.of(0, 0, 0, 0);
        SmoothRender.get().drawRing(graphics, cx, cy, outer, inner, white, clear);
    }
}