package client_files.render.render.core.batching;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class BatchManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BatchManager.class);
    private static final BatchManager INSTANCE = new BatchManager();

    private final RenderLayerBatch layerBatch = new RenderLayerBatch();
    private int flushLogLevel = 0;

    private BatchManager() {}

    public static BatchManager get() {
        return INSTANCE;
    }

    public void setFlushLogLevel(int level) {
        this.flushLogLevel = level;
    }

    public void beginFrame() {
        layerBatch.resetAll();
    }

    public void submit(RenderPipeline pipeline, BatchedQuad quad) {
        if (quad == null) return;
        VertexBatch batch = layerBatch.getOrCreate(pipeline);
        quad.pushTo(batch);
    }

    public void flush() {
        int totalVertices = 0;
        int activePipelines = 0;

        for (Map.Entry<RenderPipeline, VertexBatch> entry : layerBatch.getBatches().entrySet()) {
            RenderPipeline pipeline = entry.getKey();
            VertexBatch batch = entry.getValue();
            int count = batch.vertexCount();
            if (count == 0) continue;

            totalVertices += count;
            activePipelines++;

        }

        if (flushLogLevel > 0 && totalVertices > 0) {
            LOGGER.info("[BatchManager] Flushed {} vertices across {} pipelines", totalVertices, activePipelines);
        }
    }
}
