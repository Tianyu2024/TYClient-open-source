package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

/**
 * @author CN_tianyu
 */

public class HitBox extends Module {
    public HitBox() {
        super("HitBox", "显示碰撞箱", Category.render);
    }

    @Override
    public void onRender(float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        double renderPosX = minecraft.getRenderManager().renderPosX;
        double renderPosY = minecraft.getRenderManager().renderPosY;
        double renderPosZ = minecraft.getRenderManager().renderPosZ;

        for (Entity entity : minecraft.theWorld.loadedEntityList) {
            if (entity instanceof EntityLivingBase && !(entity instanceof EntityPlayerSP)) {
                AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
                AxisAlignedBB adjustedBoundingBox = new AxisAlignedBB(
                        entityBoundingBox.minX - renderPosX,
                        entityBoundingBox.minY - renderPosY,
                        entityBoundingBox.minZ - renderPosZ,
                        entityBoundingBox.maxX - renderPosX,
                        entityBoundingBox.maxY - renderPosY,
                        entityBoundingBox.maxZ - renderPosZ
                );

                drawESP(adjustedBoundingBox, 1.0F, 1.0F, 1.0F, 1.0F); // 设置为白色
            }
        }
    }

    private void drawESP(AxisAlignedBB boundingBox, float red, float green, float blue, float alpha) {
        GL11.glPushMatrix();
        GlStateManager.pushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(2.0F);

        GL11.glColor4f(red, green, blue, alpha);

        // Draw the HitBox outline
        GL11.glBegin(GL11.GL_LINES);

        // Bottom face
        drawLine(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.minY, boundingBox.minZ);
        drawLine(boundingBox.maxX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.minY, boundingBox.maxZ);
        drawLine(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ, boundingBox.minX, boundingBox.minY, boundingBox.maxZ);
        drawLine(boundingBox.minX, boundingBox.minY, boundingBox.maxZ, boundingBox.minX, boundingBox.minY, boundingBox.minZ);

        // Top face
        drawLine(boundingBox.minX, boundingBox.maxY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        drawLine(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        drawLine(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ, boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);
        drawLine(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ, boundingBox.minX, boundingBox.maxY, boundingBox.minZ);

        // Vertical lines
        drawLine(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.minX, boundingBox.maxY, boundingBox.minZ);
        drawLine(boundingBox.maxX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.minZ);
        drawLine(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        drawLine(boundingBox.minX, boundingBox.minY, boundingBox.maxZ, boundingBox.minX, boundingBox.maxY, boundingBox.maxZ);

        GL11.glEnd();

        GlStateManager.popMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    private void drawLine(double x1, double y1, double z1, double x2, double y2, double z2) {
        GL11.glVertex3d(x1, y1, z1);
        GL11.glVertex3d(x2, y2, z2);
    }
}
