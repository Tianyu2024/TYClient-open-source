package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.render.EventRender3D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * @Author: CN_tianyu
 */
public class EmoJiMask extends Module {
    public EmoJiMask() {
        super("EmoJiMask", "打人在身上出现一个emoji", Category.render);
    }

    @EventTarget
    public void onEmoji(EventRender3D event) {
        for (final EntityPlayer entity : mc.theWorld.playerEntities) {
            if (entity == mc.thePlayer) {
                continue;
            }
            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glDisable(2929);
            GL11.glNormal3f(0.0f, 1.0f, 0.0f);
            GlStateManager.enableBlend();
            GL11.glBlendFunc(770, 771);
            GL11.glDisable(3553);
            final float partialTicks = mc.timer.renderPartialTicks;
            final double n = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
            final double x = n - mc.getRenderManager().renderPosX;
            final double n2 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
            final double y = n2 - mc.getRenderManager().renderPosY;
            final double n3 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
            final double z = n3 - mc.getRenderManager().renderPosZ;
            float SCALE = 0.035f;
            GlStateManager.translate((float) x,
                    (float) y + entity.height + 0.5f - (entity.isChild() ? (entity.height / 2.0f) : 0.0f), (float) z);
            GL11.glNormal3f(0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
            GL11.glScalef(-SCALE, -SCALE, -(SCALE / 2.0f));
            final double xLeft = -20.0;
            final double yUp = 27.0;
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GlStateManager.disableBlend();
            GL11.glDisable(3042);

            RenderUtil.drawImage(new ResourceLocation("client/emoji.png"), (int) xLeft + 9, (int) yUp - 20, 25, 25);

            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glNormal3f(1.0f, 1.0f, 1.0f);
            GL11.glPopMatrix();
        }
    }
}
