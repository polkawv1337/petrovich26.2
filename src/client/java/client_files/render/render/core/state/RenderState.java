package client_files.render.render.core.state;

import java.util.Objects;

public final class RenderState {
    public static final RenderState DEFAULT_2D = new RenderState(BlendState.ALPHA_BLEND, DepthState.DISABLED, StencilState.DISABLED);
    public static final RenderState DEFAULT_3D = new RenderState(BlendState.ALPHA_BLEND, DepthState.NORMAL, StencilState.DISABLED);

    private final BlendState blendState;
    private final DepthState depthState;
    private final StencilState stencilState;

    public RenderState(BlendState blendState, DepthState depthState, StencilState stencilState) {
        this.blendState = Objects.requireNonNull(blendState, "blendState cannot be null");
        this.depthState = Objects.requireNonNull(depthState, "depthState cannot be null");
        this.stencilState = Objects.requireNonNull(stencilState, "stencilState cannot be null");
    }

    public BlendState blendState() { return blendState; }
    public DepthState depthState() { return depthState; }
    public StencilState stencilState() { return stencilState; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RenderState that)) return false;
        return blendState.equals(that.blendState) &&
                depthState.equals(that.depthState) &&
                stencilState.equals(that.stencilState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blendState, depthState, stencilState);
    }
}
