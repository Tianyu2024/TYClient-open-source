package cn.tianyu.client.TianyuOptiFine.betterfps.impl.chuck;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;

/**
 * @Author: CN_tianyu
 */
public final class TessHelper
{
    private static final Tessellator TESS = Tessellator.getInstance();

    private TessHelper(){}

    public static WorldRenderer getBuffer()
    {
        return TESS.getWorldRenderer();
    }

    public static void draw()
    {
        TESS.draw();
    }
}