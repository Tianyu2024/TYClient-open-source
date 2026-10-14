package cn.tianyu.client.TianyuOptiFine.betterfps.impl.render;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/**
 * @Author: CN_tianyu
 */
public final class OptimizedGlStateManager {

    private static boolean blend = false;
    private static boolean texture = true;
    private static boolean depth = true;
    private static boolean alpha = true;

    private static int boundTexture = -1;


    public static void reset() {
        blend = false;
        texture = true;
        depth = true;
        alpha = true;
        boundTexture = -1;
    }


    public static void enableBlend() {
        if (!blend) {
            GL11.glEnable(GL11.GL_BLEND);
            blend = true;
        }
    }


    public static void disableBlend() {
        if (blend) {
            GL11.glDisable(GL11.GL_BLEND);
            blend = false;
        }
    }


    public static void enableTexture() {
        if (!texture) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            texture = true;
        }
    }


    public static void disableTexture() {
        if (texture) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            texture = false;
        }
    }


    public static void enableDepth() {
        if (!depth) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            depth = true;
        }
    }


    public static void disableDepth() {
        if (depth) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            depth = false;
        }
    }


    public static void bindTexture(int textureId)
    {
        GlStateManager.bindTexture(textureId);

//        if(textureId < 0)
//        {
//            textureId = 0;
//        }
//
//
//        if(boundTexture != textureId)
//        {
//
//            GL11.glBindTexture(
//                    GL11.GL_TEXTURE_2D,
//                    textureId
//            );
//
//
//            boundTexture = textureId;
//        }
    }
}