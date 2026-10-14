package cn.tianyu.client.util.impl.render;

import cn.tianyu.client.util.impl.client.MathUtil;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class GLRendering {
    public static void glDrawArcOutline(double x2, double y2, float radius, float angleStart, float angleEnd, float lineWidth, int colour) {
        float[][] vertices;
        int segments = (int)(radius * 4.0f);
        boolean restore = GLRendering.glEnableBlend();
        GL11.glDisable((int)3553);
        GL11.glLineWidth((float)lineWidth);
        GLRendering.glColour(colour);
        GL11.glEnable((int)2848);
        GL11.glHint((int)3154, (int)4354);
        GL11.glTranslated((double)x2, (double)y2, (double)0.0);
        GL11.glBegin((int)3);
        for (float[] vertex : vertices = MathUtil.getArcVertices(radius, angleStart, angleEnd, segments)) {
            GL11.glVertex2f((float)vertex[0], (float)vertex[1]);
        }
        GL11.glEnd();
        GL11.glTranslated((double)(-x2), (double)(-y2), (double)0.0);
        GL11.glDisable((int)2848);
        GL11.glHint((int)3154, (int)4352);
        GLRendering.glRestoreBlend(restore);
        GL11.glEnable((int)3553);
    }
    public static void glDrawFilledEllipse(double x2, double y2, double radius, int startIndex, int endIndex, int polygons, boolean smooth, int colour) {
        boolean restore = GLRendering.glEnableBlend();
        if (smooth) {
            GL11.glEnable((int)2881);
            GL11.glHint((int)3155, (int)4354);
        }
        GL11.glDisable((int)3553);
        GLRendering.glColour(colour);
        GL11.glDisable((int)2884);
        GL11.glBegin((int)9);
        GL11.glVertex2d((double)x2, (double)y2);
        for (double i = (double)startIndex; i <= (double)endIndex; i += 1.0) {
            double theta = Math.PI * 2 * i / (double)polygons;
            GL11.glVertex2d((double)(x2 + radius * Math.cos(theta)), (double)(y2 + radius * Math.sin(theta)));
        }
        GL11.glEnd();
        GLRendering.glRestoreBlend(restore);
        if (smooth) {
            GL11.glDisable((int)2881);
            GL11.glHint((int)3155, (int)4352);
        }
        GL11.glEnable((int)2884);
        GL11.glEnable((int)3553);
    }

    public static void glDrawFilledEllipse(double x2, double y2, float radius, int colour) {
        boolean restore = GLRendering.glEnableBlend();
        GL11.glEnable((int)2832);
        GL11.glHint((int)3153, (int)4354);
        GL11.glDisable((int)3553);
        GLRendering.glColour(colour);
        GL11.glPointSize((float)radius);
        GL11.glBegin((int)0);
        GL11.glVertex2d((double)x2, (double)y2);
        GL11.glEnd();
        GLRendering.glRestoreBlend(restore);
        GL11.glDisable((int)2832);
        GL11.glHint((int)3153, (int)4352);
        GL11.glEnable((int)3553);
    }
    public static void glColour(int color) {
        GL11.glColor4ub((byte)((byte)(color >> 16 & 0xFF)), (byte)((byte)(color >> 8 & 0xFF)), (byte)((byte)(color & 0xFF)), (byte)((byte)(color >> 24 & 0xFF)));
    }

    public static boolean glEnableBlend() {
        boolean wasEnabled = GL11.glIsEnabled((int)3042);
        if (!wasEnabled) {
            GL11.glEnable((int)3042);
            GL14.glBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
        }
        return wasEnabled;
    }

    public static void glRestoreBlend(boolean wasEnabled) {
        if (!wasEnabled) {
            GL11.glDisable((int)3042);
        }
    }
}
