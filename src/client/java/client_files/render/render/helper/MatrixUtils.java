package client_files.render.render.helper;

import client_files.render.render.core.context.RenderContext;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public final class MatrixUtils {

    private static final Matrix4f SCRATCH_MAT = new Matrix4f();
    private static final Vector4f SCRATCH_VEC = new Vector4f();

    private MatrixUtils() {}

    public static Vector2f worldToScreen(double worldX, double worldY, double worldZ) {
        CameraRenderState camState = RenderContext.currentCameraState();
        if (camState == null || camState.pos == null) {
            return null;
        }

        float relX = (float) (worldX - camState.pos.x);
        float relY = (float) (worldY - camState.pos.y);
        float relZ = (float) (worldZ - camState.pos.z);

        SCRATCH_MAT.identity();
        if (camState.projectionMatrix != null) {
            SCRATCH_MAT.mul(camState.projectionMatrix);
        }
        if (camState.viewRotationMatrix != null) {
            SCRATCH_MAT.mul(camState.viewRotationMatrix);
        }

        SCRATCH_VEC.set(relX, relY, relZ, 1.0f);
        SCRATCH_VEC.mul(SCRATCH_MAT);

        if (SCRATCH_VEC.w <= 0.05f) {
            return null;
        }

        float ndcX = SCRATCH_VEC.x / SCRATCH_VEC.w;
        float ndcY = SCRATCH_VEC.y / SCRATCH_VEC.w;

        int screenWidth = ScreenUtils.getWidth();
        int screenHeight = ScreenUtils.getHeight();

        float screenX = (ndcX * 0.5f + 0.5f) * screenWidth;
        float screenY = (1.0f - (ndcY * 0.5f + 0.5f)) * screenHeight;

        return new Vector2f(screenX, screenY);
    }
}
