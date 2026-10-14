package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.events.render.EventShader;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.impl.render.KawaseBloom;
import cn.tianyu.client.util.impl.render.KawaseBlur;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.shader.Framebuffer;

/**
 * @Author: CN_tianyu
 */
public class PostProcessing extends Module {
    public PostProcessing() {
        super("PostProcessing","你猜", Category.render);
    }

    public final BooleanValue blur = new BooleanValue("Blur", true);
    private final NumberValue iterations = new NumberValue("Blur Iterations", 2.0, 1.0, 8.0, 1.0);
    private final NumberValue offset = new NumberValue("Blur Offset", 3.0, 1.0, 10.0, 1.0);
    private final BooleanValue bloom = new BooleanValue("Bloom", true);
    private final NumberValue shadowRadius = new NumberValue("Bloom Iterations", 3.0, 1.0, 8.0, 1.0);
    private final NumberValue shadowOffset = new NumberValue("Bloom Offset", 1.0, 1.0, 10.0, 1.0);
    private String currentMode;
    private Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

    public void blurScreen() {
        EventShader eventShader;
        if (!this.getState()) {
            return;
        }
        if (((Boolean)this.blur.getValue()).booleanValue()) {
            this.stencilFramebuffer = RenderUtil.createFrameBuffer(this.stencilFramebuffer);
            this.stencilFramebuffer.framebufferClear();
            this.stencilFramebuffer.bindFramebuffer(false);
            eventShader = new EventShader(false);
            TYClient.instance.eventManager.callEvent(eventShader);
            RenderUtil.resetColor();
            this.drawBlur();
            RenderUtil.resetColor();
            this.stencilFramebuffer.unbindFramebuffer();
            KawaseBlur.renderBlur(this.stencilFramebuffer.framebufferTexture, ((Double)this.iterations.getValue()).intValue(), ((Double)this.offset.getValue()).intValue());
        }
        if (((Boolean)this.bloom.getValue()).booleanValue()) {
            this.stencilFramebuffer = RenderUtil.createFrameBuffer(this.stencilFramebuffer);
            this.stencilFramebuffer.framebufferClear();
            this.stencilFramebuffer.bindFramebuffer(false);
            eventShader = new EventShader(true);
            TYClient.instance.eventManager.callEvent(eventShader);
            RenderUtil.resetColor();
            this.drawBloom();
            RenderUtil.resetColor();
            this.stencilFramebuffer.unbindFramebuffer();
            KawaseBloom.renderBlur(this.stencilFramebuffer.framebufferTexture, ((Double)this.shadowRadius.getValue()).intValue(), ((Double)this.shadowOffset.getValue()).intValue());
        }
    }

    private void drawBloom() {
    }

    private void drawBlur() {
    }
}
