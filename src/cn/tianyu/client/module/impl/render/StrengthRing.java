package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

/**
 * @Author: CN_tianyu
 */
public class StrengthRing extends Module {

    private final NumberValue ringRadius = new NumberValue("Radius", 45, 20, 100, 1);
    private final NumberValue ringWidth = new NumberValue("Ring Width", 3, 1, 8, 0.5);
    private final BooleanValue showTime = new BooleanValue("Show Seconds", true);

    public StrengthRing() {
        super("StrengthRing", "力量计时黄圈", Category.render);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (mc.thePlayer == null) return;

        PotionEffect strength = mc.thePlayer.getActivePotionEffect(Potion.damageBoost);
        if (strength == null) return;

        int remainTicks = strength.getDuration();
        float remainSecond = remainTicks / 20F;
        // 计算绘制角度：剩余时间越少，圆弧角度越小，从右侧向左收缩
        float totalAngle = 360F * (remainSecond / 180F);

        ScaledResolution sr = new ScaledResolution(mc);
        int centerX = sr.getScaledWidth() / 2;
        int centerY = sr.getScaledHeight() / 2;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // 从右侧(0°) 向左逆时针绘制圆弧，角度随剩余时长缩小
        drawArc(centerX, centerY, ringRadius.getValue().floatValue(), ringWidth.getValue().floatValue(),
                0, totalAngle, new Color(255, 205, 0, 255));

        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();

        // 中心剩余时间文字
        if (showTime.getValue()) {
            String secText = String.format("%.1f", remainSecond);
            mc.fontRendererObj.drawStringWithShadow(secText,
                    centerX - mc.fontRendererObj.getStringWidth(secText) / 2F,
                    centerY - 5,
                    new Color(255, 205, 0).getRGB());
        }

        GlStateManager.popMatrix();
    }

    private void drawArc(int cx, int cy, float radius, float thickness, float startAngle, float endAngle, Color color) {
        Tessellator tess = Tessellator.getInstance();
        int segments = 90;
        GL11.glLineWidth(thickness);

        tess.getWorldRenderer().begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        float angleStep = (endAngle - startAngle) / segments;

        for (int i = 0; i <= segments; i++) {
            double rad = Math.toRadians(startAngle + angleStep * i);
            float x = (float) (cx + Math.cos(rad) * radius);
            float y = (float) (cy + Math.sin(rad) * radius);
            tess.getWorldRenderer().pos(x, y, 0)
                    .color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F)
                    .endVertex();
        }
        tess.draw();
    }
}