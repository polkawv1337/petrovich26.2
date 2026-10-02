package client_files.ClientikUtils;

import client_files.ClientikUtils.render.Animation;
import client_files.Petrovich.Render.Interface;
import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import client_files.ModuleManager;
import client_files.Petrovich.Render.ClickGui;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ClickGuiScreen extends Screen {

    private static final int ROW_H = 15;
    private static final int GAP = 6;
    private static final int COL_PAD = 8;
    private static final float NAME_SIZE = 9.0f;
    private static final float HEADER_SIZE = 10.0f;

    private static final int HEADER_H = 28;
    private static final int SET_TITLE_H = 20;
    private static final int SET_BOOL_H = 16;
    private static final int SET_SLIDER_H = 24;
    private static final int SET_ACCENT_H = 3;
    private static final int SET_PAD_BOTTOM = 6;

    private static final float SET_NAME_SIZE = 7.5f;
    private static final float SET_VALUE_SIZE = 6.0f;
    private static final float SET_MODE_SIZE = 7.5f;
    private static final float SET_PADDING = 4.5f;

    private static final float BOOL_TRACK_W = 26f;
    private static final float BOOL_TRACK_H = 12f;
    private static final float BOOL_KNOB = 10f;
    private static final float BOOL_KNOB_PAD = 2f;

    private static final float MODE_OPTION_H = 11f;
    private static final float MODE_NAME_H = 10f;
    private static final float MODE_GAP = 2f;

    private static final int TRACK_H = 4;

    private static final int THEME_W = 58;
    private static final int THEME_STRIP_H = 32;
    private static final int THEME_GAP = 4;
    private static final int THEME_TOP = 8;

    private static final int PICKER_X = 8;
    private static final int PICKER_Y = 8 + Theme.count() * (THEME_STRIP_H + THEME_GAP) + 8;
    private static final int PICKER_W = 86;
    private static final int PICKER_H = 158;
    private static final int PICKER_SV = 64;
    private static final int PICKER_HUE_X = 74;
    private static final int PICKER_HUE_W = 8;

    private final Animation anim = new Animation(0.0f, 4.5f);
    private final Map<BooleanSetting, Animation> switchAnims = new HashMap<>();
    private final Map<SliderSetting, Animation> sliderAnims = new HashMap<>();
    private Module selected;
    private Module hoveredModule;
    private SliderSetting dragSlider;
    private Module bindTarget;
    private BindSetting bindSettingTarget;
    private boolean pickerOpen;
    private int pickTheme;
    private Category pickCategory;
    private float pickH;
    private float pickS;
    private float pickV;
    private int sliderTrackX;
    private int sliderTrackW;

    private float[] colX;
    private float colW;
    private int colBaseH;
    private float colTop;
    private float[] colBodyTop;
    private int[] colPanelH;
    private int[] colContentH;
    private final float[] colScroll = new float[8];
    private final float[] colScrollTarget = new float[8];

    public ClickGuiScreen() {
        super(Component.literal(""));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private List<Module> columnModules(Category category) {
        List<Module> list = new ArrayList<>(ModuleManager.getInstance().getByCategory(category));
        list.removeIf(m -> m instanceof ClickGui);
        list.removeIf(Interface::handles);
        return list;
    }

    private static int catIndex(Category category) {
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            if (cats[i] == category) {
                return i;
            }
        }
        return 0;
    }

    private List<float[]> modeChips(ModeSetting setting, int sx, int sw, int y) {
        List<float[]> out = new ArrayList<>();
        float x = sx + SET_PADDING;
        float rowY = y + MODE_NAME_H + MODE_GAP;
        for (String mode : setting.getModes()) {
            float ow = RRender.textWidth(mode, SET_MODE_SIZE) + 8f;
            if (x + ow > sx + sw - SET_PADDING) {
                x = sx + SET_PADDING;
                rowY += MODE_OPTION_H + MODE_GAP;
            }
            out.add(new float[]{x, rowY, ow, MODE_OPTION_H});
            x += ow + MODE_GAP;
        }
        return out;
    }

    private int modeHeight(ModeSetting setting, int innerW) {
        List<float[]> chips = modeChips(setting, 0, innerW, 0);
        if (chips.isEmpty()) {
            return 0;
        }
        float[] last = chips.get(chips.size() - 1);
        return (int) Math.ceil(last[1] + last[3] + 2f);
    }

    private int settingRowH(Setting setting, int innerW) {
        if (setting instanceof SliderSetting) {
            return SET_SLIDER_H;
        }
        if (setting instanceof ModeSetting mode) {
            return Math.max(SET_BOOL_H, modeHeight(mode, innerW));
        }
        if (setting instanceof ModeListSetting modeList) {
            return SET_BOOL_H * (1 + modeList.getSettings().size());
        }
        if (setting instanceof BindSetting) {
            return SET_BOOL_H;
        }
        return SET_BOOL_H;
    }

    private int settingsBlockH(Module m) {
        int innerW = Math.max(40, (int) colW - 16);
        int h = SET_TITLE_H;
        for (Setting setting : m.getSettings()) {
            if (setting.isVisible()) {
                h += settingRowH(setting, innerW);
            }
        }
        return h + SET_PAD_BOTTOM;
    }

    private float ease(float v) {
        float x = Math.max(0f, Math.min(1f, v));
        return x * x * (3f - 2f * x);
    }

    private int animatedSettingsH(Module m) {
        return Math.round(settingsBlockH(m) * ease(anim.getValue()));
    }

    private int maxScrollFor(int i) {
        Category cat = Category.values()[i];
        int full = columnModules(cat).size() * ROW_H;
        if (selected != null && selected.getCategory() == cat) {
            full += settingsBlockH(selected);
        }
        int viewport = colPanelH[i] - HEADER_H;
        return Math.max(0, full - Math.max(0, viewport));
    }

    private void computeContentHeights() {
        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int h = columnModules(cats[i]).size() * ROW_H;
            if (selected != null && selected.getCategory() == cats[i]) {
                h += animatedSettingsH(selected);
            }
            colContentH[i] = h;
        }
    }

    private void computeLayout() {
        Category[] cats = Category.values();
        int n = cats.length;
        float maxName = 0f;
        int maxMods = 0;
        for (Category cat : cats) {
            List<Module> modules = columnModules(cat);
            maxMods = Math.max(maxMods, modules.size());
            for (Module m : modules) {
                maxName = Math.max(maxName, RRender.textWidth(m.getName(), NAME_SIZE));
            }
        }
        colW = Math.max(116f, maxName + 40);
        colBaseH = HEADER_H + maxMods * ROW_H + 28;

        colX = new float[n];
        float total = n * colW + (n - 1) * GAP;
        float left = Math.max(THEME_W + 16, (width - total) / 2f);
        for (int i = 0; i < n; i++) {
            colX[i] = left + i * (colW + GAP);
        }
        colTop = Math.max(14f, (height - colBaseH) / 2f - 26f);
        colBodyTop = new float[n];
        for (int i = 0; i < n; i++) {
            colBodyTop[i] = colTop + HEADER_H;
        }

        colPanelH = new int[n];
        colContentH = new int[n];
        computeContentHeights();

        int maxAvail = Math.max(HEADER_H, height - 8 - (int) colTop);
        for (int i = 0; i < n; i++) {
            colPanelH[i] = Math.min(colBaseH, maxAvail);
            int maxScroll = maxScrollFor(i);
            colScrollTarget[i] = Math.max(0, Math.min(colScrollTarget[i], maxScroll));
            colScroll[i] += (colScrollTarget[i] - colScroll[i]) * 0.25f;
            if (Math.abs(colScroll[i] - colScrollTarget[i]) < 0.05f) {
                colScroll[i] = colScrollTarget[i];
            }
        }
    }

    private boolean hasVisibleSettings(Module module) {
        for (Setting setting : module.getSettings()) {
            if (setting.isVisible()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        anim.update();
        hoveredModule = null;
        computeLayout();
        if (selected != null && anim.getValue() < 0.01f && anim.getTarget() == 0f) {
            selected = null;
        }
        RRender.fill(graphics, 0, 0, width, height, 0x40060A18);

        Category[] cats = Category.values();
        int selIdx = selected == null ? -1 : catIndex(selected.getCategory());
        for (int i = 0; i < cats.length; i++) {
            Category cat = cats[i];
            List<Module> modules = columnModules(cat);
            int cw = (int) colW;
            int cx = (int) colX[i];
            int cy = (int) colTop;
            int panelH = colPanelH[i];
            int viewportH = Math.max(0, panelH - HEADER_H);
            int scroll = Math.round(colScroll[i]);

            RRender.panel(graphics, cx, cy, cw, panelH);
            RRender.circle(graphics, cx + COL_PAD + 2, cy + 5, 5, RRender.accent());
            RRender.textCenter(graphics, cat.name(), cx + cw / 2f, cy + 9, HEADER_SIZE, RRender.accent());

            int bodyTop = (int) colBodyTop[i];
            graphics.enableScissor(cx + 1, bodyTop, cx + cw - 1, bodyTop + viewportH);
            int rowY = bodyTop - scroll;
            for (Module module : modules) {
                boolean isSel = module == selected;
                int cellH = ROW_H + (isSel ? animatedSettingsH(module) : 0);
                if (rowY + cellH < bodyTop) {
                    rowY += cellH;
                    continue;
                }
                if (rowY > bodyTop + viewportH) {
                    break;
                }
                int ry = rowY;
                boolean hovered = RRender.hovered(mouseX, mouseY, cx + 2, ry, cw - 4, ROW_H)
                        && mouseY >= bodyTop && mouseY < bodyTop + viewportH;
                if (hovered) {
                    hoveredModule = module;
                }
                if (hovered || isSel) {
                    RRender.rounded(graphics, cx + 3, ry, cw - 6, ROW_H, 5, isSel ? 0x26FFFFFF : RRender.HOVER);
                }
                RRender.accentStrip(graphics, cx + 3, ry + 4, 2, ROW_H - 8,
                        module.isEnabled() ? RRender.accent() : 0xFF2E3448);
                int textColor = module.isEnabled() ? RRender.accent()
                        : (hovered || isSel ? RRender.TEXT : RRender.TEXT_DIM);
                RRender.text(graphics, module.getName(), cx + COL_PAD + 4, ry + 3, NAME_SIZE, textColor);

                int chipX = (int) (cx + cw - COL_PAD - 7);
                int chipY = ry + 4;
                RRender.rounded(graphics, chipX, chipY, 7, 7, 3,
                        module.isEnabled() ? RRender.accent() : 0xFF3A4157);
                if (module.getScrollBind() != 0) {
                    RRender.text(graphics, module.getScrollBind() == 1 ? "^" : "v", chipX - 10, ry + 3, 8.0f,
                            RRender.accent());
                }
                rowY = ry + ROW_H;

                if (isSel) {
                    rowY = drawSettingsBlock(graphics, module, cx, rowY, cw, mouseX, mouseY);
                }
            }
            graphics.disableScissor();

            int maxScroll = maxScrollFor(i);
            if (maxScroll > 0) {
                int trackX = cx + cw - 3;
                int trackY = bodyTop;
                int trackH = viewportH;
                RRender.rounded(graphics, trackX, trackY, 3, trackH, 1, 0x14FFFFFF);
                float thumbH = Math.max(12, (float) trackH * trackH / (float) (trackH + maxScroll));
                float thumbY = (trackH - thumbH) * scroll / (float) maxScroll;
                RRender.rounded(graphics, trackX, (int) (trackY + thumbY), 3, (int) thumbH, 1, RRender.accent());
            }
        }

        renderThemeRail(graphics, mouseX, mouseY);
        renderPicker(graphics);
        renderTopHint(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void renderThemeRail(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int railX = 6;
        for (int i = 0; i < Theme.count(); i++) {
            int sy = THEME_TOP + i * (THEME_STRIP_H + THEME_GAP);
            boolean isCur = i == Theme.getCurrent();
            boolean hoverStrip = RRender.hovered(mouseX, mouseY, railX, sy, THEME_W, THEME_STRIP_H);
            RRender.panel(graphics, railX, sy, THEME_W, THEME_STRIP_H);
            if (hoverStrip && !isCur) {
                RRender.rounded(graphics, railX, sy, THEME_W, THEME_STRIP_H, 5, RRender.HOVER);
            }
            int col = Theme.color(i, Category.COMBAT);
            boolean overSwatch = RRender.hovered(mouseX, mouseY, railX + 5, sy + 6, 20, 20);
            RRender.rounded(graphics, railX + 8, sy + 6, 20, 20, 4, col);
            if (overSwatch) {
                RRender.outline(graphics, railX + 7, sy + 5, 22, 22, 5, 1, 0x66FFFFFF);
            }
            RRender.text(graphics, Theme.NAMES[i], railX + 32, sy + 12, 6.5f,
                    isCur ? RRender.TEXT : (hoverStrip ? RRender.TEXT_DIM : RRender.TEXT_FAINT));
            if (isCur) {
                RRender.pill(graphics, railX + THEME_W - 6, sy + 8, 3, THEME_STRIP_H - 16, RRender.TEXT);
            }
        }
    }

    private void renderPicker(GuiGraphicsExtractor graphics) {
        if (!pickerOpen) {
            return;
        }
        int px = PICKER_X;
        int py = PICKER_Y;
        RRender.panelLight(graphics, px, py, PICKER_W, PICKER_H);
        RRender.accentStrip(graphics, px + 3, py + 3, PICKER_W - 6, 2, Theme.color(pickTheme, Category.COMBAT));

        int svX = px + 6;
        int svY = py + 14;
        graphics.blit(RRender.hsGradient((int) pickH), svX, svY, svX + PICKER_SV, svY + PICKER_SV, 0.0f, 1.0f, 0.0f, 1.0f);
        int svCx = svX + (int) (pickS * PICKER_SV);
        int svCy = svY + (int) ((1.0f - pickV) * PICKER_SV);
        RRender.outline(graphics, svCx - 3, svCy - 3, 7, 7, 3, 1, 0xAA000000);
        RRender.circle(graphics, svCx, svCy, 5, 0xFFFFFFFF);

        int hueX = px + PICKER_HUE_X;
        int hueY = py + 14;
        for (int i = 0; i < 16; i++) {
            int top = hueY + i * 4;
            RRender.fill(graphics, hueX, top, PICKER_HUE_W, 5, RRender.hsvToArgb(i * 360 / 16, 1.0f, 1.0f));
        }
        RRender.pill(graphics, hueX - 1, hueY + (int) (pickH / 360f * PICKER_SV) - 2, PICKER_HUE_W + 2, 4, 0xFFFFFFFF);

        int cur = RRender.hsvToArgb((int) pickH, pickS, pickV);
        RRender.rounded(graphics, px + 6, py + 88, 44, 14, 4, cur);
        if (hasPickerOverride()) {
            RRender.rounded(graphics, px + 54, py + 88, 26, 14, 4, 0xFF2E3448);
            RRender.textCenter(graphics, "Сброс", px + 67, py + 92, 7.0f, RRender.TEXT_DIM);
        }
        RRender.textCenter(graphics, "Цвет", px + PICKER_W / 2f, py + 108, 8.0f, RRender.TEXT_DIM);
    }

    private boolean hasPickerOverride() {
        return pickCategory != null && Theme.hasOverride(pickTheme, pickCategory);
    }

    private void renderTopHint(GuiGraphicsExtractor graphics) {
        String text;
        if (bindSettingTarget != null) {
            text = "Нажми клавишу, кнопку мыши или прокрути колесо для \"" + bindSettingTarget.getName() + "\" (Esc - отмена)";
        } else if (bindTarget != null) {
            text = "Нажми клавишу, кнопку мыши или прокрути колесо для \"" + bindTarget.getName() + "\" (Esc - отмена)";
        } else if (hoveredModule != null) {
            String desc = hoveredModule.getDescription();
            text = hoveredModule.getName()
                    + (desc != null && !desc.isEmpty() ? " - " + desc : "");
        } else {
            text = "ЛКМ - вкл/выкл | ПКМ - настройки | СКМ - бинд | Колесо - листать";
        }
        float hintW = RRender.textWidth(text, 9.0f);
        RRender.rounded(graphics, (int) (width / 2f - hintW / 2f) - 9, 4, (int) hintW + 18, 15, 7, 0x66060A18);
        RRender.textCenter(graphics, text, width / 2f, 7, 9.0f, 0xFFEFEFF5);
    }

private int drawSettingsBlock(GuiGraphicsExtractor graphics, Module module, int sx, int y, int sw,
                              int mouseX, int mouseY) {
        int full = settingsBlockH(module);
        int animH = animatedSettingsH(module);
        if (animH < 2) {
            return y;
        }
        int accent = RRender.accent();
        graphics.enableScissor(sx + 1, y, sx + sw - 1, y + animH);

        RRender.rounded(graphics, sx + 3, y, sw - 6, Math.min(full, animH), 6, 0xC810121B);
        RRender.roundedCorners(graphics, sx + 3, y, sw - 6, SET_ACCENT_H, 6, 6, 0, 0, RRender.withAlpha(accent, 95));
        RRender.separator(graphics, sx + 6, y + SET_TITLE_H - 1, sw - 12);
        boolean hoverClose = RRender.hovered(mouseX, mouseY, sx + sw - 24, y + 5, 14, 12);
        RRender.textCenter(graphics, "x", sx + sw - 16, y + 6, 8.0f, hoverClose ? RRender.RED_SOFT : RRender.TEXT_DIM);

        int innerX = sx + 8;
        int innerW = sw - 16;
        int ry = y + SET_TITLE_H;
        boolean hasVisible = false;
        for (Setting setting : module.getSettings()) {
            if (!setting.isVisible()) {
                continue;
            }
            hasVisible = true;
            if (setting instanceof ToggleSetting toggleSetting) {
                toggleSetting.sync();
            }
            if (setting instanceof BooleanSetting booleanSetting) {
                ry = renderBoolean(graphics, booleanSetting, innerX, innerW, ry, mouseX, mouseY);
            } else if (setting instanceof SliderSetting sliderSetting) {
                ry = renderSlider(graphics, sliderSetting, innerX, innerW, ry, mouseX, mouseY, accent);
            } else if (setting instanceof ModeSetting modeSetting) {
                ry = renderMode(graphics, modeSetting, innerX, innerW, ry, mouseX, mouseY, accent);
            } else if (setting instanceof ModeListSetting modeListSetting) {
                ry = renderModeList(graphics, modeListSetting, innerX, innerW, ry, mouseX, mouseY, accent);
            } else if (setting instanceof BindSetting bindSetting) {
                ry = renderBind(graphics, bindSetting, innerX, innerW, ry, mouseX, mouseY, accent);
            }
        }
        if (!hasVisible) {
            RRender.text(graphics, "Нет настроек", innerX + 4, ry + 6, SET_NAME_SIZE,
                    RRender.withAlpha(RRender.TEXT_FAINT, 150));
        }
        graphics.disableScissor();
        return y + animH;
    }

    private int renderBind(GuiGraphicsExtractor graphics, BindSetting setting, int sx, int sw, int y,
                           int mouseX, int mouseY, int accent) {
    boolean hovered = RRender.hovered(mouseX, mouseY, sx, y, sw, SET_BOOL_H);
    boolean capturing = bindSettingTarget == setting;
    RRender.text(graphics, setting.getName(), sx + SET_PADDING, y + (SET_BOOL_H - SET_NAME_SIZE) / 2f + 1f,
            SET_NAME_SIZE, hovered ? RRender.TEXT : RRender.TEXT_DIM);

    String key = capturing ? "..." : setting.getKeyName();
    float kw = RRender.textWidth(key, SET_VALUE_SIZE);
    float chipW = Math.max(22f, kw + 8f);
    float bx = sx + sw - chipW - 6f;
    float by = y + (SET_BOOL_H - 10f) / 2f;
    RRender.rounded(graphics, Math.round(bx), Math.round(by), Math.round(chipW), 10, 5,
            capturing ? accent : (hovered ? 0xFF343B50 : 0xFF232939));
    RRender.textCenter(graphics, key, bx + chipW / 2f, by + 2f, SET_VALUE_SIZE,
            capturing ? 0xFF10131C : (hovered ? RRender.TEXT : RRender.TEXT_DIM));
    return y + SET_BOOL_H;
}

private int renderModeList(GuiGraphicsExtractor graphics, ModeListSetting setting, int sx, int sw, int y,
                               int mouseX, int mouseY, int accent) {
        boolean hovered = RRender.hovered(mouseX, mouseY, sx, y, sw, SET_BOOL_H);
        RRender.text(graphics, setting.getName(), sx + SET_PADDING, y + 4f, SET_NAME_SIZE + 0.5f,
                hovered ? accent : RRender.TEXT);
        RRender.separator(graphics, sx + 4, y + SET_BOOL_H - 1, sw - 8);
        int cy = y + SET_BOOL_H;
        for (BooleanSetting child : setting.getSettings()) {
            boolean on = setting.isEnabled(child.getName());
            boolean hover = RRender.hovered(mouseX, mouseY, sx, cy, sw, SET_BOOL_H);
            RRender.text(graphics, child.getName(), sx + SET_PADDING + 9f, cy + 4.5f, SET_NAME_SIZE,
                    hover ? RRender.TEXT : RRender.TEXT_DIM);
            RRender.text(graphics, on ? "on" : "off", sx + sw - SET_PADDING - 12f, cy + 4.5f, SET_NAME_SIZE,
                    on ? RRender.MINT : RRender.TEXT_FAINT);
            cy += SET_BOOL_H;
        }
        return cy;
    }

    private int renderBoolean(GuiGraphicsExtractor graphics, BooleanSetting setting, int sx, int sw, int y,
                              int mouseX, int mouseY) {
        boolean hovered = RRender.hovered(mouseX, mouseY, sx, y, sw, SET_BOOL_H);
        boolean on = setting.getValue();
        RRender.text(graphics, setting.getName(), sx + SET_PADDING, y + (SET_BOOL_H - SET_NAME_SIZE) / 2f + 1f,
                SET_NAME_SIZE, hovered ? RRender.TEXT : RRender.TEXT_DIM);

        int accent = RRender.accent();
        float trackX = sx + sw - BOOL_TRACK_W - 6f;
        float trackY = y + (SET_BOOL_H - BOOL_TRACK_H) / 2f;

        Animation a = switchAnims.computeIfAbsent(setting, s -> new Animation(on ? 1f : 0f, 9f));
        a.settleTo(on ? 1f : 0f);
        a.update();
        float t = ease(a.getValue());

        int trackColor = lerpArgb(RRender.withAlpha(0xFFFFFFFF, 26), RRender.withAlpha(accent, 95), t);
        RRender.rounded(graphics, Math.round(trackX), Math.round(trackY),
                Math.round(BOOL_TRACK_W), Math.round(BOOL_TRACK_H), Math.round(BOOL_TRACK_H / 2f), trackColor);

        float knobMinX = trackX + BOOL_KNOB_PAD + BOOL_KNOB / 2f;
        float knobMaxX = trackX + BOOL_TRACK_W - BOOL_KNOB_PAD - BOOL_KNOB / 2f;
        float knobCX = knobMinX + (knobMaxX - knobMinX) * t;
        int knobColor = RRender.mix(0xFFFFFFFF, accent, t);
        RRender.circle(graphics, Math.round(knobCX), Math.round(trackY + BOOL_TRACK_H / 2f),
                Math.round(BOOL_KNOB), knobColor);
        return y + SET_BOOL_H;
    }

    private int renderSlider(GuiGraphicsExtractor graphics, SliderSetting setting, int sx, int sw, int y,
                             int mouseX, int mouseY, int accent) {
        boolean hovered = RRender.hovered(mouseX, mouseY, sx, y, sw, SET_SLIDER_H);
        RRender.text(graphics, setting.getName(), sx + SET_PADDING, y + 2f, SET_NAME_SIZE,
                hovered ? RRender.TEXT : RRender.TEXT_DIM);
        String value = formatValue(setting.getValue());
        float vw = RRender.textWidth(value, SET_VALUE_SIZE);
        RRender.text(graphics, value, sx + sw - SET_PADDING - vw, y + 2.5f, SET_VALUE_SIZE, accent);

        float ratio = (setting.getValue() - setting.getMin()) / Math.max(0.0001f, setting.getMax() - setting.getMin());
        ratio = Math.max(0f, Math.min(1f, ratio));
        float ratioFinal = ratio;
        int trackX = sx + Math.round(SET_PADDING);
        int trackW = sw - Math.round(SET_PADDING * 2f);
        int trackY = y + 17;

        Animation a = sliderAnims.computeIfAbsent(setting, s -> new Animation(ratioFinal, 10f));
        a.settleTo(ratio);
        a.update();
        float fillW = a.getValue() * trackW;

        RRender.rounded(graphics, trackX, trackY, trackW, TRACK_H, TRACK_H / 2, 0xFF20242E);
        if (fillW > 1f) {
            RRender.rounded(graphics, trackX, trackY, Math.round(fillW), TRACK_H, TRACK_H / 2, accent);
        }
        int knobX = trackX + Math.round(fillW);
        RRender.circle(graphics, knobX, trackY + TRACK_H / 2, 7, 0xFFFFFFFF);
        return y + SET_SLIDER_H;
    }

    private int renderMode(GuiGraphicsExtractor graphics, ModeSetting setting, int sx, int sw, int y,
                           int mouseX, int mouseY, int accent) {
        RRender.text(graphics, setting.getName(), sx + SET_PADDING, y + 2f, SET_NAME_SIZE, RRender.TEXT);
        List<String> modes = setting.getModes();
        List<float[]> chips = modeChips(setting, sx, sw, y);
        for (int i = 0; i < modes.size() && i < chips.size(); i++) {
            String mode = modes.get(i);
            float[] c = chips.get(i);
            int cX = Math.round(c[0]);
            int cY = Math.round(c[1]);
            int cW = Math.round(c[2]);
            int cH = Math.round(c[3]);
            boolean active = mode.equals(setting.getValue());
            boolean hover = RRender.hovered(mouseX, mouseY, cX, cY, cW, cH);
            RRender.rounded(graphics, cX, cY, cW, cH, 4,
                    active ? accent : (hover ? 0xFF343B50 : 0xFF232939));
            RRender.textCenter(graphics, mode, cX + cW / 2f, cY + 2f, SET_MODE_SIZE,
                    active ? 0xFF10131C : (hover ? RRender.TEXT : RRender.TEXT_DIM));
        }
        return y + Math.max(SET_BOOL_H, modeHeight(setting, sw));
    }

    private static int lerpArgb(int a, int b, float t) {
        float f = Math.max(0f, Math.min(1f, t));
        int oa = (a >> 24) & 0xFF;
        int or = (a >> 16) & 0xFF;
        int og = (a >> 8) & 0xFF;
        int ob = a & 0xFF;
        int ba = (b >> 24) & 0xFF;
        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;
        int na = (int) (oa + (ba - oa) * f);
        int nr = (int) (or + (br - or) * f);
        int ng = (int) (og + (bg - og) * f);
        int nb = (int) (ob + (bb - ob) * f);
        return (na << 24) | (nr << 16) | (ng << 8) | nb;
    }

    private String formatValue(float v) {
        if (Math.abs(v - Math.round(v)) < 0.001f) {
            return String.valueOf(Math.round(v));
        }
        if (Math.abs(v) >= 100f) {
            return String.format("%.0f", v);
        }
        if (Math.abs(v) >= 10f) {
            return String.format("%.1f", v);
        }
        return String.format("%.2f", v);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        anim.update();
        if (colBodyTop == null || colX == null) {
            computeLayout();
        }
        int mx = (int) event.x();
        int my = (int) event.y();
        int button = event.button();
        boolean right = button == 1;

        if (bindTarget != null) {
            if (button >= 2) {
                bindTarget.setKey(BindSetting.MOUSE_BASE + button);
                bindTarget = null;
                Config.save();
                return true;
            }
            bindTarget = null;
        }
        if (bindSettingTarget != null) {
            if (button >= 2) {
                bindSettingTarget.setValue(BindSetting.MOUSE_BASE + button);
                bindSettingTarget = null;
                Config.save();
                return true;
            }
            bindSettingTarget = null;
        }

        if (pickerOpen) {
            int py = PICKER_Y;
            if (mx >= PICKER_X && mx < PICKER_X + PICKER_W && my >= py && my < py + PICKER_H) {
                applyPickerClick(mx, my);
                return true;
            }
            pickerOpen = false;
        }

        for (int i = 0; i < Theme.count(); i++) {
            int sy = THEME_TOP + i * (THEME_STRIP_H + THEME_GAP);
            if (mx >= 6 && mx < 6 + THEME_W && my >= sy && my < sy + THEME_STRIP_H) {
                boolean onSwatch = mx >= 6 + 5 && mx < 6 + 25 && my >= sy + 6 && my < sy + 26;
                if (onSwatch) {
                    pickerOpen = true;
                    pickTheme = i;
                    pickCategory = Category.COMBAT;
                    int color = Theme.color(i, Category.COMBAT);
                    float[] hsv = hsvFromArgb(color);
                    pickH = hsv[0];
                    pickS = hsv[1];
                    pickV = hsv[2];
                } else {
                    Theme.apply(i);
                }
                return true;
            }
        }

        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            List<Module> modules = columnModules(cats[i]);
            int cx = (int) colX[i];
            int cw = (int) colW;
            int bodyTop = (int) colBodyTop[i];
            int viewportH = Math.max(0, colPanelH[i] - HEADER_H);
            if (mx < cx + 1 || mx >= cx + cw - 1 || my < bodyTop || my >= bodyTop + viewportH) {
                continue;
            }
            int rowY = bodyTop - Math.round(colScroll[i]);
            for (Module module : modules) {
                int ry = rowY;
                if (ry + ROW_H >= bodyTop && ry <= bodyTop + viewportH
                        && mx >= cx + 2 && mx < cx + cw - 2 && my >= ry && my < ry + ROW_H) {
                    if (button == 2) {
                        bindTarget = bindTarget == module ? null : module;
                    } else if (right) {
                        if (selected == module) {
                            closeSettings();
                        } else if (hasVisibleSettings(module)) {
                            selectModule(module);
                        }
                    } else {
                        if (selected == module) {
                            closeSettings();
                        }
                        module.toggle();
                    }
                    return true;
                }
                rowY += ROW_H;

                if (module == selected) {
                    int animH = animatedSettingsH(module);
                    if (animH > 2) {
                        if (mx >= cx + cw - 24 && mx < cx + cw - 10
                                && my >= rowY + 5 && my < rowY + 17) {
                            closeSettings();
                            return true;
                        }
                        int innerX = cx + 8;
                        int innerW = cw - 16;
                        int sy = rowY + SET_TITLE_H;
                        for (Setting setting : selected.getSettings()) {
                            if (!setting.isVisible()) {
                                continue;
                            }
                            int rh = settingRowH(setting, innerW);
                            if (my >= sy && my < sy + rh && mx >= innerX && mx < innerX + innerW) {
                                if (setting instanceof BooleanSetting bool) {
                                    bool.toggle();
                                } else if (setting instanceof ModeSetting mode) {
                                    List<String> modes = mode.getModes();
                                    List<float[]> chips = modeChips(mode, innerX, innerW, sy);
                                    for (int j = 0; j < modes.size() && j < chips.size(); j++) {
                                        float[] c = chips.get(j);
                                        if (mx >= Math.round(c[0]) && mx < Math.round(c[0] + c[2])
                                                && my >= Math.round(c[1]) && my < Math.round(c[1] + c[3])) {
                                            mode.setValue(modes.get(j));
                                            return true;
                                        }
                                    }
                                } else if (setting instanceof ModeListSetting modeListSetting) {
                                    int cr = sy + SET_BOOL_H;
                                    for (BooleanSetting child : modeListSetting.getSettings()) {
                                        if (mx >= innerX && mx < innerX + innerW && my >= cr && my < cr + SET_BOOL_H) {
                                            modeListSetting.toggle(child.getName());
                                            Config.save();
                                            return true;
                                        }
                                        cr += SET_BOOL_H;
                                    }
                                } else if (setting instanceof BindSetting bind) {
                                    bindSettingTarget = bindSettingTarget == bind ? null : bind;
                                    return true;
                                } else if (setting instanceof SliderSetting slider) {
                                    dragSlider = slider;
                                    sliderTrackX = innerX + Math.round(SET_PADDING);
                                    sliderTrackW = innerW - Math.round(SET_PADDING * 2f);
                                    applySlider(slider, mx);
                                }
                                return true;
                            }
                            sy += rh;
                        }
                    }
                    rowY += animH;
                }
            }
        }

        if (selected != null) {
            closeSettings();
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        if (bindTarget != null) {
            bindTarget.setKey(scrollY > 0 ? BindSetting.SCROLL_UP : BindSetting.SCROLL_DOWN);
            bindTarget = null;
            Config.save();
            return true;
        }
        if (bindSettingTarget != null) {
            bindSettingTarget.setValue(scrollY > 0 ? BindSetting.SCROLL_UP : BindSetting.SCROLL_DOWN);
            bindSettingTarget = null;
            Config.save();
            return true;
        }
        if (colBodyTop == null || colX == null) {
            computeLayout();
        }
        int mx = (int) mouseX;
        int my = (int) mouseY;

        Category[] cats = Category.values();
        for (int i = 0; i < cats.length; i++) {
            int cx = (int) colX[i];
            int cw = (int) colW;
            int bodyTop = (int) colBodyTop[i];
            int viewportH = Math.max(0, colPanelH[i] - HEADER_H);
            if (mx >= cx && mx < cx + cw && my >= bodyTop && my < bodyTop + viewportH) {
                int maxScroll = maxScrollFor(i);
                int step = scrollY > 0 ? -16 : 16;
                colScrollTarget[i] = Math.max(0, Math.min(maxScroll, colScrollTarget[i] + step));
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragSlider = null;
        Config.save();
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragSlider != null) {
            applySlider(dragSlider, (int) event.x());
            return true;
        }
        if (pickerOpen) {
            applyPickerDrag((int) event.x(), (int) event.y());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    private void applyPickerDrag(int mx, int my) {
        int svX = PICKER_X + 6;
        int svY = PICKER_Y + 14;
        if (mx >= svX && mx < svX + PICKER_SV && my >= svY && my < svY + PICKER_SV) {
            pickS = clamp01((mx - svX) / (float) (PICKER_SV - 1));
            pickV = clamp01(1.0f - (my - svY) / (float) (PICKER_SV - 1));
            commitPickerColor();
        } else if (mx >= PICKER_X + PICKER_HUE_X && mx < PICKER_X + PICKER_HUE_X + PICKER_HUE_W
                && my >= svY && my < svY + PICKER_SV) {
            pickH = clamp01((my - svY) / (float) (PICKER_SV - 1)) * 360f;
            commitPickerColor();
        }
    }

    private void applyPickerClick(int mx, int my) {
        int py = PICKER_Y;
        if (hasPickerOverride()
                && mx >= PICKER_X + 54 && mx < PICKER_X + 80 && my >= py + 88 && my < py + 102) {
            Theme.resetColor(pickTheme, pickCategory);
            pickerOpen = false;
            return;
        }
        applyPickerDrag(mx, my);
        int svX = PICKER_X + 6;
        int svY = py + 14;
        if (!(mx >= svX && mx < svX + PICKER_SV && my >= svY && my < svY + PICKER_SV)
                && !(mx >= PICKER_X + PICKER_HUE_X && mx < PICKER_X + PICKER_HUE_X + PICKER_HUE_W
                        && my >= svY && my < svY + PICKER_SV)) {
            pickerOpen = false;
        }
    }

    private void commitPickerColor() {
        Theme.setColor(pickTheme, pickCategory, RRender.hsvToArgb((int) pickH, pickS, pickV));
    }

    private static float clamp01(float v) {
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    private static float[] hsvFromArgb(int argb) {
        float rf = ((argb >> 16) & 0xFF) / 255.0f;
        float gf = ((argb >> 8) & 0xFF) / 255.0f;
        float bf = (argb & 0xFF) / 255.0f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float d = max - min;
        float h;
        if (d == 0.0f) {
            h = 0.0f;
        } else if (max == rf) {
            h = 60.0f * (((gf - bf) / d) % 6.0f);
        } else if (max == gf) {
            h = 60.0f * ((bf - rf) / d + 2.0f);
        } else {
            h = 60.0f * ((rf - gf) / d + 4.0f);
        }
        if (h < 0.0f) {
            h += 360.0f;
        }
        return new float[]{h, max == 0.0f ? 0.0f : d / max, max};
    }

    private void applySlider(SliderSetting setting, int mouseX) {
        float ratio = (mouseX - sliderTrackX) / (float) Math.max(1, sliderTrackW);
        setting.setValue(setting.getMin() + ratio * (setting.getMax() - setting.getMin()));
    }

    private void selectModule(Module module) {
        selected = module;
        dragSlider = null;
        anim.settleTo(1.0f);
        computeLayout();

        int idx = catIndex(module.getCategory());
        int li = columnModules(module.getCategory()).indexOf(module);
        int viewport = colPanelH[idx] - HEADER_H;
        int contentH = li * ROW_H + ROW_H + settingsBlockH(module);
        int target = Math.max(0, Math.min(contentH - viewport, li * ROW_H - ROW_H));
        colScrollTarget[idx] = Math.max(0, Math.min(maxScrollFor(idx), target));
    }

    private void closeSettings() {
        dragSlider = null;
        anim.settleTo(0.0f);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (bindSettingTarget != null) {
            if (event.key() == 256) {
                bindSettingTarget = null;
                return true;
            }
            bindSettingTarget.setValue(event.key());
            bindSettingTarget = null;
            Config.save();
            return true;
        }
        if (bindTarget != null) {
            if (event.key() == 256) {
                bindTarget = null;
                return true;
            }
            bindTarget.setKey(event.key());
            bindTarget = null;
            Config.save();
            return true;
        }
        if (event.key() == 256) {
            if (selected != null) {
                closeSettings();
                return true;
            }
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }
}