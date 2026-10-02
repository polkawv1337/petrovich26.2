package client_files.render.render.core.state;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public final class RenderStateStack {
    private final Deque<RenderState> stack = new ArrayDeque<>();

    public RenderStateStack() {
        stack.push(RenderState.DEFAULT_2D);
    }

    public void push(RenderState state) {
        stack.push(Objects.requireNonNull(state, "RenderState cannot be null"));
    }

    public RenderState pop() {
        if (stack.size() <= 1) {
            throw new IllegalStateException("Cannot pop the base RenderState from RenderStateStack");
        }
        return stack.pop();
    }

    public RenderState current() {
        RenderState top = stack.peek();
        if (top == null) {
            throw new IllegalStateException("RenderStateStack is empty");
        }
        return top;
    }
}
