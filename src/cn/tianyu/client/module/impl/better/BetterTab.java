package cn.tianyu.client.module.impl.better;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.util.DrawUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class BetterTab extends Module {
    public BetterTab() {
        super("BetterTab", "更美丽快捷栏", Category.better);
    }

    @Override
    public void draw() {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        //EntityPlayer entityplayer = (EntityPlayer) mc.getRenderViewEntity();
        EntityPlayer entityplayer = Minecraft.getMinecraft().thePlayer;
        int i = sr.getScaledWidth() / 2;
        DrawUtil.drawRect(i - 91 + entityplayer.inventory.currentItem * 20, sr.getScaledHeight() - 7, 22, 5, new Color(255, 255, 255, 255).getRGB());

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();

        for (int j = 0; j < 9; ++j) {
            int k = sr.getScaledWidth() / 2 - 90 + j * 20 + 2;
            int l = sr.getScaledHeight() - 16 - 3;
            renderHotbarItem(j, k, l, 0, entityplayer);
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();

        info();
    }

    public void info() {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc);
        int fps = Minecraft.getDebugFPS();
        font.drawString("FPS: " + fps, sr.getScaledWidth() / 2 - font.getStringWidth("FPS: " + fps) / 2 - 450, sr.getScaledHeight() - 20, 0xFFFFFFFF);
    }

    private void renderHotbarItem(int index, int xPos, int yPos, float partialTicks, EntityPlayer p_175184_5_) {
        ItemStack itemstack = p_175184_5_.inventory.mainInventory[index];

        if (itemstack != null) {
            float f = (float) itemstack.animationsToGo - partialTicks;

            if (f > 0.0F) {
                GlStateManager.pushMatrix();
                float f1 = 1.0F + f / 5.0F;
                GlStateManager.translate((float) (xPos + 8), (float) (yPos + 12), 0.0F);
                GlStateManager.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                GlStateManager.translate((float) (-(xPos + 8)), (float) (-(yPos + 12)), 0.0F);
            }

            Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(itemstack, xPos, yPos);

            if (f > 0.0F) {
                GlStateManager.popMatrix();
            }

            Minecraft.getMinecraft().getRenderItem().renderItemOverlays(Minecraft.getMinecraft().fontRendererObj, itemstack, xPos, yPos);
        }
    }
}
