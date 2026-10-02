package client_files.Petrovich.Player;

import client_files.ClientikUtils.BindSetting;
import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.ChatHelper;
import client_files.Module;
import client_files.ModuleManager;
import client_files.Petrovich.Render.GpsTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assistant extends Module {

    private final BindSetting addFriendKey = addSetting(new BindSetting("Добавить друга", -1));
    private final BindSetting resetCoordsKey = addSetting(new BindSetting("Сброс координат", -1));
    private final BooleanSetting autoGps = addSetting(new BooleanSetting("Ставить гпс на друзей", true));

    private static final Pattern COORDS_PATTERN = Pattern.compile("(-?\\d+)\\s+(-?\\d+)(?:\\s+(-?\\d+))?");

    public Assistant() {
        super("Assistant", "Ассистент: друзья и GPS", Category.PLAYER);
    }

    @Override
    public void onKeyPress(int key, int action) {
        if (action != 1) {
            return;
        }
        if (key == addFriendKey.getValue()) {
            toggleFriendUnderCursor();
        }
        if (key == resetCoordsKey.getValue()) {
            resetCoords();
        }
    }

    public static void onGameMessage(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        ModuleManager manager = ModuleManager.getInstance();
        Assistant assistant = manager.get(Assistant.class);
        if (assistant == null || !assistant.isEnabled() || !assistant.autoGps.getValue()) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        String lower = text.toLowerCase();
        boolean isFriend = FriendHelper.getFriends().stream().anyMatch(lower::contains);
        if (!isFriend) {
            return;
        }

        Matcher matcher = COORDS_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                double x = Double.parseDouble(matcher.group(1));
                boolean hasY = matcher.group(3) != null;
                double y = hasY ? Double.parseDouble(matcher.group(2)) : player.getY();
                double z = Double.parseDouble(hasY ? matcher.group(3) : matcher.group(2));
                GpsTarget.get().setTarget(x, y, z);
                ChatHelper.print("GPS установлен на координаты друга: " + (int) x + " " + (int) y + " " + (int) z);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private void toggleFriendUnderCursor() {
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (!(mc.crosshairPickEntity instanceof Player target)) {
            return;
        }
        if (target == mc.player) {
            return;
        }

        String name = target.getName().getString();
        if (FriendHelper.isFriend(name)) {
            FriendHelper.removeFriend(name);
            ChatHelper.print("Игрок \"" + name + "\" удалён из друзей");
        } else {
            FriendHelper.addFriend(name);
            ChatHelper.print("Игрок \"" + name + "\" добавлен в друзья");
        }
    }

    private void resetCoords() {
        if (mc.player == null) {
            return;
        }
        int x = (int) mc.player.getX();
        int z = (int) mc.player.getZ();
        ChatHelper.send("!корды " + x + " " + z);
    }
}