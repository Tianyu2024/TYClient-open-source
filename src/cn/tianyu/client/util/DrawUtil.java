package cn.tianyu.client.util;

import cn.tianyu.client.util.impl.render.GLUtil;
import cn.tianyu.client.util.impl.render.ShaderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static cn.tianyu.client.TYClient.mc;

/**
 * @author CN_tianyu
 */

public class DrawUtil {

    /**
     * 重置OpenGL状态到默认值
     * 用于解决渲染冲突和状态混乱问题
     */
    public static void resetOpenGL() {
        // 纹理状态重置
        GlStateManager.bindTexture(0);

        // 深度测试重置
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(515); // GL_LEQUAL
        GlStateManager.depthMask(true);

        // 混合模式重置
        GlStateManager.disableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0); // GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA

        // 光照重置
        GlStateManager.disableLighting();
        GlStateManager.disableLight(0);
        GlStateManager.disableLight(1);
        GlStateManager.disableLight(2);
        GlStateManager.disableLight(3);
        GlStateManager.disableLight(4);
        GlStateManager.disableLight(5);
        GlStateManager.disableLight(6);
        GlStateManager.disableLight(7);

        // 颜色材质重置
        GlStateManager.disableColorMaterial();
        GlStateManager.colorMaterial(1032, 5634); // GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE

        // 雾效重置
        GlStateManager.disableFog();
        GlStateManager.setFog(2048); // GL_EXP

        // 剔除重置
        GlStateManager.enableCull();
        GlStateManager.cullFace(1029); // GL_BACK

        // 多边形偏移重置
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(0.0F, 0.0F);

        // 颜色掩码重置
        GlStateManager.colorMask(true, true, true, true);

        // 法线重置
        GlStateManager.disableNormalize();
        GlStateManager.disableRescaleNormal();

        // 纹理环境重置
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        // 矩阵模式重置
        GlStateManager.matrixMode(5889); // GL_PROJECTION
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888); // GL_MODELVIEW
        GlStateManager.loadIdentity();

        // 视口重置
        GlStateManager.viewport(0, 0,
                Minecraft.getMinecraft().displayWidth,
                Minecraft.getMinecraft().displayHeight);

        GlStateManager.clear(16640); // GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT
        // 着色模式重置
        GlStateManager.shadeModel(7424); // GL_SMOOTH
        // 颜色重置
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // 旋转和变换重置
        GlStateManager.rotate(0.0F, 0.0F, 0.0F, 0.0F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F);
        GlStateManager.translate(0.0F, 0.0F, 0.0F);

        // Alpha测试重置
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F); // GL_GREATER

        // 索引重置
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    private static final ShaderUtil gradientMaskShader = new ShaderUtil("gradientMask");
    private static final ShaderUtil gradientShader = new ShaderUtil("gradient");


    // 在DrawUtil中添加：
    public static void drawRectOutline(int x, int y, int width, int height, int thickness, int color) {
        float a = (color >> 24 & 0xFF) / 255f;
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(r, g, b, a);

        // 绘制四条边
        GL11.glBegin(GL11.GL_QUADS);
        // 上边
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x + width, y + thickness);
        GL11.glVertex2d(x, y + thickness);
        // 右边
        GL11.glVertex2d(x + width - thickness, y);
        GL11.glVertex2d(x + width, y);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x + width - thickness, y + height);
        // 下边
        GL11.glVertex2d(x, y + height - thickness);
        GL11.glVertex2d(x + width, y + height - thickness);
        GL11.glVertex2d(x + width, y + height);
        GL11.glVertex2d(x, y + height);
        // 左边
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x + thickness, y);
        GL11.glVertex2d(x + thickness, y + height);
        GL11.glVertex2d(x, y + height);
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
    public static void drawTriangle(int x, int y, int size, boolean down, int color) {
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_TRIANGLES);
        if (down) {
            // 向下箭头
            GL11.glVertex2d(x, y + size/2.0);
            GL11.glVertex2d(x - size/2.0, y - size/2.0);
            GL11.glVertex2d(x + size/2.0, y - size/2.0);
        } else {
            // 向上箭头
            GL11.glVertex2d(x, y - size/2.0);
            GL11.glVertex2d(x - size/2.0, y + size/2.0);
            GL11.glVertex2d(x + size/2.0, y + size/2.0);
        }
        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
    public static void drawRect(int x, int y, int width, int height, int color) {
        RenderUtil.drawRect(x, y, x + width, y + height, color);
    }
    static void glColor(final Color color) {
        GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
    }
    public static void applyGradientHorizontal(float x, float y, float width, float height, float alpha, Color left, Color right, Runnable content) {
        applyGradient(x, y, width, height, alpha, left, left, right, right, content);
    }
    public static void applyGradient(float x, float y, float width, float height, float alpha, Color bottomLeft, Color topLeft, Color bottomRight, Color topRight, Runnable content) {
        RenderUtil.resetColor();
        GLUtil.startBlend();
        gradientMaskShader.init();

        ScaledResolution sr = new ScaledResolution(mc);

        gradientMaskShader.setUniformf("location", x * sr.getScaleFactor(), (Minecraft.getMinecraft().displayHeight - (height * sr.getScaleFactor())) - (y * sr.getScaleFactor()));
        gradientMaskShader.setUniformf("rectSize", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        gradientMaskShader.setUniformf("alpha", alpha);
        gradientMaskShader.setUniformi("tex", 0);
        // Bottom Left
        gradientMaskShader.setUniformf("color1", bottomLeft.getRed() / 255f, bottomLeft.getGreen() / 255f, bottomLeft.getBlue() / 255f);
        //Top left
        gradientMaskShader.setUniformf("color2", topLeft.getRed() / 255f, topLeft.getGreen() / 255f, topLeft.getBlue() / 255f);
        //Bottom Right
        gradientMaskShader.setUniformf("color3", bottomRight.getRed() / 255f, bottomRight.getGreen() / 255f, bottomRight.getBlue() / 255f);
        //Top Right
        gradientMaskShader.setUniformf("color4", topRight.getRed() / 255f, topRight.getGreen() / 255f, topRight.getBlue() / 255f);

        //Apply the gradient to whatever is put here
        content.run();

        gradientMaskShader.unload();
        GLUtil.endBlend();
    }
}
