package cn.tianyu.client.util;

import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.gui.clickgui.powerx.NLRenderUtil;
import cn.tianyu.client.util.annotations.AnimationUtil;
import cn.tianyu.client.util.impl.client.MathUtil;
import cn.tianyu.client.util.impl.render.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import org.lwjglx.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static cn.tianyu.client.TYClient.mc;
import static cn.tianyu.client.font.FontManager.tenacityBoldFont14;
import static cn.tianyu.client.util.impl.render.GLUtil.setup2DRendering;
import static net.minecraft.client.renderer.GlStateManager.disableBlend;
import static net.minecraft.client.renderer.GlStateManager.enableTexture2D;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_FLAT;

/**
 * @author CN_tianyu
 */

public class RenderUtil {

    public static void enableRenderState() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
    }

    public static void disableRenderState() {
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

//    private static final Tessellator tessellator = Tessellator.getInstance();
//    private static final WorldRenderer worldrenderer = tessellator.getWorldRenderer();

    private static final Frustum FRUSTUM = new Frustum();
    public static final ShaderUtil roundedShader = new ShaderUtil("roundedRect");

    @Getter
    @Setter
    private static double frameDeltaTime = 0;
    private static int lastScaledWidth;
    private static int lastScaledHeight;
    private static ScaledResolution scaledResolution;
    private static float lastGuiScale;
    private static final Map<Integer, Boolean> glCapMap = new HashMap<>();
    private static final ShaderUtil roundedGradientShader = new ShaderUtil("roundedRectGradient");
    private static Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

    public static void drawProgressBar(int packet){
        ScaledResolution sr = new ScaledResolution(mc);
        int startX = sr.getScaledWidth() / 2 - 60;
        int startY = sr.getScaledHeight() / 2 + 8;
        GlStateManager.disableAlpha();
        if(packet > 120) packet =120;
        drawGradientRoundLR(startX,startY,120,4,3, new Color(0,0,0,100),new Color(0,0,0,100));
        drawGradientRoundLR(startX,startY,packet,4,3, new Color(210,0,0,160),new Color(210,0,0,160));
        GlStateManager.disableAlpha();
    }

    public static int width() {
        return new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
    }

    public static int height() {
        return new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
    }

    public static void drawGradientRoundLR(float x, float y, float width, float height, float radius, Color color1, Color color2) {
        drawGradientRound(x, y, width, height, radius, color1, color2, color2, color1);
    }

    public static void drawGradientRound(float x, float y, float width, float height, float radius, Color bottomLeft, Color topLeft, Color bottomRight, Color topRight) {
        RenderUtil.setAlphaLimit(0);
        RenderUtil.resetColor();
        GLUtil.startBlend();
        roundedGradientShader.init();
        setupRoundedRectUniforms(x, y, width, height, radius, roundedGradientShader);
        //Top left
        roundedGradientShader.setUniformf("color1", topLeft.getRed() / 255f, topLeft.getGreen() / 255f, topLeft.getBlue() / 255f, topLeft.getAlpha() / 255f);
        // Bottom Left
        roundedGradientShader.setUniformf("color2", bottomLeft.getRed() / 255f, bottomLeft.getGreen() / 255f, bottomLeft.getBlue() / 255f, bottomLeft.getAlpha() / 255f);
        //Top Right
        roundedGradientShader.setUniformf("color3", topRight.getRed() / 255f, topRight.getGreen() / 255f, topRight.getBlue() / 255f, topRight.getAlpha() / 255f);
        //Bottom Right
        roundedGradientShader.setUniformf("color4", bottomRight.getRed() / 255f, bottomRight.getGreen() / 255f, bottomRight.getBlue() / 255f, bottomRight.getAlpha() / 255f);
        ShaderUtil.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedGradientShader.unload();
        GLUtil.endBlend();
    }

    static void setupRoundedRectUniforms(float x, float y, float width, float height, float radius, ShaderUtil roundedTexturedShader) {
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        roundedTexturedShader.setUniformf("location", x * sr.getScaleFactor(),
                (Minecraft.getMinecraft().displayHeight - (height * sr.getScaleFactor())) - (y * sr.getScaleFactor()));
        roundedTexturedShader.setUniformf("rectSize", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        roundedTexturedShader.setUniformf("radius", radius * sr.getScaleFactor());
    }

    public static void drawExhiEnchants(ItemStack stack, float x, float y) {
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
        disableBlend();
        GlStateManager.resetColor();
        final int darkBorder = 0xFF000000;
        if (stack.getItem() instanceof ItemArmor) {
            int prot = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack);
            int unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            int thorn = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack);
            if (prot > 0) {
                drawExhiOutlined(prot + "", drawExhiOutlined("P", x, y, darkBorder, -1) + 2, y, getBorderColor(prot), getMainColor(prot));
                y += 5;
            }
            if (unb > 0) {
                drawExhiOutlined(unb + "", drawExhiOutlined("U", x, y, darkBorder, -1) + 2, y, getBorderColor(unb), getMainColor(unb));
                y += 5;
            }
            if (thorn > 0) {
                drawExhiOutlined(thorn + "", drawExhiOutlined("T", x, y, darkBorder, -1) + 2, y, getBorderColor(thorn), getMainColor(thorn));
                y += 5;
            }
        }
        if (stack.getItem() instanceof ItemBow) {
            int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
            int flame = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack);
            int unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (power > 0) {
                drawExhiOutlined(power + "", drawExhiOutlined("Pow", x, y, darkBorder, -1) + 2, y, getBorderColor(power), getMainColor(power));
                y += 5;
            }
            if (punch > 0) {
                drawExhiOutlined(punch + "", drawExhiOutlined("Pun", x, y, darkBorder, -1) + 2, y, getBorderColor(punch), getMainColor(punch));
                y += 5;
            }
            if (flame > 0) {
                drawExhiOutlined(flame + "", drawExhiOutlined("F", x, y, darkBorder, -1) + 2, y, getBorderColor(flame), getMainColor(flame));
                y += 5;
            }
            if (unb > 0) {
                drawExhiOutlined(unb + "", drawExhiOutlined("U", x, y, darkBorder, -1) + 2, y, getBorderColor(unb), getMainColor(unb));
                y += 5;
            }
        }
        if (stack.getItem() instanceof ItemSword) {
            int sharp = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
            int kb = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
            int fire = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            int unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (sharp > 0) {
                drawExhiOutlined(sharp + "", drawExhiOutlined("S", x, y, darkBorder, -1) + 2, y, getBorderColor(sharp), getMainColor(sharp));
                y += 5;
            }
            if (kb > 0) {
                drawExhiOutlined(kb + "", drawExhiOutlined("K", x, y, darkBorder, -1) + 2, y, getBorderColor(kb), getMainColor(kb));
                y += 5;
            }
            if (fire > 0) {
                drawExhiOutlined(fire + "", drawExhiOutlined("F", x, y, darkBorder, -1) + 2, y, getBorderColor(fire), getMainColor(fire));
                y += 5;
            }
            if (unb > 0) {
                drawExhiOutlined(unb + "", drawExhiOutlined("U", x, y, darkBorder, -1) + 2, y, getBorderColor(unb), getMainColor(unb));
            }
        }
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
    }
    private static float drawExhiOutlined(String text, float x, float y, int borderColor, int mainColor) {
        tenacityBoldFont14.drawString(text, x, y - (float) 0.35, borderColor);
        tenacityBoldFont14.drawString(text, x, y + (float) 0.35, borderColor);
        tenacityBoldFont14.drawString(text, x - (float) 0.35, y, borderColor);
        tenacityBoldFont14.drawString(text, x + (float) 0.35, y, borderColor);
        tenacityBoldFont14.drawString(text, x, y, mainColor);
        return x + tenacityBoldFont14.getStringWidth(text) - 2F;
    }
    private static int getMainColor(int level) {
        if (level == 4)
            return 0xFFAA0000;
        return -1;
    }

    private static int getBorderColor(int level) {
        if (level == 2)
            return 0x7055FF55;
        if (level == 3)
            return 0x7000AAAA;
        if (level == 4)
            return 0x70AA0000;
        if (level >= 5)
            return 0x70FFAA00;
        return 0x70FFFFFF;
    }

    public static void drawRound(float x, float y, float width, float height, float radius, boolean blur, Color color, boolean topLeftCorner, boolean bottomLeftCorner, boolean topRightCorner, boolean bottomRightCorner) {
        GlStateManager.resetColor();
        GlStateManager.enableBlend();

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (0 * .01));

        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//        boolean flag = GL11.glIsEnabled(GL11.GL_ALPHA_TEST) && disableAlphaTest;
//        if (flag) {
//            GL11.glDisable(GL11.GL_ALPHA_TEST);
//        }

        roundedShader.init();

        float alpha = color.getAlpha() / 255f;
//        if (alpha < 0.101 && !disableAlphaTest) {
//            alpha = 0.101f;
//        }

        setupRoundedRectUniforms(x, y, width, height, radius, roundedShader);
        roundedShader.setUniformi("blur", blur ? 1 : 0);
        roundedShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, alpha);
        roundedShader.setUniformf("corner", topLeftCorner ? 1 : 0, bottomLeftCorner ? 1 : 0, topRightCorner ? 1 : 0, bottomRightCorner ? 1 : 0);

        ShaderUtil.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedShader.unload();
//        if (flag)
//            GL11.glEnable(GL11.GL_ALPHA_TEST);
        disableBlend();
    }

    public static void drawLoadingRect(String name, int x, int y, int max) {
        int width = 200; // 长方形框的宽度
        int height = 20; // 长方形框的高度
        int padding = 5; // 内边距

        x = x - width / 2;

        // 绘制长方形框
        RenderUtil.drawRect(x, y, x + width, y + height, new Color(50, 50, 50, 150).getRGB()); // 灰色背景

        // 绘制进度条底座
        RenderUtil.drawRect(x + padding, y + padding, x + width - padding, y + height - padding, new Color(100, 100, 100, 255).getRGB()); // 灰色底座

        // 计算进度条宽度
        int progressWidth = (int) ((width - 2 * padding) * ((double) max / 100.0)); // 根据 max 计算进度条宽度
        if (progressWidth > width - 2 * padding) {
            progressWidth = width - 2 * padding; // 确保进度条不超过底座宽度
        }

        // 绘制进度条
        RenderUtil.drawRect(x + padding, y + padding, x + padding + progressWidth, y + height - padding, new Color(0, 128, 255, 255).getRGB()); // 蓝色进度条

        // 在长方形框上方绘制字符串
        FontManager.tenacityBoldFont20.drawString(name, x + (width - FontManager.tenacityBoldFont20.getStringWidth(name)) / 2, y - FontManager.tenacityBoldFont20.getHeight() - 2, Color.WHITE.getRGB()); // 白色字体
    }
    public static Color getColor(int color) {
        int f = color >> 24 & 0xFF;
        int f1 = color >> 16 & 0xFF;
        int f2 = color >> 8 & 0xFF;
        int f3 = color & 0xFF;
        return new Color(f1, f2, f3, f);
    }

    private static final Tessellator tessellator = Tessellator.getInstance();
    public static final WorldRenderer worldrenderer = tessellator.getWorldRenderer();

    public static void drawHGradientRect(double x2, double y2, double width, double height, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 0xFF) / 255.0f;
        float f1 = (float)(startColor >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(startColor >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(startColor & 0xFF) / 255.0f;
        float f4 = (float)(endColor >> 24 & 0xFF) / 255.0f;
        float f5 = (float)(endColor >> 16 & 0xFF) / 255.0f;
        float f6 = (float)(endColor >> 8 & 0xFF) / 255.0f;
        float f7 = (float)(endColor & 0xFF) / 255.0f;
        setup2DRendering(() -> {
            GL11.glShadeModel((int)7425);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(x2, y2, 0.0).color(f1, f2, f3, f).endVertex();
            worldrenderer.pos(x2, y2 + height, 0.0).color(f1, f2, f3, f).endVertex();
            worldrenderer.pos(x2 + width, y2 + height, 0.0).color(f5, f6, f7, f4).endVertex();
            worldrenderer.pos(x2 + width, y2, 0.0).color(f5, f6, f7, f4).endVertex();
            tessellator.draw();
            GlStateManager.resetColor();
            GL11.glShadeModel((int)7424);
        });
    }
    public static void drawVGradientRect(double x2, double y2, double width, double height, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 0xFF) / 255.0f;
        float f1 = (float)(startColor >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(startColor >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(startColor & 0xFF) / 255.0f;
        float f4 = (float)(endColor >> 24 & 0xFF) / 255.0f;
        float f5 = (float)(endColor >> 16 & 0xFF) / 255.0f;
        float f6 = (float)(endColor >> 8 & 0xFF) / 255.0f;
        float f7 = (float)(endColor & 0xFF) / 255.0f;
        setup2DRendering(() -> {
            GL11.glShadeModel((int)7425);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldrenderer.pos(x2 + width, y2, 0.0).color(f1, f2, f3, f).endVertex();
            worldrenderer.pos(x2, y2, 0.0).color(f1, f2, f3, f).endVertex();
            worldrenderer.pos(x2, y2 + height, 0.0).color(f5, f6, f7, f4).endVertex();
            worldrenderer.pos(x2 + width, y2 + height, 0.0).color(f5, f6, f7, f4).endVertex();
            tessellator.draw();
            GlStateManager.resetColor();
            GL11.glShadeModel((int)7424);
        });
    }
    public static float[] getNextWheelPosition(int wheel, float[] memorize, float topY, float bottomY, float objectY, float offset, boolean updateAction) {
        float reversingOffset;
        float target = memorize[0];
        float current = memorize[1];
        if (updateAction) {
            int i;
            if (wheel > 0) {
                for (i = 0; i < 15; ++i) {
                    float f = target;
                    target = f + 1.0f;
                    if (f > 0.0f) break;
                }
                if (target > 0.0f) {
                    target = 0.0f;
                }
            } else if (wheel < 0) {
                for (i = 0; i < 15 && !(topY + (objectY - current + (target - 1.0f)) + offset < bottomY); ++i) {
                    target -= 1.0f;
                }
            }
        }
        if (objectY - current + target < (reversingOffset = bottomY - topY - offset)) {
            float diff = reversingOffset - (objectY - current + target);
            target = target + diff <= 0.0f ? (target += diff) : (target += diff - (target + diff));
        }
        current = AnimationUtil.animateSmooth(current, target, 0.2f);
        return new float[]{target, current};
    }
    public static Color reAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static void drawCircle2(double x2, double y2, float radius, int color) {
        if (radius == 0.0f) {
            return;
        }
        float correctRadius = radius * 2.0f;
        setup2DRendering(() -> {
            RenderUtil.glColor(color);
            glEnable((int)2832);
            glHint((int)3153, (int)4354);
            glPointSize((float)correctRadius);
            GLUtil.setupRendering(0, () -> glVertex2d((double)x2, (double)y2));
            glDisable((int)2832);
            GlStateManager.resetColor();
        });
    }
    public static boolean isHovering(float x2, float y2, float width, float height, int mouseX, int mouseY) {
        return (float)mouseX >= x2 && (float)mouseY >= y2 && (float)mouseX < x2 + width && (float)mouseY < y2 + height;
    }
    public static void resetColor() {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }
    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, (float)((double)limit * 0.01));
    }

    public static void drawRect(double left, double top, double right, double bottom, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        double minX = Math.min(left, right);
        double maxX = Math.max(left, right);
        double minY = Math.min(top, bottom);
        double maxY = Math.max(top, bottom);
        float alpha = (float)(color >> 24 & 0xFF) / 255.0f;
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, green, blue, alpha);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(minX, maxY, 0.0).endVertex();
        worldrenderer.pos(maxX, maxY, 0.0).endVertex();
        worldrenderer.pos(maxX, minY, 0.0).endVertex();
        worldrenderer.pos(minX, minY, 0.0).endVertex();
        tessellator.draw();
        enableTexture2D();
        disableBlend();
    }
    public static void drawLoadingCircle(float x2, float y2) {
        for (int i = 0; i < 2; ++i) {
            int rot = (int)(System.nanoTime() / 5000000L * (long)i % 360L);
            RenderUtil.drawCircle(x2, y2, i * 8, rot - 180, rot);
        }
    }
    public static void drawCircle(float x2, float y2, float radius, int start, int end) {
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderUtil.glColor(Color.WHITE.getRGB());
        glEnable((int)2848);
        glLineWidth((float)3.0f);
        glBegin((int)3);
        for (float i = (float)end; i >= (float)start; i -= 4.0f) {
            glVertex2f((float)((float)((double)x2 + Math.cos((double)i * Math.PI / 180.0) * (double)(radius * 1.001f))), (float)((float)((double)y2 + Math.sin((double)i * Math.PI / 180.0) * (double)(radius * 1.001f))));
        }
        glEnd();
        glDisable((int)2848);
        enableTexture2D();
        disableBlend();
    }
    public static void drawImage(final ResourceLocation image, final float x, final float y, final int width, final int height) {
        glDisable(2929);
        glEnable(3042);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        mc.getTextureManager().bindTexture(image);
        final float f = 1.0f / width;
        final float f2 = 1.0f / height;
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0).tex(0.0f * f, height * f2).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0).tex(width * f, height * f2).endVertex();
        worldrenderer.pos(x + width, y, 0.0).tex(width * f, 0.0f * f2).endVertex();
        worldrenderer.pos(x, y, 0.0).tex(0.0f * f, 0.0f * f2).endVertex();
        tessellator.draw();
        glDepthMask(true);
        glDisable(3042);
        glEnable(2929);
    }
    public static void drawImage(ResourceLocation image, int x2, int y2, int width, int height) {
        RenderUtil.drawImage(image, x2, y2, width, height, Color.white.getRGB());
    }

    public static void drawImage(ResourceLocation image, float x2, float y2, double width, double height, int color) {
        RenderUtil.drawTexturedRect(x2, y2, width, height, image, color);
    }
    public static void drawTexturedRect(float x2, float y2, double width, double height, ResourceLocation image, int color) {
        boolean disableAlpha;
        glPushMatrix();
        boolean enableBlend = glIsEnabled((int)3042);
        boolean bl = disableAlpha = !glIsEnabled((int)3008);
        if (!enableBlend) {
            glEnable((int)3042);
        }
        if (!disableAlpha) {
            glDisable((int)3008);
        }
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        RenderUtil.glColor(color);
        RenderUtil.drawModalRectWithCustomSizedTexture(x2, y2, 0.0f, 0.0f, width, height, width, height);
        if (!enableBlend) {
            glDisable((int)3042);
        }
        if (!disableAlpha) {
            glEnable((int)3008);
        }
        glPopMatrix();
    }

    public static void color(int color) {
        float f = (float) (color >> 24 & 255) / 255.0f;
        float f1 = (float) (color >> 16 & 255) / 255.0f;
        float f2 = (float) (color >> 8 & 255) / 255.0f;
        float f3 = (float) (color & 255) / 255.0f;
        glColor4f((float) f1, (float) f2, (float) f3, (float) f);
    }

    public static void color(int color, double alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color(r, g, b, (float) alpha);
    }

    public static void color2(int color, float alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color(r, g, b, alpha);
    }

    public static void drawModalRectWithCustomSizedTexture(float x2, float y2, float u2, float v, double width, double height, double textureWidth, double textureHeight) {
        double f = 1.0f / textureWidth;
        double f1 = 1.0f / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x2, y2 + height, 0.0).tex(u2 * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x2 + width, y2 + height, 0.0).tex((u2 + width) * f, (v + height) * f1).endVertex();
        worldrenderer.pos(x2 + width, y2, 0.0).tex((u2 + width) * f, v * f1).endVertex();
        worldrenderer.pos(x2, y2, 0.0).tex(u2 * f, v * f1).endVertex();
        tessellator.draw();
    }

    public static void glColor(int color) {
        float f = (float)(color >> 24 & 0xFF) / 255.0f;
        float f1 = (float)(color >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(color >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(color & 0xFF) / 255.0f;
        glColor4f((float)f1, (float)f2, (float)f3, (float)f);
    }

    public static void renderClientLabel(Entity entityIn, String str, float partialTicks) {
        double x = (entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * partialTicks)
                - Minecraft.getMinecraft().getRenderManager().renderPosX;
        double y = (entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * partialTicks)
                - Minecraft.getMinecraft().getRenderManager().renderPosY;
        double z = (entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * partialTicks)
                - Minecraft.getMinecraft().getRenderManager().renderPosZ;

        BlockPos entityPos = new BlockPos(entityIn.posX, entityIn.posY, entityIn.posZ);
        BlockPos playerPos = new BlockPos(Minecraft.getMinecraft().thePlayer.posX,
                Minecraft.getMinecraft().thePlayer.posY,
                Minecraft.getMinecraft().thePlayer.posZ);

        if (isInRenderChunkRange(entityPos, playerPos)) {
            FontRenderer fontrenderer = Minecraft.getMinecraft().fontRendererObj;
            float scale = 1.6F;
            float scaleFactor = 0.016666668F * scale;
            float fontHeight = fontrenderer.FONT_HEIGHT;
            float iconSize = fontHeight + 2;
            int textWidth = fontrenderer.getStringWidth(str);
            float totalWidth = iconSize + 4 + textWidth;

            GlStateManager.pushMatrix();
            GlStateManager.translate((float)x, (float)y + entityIn.height + 0.5F, (float)z);
            glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-scaleFactor, -scaleFactor, scaleFactor);

            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            enableTexture2D();

            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("client/icon.png"));
            Tessellator tess = Tessellator.getInstance();
            WorldRenderer wr = tess.getWorldRenderer();
            float iconOffsetX = -(totalWidth / 2F);
            wr.begin(7, DefaultVertexFormats.POSITION_TEX);
            wr.pos(iconOffsetX, 0, 0).tex(0, 0).endVertex();
            wr.pos(iconOffsetX, iconSize, 0).tex(0, 1).endVertex();
            wr.pos(iconOffsetX + iconSize, iconSize, 0).tex(1, 1).endVertex();
            wr.pos(iconOffsetX + iconSize, 0, 0).tex(1, 0).endVertex();
            tess.draw();

            int textStartX = (int) (iconOffsetX + iconSize + 4);
            fontrenderer.drawString(str, textStartX, 0, 0x80000000);

            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            fontrenderer.drawString(str, textStartX, 0, -1);

            disableBlend();
            GlStateManager.popMatrix();
        }
    }
    public static void renderLabel(Entity entityIn, String str, float partialTicks) {
        // 获取实体渲染位置
        double x = (entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * partialTicks)
                - Minecraft.getMinecraft().getRenderManager().renderPosX;
        double y = (entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * partialTicks)
                - Minecraft.getMinecraft().getRenderManager().renderPosY;
        double z = (entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * partialTicks)
                - Minecraft.getMinecraft().getRenderManager().renderPosZ;

        // 检查是否在渲染区块内（16x16区块范围）
        BlockPos entityPos = new BlockPos(entityIn.posX, entityIn.posY, entityIn.posZ);
        BlockPos playerPos = new BlockPos(Minecraft.getMinecraft().thePlayer.posX,
                Minecraft.getMinecraft().thePlayer.posY,
                Minecraft.getMinecraft().thePlayer.posZ);

        if (isInRenderChunkRange(entityPos, playerPos)) {
            FontRenderer fontrenderer = Minecraft.getMinecraft().fontRendererObj;
            float scale = 1.6F;
            float scaleFactor = 0.016666668F * scale;

            GlStateManager.pushMatrix();
            GlStateManager.translate((float)x, (float)y + entityIn.height + 0.5F, (float)z);
            glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(Minecraft.getMinecraft().getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-scaleFactor, -scaleFactor, scaleFactor);

            // 渲染设置
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            // 绘制文本阴影
            enableTexture2D();
            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, 0, 0x80000000);

            // 绘制主文本
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, 0, -1); // 白色

            // 恢复状态
            disableBlend();
            GlStateManager.popMatrix();
        }
    }

    private static boolean isInRenderChunkRange(BlockPos entityPos, BlockPos playerPos) {
        // Minecraft默认渲染距离为16个区块（16*16=256格）
        final int RENDER_DISTANCE = 256;

        return Math.abs(entityPos.getX() - playerPos.getX()) < RENDER_DISTANCE &&
                Math.abs(entityPos.getZ() - playerPos.getZ()) < RENDER_DISTANCE;
    }


    public static void drawCube(final AxisAlignedBB axisAlignedBB) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();

        worldRenderer.begin(7, DefaultVertexFormats.POSITION);

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();

        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();

        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawVerticalGradientSideways(double x, double y, double width, double height, int topColor, int bottomColor) {
        setup2DRendering(() -> {
            glShadeModel(GL_SMOOTH);
            GLUtil.setupRendering(GL_QUADS, () -> {
                color(topColor);
                glVertex2d(x + width, y);
                glVertex2d(x, y);
                color(bottomColor);
                glVertex2d(x, y + height);
                glVertex2d(x + width, y + height);
                GlStateManager.resetColor();
            });
            glShadeModel(GL_FLAT);
        });
    }

    public static void drawHorizontalGradientSideways(double x, double y, double width, double height, int leftColor, int rightColor) {
        setup2DRendering(() -> {
            glShadeModel(GL_SMOOTH);
            GLUtil.setupRendering(GL_QUADS, () -> {
                color(leftColor);
                glVertex2d(x, y);
                glVertex2d(x, y + height);
                color(rightColor);
                glVertex2d(x + width, y + height);
                glVertex2d(x + width, y);
                GlStateManager.resetColor();
            });
            glShadeModel(GL_FLAT);
        });
    }

    public static void enableRender3D(boolean disableDepth) {
        if (disableDepth) {
            glDepthMask(false);
            glDisable(2929);
        }

        glDisable(3008);
        glEnable(3042);
        glDisable(3553);
        glBlendFunc(770, 771);
        glEnable(2848);
        glHint(3154, 4354);
        glLineWidth(1.0F);
    }

    public static void disableRender3D(boolean enableDepth) {
        if (enableDepth) {
            glDepthMask(true);
            glEnable(2929);
        }

        glEnable(3553);
        glDisable(3042);
        glEnable(3008);
        glDisable(2848);
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void scissorEnd() {
        glDisable(GL_SCISSOR_TEST);
    }

    public static void drawBlockBox(final BlockPos blockPos, final Color color, final boolean outline) {
        final RenderManager renderManager = mc.getRenderManager();
        final Timer timer = mc.timer;

        final double x = blockPos.getX() - renderManager.renderPosX;
        final double y = blockPos.getY() - renderManager.renderPosY;
        final double z = blockPos.getZ() - renderManager.renderPosZ;

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        final Block block = mc.theWorld.getBlockState(blockPos).getBlock();


        if (block != null) {
            final EntityPlayer player = mc.thePlayer;

            final double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) timer.renderPartialTicks;
            final double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) timer.renderPartialTicks;
            final double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) timer.renderPartialTicks;
            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                    .expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D)
                    .offset(-posX, -posY, -posZ);
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() != 255 ? color.getAlpha() : outline ? 26 : 35);
        drawFilledBox(axisAlignedBB);

        if (outline) {
            glLineWidth(1F);
            enableGlCap(GL_LINE_SMOOTH);
            glColor(color.getRGB());

            drawSelectionBoundingBox(axisAlignedBB);
        }

        GlStateManager.resetColor();
        glDepthMask(true);
        resetCaps();

    }

    public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION);
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
        worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
        tessellator.draw();
    }

    public static void enableGlCap(final int cap) {
        setGlCap(cap, true);
    }

    public static void disableGlCap(final int... caps) {
        for (final int cap : caps)
            setGlCap(cap, false);
    }

    public static void setGlCap(final int cap, final boolean state) {
        glCapMap.put(cap, glGetBoolean(cap));
        setGlState(cap, state);
    }

    public static void setGlState(final int cap, final boolean state) {
        if (state)
            glEnable(cap);
        else
            glDisable(cap);
    }

    public static void resetCaps() {
        glCapMap.forEach(RenderUtil::setGlState);
    }

    public static void glColor(final int red, final int green, final int blue, final int alpha) {
        GlStateManager.color(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }

    public static void drawFilledBox(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        // 底部
        worldRenderer.pos(x1, y1, z1).endVertex();
        worldRenderer.pos(x2, y1, z1).endVertex();
        worldRenderer.pos(x2, y1, z2).endVertex();
        worldRenderer.pos(x1, y1, z2).endVertex();

        // 顶部
        worldRenderer.pos(x1, y2, z1).endVertex();
        worldRenderer.pos(x1, y2, z2).endVertex();
        worldRenderer.pos(x2, y2, z2).endVertex();
        worldRenderer.pos(x2, y2, z1).endVertex();

        // 北
        worldRenderer.pos(x1, y1, z1).endVertex();
        worldRenderer.pos(x1, y2, z1).endVertex();
        worldRenderer.pos(x2, y2, z1).endVertex();
        worldRenderer.pos(x2, y1, z1).endVertex();

        // 南
        worldRenderer.pos(x1, y1, z2).endVertex();
        worldRenderer.pos(x2, y1, z2).endVertex();
        worldRenderer.pos(x2, y2, z2).endVertex();
        worldRenderer.pos(x1, y2, z2).endVertex();

        // 西
        worldRenderer.pos(x1, y1, z1).endVertex();
        worldRenderer.pos(x1, y1, z2).endVertex();
        worldRenderer.pos(x1, y2, z2).endVertex();
        worldRenderer.pos(x1, y2, z1).endVertex();

        // 东
        worldRenderer.pos(x2, y1, z1).endVertex();
        worldRenderer.pos(x2, y2, z1).endVertex();
        worldRenderer.pos(x2, y2, z2).endVertex();
        worldRenderer.pos(x2, y1, z2).endVertex();

        tessellator.draw();

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void drawBoundingBox(double x1, double y1, double z1, double x2, double y2, double z2, float lineWidth, Color color) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glLineWidth(lineWidth);
        GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        // 底部
        worldRenderer.pos(x1, y1, z1).endVertex();
        worldRenderer.pos(x2, y1, z1).endVertex();
        worldRenderer.pos(x2, y1, z2).endVertex();
        worldRenderer.pos(x1, y1, z2).endVertex();
        worldRenderer.pos(x1, y1, z1).endVertex();

        // 顶部
        worldRenderer.pos(x1, y2, z1).endVertex();
        worldRenderer.pos(x2, y2, z1).endVertex();
        worldRenderer.pos(x2, y2, z2).endVertex();
        worldRenderer.pos(x1, y2, z2).endVertex();
        worldRenderer.pos(x1, y2, z1).endVertex();

        // 连接线
        worldRenderer.pos(x1, y1, z1).endVertex();
        worldRenderer.pos(x1, y2, z1).endVertex();

        worldRenderer.pos(x2, y1, z1).endVertex();
        worldRenderer.pos(x2, y2, z1).endVertex();

        worldRenderer.pos(x2, y1, z2).endVertex();
        worldRenderer.pos(x2, y2, z2).endVertex();

        worldRenderer.pos(x1, y1, z2).endVertex();
        worldRenderer.pos(x1, y2, z2).endVertex();

        tessellator.draw();

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public static void drawFilledBox(double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        drawFilledBox(x1, y1, z1, x2, y2, z2, new Color(color, true));
    }

    public static void drawBoundingBox(double x1, double y1, double z1, double x2, double y2, double z2, float lineWidth, int color) {
        drawBoundingBox(x1, y1, z1, x2, y2, z2, lineWidth, new Color(color, true));
    }


    public static void drawFilledBox(final AxisAlignedBB axisAlignedBB) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }

    public static Color tripleColor(int rgbValue, float alpha) {
        alpha = Math.min(1, Math.max(0, alpha));
        return new Color(rgbValue, rgbValue, rgbValue, (int) (255 * alpha));
    }

    public static void drawCircleCGUI(double x, double y, float radius, int color) {
        if (radius == 0)
            return;
        final float correctRadius = radius * 2;
        setup2DRendering(() -> {
            glColor(color);
            glEnable(GL_POINT_SMOOTH);
            glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
            glPointSize(correctRadius);
            GLUtil.setupRendering(GL_POINTS, () -> glVertex2d(x, y));
            glDisable(GL_POINT_SMOOTH);
            GlStateManager.resetColor();
        });
    }

    public static void drawBigHead(float x, float y, float width, float height, AbstractClientPlayer player) {
        drawBigHead(x, y, width, height, 1F, player);
    }

    public static void drawBigHead(float x, float y, float width, float height, float opacity, AbstractClientPlayer player) {
        final double offset = -(player.hurtTime * 23);
        color2(ColorUtil.getColor(255, (int) (255 + offset), (int) (255 + offset)), opacity);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        mc.getTextureManager().bindTexture(player.getLocationSkin());
        Gui.drawScaledCustomSizeModalRect2(x, y, (float) 8.0, (float) 8.0, 8, 8, width, height, 64.0F, 64.0F);
        disableBlend();
        GlStateManager.resetColor();
    }

    public static void scaleStart(float x, float y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1);
        GlStateManager.translate(-x, -y, 0);
    }

    public static void scaleEnd() {
        GlStateManager.popMatrix();
    }

    public static void drawBigHeadRound(float x, float y, float width, float height, AbstractClientPlayer player) {
        drawBigHeadRound(x, y, width, height, 1F, player);
    }

    public static void drawBigHeadRound(float x, float y, float width, float height, float opacity, AbstractClientPlayer player) {
        StencilUtil.write(false);

        renderRoundedRect(x, y, width, height, 0, -1);
        StencilUtil.erase(true);

        color(-1);
        drawBigHead(x, y, width, height, opacity, player);
        StencilUtil.dispose();
        disableBlend();
    }

    public static void renderRoundedRect(float x, float y, float width, float height, float radius, int color) {
        drawGoodCircle(x + radius, y + radius, radius, color);
        drawGoodCircle(x + width - radius, y + radius, radius, color);
        drawGoodCircle(x + radius, y + height - radius, radius, color);
        drawGoodCircle(x + width - radius, y + height - radius, radius, color);

        Gui.drawRect3(x + radius, y, width - radius * 2, height, color);
        Gui.drawRect3(x, y + radius, width, height - radius * 2, color);
    }

    public static void drawGoodCircle(double x, double y, float radius, int color) {

        color(color);
        setup2DRendering(() -> {
            glEnable(GL_POINT_SMOOTH);
            glHint(GL_POINT_SMOOTH_HINT, GL_NICEST);
            glPointSize(radius * (2 * Minecraft.getMinecraft().gameSettings.guiScale));
            GLUtil.render(GL_POINTS, () -> glVertex2d(x, y));
        });
    }

    public static ScaledResolution getScaledResolution() {
        int displayWidth = Display.getWidth();
        int displayHeight = Display.getHeight();
        float guiScale = mc.gameSettings.guiScale;

        if (displayWidth != lastScaledWidth ||
                displayHeight != lastScaledHeight ||
                guiScale != lastGuiScale) {
            lastScaledWidth = displayWidth;
            lastScaledHeight = displayHeight;
            lastGuiScale = guiScale;
            return scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        }

        return scaledResolution;
    }

    public static boolean isInViewFrustrum(final Entity entity) {
        return (isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck);
    }

    private static boolean isInViewFrustrum(final AxisAlignedBB bb) {
        final Entity current = mc.getRenderViewEntity();
        FRUSTUM.setPosition(current.posX, current.posY, current.posZ);
        return FRUSTUM.isBoundingBoxInFrustum(bb);
    }

    public static boolean isInView(Entity ent) {
        FRUSTUM.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
        return FRUSTUM.isBoundingBoxInFrustum(ent.getEntityBoundingBox()) || ent.ignoreFrustumCheck;
    }

    public static void drawBorderedRect(double x2, double y2, double x22, double d2, float l1, int col1, int col2) {
        RenderUtil.drawRect(x2, y2, x22, d2, col2);
        float f = (float)(col1 >> 24 & 0xFF) / 255.0f;
        float f2 = (float)(col1 >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(col1 >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(col1 & 0xFF) / 255.0f;
        glEnable((int)3042);
        glDisable((int)3553);
        glBlendFunc((int)770, (int)771);
        glEnable((int)2848);
        glPushMatrix();
        glColor4f((float)f2, (float)f3, (float)f4, (float)f);
        glLineWidth((float)l1);
        glBegin((int)1);
        glVertex2d((double)x2, (double)y2);
        glVertex2d((double)x2, (double)d2);
        glVertex2d((double)x22, (double)d2);
        glVertex2d((double)x22, (double)y2);
        glVertex2d((double)x2, (double)y2);
        glVertex2d((double)x22, (double)y2);
        glVertex2d((double)x2, (double)d2);
        glVertex2d((double)x22, (double)d2);
        glEnd();
        glPopMatrix();
        glEnable((int)3553);
        glDisable((int)3042);
        glDisable((int)2848);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return RenderUtil.createFrameBuffer(framebuffer, false);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        if (RenderUtil.needsNewFramebuffer(framebuffer)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, depth);
        }
        return framebuffer;
    }

    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight;
    }

    public static void bindTexture(int texture) {
        glBindTexture((int)3553, (int)texture);
    }

    public static void startBlend() {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
    }

    public static void drawEntityESP(double x, double y, double z, double x1, double y1, double z1, float red,
                                     float green, float blue, float alpha) {
        glPushMatrix();
        glEnable(3042);
        glBlendFunc(770, 771);
        glDisable(3553);
        glEnable(2848);
        glDisable(2929);
        glDepthMask(false);
        glLineWidth(1.0F);
        glColor4f(red, green, blue, 1.0f);
        drawOutlinedBoundingBox(new AxisAlignedBB(x, y, z, x1, y1, z1));
        glColor4f(red, green, blue, alpha);
        drawBoundingBox(new AxisAlignedBB(x, y, z, x1, y1, z1));
        glDisable(2848);
        glEnable(3553);
        glEnable(2929);
        glDepthMask(true);
        glDisable(3042);
        glPopMatrix();
    }

    public static void drawBoundingBox(AxisAlignedBB axisAlignedBB, int red, int green, int blue, int alpha, float lineWidth) {
        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        RenderGlobal.drawOutlinedBoundingBox(axisAlignedBB, red, green, blue, alpha);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(2.0f);
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(3, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(1, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static void drawBoundingBox(AxisAlignedBB aa) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
        worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
        tessellator.draw();
    }

    public static void renderOne() {
        StencilUtil.checkSetupFBO(mc.getFramebuffer());
        glPushAttrib(1048575);
        glDisable(3008);
        glDisable(3553);
        glDisable(2896);
        glEnable(3042);
        glBlendFunc(770, 771);
        glLineWidth(4.0F);
        glEnable(2848);
        glEnable(2960);
        glClear(1024);
        glClearStencil(15);
        glStencilFunc(512, 1, 15);
        glStencilOp(7681, 7681, 7681);
        glPolygonMode(1032, 6913);
    }

    public static void renderTwo() {
        glStencilFunc(512, 0, 15);
        glStencilOp(7681, 7681, 7681);
        glPolygonMode(1032, 6914);
    }

    public static void renderThree() {
        glStencilFunc(514, 1, 15);
        glStencilOp(7680, 7680, 7680);
        glPolygonMode(1032, 6913);
    }

    public static void renderFour(int color) {
        setColor(color);
        glDepthMask(false);
        glDisable(2929);
        glEnable(10754);
        glPolygonOffset(1.0F, -2000000.0F);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
    }

    public static void renderFive() {
        glPolygonOffset(1.0F, 2000000.0F);
        glDisable(10754);
        glEnable(2929);
        glDepthMask(true);
        glDisable(2960);
        glDisable(2848);
        glHint(3154, 4352);
        glEnable(3042);
        glEnable(2896);
        glEnable(3553);
        glEnable(3008);
        glPopAttrib();
    }

    public static void setColor(int colorHex) {
        float alpha = (float) (colorHex >> 24 & 255) / 255.0F;
        float red = (float) (colorHex >> 16 & 255) / 255.0F;
        float green = (float) (colorHex >> 8 & 255) / 255.0F;
        float blue = (float) (colorHex & 255) / 255.0F;
        glColor4f(red, green, blue, alpha);
    }


    public static void customRotatedObject2D(float oXpos, float oYpos, float oWidth, float oHeight, double rotate) {
        GlStateManager.translate(oXpos + oWidth / 2, oYpos + oHeight / 2, 0);
        glRotated(rotate, 0.0, 0.0, 1.0);
        GlStateManager.translate(-oXpos - oWidth / 2, -oYpos - oHeight / 2, 0);
    }

    public static void drawAxisAlignedBB(AxisAlignedBB axisAlignedBB, boolean outline, int color) {
        drawAxisAlignedBB(axisAlignedBB,outline,true,color);
    }

    public static void drawAxisAlignedBB(AxisAlignedBB axisAlignedBB,boolean filled, boolean outline, int color) {
        drawSelectionBoundingBox(axisAlignedBB, outline, filled,color);
    }

    public static void drawOutlineBoundingBox(final AxisAlignedBB bb,Color color) {
        RenderGlobal.drawOutlinedBoundingBox(bb,color.getRed(),color.getGreen(),color.getBlue(),color.getAlpha());
    }

    public static void drawSelectionBoundingBox(final AxisAlignedBB bb, final boolean outline, final boolean filled,int color) {
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GL11.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GL11.glDisable(2929);
        GlStateManager.depthMask(false);
        GlStateManager.pushMatrix();

        if (outline) {

            glEnable(GL_LINE_SMOOTH);

            drawOutlineBoundingBox(bb,new Color(color,true));

            glDisable(GL_LINE_SMOOTH);
        }
        if (filled) {
            drawFilledBoundingBox(bb,new Color(color,true));
        }

        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GL11.glEnable(2929);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawFilledBoundingBox(final AxisAlignedBB bb,Color color) {

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        tessellator.draw();
    }


    public static void drawRoundedRect(float x, float y, float x2, float y2, final float round, final int color) {
        x += (float) (round / 2.0f + 0.5);
        y += (float) (round / 2.0f + 0.5);
        x2 -= (float) (round / 2.0f + 0.5);
        y2 -= (float) (round / 2.0f + 0.5);
        drawRect((int) x, (int) y, (int) x2, (int) y2, color);
        circle(x2 - round / 2.0f, y + round / 2.0f, round, color);
        circle(x + round / 2.0f, y2 - round / 2.0f, round, color);
        circle(x + round / 2.0f, y + round / 2.0f, round, color);
        circle(x2 - round / 2.0f, y2 - round / 2.0f, round, color);
        drawRect((int) (x - round / 2.0f - 0.5f), (int) (y + round / 2.0f), (int) x2, (int) (y2 - round / 2.0f), color);
        drawRect((int) x, (int) (y + round / 2.0f), (int) (x2 + round / 2.0f + 0.5f), (int) (y2 - round / 2.0f), color);
        drawRect((int) (x + round / 2.0f), (int) (y - round / 2.0f - 0.5f), (int) (x2 - round / 2.0f), (int) (y2 - round / 2.0f), color);
        drawRect((int) (x + round / 2.0f), (int) y, (int) (x2 - round / 2.0f), (int) (y2 + round / 2.0f + 0.5f), color);
    }

    public static void fixBlendIssues() {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.0f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static double lerpDouble(double current, double previous, double t) {
        return previous + (current - previous) * t;
    }

    public void image(final ResourceLocation imageLocation, final double x, final double y, final double width, final double height) {
        image(imageLocation, (float) x, (float) y, (float) width, (float) height);
    }

    public static void drawRectWH(double x, double y, double width, double height, int color) {
        RenderUtil.resetColor();
        RenderUtil.setAlphaLimit(0);
        setup2DRendering(true);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(x, y, 0.0D).color(color).endVertex();
        worldrenderer.pos(x, y + height, 0.0D).color(color).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).color(color).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).color(color).endVertex();
        tessellator.draw();

        GLUtil.end2DRendering();
    }
    public static void scissorStart(double x, double y, double width, double height) {
        glEnable(GL_SCISSOR_TEST);
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        final double scale = sr.getScaleFactor();
        double finalHeight = height * scale;
        double finalY = (sr.getScaledHeight() - y) * scale;
        double finalX = x * scale;
        double finalWidth = width * scale;
        glScissor((int) finalX, (int) (finalY - finalHeight), (int) finalWidth, (int) finalHeight);
    }

    public static void drawRectBordered(final double x, final double y, final double x1, final double y1, final double width, final int internalColor, final int borderColor) {
        rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
        rectangle(x + width, y, x1 - width, y + width, borderColor);
        rectangle(x, y, x + width, y1, borderColor);
        rectangle(x1 - width, y, x1, y1, borderColor);
        rectangle(x + width, y1 - width, x1 - width, y1, borderColor);
    }

    public static void rectangle(double left, double top, double right, double bottom, final int color) {
        if (left < right) {
            final double var5 = left;
            left = right;
            right = var5;
        }
        if (top < bottom) {
            final double var5 = top;
            top = bottom;
            bottom = var5;
        }
        final float var6 = (color >> 24 & 0xFF) / 255.0f;
        final float var7 = (color >> 16 & 0xFF) / 255.0f;
        final float var8 = (color >> 8 & 0xFF) / 255.0f;
        final float var9 = (color & 0xFF) / 255.0f;
        final WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var7, var8, var9, var6);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(left, bottom, 0.0).endVertex();
        worldRenderer.pos(right, bottom, 0.0).endVertex();
        worldRenderer.pos(right, top, 0.0).endVertex();
        worldRenderer.pos(left, top, 0.0).endVertex();
        Tessellator.getInstance().draw();
        enableTexture2D();
        disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void drawESPRect(float left, float top, float right, float bottom, int color) {
        if (left < right) {
            float i = left;
            left = right;
            right = i;
        }
        if (top < bottom) {
            float j = top;
            top = bottom;
            bottom = j;
        }
        float f3 = (color >> 24 & 0xFF) / 255.0f;
        float f = (color >> 16 & 0xFF) / 255.0f;
        float f1 = (color >> 8 & 0xFF) / 255.0f;
        float f2 = (color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0).endVertex();
        worldrenderer.pos(right, bottom, 0.0).endVertex();
        worldrenderer.pos(right, top, 0.0).endVertex();
        worldrenderer.pos(left, top, 0.0).endVertex();
        tessellator.draw();
        enableTexture2D();
        disableBlend();
    }

    public static void drawBloom(int shadowRadius, int shadowOffset, float shadowAlpha, Runnable content) {
        stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
        stencilFramebuffer.framebufferClear();
        stencilFramebuffer.bindFramebuffer(false);
        content.run();
        stencilFramebuffer.unbindFramebuffer();
        glColor4f(1F, 1F, 1F, shadowAlpha);
        KawaseBloom.renderBlur(stencilFramebuffer.framebufferTexture, shadowRadius, shadowOffset);
        glColor4f(1F, 1F, 1F, 1F);
    }
    public static void drawBlur(int iterations, int offset, Runnable content) {
        stencilFramebuffer = RenderUtil.createFrameBuffer(stencilFramebuffer);
        stencilFramebuffer.framebufferClear();
        stencilFramebuffer.bindFramebuffer(false);
        content.run();
        stencilFramebuffer.unbindFramebuffer();
        KawaseBlur.renderBlur(stencilFramebuffer.framebufferTexture, iterations, offset);
    }

    public static void drawCircle(final double xPos, final double yPos, final double radius, final Color color) {
        NLRenderUtil.startRender();
        NLRenderUtil.color(color);
        glBegin(GL_POLYGON);
        {
            drawCircle3(xPos, yPos, radius);
        }
        glEnd();

        glEnable(GL_LINE_SMOOTH);
        glLineWidth(2);
        glBegin(GL_LINE_LOOP);
        {
            drawCircle3(xPos, yPos, radius);
        }
        glEnd();
        NLRenderUtil.stopRender();
    }

    private static void drawCircle3(final double xPos, final double yPos, final double radius) {
        final double theta = (2 * Math.PI / 360.0);
        final double tangetial_factor = Math.tan(theta);//calculate the tangential factor
        final double radial_factor = Math.cos(theta);//calculate the radial factor
        double x = radius;//we start at angle = 0
        double y = 0;
        for (int i = 0; i < 360; i++) {
            glVertex2d(x + xPos, y + yPos);

            //calculate the tangential vector
            //remember, the radial vector is (x, y)
            //to get the tangential vector we flip those coordinates and negate one of them
            final double tx = -y;
            final double ty = x;

            //add the tangential vector
            x += tx * tangetial_factor;
            y += ty * tangetial_factor;

            //correct using the radial factor
            x *= radial_factor;
            y *= radial_factor;
        }
    }

    public static void circle(final float x, final float y, final float radius, final int fill) {
        arc(x, y, 0.0f, 360.0f, radius, fill);
    }
    public static void arc(final float x, final float y, final float start, final float end, final float radius, final int color) {
        arcEllipse(x, y, start, end, radius, radius, color);
    }

    public static void arcEllipse(final float x, final float y, float start, float end, final float w, final float h, final int color) {
        GlStateManager.color(0.0F, 0.0F, 0.0F);
        glColor4f(0.0F, 0.0F, 0.0F, 0.0F);
        final float temp;
        if (start > end) {
            temp = end;
            end = start;
            start = temp;
        }

        final float var11 = (color >> 24 & 255) / 255.0F;
        final float var12 = (color >> 16 & 255) / 255.0F;
        final float var13 = (color >> 8 & 255) / 255.0F;
        final float var14 = (color & 255) / 255.0F;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var12, var13, var14, var11);
        float i;
        float ldx;
        float ldy;
        if (var11 > 0.5F) {
            glEnable(2848);
            glLineWidth(2.0F);
            glBegin(3);

            for (i = end; i >= start; i -= 4.0F) {
                ldx = (float) Math.cos(i * 3.141592653589793D / 180.0D) * w * 1.001F;
                ldy = (float) Math.sin(i * 3.141592653589793D / 180.0D) * h * 1.001F;
                glVertex2f(x + ldx, y + ldy);
            }

            glEnd();
            glDisable(2848);
        }

        glBegin(6);

        for (i = end; i >= start; i -= 4.0F) {
            ldx = (float) Math.cos(i * 3.141592653589793D / 180.0D) * w;
            ldy = (float) Math.sin(i * 3.141592653589793D / 180.0D) * h;
            glVertex2f(x + ldx, y + ldy);
        }

        glEnd();
        enableTexture2D();
        disableBlend();
    }

    public static void startGlScissor(final int x, final int y, final int width, final int height) {
        final int scaleFactor = new ScaledResolution(mc).getScaleFactor();
        glPushMatrix();
        glEnable(3089);
        glScissor(x * scaleFactor, mc.displayHeight - (y + height) * scaleFactor, width * scaleFactor, (height + 14) * scaleFactor);
    }

    public static void stopGlScissor() {
        glDisable(3089);
        glPopMatrix();
    }

    private static boolean isInViewFrustum(AxisAlignedBB bb) {
        Entity current = mc.getRenderViewEntity();
        FRUSTUM.setPosition(current.posX, current.posY, current.posZ);
        return FRUSTUM.isBoundingBoxInFrustum(bb);
    }

    public static boolean isInViewFrustum(Entity entity) {
        return isInViewFrustum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    public static void drawCustomImage(final float x, final float y, final float width, final float height, final float texWidth, final float texHeight, final ResourceLocation image) {
        glPushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
//        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        mc.getTextureManager().bindTexture(image);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, texWidth, texHeight);
        disableBlend();
        GlStateManager.enableAlpha();
        glPopMatrix();
    }

    public static void renderBlock(BlockPos blockPos, int color, boolean outline, boolean shade) {
        renderBox(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1, 1, 1, color, outline, shade);
    }

    public static void renderBox(int x, int y, int z, double x2, double y2, double z2, int color, boolean outline, boolean shade) {
        double xPos = x - mc.getRenderManager().viewerPosX;
        double yPos = y - mc.getRenderManager().viewerPosY;
        double zPos = z - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(xPos, yPos, zPos, xPos + x2, yPos + y2, zPos + z2);
        drawAxisAlignedBB(axisAlignedBB, shade, outline,color);
    }

    public static void renderBoundingBox(EntityLivingBase entityLivingBase, Color color, float alpha) {
        AxisAlignedBB bb = RenderUtil.getInterpolatedBoundingBox(entityLivingBase);
        GlStateManager.pushMatrix();
        GLUtil.setup2DRendering();
        GLUtil.enableCaps(3042, 2832, 2881, 2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(3.0f);
        GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        RenderUtil.color(RenderUtil.reAlpha(color, (int)alpha).getRGB());
        RenderGlobal.drawSelectionBoundingBox(bb, false, true);
        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GLUtil.disableCaps();
        GLUtil.end2DRendering();
        GlStateManager.popMatrix();
    }
    public static void renderBoundingBox(AxisAlignedBB entityLivingBase, Color color, float alpha) {
        GlStateManager.pushMatrix();
        GLUtil.setup2DRendering();
        GLUtil.enableCaps(3042, 2832, 2881, 2848);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(3.0f);
        GL11.glColor4f(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        RenderUtil.color(RenderUtil.reAlpha(color, (int)alpha).getRGB());
        RenderGlobal.drawSelectionBoundingBox(entityLivingBase, false, true);
        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GLUtil.disableCaps();
        GLUtil.end2DRendering();
        GlStateManager.popMatrix();
    }
    public static AxisAlignedBB getInterpolatedBoundingBox(Entity entity) {
        double[] renderingEntityPos = RenderUtil.getInterpolatedPos(entity);
        double entityRenderWidth = (double)entity.width / 1.5;
        return new AxisAlignedBB(renderingEntityPos[0] - entityRenderWidth, renderingEntityPos[1], renderingEntityPos[2] - entityRenderWidth, renderingEntityPos[0] + entityRenderWidth, renderingEntityPos[1] + (double)entity.height + (entity.isSneaking() ? -0.3 : 0.18), renderingEntityPos[2] + entityRenderWidth).expand(0.15, 0.15, 0.15);
    }
    public static double[] getInterpolatedPos(Entity entity) {
        float ticks = mc.timer.renderPartialTicks;
        return new double[]{MathUtil.interpolate(entity.lastTickPosX, entity.posX, ticks) - mc.getRenderManager().viewerPosX, MathUtil.interpolate(entity.lastTickPosY, entity.posY, ticks) - mc.getRenderManager().viewerPosY, MathUtil.interpolate(entity.lastTickPosZ, entity.posZ, ticks) - mc.getRenderManager().viewerPosZ};
    }

    public static void drawScaledCustomSizeModalRect(final double d, final double e, final float u, final float v, final int uWidth, final int vHeight, final float size, final float size2, final float tileWidth, final float tileHeight) {
        final float f = 1.0f / tileWidth;
        final float f1 = 1.0f / tileHeight;
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(d, e + size2, 0.0).tex(u * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(d + size, e + size2, 0.0).tex((u + uWidth) * f, (v + vHeight) * f1).endVertex();
        worldrenderer.pos(d + size, e, 0.0).tex((u + uWidth) * f, v * f1).endVertex();
        worldrenderer.pos(d, e, 0.0).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }
}
