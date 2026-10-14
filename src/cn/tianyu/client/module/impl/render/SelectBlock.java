package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.render.EventBlockOverlay;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.util.impl.render.color.HSBColor;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ColorValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.awt.*;

/**
 * @author CN_tianyu
 */

@DefaultEnabled
public class SelectBlock extends Module {
    public SelectBlock() {
        super("SelectBlock", "显示方块高亮", Category.render);
    }

    public BooleanValue rect = new BooleanValue("Rect", true);
    public BooleanValue outline = new BooleanValue("Outline", false);
    public NumberValue lineWidth = new NumberValue("LineWidth", 2.0, 2.0, 5.0, 1.0);

    public ColorValue rectColor = new ColorValue("RectColor", new HSBColor(255, 255, 255, 160));
    public ColorValue outlineColor = new ColorValue("OutlineColor", new HSBColor(255, 255, 255, 255));

    @EventTarget
    public void onRender3D(EventBlockOverlay event) {
        event.setCancelled(true);

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glLineWidth(this.lineWidth.getValue().floatValue());

        if (rect.getValue()) {
            Color color = this.rectColor.getValue();

            float red = color.getRed() / 255.0f;
            float green = color.getGreen() / 255.0f;
            float blue = color.getBlue() / 255.0f;
            float alpha = color.getAlpha() / 255.0f;

            this.drawFilledBoundingBox(event.getBB(), red, green, blue, alpha);
        }
        if (outline.getValue()) {
            Color color = this.outlineColor.getValue();

            float red = color.getRed() / 255.0f;
            float green = color.getGreen() / 255.0f;
            float blue = color.getBlue() / 255.0f;
            float alpha = color.getAlpha() / 255.0f;

            GlStateManager.color(red, green, blue, alpha);

            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
            RenderGlobal.drawSelectionBoundingBox(event.getBB());
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
        }
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private void drawFilledBoundingBox(AxisAlignedBB box, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(box.minX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(box.minX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.minX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.minZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.maxY, box.maxZ).color(red, green, blue, alpha).endVertex();
        worldRenderer.pos(box.maxX, box.minY, box.maxZ).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
    }
}
