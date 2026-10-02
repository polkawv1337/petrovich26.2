package client_files.render.render.core.shader;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import client_files.render.accessor.RenderPipelinesAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ShaderManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShaderManager.class);
    private static final ShaderManager INSTANCE = new ShaderManager();

    private final Map<String, RenderPipeline> pipelines = new ConcurrentHashMap<>();

    private ShaderManager() {}

    public static ShaderManager get() {
        return INSTANCE;
    }

    public void init() {
        Shaders.init();
    }

    public RenderPipeline register(String id, RenderPipeline pipeline) {
        if (pipeline == null) return null;
        return pipelines.computeIfAbsent(id, key -> {
            try {
                return RenderPipelinesAccessor.Render$register(pipeline);
            } catch (Throwable t) {
                LOGGER.warn("[Render] Could not register pipeline '{}' via RenderPipelinesAccessor: {}", id, t.getMessage());
                return pipeline;
            }
        });
    }

    public RenderPipeline getPipeline(String id) {
        return pipelines.get(id);
    }
}
