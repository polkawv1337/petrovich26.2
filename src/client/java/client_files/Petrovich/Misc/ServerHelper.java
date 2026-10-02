package client_files.Petrovich.Misc;

import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.ChatHelper;
import client_files.Module;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.PlayerInfo;

public class ServerHelper extends Module {

    public ServerHelper() {
        super("ServerHelper", "Показывает информацию о сервере и пинге", Category.MISC);
    }

    @Override
    public void onEnable() {
        LocalPlayer player = mc.player;
        if (player == null || player.connection == null) return;

        int count = 0;
        long sum = 0;
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (PlayerInfo info : player.connection.getOnlinePlayers()) {
            int latency = info.getLatency();
            if (latency < 0) continue;
            count++;
            sum += latency;
            min = Math.min(min, latency);
            max = Math.max(max, latency);
        }
        if (count == 0) {
            ChatHelper.print("Игроков в табе: " + count);
            return;
        }
        long avg = sum / count;
        ChatHelper.print("Игроков: " + count + " | Пинг средн.: " + avg
                + " | Мин: " + min + " | Макс: " + max);
    }
}