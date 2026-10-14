package cn.tianyu.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClientSplashScreen {

    private static final ResourceLocation LOGO_LOCATION = new ResourceLocation("client/emoji.png");

    private static int targetProgress = 0;
    private static double currentProgress = 0;

    private static final int MAX_STEPS = 7;
    private static final List<SplashParticle> particles = new ArrayList<>();
    private static final Random random = new Random();

    public static void updateProgress(int stepNumber, String stepName) {
        targetProgress = (int) (((double) stepNumber / MAX_STEPS) * 100);

        // 【核心修复1：平滑渐进循环】
        // 当收到新的进度目标时，不让它直接蹦过去，而是在主线程间隙强制渲染过渡帧
        // 限制最大内循环次数，防止彻底卡死启动
        int safetyTicks = 0;
        while (currentProgress < targetProgress && safetyTicks < 100) {
            // 让 currentProgress 丝滑地慢慢蹭过去
            currentProgress += (targetProgress - currentProgress) * 0.15;
            if (targetProgress - currentProgress < 0.5) {
                currentProgress = targetProgress;
            }

            // 每次递增都高频触发一次完整的渲染流程（更新粒子位置+重绘）
            renderSplash();

            // 极其短暂的平滑延迟，给显卡喘息和同步的时间
            try {
                Thread.sleep(5);
            } catch (InterruptedException ignored) {}

            safetyTicks++;
        }
    }

    public static void renderSplash() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int i = sr.getScaleFactor();
        Framebuffer framebuffer = mc.getFramebuffer();

        if (framebuffer == null) return;
        framebuffer.bindFramebuffer(true);

        // 备份 OpenGL 状态，防污染
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GlStateManager.pushMatrix();

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, sr.getScaledWidth_double(), sr.getScaledHeight_double(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        int width = sr.getScaledWidth();
        int height = sr.getScaledHeight();

        // 1. 暗色极简底色
        drawRect(0, 0, width, height, new Color(10, 10, 12).getRGB());

        // 2. 动态粒子背景（采用固定步长更新逻辑，告别跳跃僵硬）
        handleParticles(width, height);

        // 3. 绘制中央横向 Logo
        drawCenteredLogo(mc, width, height);

        // 4. 进度条背景
        float barWidth = 240;
        float barHeight = 4;
        float barX = width / 2f - barWidth / 2f;
        float barY = height / 2f + 55;
        drawRect(barX, barY, barX + barWidth, barY + barHeight, new Color(25, 25, 28).getRGB());

        // 5. 极度丝滑的霓虹渐变进度条
        float progressWidth = (float) ((currentProgress / 100f) * barWidth);
        if (progressWidth > 0) {
            int color1 = new Color(54, 137, 255).getRGB();
            int color2 = new Color(181, 91, 255).getRGB();
            drawGradientRect(barX, barY, barX + progressWidth, barY + barHeight, color1, color2);
        }

        framebuffer.unbindFramebuffer();
        framebuffer.framebufferRender(sr.getScaledWidth() * i, sr.getScaledHeight() * i);

        // 状态完美重置，解除毒性
        GlStateManager.popMatrix();
        GL11.glPopAttrib();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GlStateManager.shadeModel(7424);

        mc.updateDisplay();
    }

    private static void drawCenteredLogo(Minecraft mc, int screenWidth, int screenHeight) {
        double logoWidth = 180;
        double logoHeight = 60;
        double x = (screenWidth / 2.0) - (logoWidth / 2.0);
        double y = (screenHeight / 2.0) - (logoHeight / 2.0) - 10;

        GlStateManager.enableTexture2D();
        mc.getTextureManager().bindTexture(LOGO_LOCATION);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + logoHeight, 0.0D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(x + logoWidth, y + logoHeight, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(x + logoWidth, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
    }

    private static void handleParticles(int width, int height) {
        // 【核心修复2：全屏自然初始化】
        // 首次运行时，直接铺满全屏，防止死板地从最顶部一行一行往下掉
        if (particles.isEmpty()) {
            for (int k = 0; k < 60; k++) {
                SplashParticle p = new SplashParticle(width, height);
                p.y = random.nextFloat() * height; // 随机分布在屏幕任何高度
                particles.add(p);
            }
        }

        // 持续补充新粒子
        if (particles.size() < 100 && random.nextInt(2) == 0) {
            particles.add(new SplashParticle(width, height));
        }

        for (int i = 0; i < particles.size(); i++) {
            SplashParticle p = particles.get(i);
            p.update(); // 每次调用固定前移一小步

            if (p.isDead(height)) {
                particles.remove(i);
                i--;
            } else {
                // 增加一层极其微弱的形变（长条状细线落体感），看起来更像星空流动而非死板的像素方块
                drawRect(p.x, p.y, p.x + p.size, p.y + p.size + (p.motionY * 0.05f), p.getColor());
            }
        }
    }

    private static void drawRect(float left, float top, float right, float bottom, int color) {
        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.disableTexture2D();
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    private static void drawGradientRect(float left, float top, float right, float bottom, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, bottom, 0.0D).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(right, top, 0.0D).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(left, top, 0.0D).color(f1, f2, f3, f).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.enableTexture2D();
    }

    private static class SplashParticle {
        float x, y;
        float motionX, motionY;
        float size;
        int alpha;
        int baseColor;

        public SplashParticle(int screenWidth, int screenHeight) {
            this.x = random.nextFloat() * screenWidth;
            this.y = -10f;
            // 【核心修复3：摒弃系统时间Delta，改用固定像素步长】
            this.motionX = (random.nextFloat() - 0.5f) * 0.3f; // 每帧平移像素
            this.motionY = random.nextFloat() * 1.2f + 0.6f;   // 每帧下落像素
            this.size = random.nextFloat() * 1.5f + 1.0f;
            this.alpha = random.nextInt(100) + 40;

            if (random.nextBoolean()) {
                this.baseColor = new Color(54, 137, 255).getRGB();
            } else {
                this.baseColor = new Color(181, 91, 255).getRGB();
            }
        }

        public void update() {
            this.x += this.motionX;
            this.y += this.motionY;
        }

        public boolean isDead(int screenHeight) {
            return this.y > screenHeight + 10;
        }

        public int getColor() {
            int r = (baseColor >> 16) & 255;
            int g = (baseColor >> 8) & 255;
            int b = baseColor & 255;
            return new Color(r, g, b, alpha).getRGB();
        }
    }
}