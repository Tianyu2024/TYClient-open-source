package cn.tianyu.client.TianyuOptiFine.betterfps.impl.chuck;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

/**
 * @Author: CN_tianyu
 */
public final class MCGLPipeline
{
    public static final MCGLPipeline INSTANCE = new MCGLPipeline();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();

    private boolean blendState;
    private boolean depthState;
    private boolean tex2DState;
    private boolean cullState;
    private boolean alphaTestState;
    private int lastTexId = -1;
    private int matrixStackDepth = 0;

    private MCGLPipeline(){}

    public void reset()
    {
        blendState = false;
        depthState = false;
        tex2DState = false;
        cullState = false;
        alphaTestState = false;
        lastTexId = -1;

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        while (matrixStackDepth > 0)
        {
            GL11.glPopMatrix();
            matrixStackDepth--;
        }
    }

    public void enableBlend()
    {
        if (!blendState)
        {
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            blendState = true;
        }
    }

    public void disableBlend()
    {
        if (blendState)
        {
            GL11.glDisable(GL11.GL_BLEND);
            blendState = false;
        }
    }

    public void enableDepth()
    {
        if (!depthState)
        {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            depthState = true;
        }
    }

    public void disableDepth()
    {
        if (depthState)
        {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            depthState = false;
        }
    }

    public void enableTexture2D()
    {
        if (!tex2DState)
        {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            tex2DState = true;
        }
    }

    public void disableTexture2D()
    {
        if (tex2DState)
        {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            tex2DState = false;
        }
    }

    public void enableCull()
    {
        if (!cullState)
        {
            GL11.glEnable(GL11.GL_CULL_FACE);
            cullState = true;
        }
    }

    public void disableCull()
    {
        if (cullState)
        {
            GL11.glDisable(GL11.GL_CULL_FACE);
            cullState = false;
        }
    }

    public void enableAlphaTest()
    {
        if (!alphaTestState)
        {
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
            alphaTestState = true;
        }
    }

    public void disableAlphaTest()
    {
        if (alphaTestState)
        {
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            alphaTestState = false;
        }
    }

    public void bindTexture(int textureId)
    {
        if (lastTexId != textureId)
        {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            lastTexId = textureId;
            enableTexture2D();
        }
    }

    // 1.8.9 原生Tessellator绘制，无startDrawingQuads调用，上层手动控制
    public void startWorldBatch()
    {
        reset();
        enableDepth();
        enableCull();
        enableTexture2D();
        enableAlphaTest();
        // 关键：开启绘制，消除 Not building 报错
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
    }

    public void flushBatch()
    {
        TessHelper.draw();
    }

    public void pushMatrix()
    {
        GL11.glPushMatrix();
        matrixStackDepth++;
    }

    public void popMatrix()
    {
        if (matrixStackDepth > 0)
        {
            GL11.glPopMatrix();
            matrixStackDepth--;
        }
    }

    public void translate(double x, double y, double z)
    {
        GL11.glTranslated(x, y, z);
    }
}