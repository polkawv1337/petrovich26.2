package client_files.render.api;

public record RenderConfig(int maxParticles, boolean debugMode, int batchFlushLogLevel) {
    public static final RenderConfig DEFAULT = new RenderConfig(4096, false, 0);

    public RenderConfig {
        if (maxParticles <= 0) maxParticles = 4096;
        if (batchFlushLogLevel < 0) batchFlushLogLevel = 0;
    }
}
