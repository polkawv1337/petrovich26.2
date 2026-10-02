package client_files.render.render.core.shader;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ShaderProgram {
    private final String id;
    private final RenderPipeline pipeline;
    private final Map<String, ShaderUniform> uniforms = new LinkedHashMap<>();

    public ShaderProgram(String id, RenderPipeline pipeline, List<ShaderUniform> uniformList) {
        this.id = id;
        this.pipeline = pipeline;
        if (uniformList != null) {
            for (ShaderUniform u : uniformList) {
                this.uniforms.put(u.name(), u);
            }
        }
    }

    public String getId() {
        return id;
    }

    public RenderPipeline getPipeline() {
        return pipeline;
    }

    public ShaderUniform getUniform(String name) {
        return uniforms.get(name);
    }

    public Map<String, ShaderUniform> getUniforms() {
        return Collections.unmodifiableMap(uniforms);
    }

    public void bind() {

    }
}
