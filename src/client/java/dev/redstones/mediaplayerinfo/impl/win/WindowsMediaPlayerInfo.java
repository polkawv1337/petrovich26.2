package dev.redstones.mediaplayerinfo.impl.win;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class WindowsMediaPlayerInfo implements MediaPlayerInfo {
    @Override
    public native List<IMediaSession> getMediaSessions();

    static {
        try {
            Path tempDir = Files.createTempDirectory("mediaplayerinfo-");
            Path dllFile = tempDir.resolve("MediaPlayerInfo.dll");
            try (InputStream is = WindowsMediaPlayerInfo.class.getResourceAsStream("/mediaplayerinfo/natives/win/MediaPlayerInfo.dll")) {
                if (is == null) throw new IOException("MediaPlayerInfo.dll not found");
                Files.write(dllFile, is.readAllBytes());
            }
            System.load(dllFile.toAbsolutePath().toString());
            try {
                Files.deleteIfExists(dllFile);
                Files.deleteIfExists(tempDir);
            } catch (IOException e) {
                dllFile.toFile().deleteOnExit();
                tempDir.toFile().deleteOnExit();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load MediaPlayerInfo.dll", e);
        }
    }
}
