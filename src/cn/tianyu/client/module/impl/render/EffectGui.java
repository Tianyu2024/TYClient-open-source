package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

@DefaultEnabled
public class EffectGui extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public EffectGui() {
        super("EffectGui", "显示药水效果", Category.render);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void draw() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return; // 如果玩家或世界为空，直接返回
        }

        // 获取玩家当前的所有药水效果
        Collection<PotionEffect> activePotionEffects = mc.thePlayer.getActivePotionEffects();
        if (activePotionEffects.isEmpty()) {
            return; // 如果没有药水效果，直接返回
        }

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int screenWidth = scaledResolution.getScaledWidth();
        int screenHeight = scaledResolution.getScaledHeight();
        int startX = 10; // 左边距
        int startY = screenHeight / 2 - (activePotionEffects.size() * 20) / 2; // 垂直居中

        for (PotionEffect effect : activePotionEffects) {
            // 获取药水ID
            int potionID = effect.getPotionID();

            // 加载药水贴图
            ResourceLocation potionTexture = new ResourceLocation("client/potion/" + potionID + ".png");
            mc.getTextureManager().bindTexture(potionTexture);

            // 渲染药水图标（假设图标大小为 16x16）
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // 重置颜色
            Gui.drawModalRectWithCustomSizedTexture(startX, startY, 0, 0, 16, 16, 16, 16);

            // 去掉前缀
            String effectName = Potion.potionTypes[potionID].getName();
            effectName = effectName.replace("potion.", ""); // 去掉 "potion." 前缀

            // 获取药水效果的时间
            int duration = effect.getDuration() / 20;

            // 渲染间
            String displayText = effectName + " " + duration + "s";

            // 渲染文本
            mc.fontRendererObj.drawString(displayText, startX + 20, startY + 4, 0xFFFFFF);

            // 更Y 坐标
            startY += 20;
        }
    }
}