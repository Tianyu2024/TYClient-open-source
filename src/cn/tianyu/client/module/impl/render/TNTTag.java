package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderTNTPrimed;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityTNTPrimed;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.DecimalFormat;

/**
 * @Author: CN_tianyu
 */
@DefaultEnabled
public class TNTTag extends Module {

    public static TNTTag getInstance;

    public TNTTag(){
        super("TNTTag","显示TNT的爆炸时间", Category.render);
        getInstance = this;
    }

    public void renderTag(RenderTNTPrimed renderTNTPrimed, EntityTNTPrimed tntPrimed, double x, double y, double z, float partialTicks) {
        if (tntPrimed.fuse < 1)
            return;
        double d0 = tntPrimed.getDistanceSqToEntity((renderTNTPrimed.getRenderManager()).livingPlayer);
        if (d0 <= 4096.0D) {
            float number = (tntPrimed.fuse - partialTicks) / 20.0F;
            String str = (new DecimalFormat("0.00")).format(number);
            float f = 1.6F;
            float f1 = 0.016666668F * f;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.0F, (float) y + tntPrimed.height + 0.5F, (float) z);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-(renderTNTPrimed.getRenderManager()).playerViewY, 0.0F, 1.0F, 0.0F);
            int xMultiplier = 1;
            if (mc != null && mc.gameSettings != null && mc.gameSettings.thirdPersonView == 2)
                xMultiplier = -1;
            GlStateManager.rotate((renderTNTPrimed.getRenderManager()).playerViewX * xMultiplier, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-f1, -f1, f1);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            int i = 0;
            int j = FontManager.tenacityBoldFont20.getStringWidth(str) / 2;
            float green = Math.min(tntPrimed.fuse / 80.0F, 1.0F);
            Color color = new Color(1.0F - green, green, 0.0F);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.disableTexture2D();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos((-j - 1), (-1 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            worldrenderer.pos((-j - 1), (8 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            worldrenderer.pos((j + 1), (8 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            worldrenderer.pos((j + 1), (-1 + i), 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
            FontManager.axBold20.drawString(str, -FontManager.axBold20.getStringWidth(str) / 2, i - 3, color.getRGB());
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }
}
