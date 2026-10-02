package client_files.render.render.core.state;

import java.util.Objects;

public final class BlendState {
    public enum Factor {
        ZERO,
        ONE,
        SRC_COLOR,
        ONE_MINUS_SRC_COLOR,
        SRC_ALPHA,
        ONE_MINUS_SRC_ALPHA,
        DST_ALPHA,
        ONE_MINUS_DST_ALPHA,
        DST_COLOR,
        ONE_MINUS_DST_COLOR
    }

    public static final BlendState ALPHA_BLEND = new BlendState(Factor.SRC_ALPHA, Factor.ONE_MINUS_SRC_ALPHA);
    public static final BlendState ADDITIVE = new BlendState(Factor.SRC_ALPHA, Factor.ONE);
    public static final BlendState OPAQUE = new BlendState(Factor.ONE, Factor.ZERO);

    private final Factor srcFactor;
    private final Factor dstFactor;

    public BlendState(Factor srcFactor, Factor dstFactor) {
        this.srcFactor = Objects.requireNonNull(srcFactor, "srcFactor cannot be null");
        this.dstFactor = Objects.requireNonNull(dstFactor, "dstFactor cannot be null");
    }

    public Factor srcFactor() { return srcFactor; }
    public Factor dstFactor() { return dstFactor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlendState that)) return false;
        return srcFactor == that.srcFactor && dstFactor == that.dstFactor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(srcFactor, dstFactor);
    }
}
