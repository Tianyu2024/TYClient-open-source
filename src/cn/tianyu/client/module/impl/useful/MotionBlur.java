package cn.tianyu.client.module.impl.useful;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjglx.opengl.Display;

public class MotionBlur extends Module {
    public static final NumberValue blurAmount = new NumberValue("Amount", 7, 0.0, 10.0, 0.1);
    public final ModeValue mode = new ModeValue("Mode", "Shader", "Shader", "FrameBuffer");

    private boolean shaderLoaded;
    private Framebuffer framebuffer;
    private int width;
    private int height;

    public MotionBlur() {
        super("MotionBlur", "动态模糊", Category.useful);
    }

    @Override
    public void onEnable() {
        shaderLoaded = false;
        if (mode.getValue().equals("FrameBuffer")) {
            width = Display.getWidth();
            height = Display.getHeight();
            framebuffer = new Framebuffer(width, height, true);
            framebuffer.setFramebufferColor(0, 0, 0, 0);
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mode.getValue().equals("Shader")) {
            mc.entityRenderer.stopUseShader();
        } else {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
                framebuffer = null;
            }
            GlStateManager.enableBlend();
            GlStateManager.color(1, 1, 1, 1);
        }
        shaderLoaded = false;
        super.onDisable();
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (mc.theWorld == null) return;

        if (mode.getValue().equals("Shader")) {
            handleShader();
        }
    }

    private void handleShader() {
        if (mc.entityRenderer.getShaderGroup() == null && !shaderLoaded) {
            mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/motion_blur.json"));
            shaderLoaded = true;
        }

        if (mc.entityRenderer.getShaderGroup() != null) {
            float uniform = 1F - Math.min(blurAmount.getValue().floatValue() / 10F, 0.9f);
            try {
                mc.entityRenderer.getShaderGroup().listShaders.get(0).getShaderManager()
                        .getShaderUniform("Phosphor").set(uniform, 0F, 0F);
            } catch (Exception e) {
                try {
                    mc.entityRenderer.getShaderGroup().listShaders.get(0).getShaderManager()
                            .getShaderUniform("BlurFactor").set(uniform);
                } catch (Exception e2) {
                }
            }
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (!mode.getValue().equals("FrameBuffer")) return;
        if (framebuffer == null) return;

        width = Display.getWidth();
        height = Display.getHeight();

        if (framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
            framebuffer.createBindFramebuffer(width, height);
        }

        framebuffer.bindFramebuffer(true);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0, width, height, 0, -1, 1);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        mc.getFramebuffer().framebufferRender(width, height);
        framebuffer.unbindFramebuffer();

        float alpha = Math.min(blurAmount.getValue().floatValue() / 10F, 0.9f);
        int passes = Math.max(1, (int) (alpha * 8));

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (int i = 0; i < passes; i++) {
            float currentAlpha = (float) Math.pow(alpha, i + 1);
            if (currentAlpha < 0.01) break;
            GlStateManager.color(1, 1, 1, currentAlpha);
            framebuffer.bindFramebufferTexture();
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 1);
            GL11.glVertex2f(0, 0);
            GL11.glTexCoord2f(1, 1);
            GL11.glVertex2f(width, 0);
            GL11.glTexCoord2f(1, 0);
            GL11.glVertex2f(width, height);
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex2f(0, height);
            GL11.glEnd();
            GlStateManager.bindTexture(0);
        }

        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
    }
}