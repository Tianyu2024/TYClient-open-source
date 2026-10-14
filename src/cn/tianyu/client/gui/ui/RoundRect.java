package cn.tianyu.client.gui.ui;

import cn.tianyu.client.util.RenderUtil;
import net.minecraft.client.gui.Gui;
import org.lwjgl.opengl.GL11;

public class RoundRect {

    public static void drawFilledCircle(double x, double y, double r, int c, int id) {
        float f = (float) (c >> 24 & 0xff) / 255F;
        float f1 = (float) (c >> 16 & 0xff) / 255F;
        float f2 = (float) (c >> 8 & 0xff) / 255F;
        float f3 = (float) (c & 0xff) / 255F;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glBegin(GL11.GL_POLYGON);
        if (id == 1) {
            GL11.glVertex2d(x, y);
            for (int i = 0; i <= 90; i++) {
                double x2 = Math.sin((i * 3.141526D / 180)) * r;
                double y2 = Math.cos((i * 3.141526D / 180)) * r;
                GL11.glVertex2d(x - x2, y - y2);
            }
        } else if (id == 2) {
            GL11.glVertex2d(x, y);
            for (int i = 90; i <= 180; i++) {
                double x2 = Math.sin((i * 3.141526D / 180)) * r;
                double y2 = Math.cos((i * 3.141526D / 180)) * r;
                GL11.glVertex2d(x - x2, y - y2);
            }
        } else if (id == 3) {
            GL11.glVertex2d(x, y);
            for (int i = 270; i <= 360; i++) {
                double x2 = Math.sin((i * 3.141526D / 180)) * r;
                double y2 = Math.cos((i * 3.141526D / 180)) * r;
                GL11.glVertex2d(x - x2, y - y2);
            }
        } else if (id == 4) {
            GL11.glVertex2d(x, y);
            for (int i = 180; i <= 270; i++) {
                double x2 = Math.sin((i * 3.141526D / 180)) * r;
                double y2 = Math.cos((i * 3.141526D / 180)) * r;
                GL11.glVertex2d(x - x2, y - y2);
            }
        } else {
            for (int i = 0; i <= 360; i++) {
                double x2 = Math.sin((i * 3.141526D / 180)) * r;
                double y2 = Math.cos((i * 3.141526D / 180)) * r;
                GL11.glVertex2f((float) (x - x2), (float) (y - y2));
            }
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
    public static void drawRoundRect(double xPosition, double yPosition, double endX, double endY, double radius,
                                     int color) {
        double width = endX - xPosition;
        double height = endY - yPosition;
        RenderUtil.drawRect((int) (xPosition + radius), (int) (yPosition + radius), (int) (xPosition + width - radius), (int) (yPosition + height - radius),
                color);
        RenderUtil.drawRect((int) xPosition, (int) (yPosition + radius), (int) (xPosition + radius), (int) (yPosition + height - radius), color);
        RenderUtil.drawRect((int) (xPosition + width - radius), (int) (yPosition + radius), (int) (xPosition + width), (int) (yPosition + height - radius),
                color);
        RenderUtil.drawRect((int) (xPosition + radius), (int) yPosition, (int) (xPosition + width - radius), (int) (yPosition + radius), color);
        RenderUtil.drawRect((int) (xPosition + radius), (int) (yPosition + height - radius), (int) (xPosition + width - radius), (int) (yPosition + height),
                color);
        drawFilledCircle(xPosition + radius, yPosition + radius, radius, color, 1);
        drawFilledCircle(xPosition + radius, yPosition + height - radius, radius, color, 2);
        drawFilledCircle(xPosition + width - radius, yPosition + radius, radius, color, 3);
        drawFilledCircle(xPosition + width - radius, yPosition + height - radius, radius, color, 4);
    }
}
