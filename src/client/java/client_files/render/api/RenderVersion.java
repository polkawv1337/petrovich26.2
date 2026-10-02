package client_files.render.api;

public final class RenderVersion {
    public static final String VERSION = "1.0.0";

    private RenderVersion() {}

    public static boolean isCompatible(String requiredVersion) {
        if (requiredVersion == null || requiredVersion.isEmpty()) return true;
        String curMajor = VERSION.split("\\.")[0];
        String reqMajor = requiredVersion.split("\\.")[0];
        return curMajor.equals(reqMajor);
    }
}
