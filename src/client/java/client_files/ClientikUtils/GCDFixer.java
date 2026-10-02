package client_files.ClientikUtils;

import net.minecraft.client.Minecraft;

public class GCDFixer {
    private static final Minecraft mc = Minecraft.getInstance();

    private GCDFixer() {
    }

    public static float getFixRotate(float rot) {
        return getDeltaMouse(rot) * getGCDValue();
    }

    public static float getGCDValue() {
        return (float) (getGCD() * 0.15);
    }

    public static float getGCD() {
        double sens = (double) mc.options.sensitivity().get();
        double var11 = sens / 0.15 / 8.0;
        double var9 = Math.cbrt(var11);
        float f1 = (float) ((var9 - 0.2) / 0.6 * 0.6 + 0.2);
        return f1 * f1 * f1 * 8;
    }

    public static float getDeltaMouse(float delta) {
        return Math.round(delta / getGCDValue());
    }
}