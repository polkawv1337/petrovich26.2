package client_files.ClientikUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;

public final class PacketUtil {

    private static final ThreadLocal<Boolean> SILENT = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private PacketUtil() {
    }

    public static boolean isSilent() {
        return SILENT.get();
    }

    public static void sendSilent(Packet<?> packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return;
        SILENT.set(Boolean.TRUE);
        try {
            mc.getConnection().send(packet);
        } finally {
            SILENT.set(Boolean.FALSE);
        }
    }
}
