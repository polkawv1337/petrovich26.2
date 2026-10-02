package client_files.render.render.helper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class BufferUtils {
    private BufferUtils() {}

    public static ByteBuffer createByteBuffer(int size) {
        return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
    }

    public static FloatBuffer createFloatBuffer(int size) {
        return createByteBuffer(size * Float.BYTES).asFloatBuffer();
    }
}
