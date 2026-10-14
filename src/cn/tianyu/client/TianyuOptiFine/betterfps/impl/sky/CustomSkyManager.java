package cn.tianyu.client.TianyuOptiFine.betterfps.impl.sky;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CustomSkyManager {

    private static final String BASE_PATH = "mcpatcher/sky/world0/";
    private static final List<SkyLayer> skyLayers = new ArrayList<>();

    public static void loadCustomSky() {
        skyLayers.clear();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getResourceManager() == null) return;

        // 遍历 1 到 20 个可能的 OptiFine 天空配置文件
        for (int i = 1; i <= 20; i++) {
            // 在 1.8.8 中，使用标准的 ResourceLocation 虚拟路径
            ResourceLocation propLoc = new ResourceLocation("mcpatcher/sky/world0/sky" + i + ".properties");

            java.io.InputStream stream = null;
            try {
                // 通过全版本通用的 getResource 动态获取。原版系统会自动去默认包和玩家当前材质包里搜寻，谁权重高用谁。
                net.minecraft.client.resources.IResource resource = mc.getResourceManager().getResource(propLoc);
                if (resource != null) {
                    stream = resource.getInputStream();
                }
            } catch (Exception ignored) {
                // 如果这个编号的 .properties 不存在，直接跳过
                continue;
            }

            if (stream == null) continue;

            try {
                Properties props = new Properties();
                props.load(stream);
                stream.close();

                SkyLayer layer = new SkyLayer();

                // 动态从配置文件中读取真实贴图路径
                String source = props.getProperty("source", "sky" + i + ".png");

                if (source.startsWith("../")) {
                    // 处理相对路径，例如 "../stars.png" -> "mcpatcher/sky/stars.png"
                    String cleanPath = "mcpatcher/sky/" + source.substring(3);
                    layer.texture = new ResourceLocation("minecraft", cleanPath);
                } else if (!source.contains("/")) {
                    // 纯文件名，例如 "my_sky.png" -> "mcpatcher/sky/world0/my_sky.png"
                    layer.texture = new ResourceLocation("minecraft", BASE_PATH + source);
                } else {
                    // 绝对路径，例如 "textures/environment/custom_sky.png"
                    layer.texture = new ResourceLocation("minecraft", source);
                }

                layer.blendMode = props.getProperty("blend", "add");
                layer.speed = Float.parseFloat(props.getProperty("speed", "1.0"));

                layer.startFadeIn = parseOptifineTime(props.getProperty("startFadeIn", "18:00"));
                layer.endFadeIn = parseOptifineTime(props.getProperty("endFadeIn", "19:00"));
                layer.startFadeOut = parseOptifineTime(props.getProperty("startFadeOut", "05:00"));
                layer.endFadeOut = parseOptifineTime(props.getProperty("endFadeOut", "06:00"));

                // 预先注册并将该动态纹理上传至显卡，彻底解决空白问题
                mc.getTextureManager().bindTexture(layer.texture);

                skyLayers.add(layer);
                System.out.println("[TYClient] Successfully resolved texture: " + layer.texture.getResourcePath() + " for sky" + i);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void renderCustomSky(float partialTicks) {
        if (skyLayers.isEmpty()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return;

        long worldTime = mc.theWorld.getWorldTime() % 24000L;

        // LWJGL 3 矩阵与 VBO 深度隔离
        GlStateManager.pushMatrix();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // 状态机初始化：OptiFine 平面天穹必须完全依赖正确的混合模式
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.depthMask(false); // 关键：不写入深度，地形方块绝对不会再被天空盒遮挡或产生“区块飞天”

        for (SkyLayer layer : skyLayers) {
            float alpha = layer.getAlpha(worldTime);
            if (alpha <= 0.0f) continue; // 不在显示时间段则不渲染

            // 动态适配混合模式
            if ("add".equalsIgnoreCase(layer.blendMode) || "screen".equalsIgnoreCase(layer.blendMode)) {
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            } else if ("clear".equalsIgnoreCase(layer.blendMode)) {
                GlStateManager.blendFunc(GL11.GL_ZERO, GL11.GL_ZERO);
            } else {
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }

            // 绑定该层专属的、从 properties 动态获取到的图片
            mc.getTextureManager().bindTexture(layer.texture);

            // 必须开启边缘重复平铺，否则非标准比例的图片会因为采样越界变成紫黑块
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

            // 为这一张图开启独立的局部矩阵，防止多张图互相污染
            GlStateManager.pushMatrix();

            // 天空层自转计算（绕 Y 轴旋转）
            float rotateAngle = ((float) worldTime + partialTicks) * 0.015f * layer.speed;
            GlStateManager.rotate(rotateAngle, 0.0f, 1.0f, 0.0f);

            // 强制应用贴图本身的色彩，并附加上动态淡入淡出的透明度
            GlStateManager.color(1.0f, 1.0f, 1.0f, alpha);

            // 执行这一层贴图的绘制
            drawSkyBox();

            // 释放当前图层的局部矩阵
            GlStateManager.popMatrix();
        }

        // 彻底解除残留状态，将控制权完好地归还给原版地形渲染管线
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.enableFog();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.popMatrix();
    }

    private static void drawSkyBox() {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        // 这是一个极大的平铺面，用来包裹住玩家视野，防止边缘漏底
        double size = 100.0;
        double height = 100.0; // 距离玩家的垂直高度

        // OptiFine 核心：它只绘制一个面向玩家的巨大天穹穹顶（或上下对称面），而不是旋转 6 个方向
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        // 顶面/天穹平铺（利用完整的 0.0 ~ 1.0 UV 动态映射整个图片）
        worldrenderer.pos(-size, height, -size).tex(0.0D, 0.0D).endVertex();
        worldrenderer.pos(-size, height,  size).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos( size, height,  size).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos( size, height, -size).tex(1.0D, 0.0D).endVertex();

        // 底面平铺（用于有些材质包做地底星空或下半球渲染）
        worldrenderer.pos(-size, -height,  size).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(-size, -height, -size).tex(0.0D, 0.0D).endVertex();
        worldrenderer.pos( size, -height, -size).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos( size, -height,  size).tex(1.0D, 1.0D).endVertex();

        tessellator.draw();
    }

    private static int parseOptifineTime(String timeStr) {
        try {
            if (timeStr.contains(":")) {
                String[] split = timeStr.split(":");
                int hours = Integer.parseInt(split[0]);
                int minutes = Integer.parseInt(split[1]);
                int totalMinutes = (hours * 60 + minutes) - (6 * 60);
                if (totalMinutes < 0) totalMinutes += 24 * 60;
                return (int) (((double) totalMinutes / (24 * 60)) * 24000);
            } else {
                return Integer.parseInt(timeStr);
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private static class SkyLayer {
        ResourceLocation texture;
        float speed;
        String blendMode;
        int startFadeIn, endFadeIn, startFadeOut, endFadeOut;

        float getAlpha(long time) {
            if (startFadeIn < endFadeIn) {
                if (time >= startFadeIn && time < endFadeIn) {
                    return (float)(time - startFadeIn) / (endFadeIn - startFadeIn);
                }
            } else {
                if (time >= startFadeIn || time < endFadeIn) {
                    long t = time < endFadeIn ? time + 24000 : time;
                    long s = startFadeIn;
                    long e = endFadeIn + 24000;
                    return (float)(t - s) / (e - s);
                }
            }

            if (startFadeOut < endFadeOut) {
                if (time >= startFadeOut && time < endFadeOut) {
                    return 1.0f - (float)(time - startFadeOut) / (endFadeOut - startFadeOut);
                }
            } else {
                if (time >= startFadeOut || time < endFadeOut) {
                    long t = time < endFadeOut ? time + 24000 : time;
                    long s = startFadeOut;
                    long e = endFadeOut + 24000;
                    return 1.0f - (float)(t - s) / (e - s);
                }
            }

            if (endFadeIn < startFadeOut) {
                if (time >= endFadeIn && time < startFadeOut) return 1.0f;
            } else {
                if (time >= endFadeIn || time < startFadeOut) return 1.0f;
            }

            return 0.0f;
        }
    }
}