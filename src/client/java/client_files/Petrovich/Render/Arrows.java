package client_files.Petrovich.Render;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.SliderSetting;
import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import client_files.Petrovich.Combat.Aura;
import client_files.Petrovich.Player.FriendHelper;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class Arrows extends Module {

    private static final float RING_SMOOTHING = 9.0f;
    private static final float ARROW_SMOOTHING = 7.0f;
    private static final float CHAT_RADIUS_BONUS = 10.0f;

    private final SliderSetting radius = addSetting(new SliderSetting("Радиус", 58, 30, 110, 2));
    private final SliderSetting size = addSetting(new SliderSetting("Размер", 17, 11, 20, 1));
    private final BooleanSetting dinam = addSetting(new BooleanSetting("Динамический", true));
    private final BooleanSetting showName = addSetting(new BooleanSetting("Ник", true));
    private final BooleanSetting showDist = addSetting(new BooleanSetting("Дистанция", true));

    private static final String ARROW_TEXTURE = "arrows.png";

    /** Более "жирное" геометрическое начертание для ников и дистанций. */
    private static final String FONT = "montserrat";

    private Player lastTarget;
    private long lastTargetTime;

    private final Map<UUID, ArrowState> states = new HashMap<>();
    private long lastFrame = System.currentTimeMillis();
    private float currentRadius = 58.0f;
    private boolean radiusInit;

    private static final class ArrowState {
        float prog;
        float angle;
        String name = "";
        String dist = "";
        int color = 0xFFFFFFFF;
    }

    public Arrows() {
        super("Arrows", "Стрелки к игрокам в мире", Category.RENDER);
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
        if (mc.level == null || mc.player == null) {
            return;
        }

        long now = System.currentTimeMillis();
        float dt = Math.min(0.05f, (now - lastFrame) / 1000f);
        lastFrame = now;

        if (now - lastTargetTime > 15000) {
            lastTarget = null;
        }
        Entity currentTarget = Aura.target;
        if (currentTarget instanceof Player tracked) {
            lastTarget = tracked;
            lastTargetTime = now;
        }

        float base = radius.getValue();
        float targetRadius = base;
        if (mc.gui.screen() != null) {
            targetRadius += CHAT_RADIUS_BONUS;
        }
        if (isMoving() && dinam.getValue()) {
            targetRadius += 10.0f;
        }
        if (!radiusInit) {
            currentRadius = targetRadius;
            radiusInit = true;
        } else {
            currentRadius = approach(currentRadius, targetRadius, dt, RING_SMOOTHING);
        }

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 cameraPos = mc.gameRenderer.mainCamera().position();
        float cameraYaw = mc.gameRenderer.mainCamera().yRot();

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        Set<UUID> seen = new HashSet<>();
        for (Player player : mc.level.players()) {
            if (player == mc.player) {
                continue;
            }
            UUID id = player.getUUID();
            seen.add(id);

            Vec3 pos = player.getPosition(partialTick);
            double x = pos.x - cameraPos.x;
            double z = pos.z - cameraPos.z;

            float cos = Mth.cos((float) Math.toRadians(cameraYaw));
            float sin = Mth.sin((float) Math.toRadians(cameraYaw));
            float angle = (float) (Math.atan2(-(z * cos - x * sin), -(x * cos + z * sin)) * 180.0 / Math.PI);

            ArrowState st = states.computeIfAbsent(id, k -> new ArrowState());
            st.angle = angle;

            String name = player.getName().getString();
            if (name.length() > 7) {
                name = name.substring(0, 7);
            }
            st.name = name;
            st.dist = Math.min(99, (int) mc.player.distanceTo(player)) + "m";

            boolean highlighted = player == lastTarget && now - lastTargetTime <= 15000;
            if (highlighted) {
                st.color = RRender.accent();
            } else if (FriendHelper.isFriend(player.getName().getString())) {
                st.color = RRender.MINT;
            } else {
                st.color = RRender.accent();
            }
        }

        for (Map.Entry<UUID, ArrowState> entry : states.entrySet()) {
            ArrowState st = entry.getValue();
            float target = seen.contains(entry.getKey()) ? 1.0f : 0.0f;
            st.prog = approach(st.prog, target, dt, ARROW_SMOOTHING);
        }

        Iterator<Map.Entry<UUID, ArrowState>> it = states.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, ArrowState> entry = it.next();
            if (!seen.contains(entry.getKey()) && entry.getValue().prog <= 0.01f) {
                it.remove();
            }
        }

        for (ArrowState st : states.values()) {
            if (st.prog <= 0.01f) {
                continue;
            }
            float t = smoothStep(st.prog);
            float rad = currentRadius * t;
            float arrowX = (float) (rad * Mth.cos((float) Math.toRadians(st.angle)) + width / 2.0f);
            float arrowY = (float) (rad * Mth.sin((float) Math.toRadians(st.angle)) + height / 2.0f);

            float arrowSize = size.getValue() * (0.6f + 0.4f * st.prog);
            int alpha = quantizeAlpha(210 * st.prog);
            RRender.picture(graphics, ARROW_TEXTURE, arrowX, arrowY, arrowSize, arrowSize,
                    st.angle + 90, RRender.withAlpha(st.color, 255));

            if (showName.getValue()) {
                RRender.textCenterFamily(graphics, FONT, st.name, arrowX, arrowY - arrowSize / 2f - 10f, 7.2f,
                        RRender.withAlpha(RRender.TEXT, alpha));
            }
            if (showDist.getValue()) {
                RRender.textCenterFamily(graphics, FONT, st.dist, arrowX, arrowY + arrowSize / 2f + 3f, 7.2f,
                        RRender.withAlpha(RRender.TEXT_DIM, alpha));
            }
        }
    }

    /** Frame-rate independent exponential smoothing. */
    private static float approach(float current, float target, float dt, float speed) {
        float k = 1.0f - (float) Math.exp(-speed * dt);
        return current + (target - current) * k;
    }

    /** Alpha is bucketed so the font atlas cache never thrashes. */
    private static int quantizeAlpha(float value) {
        int a = Math.round(Math.max(0f, Math.min(255f, value)));
        if (a >= 250) {
            return 255;
        }
        return a & 0xC0;
    }

    private float smoothStep(float value) {
        float v = Math.max(0f, Math.min(1f, value));
        return v * v * (3f - 2f * v);
    }

    private boolean isMoving() {
        if (mc.player == null) {
            return false;
        }
        double vx = mc.player.getDeltaMovement().x;
        double vz = mc.player.getDeltaMovement().z;
        return vx * vx + vz * vz > 0.001 * 0.001;
    }
}
