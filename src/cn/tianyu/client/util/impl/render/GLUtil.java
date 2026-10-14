package cn.tianyu.client.util.impl.render;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class GLUtil {
    public static int[] enabledCaps = new int[32];

    public static void startBlend() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }
    public static void setupRendering(int mode, Runnable runnable) {
        glBegin(mode);
        runnable.run();
        glEnd();
    }

    public static void enableDepth() {
        GL11.glDepthMask((boolean)true);
        GL11.glEnable((int)2929);
    }

    public static void disableCaps() {
        for (int cap : enabledCaps) {
            GL11.glDisable((int)cap);
        }
    }

    public static void enableCaps(int ... caps) {
        for (int cap : caps) {
            GL11.glEnable((int)cap);
        }
        enabledCaps = caps;
    }

    public static void enableTexture2D() {
        GL11.glEnable((int)3553);
    }

    public static void disableTexture2D() {
        GL11.glDisable((int)3553);
    }

    public static void enableBlending() {
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
    }

    public static void disableDepth() {
        GL11.glDepthMask((boolean)false);
        GL11.glDisable((int)2929);
    }

    public static void disableBlending() {
        GL11.glDisable((int)3042);
    }

    public static void endBlend() {
        GlStateManager.disableBlend();
    }

    public static void render(int mode, Runnable render){
        glBegin(mode);
        render.run();
        glEnd();
    }

//    public static void setup2DRendering(Runnable f) {
//        glEnable(GL_BLEND);
//        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//        glDisable(GL_TEXTURE_2D);
//        f.run();
//        glEnable(GL_TEXTURE_2D);
//        GlStateManager.disableBlend();
//    }
    public static void setup2DRendering(Runnable f) {
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glDisable((int)3553);
        f.run();
        GL11.glEnable((int)3553);
        GlStateManager.disableBlend();
    }

    public static void setup2DRendering() {
        GLUtil.setup2DRendering(true);
    }

    public static void setup2DRendering(boolean blend) {
        if (blend) {
            GLUtil.startBlend();
        }
        GlStateManager.disableTexture2D();
    }

    public static void end2DRendering() {
        GlStateManager.enableTexture2D();
        GLUtil.endBlend();
    }

    public static void rotate(float x2, float y2, float rotate, Runnable f) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x2, y2, 0.0f);
        GlStateManager.rotate(rotate, 0.0f, 0.0f, -1.0f);
        GlStateManager.translate(-x2, -y2, 0.0f);
        f.run();
        GlStateManager.popMatrix();
    }
}
