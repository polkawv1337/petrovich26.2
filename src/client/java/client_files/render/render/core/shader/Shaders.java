package client_files.render.render.core.shader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class Shaders {
    private static final Logger LOGGER = LoggerFactory.getLogger(Shaders.class);
    public static ShaderProgram ROUNDED_RECT;
    public static ShaderProgram LINEAR_GRADIENT;
    public static ShaderProgram RADIAL_GRADIENT;
    public static ShaderProgram BLUR;
    public static ShaderProgram GLOW;
    public static ShaderProgram TEXTURE;

    private static boolean initialized = false;

    private Shaders() {}

    public static synchronized void init() {
        if (initialized) return;

        ROUNDED_RECT = createProgram("Render:rounded_rect", List.of(
                new ShaderUniform("u_Size", ShaderUniform.Type.VEC2),
                new ShaderUniform("u_Radius", ShaderUniform.Type.VEC4),
                new ShaderUniform("u_Color", ShaderUniform.Type.VEC4)
        ));

        LINEAR_GRADIENT = createProgram("Render:linear_gradient", List.of(
                new ShaderUniform("u_StartColor", ShaderUniform.Type.VEC4),
                new ShaderUniform("u_EndColor", ShaderUniform.Type.VEC4),
                new ShaderUniform("u_Angle", ShaderUniform.Type.FLOAT)
        ));

        RADIAL_GRADIENT = createProgram("Render:radial_gradient", List.of(
                new ShaderUniform("u_Center", ShaderUniform.Type.VEC2),
                new ShaderUniform("u_Radius", ShaderUniform.Type.FLOAT),
                new ShaderUniform("u_CenterColor", ShaderUniform.Type.VEC4),
                new ShaderUniform("u_EdgeColor", ShaderUniform.Type.VEC4)
        ));

        BLUR = createProgram("Render:blur", List.of(
                new ShaderUniform("u_Radius", ShaderUniform.Type.FLOAT),
                new ShaderUniform("u_Direction", ShaderUniform.Type.VEC2)
        ));

        GLOW = createProgram("Render:glow", List.of(
                new ShaderUniform("u_GlowColor", ShaderUniform.Type.VEC4),
                new ShaderUniform("u_Intensity", ShaderUniform.Type.FLOAT)
        ));

        TEXTURE = createProgram("Render:texture", List.of(
                new ShaderUniform("u_Color", ShaderUniform.Type.VEC4)
        ));

        initialized = true;
    }

    private static ShaderProgram createProgram(String id, List<ShaderUniform> uniforms) {
        LOGGER.warn("[Render] Shader program '{}' registered with null pipeline (placeholder)", id);

        ShaderManager.get().register(id, null);
        return new ShaderProgram(id, null, uniforms);
    }
}
