package client_files.render.render.assets.vid;

import client_files.render.render.assets.img.TextureAsset;

public final class VideoFrameBuffer {
    private final TextureAsset[] buffer;
    private int head = 0;
    private int tail = 0;
    private int count = 0;

    public VideoFrameBuffer(int capacity) {
        this.buffer = new TextureAsset[Math.max(2, capacity)];
    }

    public synchronized void offerFrame(TextureAsset frame) {
        if (frame == null) return;
        if (count == buffer.length) {

            head = (head + 1) % buffer.length;
            count--;
        }
        buffer[tail] = frame;
        tail = (tail + 1) % buffer.length;
        count++;
    }

    public synchronized TextureAsset pollNextFrame() {
        if (count == 0) return null;
        TextureAsset frame = buffer[head];
        head = (head + 1) % buffer.length;
        count--;
        return frame;
    }

    public synchronized int size() {
        return count;
    }
}
