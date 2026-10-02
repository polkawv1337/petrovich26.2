package client_files.ClientikUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class ChatHelper {

    private ChatHelper() {
    }

    public static void send(String message) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.connection == null) return;
        if (message == null || message.isEmpty()) return;
        if (message.startsWith("/")) {
            player.connection.sendCommand(message.substring(1));
        } else {
            player.connection.sendChat(message);
        }
    }

    public static void print(String message) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        // Новый формат: стрелочка и переливающийся текст Petrovich Client
        String prefix = "\u00a7d\u27a4 \u00a7a\u00a7lP\u00a7b\u00a7le\u00a7c\u00a7lt\u00a7d\u00a7lr\u00a7e\u00a7lo\u00a7a\u00a7lv\u00a7b\u00a7li\u00a7c\u00a7lc\u00a7d\u00a7lh \u00a7a\u00a7lC\u00a7b\u00a7ll\u00a7c\u00a7li\u00a7d\u00a7le\u00a7e\u00a7ln\u00a7a\u00a7lt \u00a7d\u27a4 \u00a7f";
        player.sendSystemMessage(Component.literal(prefix + message));
    }

    public static void printError(String message) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        // Новый формат для ошибок с красным цветом
        String prefix = "\u00a7d\u27a4 \u00a7a\u00a7lP\u00a7b\u00a7le\u00a7c\u00a7lt\u00a7d\u00a7lr\u00a7e\u00a7lo\u00a7a\u00a7lv\u00a7b\u00a7li\u00a7c\u00a7lc\u00a7d\u00a7lh \u00a7a\u00a7lC\u00a7b\u00a7ll\u00a7c\u00a7li\u00a7d\u00a7le\u00a7e\u00a7ln\u00a7a\u00a7lt \u00a7d\u27a4 \u00a7c";
        player.sendSystemMessage(Component.literal(prefix + message));
    }
}