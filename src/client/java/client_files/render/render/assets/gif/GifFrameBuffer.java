package client_files.render.render.assets.gif;

public final class GifFrameBuffer {
    private final GifFrame[] buffer;
    private int head = 0;
    private int tail = 0;
    private int count = 0;

    public GifFrameBuffer(int capacity) {
        this.buffer = new GifFrame[Math.max(2, capacity)];
    }

    public synchronized void offerFrame(GifFrame frame) {
        if (frame == null) return;
        if (count == buffer.length) {
            head = (head + 1) % buffer.length;
            count--;
        }
        buffer[tail] = frame;
        tail = (tail + 1) % buffer.length;
        count++;
    }

    public synchronized GifFrame pollNextFrame() {
        if (count == 0) return null;
        GifFrame frame = buffer[head];
        head = (head + 1) % buffer.length;
        count--;
        return frame;
    }

    public synchronized int size() {
        return count;
    }
}
