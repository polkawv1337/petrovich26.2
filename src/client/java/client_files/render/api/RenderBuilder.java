package client_files.render.api;

public final class RenderBuilder {
    private int maxParticles = 4096;
    private boolean debugMode = false;
    private int batchFlushLogLevel = 0;

    public RenderBuilder withMaxParticles(int maxParticles) {
        this.maxParticles = maxParticles;
        return this;
    }

    public RenderBuilder withDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }

    public RenderBuilder withBatchFlushLogLevel(int level) {
        this.batchFlushLogLevel = level;
        return this;
    }

    public RenderConfig build() {
        return new RenderConfig(maxParticles, debugMode, batchFlushLogLevel);
    }
}
