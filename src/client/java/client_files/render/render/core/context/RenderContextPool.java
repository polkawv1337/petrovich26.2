package client_files.render.render.core.context;

import java.util.ArrayDeque;
import java.util.Deque;

public final class RenderContextPool {
    private static final Deque<RenderContext.Snapshot> STACK = new ArrayDeque<>();

    private RenderContextPool() {}

    public static Object push() {
        RenderContext.Snapshot snapshot = RenderContext.snapshot();
        STACK.push(snapshot);
        return snapshot;
    }

    public static void pop(Object token) {
        if (STACK.isEmpty()) {
            throw new IllegalStateException("RenderContextPool underflow: no snapshot to restore");
        }
        RenderContext.Snapshot top = STACK.pop();
        if (token != null && top != token) {
            throw new IllegalStateException("RenderContextPool token mismatch on pop");
        }
        RenderContext.restore(top);
    }
}
