package client_files.render.render.core.state;

import java.util.Objects;

public final class DepthState {
    public enum CompareFunction {
        NEVER,
        LESS,
        EQUAL,
        LEQUAL,
        GREATER,
        NOTEQUAL,
        GEQUAL,
        ALWAYS
    }

    public static final DepthState DISABLED = new DepthState(false, false, CompareFunction.ALWAYS);
    public static final DepthState NORMAL = new DepthState(true, true, CompareFunction.LEQUAL);

    private final boolean testEnabled;
    private final boolean writeEnabled;
    private final CompareFunction function;

    public DepthState(boolean testEnabled, boolean writeEnabled, CompareFunction function) {
        this.testEnabled = testEnabled;
        this.writeEnabled = writeEnabled;
        this.function = Objects.requireNonNull(function, "CompareFunction cannot be null");
    }

    public boolean testEnabled() { return testEnabled; }
    public boolean writeEnabled() { return writeEnabled; }
    public CompareFunction function() { return function; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DepthState that)) return false;
        return testEnabled == that.testEnabled && writeEnabled == that.writeEnabled && function == that.function;
    }

    @Override
    public int hashCode() {
        return Objects.hash(testEnabled, writeEnabled, function);
    }
}
