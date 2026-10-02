package client_files.render.render.core.context;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;

public final class ScissorContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScissorContext.class);

    private static final Deque<int[]> SCISSOR_STACK = new ArrayDeque<>();

    private ScissorContext() {}

    public static void pushScissor(int x, int y, int width, int height) {
        int minX = x;
        int minY = y;
        int maxX = x + Math.max(0, width);
        int maxY = y + Math.max(0, height);

        if (!SCISSOR_STACK.isEmpty()) {
            int[] parent = SCISSOR_STACK.peek();
            minX = Math.max(minX, parent[0]);
            minY = Math.max(minY, parent[1]);
            maxX = Math.max(minX, Math.min(maxX, parent[2]));
            maxY = Math.max(minY, Math.min(maxY, parent[3]));
        }

        int[] current = new int[]{minX, minY, maxX, maxY};
        SCISSOR_STACK.push(current);

        notifyExtractorPush();
    }

    public static void popScissor() {
        if (!SCISSOR_STACK.isEmpty()) {
            SCISSOR_STACK.pop();
        }
        notifyExtractorPop();
    }

    public static boolean hasScissor() {
        return !SCISSOR_STACK.isEmpty();
    }

    public static int[] currentScissor() {
        return SCISSOR_STACK.peek();
    }

    private static void notifyExtractorPush() {
        GuiGraphicsExtractor extractor = RenderContext.current2DExtractor();
        if (extractor == null) return;
        try {
            Method m = extractor.getClass().getMethod("Render$pushScissor");
            m.invoke(extractor);
        } catch (Exception ignored) {

        }
    }

    private static void notifyExtractorPop() {
        GuiGraphicsExtractor extractor = RenderContext.current2DExtractor();
        if (extractor == null) return;
        try {
            Method m = extractor.getClass().getMethod("Render$popScissor");
            m.invoke(extractor);
        } catch (Exception ignored) {

        }
    }
}
