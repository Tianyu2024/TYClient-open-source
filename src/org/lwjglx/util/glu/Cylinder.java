package org.lwjglx.util.glu;

import org.lwjgl.opengl.GL11;

/**
 * 完美平替原版 GLU 的 Cylinder（圆柱体/圆锥体）绘制器（已补充 setDrawStyle 逻辑）
 */
public class Cylinder {
    // 模拟原版 GLU 的绘制样式常量
    public static final int GLU_FILL = 100012;
    public static final int GLU_LINE = 100011;
    public static final int GLU_POINT = 100010;
    public static final int GLU_SILHOUETTE = 100013;

    private int normals = 100000;
    private boolean textureFlag = false;
    private int drawStyle = GLU_FILL; // 默认是填充模式

    public void setNormals(int normals) {
        this.normals = normals;
    }

    public void setTextureFlag(boolean textureFlag) {
        this.textureFlag = textureFlag;
    }

    /**
     * 新增：设置绘制样式（Cylinder.setDrawStyle）
     */
    public void setDrawStyle(int drawStyle) {
        this.drawStyle = drawStyle;
    }

    /**
     * 仿写 GLU 的 draw 方法，利用数学公式直接生成圆柱体顶点
     */
    public void draw(float baseRadius, float topRadius, float height, int slices, int stacks) {
        float da, dr, zLow, zHigh;
        float x, y;

        da = 2.0f * (float) Math.PI / slices;
        dr = (topRadius - baseRadius) / stacks;

        // 根据设置的 drawStyle 决定 OpenGL 的绘制模式
        int glMode = GL11.GL_QUAD_STRIP;
        if (this.drawStyle == GLU_LINE || this.drawStyle == GLU_SILHOUETTE) {
            glMode = GL11.GL_LINE_STRIP; // 切换为线框模式
        } else if (this.drawStyle == GLU_POINT) {
            glMode = GL11.GL_POINTS;     // 切换为点集模式
        }

        for (int i = 0; i < stacks; i++) {
            zLow = i * height / stacks;
            zHigh = (i + 1) * height / stacks;
            float rLow = baseRadius + i * dr;
            float rHigh = baseRadius + (i + 1) * dr;

            GL11.glBegin(glMode);
            for (int j = 0; j <= slices; j++) {
                float angle = (j == slices) ? 0.0f : j * da;
                x = (float) Math.sin(angle);
                y = (float) Math.cos(angle);

                if (textureFlag) {
                    GL11.glTexCoord2f((float) j / slices, (float) i / stacks);
                }
                GL11.glNormal3f(x, y, 0.0f);
                GL11.glVertex3f(x * rLow, y * rLow, zLow);

                if (textureFlag) {
                    GL11.glTexCoord2f((float) j / slices, (float) (i + 1) / stacks);
                }
                GL11.glVertex3f(x * rHigh, y * rHigh, zHigh);
            }
            GL11.glEnd();
        }
    }
}