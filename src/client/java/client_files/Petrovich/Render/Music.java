package client_files.Petrovich.Render;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.HudEditor;
import client_files.ClientikUtils.render.RRender;
import client_files.Module;
import com.mojang.blaze3d.platform.NativeImage;
import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

public class Music extends Module {

    private final BooleanSetting showArt = addSetting(new BooleanSetting("Обложка", true));

    private static final float TEXT_SIZE = 9.0f;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private volatile MediaInfo mediaInfo = null;
    private volatile boolean polling = false;
    private long lastPoll = 0L;

    private byte[] lastArtworkBytes = null;
    private final Identifier dynamicArtwork = Identifier.fromNamespaceAndPath("petrovich_26_2", "music_dynamic_art");
    private Identifier currentArtwork = null;

    private static final float MAX_WIDTH = 150f;
    private static final float MIN_WIDTH = 60f;
    private static final float HEIGHT = 20f;

    public Music() {
        super("Music", "Медиаплеер: что сейчас играет на ПК", Category.RENDER);
        setHud(100000, 4);
    }

    @Override
    public void onRender(GuiGraphicsExtractor graphics) {
        pollMedia();

        float padding = 4f;
        float coverSize = 12f;
        float textGap = 4f;
        float separatorGap = 5f;
        float separatorWidth = 1f;
        float separatorHeight = 11f;

        String title = "No media";
        String artist = "Nothing is playing";
        String time = "0:00";

        if (mediaInfo != null) {
            if (mediaInfo.getTitle() != null && !mediaInfo.getTitle().isEmpty()) {
                title = mediaInfo.getTitle();
            }
            if (mediaInfo.getArtist() != null && !mediaInfo.getArtist().isEmpty()) {
                artist = mediaInfo.getArtist();
            } else {
                artist = "";
            }
            time = formatTime(mediaInfo.getPosition());
        }

        String textSeparator = (artist != null && !artist.isEmpty()) ? " " : "";
        float titleWidth = RRender.textWidth(title, TEXT_SIZE);
        float textSeparatorWidth = RRender.textWidth(textSeparator, TEXT_SIZE);
        float artistWidth = (artist != null && !artist.isEmpty()) ? RRender.textWidth(artist, TEXT_SIZE) : 0f;
        float timeWidth = RRender.textWidth(time, TEXT_SIZE);
        float textLineWidth = titleWidth + textSeparatorWidth + artistWidth;

        float contentWidth = padding + coverSize + textGap + textLineWidth + separatorGap + separatorWidth + separatorGap + timeWidth + padding + 2;
        float width = Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, contentWidth));
        boolean needsScroll = contentWidth > MAX_WIDTH;

        int x = HudEditor.xPos(this, (int) width, graphics.guiWidth());
        int y = HudEditor.yPos(this, (int) HEIGHT, graphics.guiHeight());

        int bgColor = RRender.BG;
        int titleColor = RRender.TEXT;
        int artistColor = RRender.TEXT_FAINT;
        int timeColor = RRender.LAVENDER;
        int separatorColor = 0x66FFFFFF;
        int placeholderColor = 0xFF232323;
        int placeholderIconColor = 0xFFAAAAAA;

        RRender.panel(graphics, (int) x, (int) y, (int) width, (int) HEIGHT);
        RRender.accentStrip(graphics, (int) x + 4, (int) (y + 4), 2, (int) HEIGHT - 8, RRender.LAVENDER);

        float coverX = x + padding + 4;
        float coverY = y + (HEIGHT - coverSize) / 2f;

        if (showArt.getValue()) {
            if (currentArtwork != null) {
                graphics.blit(currentArtwork, (int) coverX, (int) coverY, (int) (coverX + coverSize), (int) (coverY + coverSize), 0.0f, 1.0f, 0.0f, 1.0f);
            } else {
                RRender.rounded(graphics, (int) coverX, (int) coverY, (int) coverSize, (int) coverSize, 4, placeholderColor);
            }
        }

        float timeX = x + width - padding - timeWidth;
        float sepX = timeX - separatorGap - separatorWidth - 2;
        float sepY = y + (HEIGHT - separatorHeight) / 2f;

        RRender.fill(graphics, (int) sepX, (int) (sepY), (int) separatorWidth, (int) separatorHeight, separatorColor);

        float textX = coverX + coverSize + textGap;
        float maxLineWidth = Math.max(10f, sepX - separatorGap - textX);
        float lineY = y + 5f;

        if (needsScroll) {
            renderScrollingTrackLine(graphics, title, artist, textX, lineY, titleColor, artistColor, maxLineWidth);
        } else {
            float drawX = textX;
            RRender.text(graphics, title, drawX, lineY, TEXT_SIZE, titleColor);
            drawX += titleWidth;
            if (!textSeparator.isEmpty()) {
                RRender.text(graphics, textSeparator, drawX, lineY + 0.5f, TEXT_SIZE, artistColor);
                drawX += textSeparatorWidth;
                RRender.text(graphics, artist, drawX, lineY + 0.5f, TEXT_SIZE, artistColor);
            }
        }

        RRender.text(graphics, time, timeX - 2, lineY, TEXT_SIZE, timeColor);

        HudEditor.place(this, x, y, (int) width, (int) HEIGHT);
    }

    private void pollMedia() {
        long now = System.currentTimeMillis();
        if (polling || now - lastPoll < 500L) {
            return;
        }

        lastPoll = now;
        polling = true;

        executor.execute(() -> {
            try {
                List<IMediaSession> sessions = MediaPlayerInfo.INSTANCE.getMediaSessions();

                if (sessions == null || sessions.isEmpty()) {
                    mediaInfo = null;
                    currentArtwork = null;
                    return;
                }

                IMediaSession currentSession = sessions.stream()
                        .filter(session -> session != null && session.getMedia() != null)
                        .max(Comparator.comparing(s -> s.getMedia().isPlaying()))
                        .orElse(null);

                if (currentSession == null) {
                    mediaInfo = null;
                    currentArtwork = null;
                    return;
                }

                MediaInfo info = currentSession.getMedia();
                if (info == null) {
                    mediaInfo = null;
                    currentArtwork = null;
                    return;
                }

                mediaInfo = info;

                byte[] artworkBytes = info.getArtworkPng();
                if (artworkBytes != null && artworkBytes.length > 0) {
                    if (!Arrays.equals(lastArtworkBytes, artworkBytes)) {
                        lastArtworkBytes = artworkBytes.clone();
                        updateArtwork(artworkBytes);
                    }
                } else {
                    lastArtworkBytes = null;
                    currentArtwork = null;
                }
            } catch (Throwable t) {
                t.printStackTrace();
                mediaInfo = null;
                currentArtwork = null;
            } finally {
                polling = false;
            }
        });
    }

    private void updateArtwork(byte[] artworkBytes) {
        Minecraft.getInstance().execute(() -> {
            try {
                ByteArrayInputStream input = new ByteArrayInputStream(artworkBytes);
                NativeImage image = NativeImage.read(input);
                DynamicTexture texture = new DynamicTexture(() -> "music_art", image);
                TextureManager manager = Minecraft.getInstance().getTextureManager();
                manager.release(dynamicArtwork);
                manager.register(dynamicArtwork, texture);
                currentArtwork = dynamicArtwork;
            } catch (Exception e) {
                e.printStackTrace();
                currentArtwork = null;
            }
        });
    }

    private void renderScrollingTrackLine(GuiGraphicsExtractor graphics, String title, String artist, float x, float y, int titleColor, int artistColor, float maxWidth) {
        String separator = (artist != null && !artist.isEmpty()) ? " " : "";

        float titleWidth = RRender.textWidth(title, TEXT_SIZE);
        float separatorWidth = RRender.textWidth(separator, TEXT_SIZE);
        float artistWidth = (artist != null && !artist.isEmpty()) ? RRender.textWidth(artist, TEXT_SIZE) : 0f;

        float fullWidth = titleWidth + separatorWidth + artistWidth;
        float scroll = 0f;

        if (fullWidth > maxWidth) {
            float scrollMax = fullWidth - maxWidth;
            float pause = 1000f;
            float duration = 4000f;
            float cycle = pause + duration + pause + duration;
            float time = System.currentTimeMillis() % (long) cycle;

            if (time < pause) {
                scroll = 0f;
            } else if (time < pause + duration) {
                float t = (time - pause) / duration;
                scroll = t * scrollMax;
            } else if (time < pause + duration + pause) {
                scroll = scrollMax;
            } else {
                float t = (time - pause - duration - pause) / duration;
                scroll = scrollMax * (1f - t);
            }
        }

        float drawX = x - scroll;

        graphics.enableScissor((int) (x - 1), (int) (y - 3), (int) (x + maxWidth + 1), (int) (y + 9));

        RRender.text(graphics, title, drawX, y, TEXT_SIZE, titleColor);
        drawX += titleWidth;

        if (!separator.isEmpty()) {
            RRender.text(graphics, separator, drawX, y + 0.5f, TEXT_SIZE, artistColor);
            drawX += separatorWidth;
            RRender.text(graphics, artist, drawX, y + 0.5f, TEXT_SIZE, artistColor);
        }

        graphics.disableScissor();
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60L;
        long secs = seconds % 60L;
        return String.format("%d:%02d", minutes, secs);
    }
}