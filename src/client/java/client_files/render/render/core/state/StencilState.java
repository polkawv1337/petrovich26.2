package client_files.render.render.core.state;

import java.util.Objects;

public final class StencilState {
    public static final StencilState DISABLED = new StencilState(false, 0, 0xFF, 0xFF);

    private final boolean enabled;
    private final int referenceValue;
    private final int compareMask;
    private final int writeMask;

    public StencilState(boolean enabled, int referenceValue, int compareMask, int writeMask) {
        this.enabled = enabled;
        this.referenceValue = referenceValue;
        this.compareMask = compareMask;
        this.writeMask = writeMask;
    }

    public static StencilState write(int referenceValue) {
        return new StencilState(true, referenceValue, 0xFF, 0xFF);
    }

    public boolean enabled() { return enabled; }
    public int referenceValue() { return referenceValue; }
    public int compareMask() { return compareMask; }
    public int writeMask() { return writeMask; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StencilState that)) return false;
        return enabled == that.enabled &&
                referenceValue == that.referenceValue &&
                compareMask == that.compareMask &&
                writeMask == that.writeMask;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, referenceValue, compareMask, writeMask);
    }
}
