package client_files.Petrovich.Movement;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.ModeSetting;
import client_files.ClientikUtils.SliderSetting;
import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ElytraBooster extends Module {

    private final ModeSetting preset = addSetting(new ModeSetting("Пресет", "Нет", "SvinWorld", "Bravo"));
    private final ModeSetting resetPreset = addSetting(new ModeSetting("Сбросить пресет", "Нет", "Сбросить"));
    private final BooleanSetting showAngles = addSetting(new BooleanSetting("Показывать углы", false));
    private final BooleanSetting autoTune = addSetting(new BooleanSetting("Авто-подбор", false));

    private static final float TUNE_STEP = 0.01f;
    private static final int RECOVER_DELAY_TICKS = 40;
    private static final int RECOVER_INTERVAL_TICKS = 20;
    private static final long BOOST_WINDOW_MS = 1000;

    private int lastXzIdx;
    private int lastYIdx;
    private boolean lastDominantXZ = true;
    private int tuneSinceFlag = Integer.MAX_VALUE / 2;
    private long lastBoostMs;

    private final SliderSetting xz0_5 = addSetting(new SliderSetting("XZ 0-5\u00b0", 1.52f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting xz5_10 = addSetting(new SliderSetting("XZ 5-10\u00b0", 1.53f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting xz10_15 = addSetting(new SliderSetting("XZ 10-15\u00b0", 1.54f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting xz15_20 = addSetting(new SliderSetting("XZ 15-20\u00b0", 1.55f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting xz20_25 = addSetting(new SliderSetting("XZ 20-25\u00b0", 1.56f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting xz25_30 = addSetting(new SliderSetting("XZ 25-30\u00b0", 1.57f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting xz30_35 = addSetting(new SliderSetting("XZ 30-35\u00b0", 1.58f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting xz35_40 = addSetting(new SliderSetting("XZ 35-40\u00b0", 1.59f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting xz40_45 = addSetting(new SliderSetting("XZ 40-45\u00b0", 1.60f, 1.5f, 3.0f, 0.01f));

    private final SliderSetting y0_5 = addSetting(new SliderSetting("Y 0-5\u00b0", 1.51f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting y5_10 = addSetting(new SliderSetting("Y 5-10\u00b0", 1.52f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting y10_15 = addSetting(new SliderSetting("Y 10-15\u00b0", 1.53f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting y15_20 = addSetting(new SliderSetting("Y 15-20\u00b0", 1.54f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting y20_25 = addSetting(new SliderSetting("Y 20-25\u00b0", 1.55f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting y25_30 = addSetting(new SliderSetting("Y 25-30\u00b0", 1.56f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting y30_35 = addSetting(new SliderSetting("Y 30-35\u00b0", 1.57f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting y35_40 = addSetting(new SliderSetting("Y 35-40\u00b0", 1.58f, 1.5f, 3.0f, 0.01f));
    private final SliderSetting y40_45 = addSetting(new SliderSetting("Y 40-45\u00b0", 1.59f, 1.5f, 3.0f, 0.01f));

    private final SliderSetting[] xzSliders = {xz0_5, xz5_10, xz10_15, xz15_20, xz20_25, xz25_30, xz30_35, xz35_40, xz40_45};
    private final SliderSetting[] ySliders = {y0_5, y5_10, y10_15, y15_20, y20_25, y25_30, y30_35, y35_40, y40_45};

    private static final float[] DEFAULT_XZ = {1.52f, 1.53f, 1.54f, 1.55f, 1.56f, 1.57f, 1.58f, 1.59f, 1.60f};
    private static final float[] DEFAULT_Y = {1.51f, 1.52f, 1.53f, 1.54f, 1.55f, 1.56f, 1.57f, 1.58f, 1.59f};

    private static final float[] SVINWORLD_XZ = {1.91f, 1.91f, 1.91f, 1.91f, 1.91f, 1.91f, 1.91f, 1.91f, 1.91f};
    private static final float[] SVINWORLD_Y = {1.91f, 1.89f, 1.91f, 1.91f, 1.97f, 2.06f, 2.11f, 2.20f, 2.70f};

    private static final float[] BRAVO_XZ = {1.97f, 1.98f, 1.99f, 2.00f, 2.01f, 2.02f, 2.03f, 2.04f, 2.04f};
    private static final float[] BRAVO_Y = {1.64f, 1.69f, 1.75f, 1.80f, 1.86f, 1.91f, 1.96f, 2.02f, 2.04f};

    private float currentPitch;
    private float currentYaw;
    private float currentBPS;
    private String currentMode = "";
    private String lastPreset = "Нет";
    private String lastReset = "Нет";

    public ElytraBooster() {
        super("ElytraBooster", "Увеличивает скорость фейерверка", Category.MOVEMENT);
        FireworkBoost.setBooster(this);
    }

    public float computeBoost(LivingEntity boosted, float original) {
        if (mc.player == null || boosted != mc.player) {
            return original;
        }

        float pitch = Math.abs(Mth.wrapDegrees(mc.player.getXRot()));
        float yaw = Math.abs(Mth.wrapDegrees(mc.player.getYRot()));
        yaw = yaw > 180f ? 360f - yaw : yaw;
        yaw = yaw > 90f ? 180f - yaw : yaw;

        currentPitch = pitch;
        currentYaw = yaw;

        float normalizedYaw = yaw % 180;
        if (normalizedYaw > 90) normalizedYaw -= 180;
        else if (normalizedYaw < -90) normalizedYaw += 180;

        float normalizedPitch = pitch % 180;
        if (normalizedPitch > 90) normalizedPitch -= 180;
        else if (normalizedPitch < -90) normalizedPitch += 180;

        currentMode = "Кастомный";
        float speedXZ = getCustomSpeed(normalizedYaw, xzSliders);
        float speedY = getCustomSpeed(normalizedPitch, ySliders);
        lastXzIdx = getBucketIndex(normalizedYaw, xzSliders.length);
        lastYIdx = getBucketIndex(normalizedPitch, ySliders.length);
        lastDominantXZ = speedXZ >= speedY;

        lastBoostMs = System.currentTimeMillis();
        currentBPS = Math.max(speedXZ, speedY) * 19.2f;

        return Math.max(speedXZ, speedY);
    }

    public void onFlagged() {
        if (mc.player == null || !mc.player.isFallFlying()) return;
        if (System.currentTimeMillis() - lastBoostMs > BOOST_WINDOW_MS) return;

        SliderSetting slider = lastDominantXZ ? xzSliders[lastXzIdx] : ySliders[lastYIdx];
        float oldValue = slider.getValue();
        slider.setValue(oldValue - TUNE_STEP);
        tuneSinceFlag = 0;

        client_files.ClientikUtils.ChatHelper.print(
                "\u00a7cФлаг по " + (lastDominantXZ ? "XZ" : "Y") + "! "
                        + angleRange(lastDominantXZ ? currentYaw : currentPitch,
                        lastDominantXZ ? xzSliders.length : ySliders.length) + "\u00b0 "
                        + fmt(oldValue) + " \u2192 " + fmt(slider.getValue()));
    }

    @Override
    public void onTick() {
        String p = preset.getValue();
        if (!p.equals(lastPreset)) {
            lastPreset = p;
            if (p.equals("SvinWorld")) applyValues(SVINWORLD_XZ, SVINWORLD_Y);
            else if (p.equals("Bravo")) applyValues(BRAVO_XZ, BRAVO_Y);
        }
        String r = resetPreset.getValue();
        if (!r.equals(lastReset)) {
            lastReset = r;
            if (r.equals("Сбросить")) {
                applyValues(DEFAULT_XZ, DEFAULT_Y);
                lastPreset = "Нет";
                lastReset = "Нет";
                preset.setValue("Нет");
                resetPreset.setValue("Нет");
            }
        }

        if (!autoTune.getValue()) return;
        if (mc.player == null || !mc.player.isFallFlying()) return;
        if (System.currentTimeMillis() - lastBoostMs > BOOST_WINDOW_MS) return;

        ++tuneSinceFlag;
        if (tuneSinceFlag > RECOVER_DELAY_TICKS && tuneSinceFlag % RECOVER_INTERVAL_TICKS == 0) {
            SliderSetting slider = lastDominantXZ ? xzSliders[lastXzIdx] : ySliders[lastYIdx];
            float oldValue = slider.getValue();
            slider.setValue(oldValue + TUNE_STEP);
            client_files.ClientikUtils.ChatHelper.print(
                    "\u00a7aВосстановление " + (lastDominantXZ ? "XZ" : "Y") + " "
                            + fmt(oldValue) + " \u2192 " + fmt(slider.getValue()));
        }
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
        if (!showAngles.getValue() || mc.player == null) return;

        String text = "XZ " + angleRange(currentYaw, xzSliders.length) + "\u00b0 | Y "
                + angleRange(currentPitch, ySliders.length) + "\u00b0 | BPS: "
                + String.format("%.1f", currentBPS) + " | " + currentMode;

        if (autoTune.getValue()) {
            text += String.format(" | Авто: %.2f/%.2f",
                    xzSliders[lastXzIdx].getValue(), ySliders[lastYIdx].getValue());
        }

        RRender.textCenter(graphics, text, graphics.guiWidth() / 2f, graphics.guiHeight() / 2f + 5, 7, RRender.TEXT);
    }

    private String angleRange(float angle, int sliderCount) {
        int index = getBucketIndex(angle, sliderCount);
        int low = index * 5;
        return low + "-" + (low + 5);
    }

    private String fmt(float value) {
        return String.format("%.2f", value);
    }

    private float getCustomSpeed(float angle, SliderSetting[] sliders) {
        return sliders[getBucketIndex(angle, sliders.length)].getValue();
    }

    private int getBucketIndex(float angle, int sliderCount) {
        int index = (int) (Math.abs(angle) / 5.0f);
        if (index >= sliderCount) index = sliderCount - 1;
        if (index < 0) index = 0;
        return index;
    }

    private void applyValues(float[] xzValues, float[] yValues) {
        for (int i = 0; i < xzSliders.length; i++) {
            xzSliders[i].setValue(xzValues[i]);
        }
        for (int i = 0; i < ySliders.length; i++) {
            ySliders[i].setValue(yValues[i]);
        }
    }
}