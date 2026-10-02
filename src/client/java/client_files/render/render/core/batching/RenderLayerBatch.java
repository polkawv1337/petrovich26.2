package client_files.render.render.core.batching;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RenderLayerBatch {
    private final Map<RenderPipeline, VertexBatch> batches = new LinkedHashMap<>();

    public VertexBatch getOrCreate(RenderPipeline pipeline) {
        return batches.computeIfAbsent(pipeline, k -> new VertexBatch());
    }

    public void resetAll() {
        for (VertexBatch batch : batches.values()) {
            batch.reset();
        }
    }

    public Map<RenderPipeline, VertexBatch> getBatches() {
        return Collections.unmodifiableMap(batches);
    }
}
