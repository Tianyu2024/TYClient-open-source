package cn.tianyu.client.TianyuOptiFine.betterfps;

import cn.tianyu.client.TianyuOptiFine.betterfps.impl.FastMath;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;
import org.lwjglx.opengl.Display;
import org.lwjglx.util.glu.GLU;
import org.lwjgl.opengl.GL11;

/**
 * @Author: CN_tianyu
 */
public class BetterFPSOptimizer {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean useFastMath = true;
    private static boolean useFrustumCulling = true;
    private static boolean useChunkUpdatesOptimization = true;

    // ===== 数学计算优化 =====
    public static double optimizedSin(double value) {
        return useFastMath ? FastMath.sin(value) : Math.sin(value);
    }

    public static double optimizedCos(double value) {
        return useFastMath ? FastMath.cos(value) : Math.cos(value);
    }

    public static float optimizedSqrt(float value) {
        return FastMath.sqrt(value);
    }

    // ===== 渲染优化 =====
    public static void setupRenderOptimizations() {
        if (useFrustumCulling) {
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glCullFace(GL11.GL_BACK);
        }

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        float fov = MathHelper.clamp_float(mc.gameSettings.fovSetting, 70, 110);
        // 使用正确的GLU方法
        GLU.gluPerspective(
                fov,
                (float) Display.getWidth()/(float)Display.getHeight(),
                0.05F,
                mc.gameSettings.renderDistanceChunks * 16 * 4
        );
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    // ===== 区块更新优化 =====
    public static boolean shouldUpdateChunk(int chunkX, int chunkZ) {
        if (!useChunkUpdatesOptimization) return true;

        int playerChunkX = (int)mc.thePlayer.posX >> 4;
        int playerChunkZ = (int)mc.thePlayer.posZ >> 4;

        // 只更新玩家附近的区块
        int distX = Math.abs(chunkX - playerChunkX);
        int distZ = Math.abs(chunkZ - playerChunkZ);
        return distX <= mc.gameSettings.renderDistanceChunks &&
                distZ <= mc.gameSettings.renderDistanceChunks;
    }

    // ===== 配置方法 =====
    public static void setUseFastMath(boolean enabled) {
        useFastMath = enabled;
    }

    public static void setUseFrustumCulling(boolean enabled) {
        useFrustumCulling = enabled;
    }

    public static void setUseChunkUpdatesOptimization(boolean enabled) {
        useChunkUpdatesOptimization = enabled;
    }
}