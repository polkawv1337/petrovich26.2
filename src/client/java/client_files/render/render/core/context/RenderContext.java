package client_files.render.render.core.context;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.joml.Matrix4fc;

public final class RenderContext {
    private static int depth2D = 0;
    private static GuiGraphicsExtractor extractor2D;
    private static DeltaTracker deltaTracker2D;

    private static int depth3D = 0;
    private static CameraRenderState cameraRenderState3D;
    private static Matrix4fc projectionMatrix3D;
    private static DeltaTracker deltaTracker3D;

    private static CameraRenderState lastCapturedCameraState;
    private static LevelRenderState currentLevelRenderState;

    private RenderContext() {}

    public static void enter2D(GuiGraphicsExtractor guiGraphicsExtractor, DeltaTracker deltaTracker) {
        depth2D++;
        extractor2D = guiGraphicsExtractor;
        deltaTracker2D = deltaTracker;
    }

    public static void exit2D() {
        depth2D--;
        if (depth2D <= 0) {
            depth2D = 0;
            extractor2D = null;
            deltaTracker2D = null;
        }
    }

    public static void enter3D(CameraRenderState cameraRenderState, Matrix4fc projectionMatrix, DeltaTracker deltaTracker) {
        depth3D++;
        cameraRenderState3D = cameraRenderState;
        projectionMatrix3D = projectionMatrix;
        deltaTracker3D = deltaTracker;
    }

    public static void exit3D() {
        depth3D--;
        if (depth3D <= 0) {
            depth3D = 0;
            cameraRenderState3D = null;
            projectionMatrix3D = null;
            deltaTracker3D = null;
        }
    }

    public static void captureCameraState(CameraRenderState cameraRenderState) {
        lastCapturedCameraState = cameraRenderState;
    }

    public static void captureEntityRenderStates(LevelRenderState levelRenderState) {
        currentLevelRenderState = levelRenderState;
    }

    public static boolean is2DActive() {
        return depth2D > 0;
    }

    public static boolean is3DActive() {
        return depth3D > 0;
    }

    public static GuiGraphicsExtractor current2DExtractor() {
        return extractor2D;
    }

    public static DeltaTracker currentDeltaTracker() {
        return deltaTracker2D != null ? deltaTracker2D : deltaTracker3D;
    }

    public static CameraRenderState currentCameraState() {
        return cameraRenderState3D != null ? cameraRenderState3D : lastCapturedCameraState;
    }

    public static Matrix4fc current3DProjection() {
        return projectionMatrix3D;
    }

    public static LevelRenderState currentLevelRenderState() {
        return currentLevelRenderState;
    }

    static Snapshot snapshot() {
        return new Snapshot(
                depth2D, extractor2D, deltaTracker2D,
                depth3D, cameraRenderState3D, projectionMatrix3D, deltaTracker3D,
                lastCapturedCameraState, currentLevelRenderState
        );
    }

    static void restore(Snapshot s) {
        depth2D = s.depth2D;
        extractor2D = s.extractor2D;
        deltaTracker2D = s.deltaTracker2D;
        depth3D = s.depth3D;
        cameraRenderState3D = s.cameraRenderState3D;
        projectionMatrix3D = s.projectionMatrix3D;
        deltaTracker3D = s.deltaTracker3D;
        lastCapturedCameraState = s.lastCapturedCameraState;
        currentLevelRenderState = s.currentLevelRenderState;
    }

    record Snapshot(
            int depth2D,
            GuiGraphicsExtractor extractor2D,
            DeltaTracker deltaTracker2D,
            int depth3D,
            CameraRenderState cameraRenderState3D,
            Matrix4fc projectionMatrix3D,
            DeltaTracker deltaTracker3D,
            CameraRenderState lastCapturedCameraState,
            LevelRenderState currentLevelRenderState
    ) {}
}
