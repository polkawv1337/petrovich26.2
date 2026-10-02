package client_files.ClientikUtils;

import client_files.Module;
import client_files.ModuleManager;
import client_files.Petrovich.Player.FriendHelper;
import client_files.Petrovich.Render.GpsTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChatCommands {

    private ChatCommands() {
    }

    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean dispatch(String text) {
        if (text == null || !text.startsWith(".")) {
            return false;
        }
        String trimmed = text.substring(1).trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        String[] args = trimmed.split("\\s+");
        String command = args[0].toLowerCase(Locale.ROOT);
        String[] rest = new String[args.length - 1];
        System.arraycopy(args, 1, rest, 0, rest.length);

        switch (command) {
            case "gps" -> gps(rest);
            case "friend" -> friend(rest);
            case "bind" -> bind(rest);
            case "vclip" -> vclip(rest);
            case "help" -> help();
            default -> ChatHelper.printError("Неизвестная команда. Напиши " + "\u00a79.help");
        }
        return true;
    }

    private static void gps(String[] args) {
        if (args.length == 0) {
            ChatHelper.print("Использование: " + "\u00a79.gps <x> <z> [y]" + " \u00a77или " + "\u00a79.gps off");
            return;
        }
        if (args[0].equalsIgnoreCase("off")) {
            GpsTarget.get().clear();
            ChatHelper.print("ГПС отключен");
            return;
        }
        if (args.length < 2) {
            ChatHelper.print("Укажи координаты X и Z");
            return;
        }
        try {
            double x = Double.parseDouble(args[0]);
            double z = Double.parseDouble(args[1]);
            double y = args.length > 2 ? Double.parseDouble(args[2]) : Minecraft.getInstance().player.getY();
            GpsTarget.get().setTarget(x, y, z);
            ChatHelper.print("ГПС установлен: " + "\u00a7f" + (int) x + " " + (int) y + " " + (int) z);
        } catch (NumberFormatException e) {
            ChatHelper.printError("Некорректные координаты");
        }
    }

    private static void friend(String[] args) {
        String action = args.length == 0 ? "list" : args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "add" -> friendAdd(args);
            case "remove" -> friendRemove(args);
            case "list" -> friendList();
            case "clear" -> friendClear();
            default -> ChatHelper.printError("Неизвестная подкоманда. Используй add/remove/list/clear");
        }
    }

    private static void friendAdd(String[] args) {
        if (args.length < 2) {
            ChatHelper.print("Использование: " + "\u00a79.friend add <ник>");
            return;
        }
        String name = args[1];
        if (FriendHelper.isFriend(name)) {
            ChatHelper.print("Этот игрок уже в списке друзей");
            return;
        }
        FriendHelper.addFriend(name);
        ChatHelper.print("Игрок " + "\u00a7f" + name + " \u00a77успешно добавлен в друзья");
    }

    private static void friendRemove(String[] args) {
        if (args.length < 2) {
            ChatHelper.print("Использование: " + "\u00a79.friend remove <ник>");
            return;
        }
        String name = args[1];
        if (!FriendHelper.isFriend(name)) {
            ChatHelper.print("Такой друг не найден");
            return;
        }
        FriendHelper.removeFriend(name);
        ChatHelper.print("Игрок " + "\u00a7f" + name + " \u00a77успешно удален из друзей");
    }

    private static void friendList() {
        Set<String> friends = FriendHelper.getFriends();
        if (friends.isEmpty()) {
            ChatHelper.print("Список друзей пуст");
            return;
        }
        ChatHelper.print("Список друзей:");
        for (String name : friends) {
            ChatHelper.print(" \u00a7f- " + name);
        }
    }

    private static void friendClear() {
        Set<String> friends = FriendHelper.getFriends();
        for (String name : friends) {
            FriendHelper.removeFriend(name);
        }
        ChatHelper.print("Список друзей очищен");
    }

    private static void bind(String[] args) {
        String action = args.length == 0 ? "list" : args[0].toLowerCase(Locale.ROOT);
        switch (action) {
            case "add" -> bindAdd(args);
            case "remove" -> bindRemove(args);
            case "list" -> bindList();
            case "clear" -> bindClear();
            default -> ChatHelper.printError("Неизвестная подкоманда. Используй add/remove/list/clear");
        }
    }

    private static void bindAdd(String[] args) {
        if (args.length < 3) {
            ChatHelper.print("Использование: " + "\u00a79.bind add <модуль> <клавиша>");
            return;
        }
        Module module = findModule(args[1]);
        if (module == null) {
            ChatHelper.printError("Модуль " + "\u00a7f" + args[1] + " \u00a7cне найден");
            return;
        }
        int key = parseKey(args[2]);
        if (key == -1) {
            ChatHelper.printError("Клавиша " + "\u00a7f" + args[2] + " \u00a7cне распознана");
            return;
        }
        module.setKey(key);
        ChatHelper.print("Модуль " + "\u00a7f" + module.getName() + " \u00a77привязан к клавише " + "\u00a7f" + keyName(key));
    }

    private static void bindRemove(String[] args) {
        if (args.length < 2) {
            ChatHelper.print("Использование: " + "\u00a79.bind remove <модуль>");
            return;
        }
        Module module = findModule(args[1]);
        if (module == null) {
            ChatHelper.printError("Модуль " + "\u00a7f" + args[1] + " \u00a7cне найден");
            return;
        }
        module.setKey(-1);
        ChatHelper.print("Модуль " + "\u00a7f" + module.getName() + " \u00a77больше не привязан к клавише");
    }

    private static void bindList() {
        List<Module> bound = ModuleManager.getInstance().getModules().stream()
                .filter(m -> m.getKey() != -1)
                .toList();
        if (bound.isEmpty()) {
            ChatHelper.print("Нет привязанных модулей");
            return;
        }
        ChatHelper.print("Привязанные модули:");
        for (Module m : bound) {
            ChatHelper.print(" \u00a7f" + m.getName() + ":" + " \u00a79" + keyName(m.getKey()));
        }
    }

    private static void bindClear() {
        for (Module m : ModuleManager.getInstance().getModules()) {
            m.setKey(-1);
        }
        ChatHelper.print("Все модули отвязаны от клавиш");
    }

    private static Module findModule(String name) {
        for (Module m : ModuleManager.getInstance().getModules()) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }

    private static int parseKey(String input) {
        String n = input.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
        if (n.isEmpty()) {
            return -1;
        }
        if (n.equals("none")) {
            return -1;
        }
        if (n.equals("wheelup") || n.equals("scrollup")) {
            return BindSetting.SCROLL_UP;
        }
        if (n.equals("wheeldown") || n.equals("scrolldown")) {
            return BindSetting.SCROLL_DOWN;
        }
        if (n.equals("lmb")) {
            return BindSetting.MOUSE_BASE;
        }
        if (n.equals("rmb")) {
            return BindSetting.MOUSE_BASE + 1;
        }
        if (n.equals("mmb") || n.equals("m3")) {
            return BindSetting.MOUSE_BASE + 2;
        }
        if (n.matches("m\\d+")) {
            int button = Integer.parseInt(n.substring(1));
            if (button >= 3) {
                return BindSetting.MOUSE_BASE + button - 1;
            }
        }
        if (n.startsWith("f") && n.length() <= 3) {
            try {
                int num = Integer.parseInt(n.substring(1));
                if (num >= 1 && num <= 25) {
                    return GLFW.GLFW_KEY_F1 + num - 1;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
            String name = GLFW.glfwGetKeyName(key, 0);
            if (name != null && name.replace("_", "").equalsIgnoreCase(input)) {
                return key;
            }
        }
        return -1;
    }

    private static String keyName(int value) {
        if (value <= 0) {
            return "NONE";
        }
        if (value == BindSetting.SCROLL_UP) {
            return "WHEEL_UP";
        }
        if (value == BindSetting.SCROLL_DOWN) {
            return "WHEEL_DOWN";
        }
        if (value >= BindSetting.MOUSE_BASE && value < BindSetting.MOUSE_BASE + 32) {
            int button = value - BindSetting.MOUSE_BASE;
            return switch (button) {
                case 0 -> "LMB";
                case 1 -> "RMB";
                case 2 -> "MMB";
                default -> "M" + (button + 1);
            };
        }
        if (value >= 290 && value <= 301) {
            return "F" + (value - 289);
        }
        String name = GLFW.glfwGetKeyName(value, 0);
        if (name != null && !name.isEmpty()) {
            return name.toUpperCase(Locale.ROOT);
        }
        return "B" + value;
    }

    private static void vclip(String[] args) {
        if (args.length == 0) {
            ChatHelper.print("Использование: " + "\u00a79.vclip <расстояние|up|down>");
            return;
        }
        LocalPlayer player = mc.player;
        Level level = mc.level;
        if (player == null || level == null) {
            return;
        }

        double yOffset;
        String input = args[0];
        switch (input.toLowerCase(Locale.ROOT)) {
            case "up" -> yOffset = findOffset(player.blockPosition(), true, level, player);
            case "down" -> yOffset = findOffset(player.blockPosition(), false, level, player);
            default -> {
                try {
                    yOffset = Double.parseDouble(input);
                } catch (NumberFormatException e) {
                    ChatHelper.printError("\u00a7f" + input + " \u00a7cне является числом");
                    return;
                }
            }
        }

        if (yOffset == 0) {
            ChatHelper.printError("Не удалось выполнить телепортацию");
            return;
        }

        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();

        for (int i = 0; i < 3; i++) {
            player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(
                    player.onGround(), player.horizontalCollision));
        }
        player.connection.send(new ServerboundMovePlayerPacket.Pos(
                x, y + yOffset, z, false, player.horizontalCollision));
        player.setPos(x, y + yOffset, z);

        ChatHelper.print("Телепортировано на " + "\u00a7f" + (int) yOffset + " \u00a77блоков по вертикали");
    }

    private static double findOffset(BlockPos pos, boolean toUp, Level level, LocalPlayer player) {
        int minY = level.dimensionType().minY();
        int maxY = minY + level.dimensionType().height();
        if (toUp) {
            for (int i = 3; i < maxY; i++) {
                BlockPos base = pos.above(i);
                BlockPos head = base.above();
                if (level.getBlockState(base).isAir() && level.getBlockState(head).isAir()) {
                    return base.getY() - player.getY();
                }
            }
        } else {
            for (int i = -1; i > minY - pos.getY(); i--) {
                BlockPos solid = pos.above(i);
                BlockPos air1 = solid.below();
                BlockPos air2 = air1.below();
                if (!level.getBlockState(solid).isAir()
                        && level.getBlockState(air1).isAir()
                        && level.getBlockState(air2).isAir()) {
                    return air2.getY() - player.getY();
                }
            }
        }
        return 0;
    }

    private static void help() {
        ChatHelper.print("Команды:");
        ChatHelper.print(" \u00a79.gps <x> <z> \u00a77- стрелка на координаты");
        ChatHelper.print(" \u00a79.gps off \u00a77- выключить стрелку");
        ChatHelper.print(" \u00a79.friend add/remove/list/clear");
        ChatHelper.print(" \u00a79.bind add/remove/list/clear");
        ChatHelper.print(" \u00a79.vclip <расстояние|up|down>");
    }
}