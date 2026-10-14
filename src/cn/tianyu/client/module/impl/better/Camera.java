package cn.tianyu.client.module.impl.better;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.impl.render.ColorUtil;
import cn.tianyu.client.util.impl.render.GLUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.gui.ScaledResolution;

import static org.lwjgl.opengl.GL11.*;

/**
 * @Author: CN_tianyu
 */

@DefaultEnabled
public class Camera extends Module {
    public final BooleanValue cameraClipValue = new BooleanValue("CameraClip", true);
    public final BooleanValue FPSHurtCam = new BooleanValue("FPS Hurt Cam", true);
    public final BooleanValue removeHandViewBobbing = new BooleanValue("Remove Hand View Bobbing", false);
    public final BooleanValue removeHandInertiaAnimation = new BooleanValue("Remove Hand Inertia Animation", true);
    public final BooleanValue smoothThirdPerson = new BooleanValue("Smooth Third Person", true);
    public NumberValue smooth = new NumberValue("Speed", 2.0, 1.0, 4.0, 0.1, this.smoothThirdPerson::getValue);
    //public static final BooleanValue animation = new BooleanValue("Animation", true);
    public final BooleanValue motionCamera = new BooleanValue("Motion", true);
    public static final NumberValue interpolation = new NumberValue("Motion Interpolation", 0.05f, 0.01f, 0.3f, 0.05f);
    public static final BooleanValue nofire = new BooleanValue("Nofire", false);
    //public static final BooleanValue transform = new BooleanValue("Transform", false);
    //public static final NumberComponent x = new NumberComponent("TransformX", 0f, 5f, -5f, 0.1f);
    //public static final NumberComponent y = new NumberComponent("TransformY", 0f, 5f, 0f, 0.1f);
    //public static final NumberComponent z = new NumberComponent("TransformZ", 0f, 5f, -5f, 0.1f);

    public Camera(){
        super("Camera","Camera", Category.better);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (FPSHurtCam.getValue()) {
            final float hurtTimePercentage = (this.mc.thePlayer.hurtTime - event.getPartialTicks()) / this.mc.thePlayer.maxHurtTime;

            if (hurtTimePercentage > 0.0) {
                glDisable(GL_TEXTURE_2D);
                GLUtil.startBlend();
                glShadeModel(GL_SMOOTH);
                glDisable(GL_ALPHA_TEST);

                final ScaledResolution scaledResolution = event.getScaledResolution();

                final float lineWidth = 20.f;

                glLineWidth(lineWidth);

                final int width = scaledResolution.getScaledWidth();
                final int height = scaledResolution.getScaledHeight();

                final int fadeOutColour = ColorUtil.fadeTo(0x00FF0000, 0xFFFF0000, hurtTimePercentage);

                glBegin(GL_QUADS);
                {
                    // Left
                    RenderUtil.color(fadeOutColour);
                    glVertex2f(0, 0);
                    glVertex2f(0, height);
                    RenderUtil.color(0x00FF0000);
                    glVertex2f(lineWidth, height - lineWidth);
                    glVertex2f(lineWidth, lineWidth);

                    // Right
                    RenderUtil.color(0x00FF0000);
                    glVertex2f(width - lineWidth, lineWidth);
                    glVertex2f(width - lineWidth, height - lineWidth);
                    RenderUtil.color(fadeOutColour);
                    glVertex2f(width, height);
                    glVertex2f(width, 0);

                    // Top
                    RenderUtil.color(fadeOutColour);
                    glVertex2f(0, 0);
                    RenderUtil.color(0x00FF0000);
                    glVertex2d(lineWidth, lineWidth);
                    glVertex2f(width - lineWidth, lineWidth);
                    RenderUtil.color(fadeOutColour);
                    glVertex2f(width, 0);

                    // Bottom
                    RenderUtil.color(0x00FF0000);
                    glVertex2f(lineWidth, height - lineWidth);
                    RenderUtil.color(fadeOutColour);
                    glVertex2d(0, height);
                    glVertex2f(width, height);
                    RenderUtil.color(0x00FF0000);
                    glVertex2f(width - lineWidth, height - lineWidth);
                }
                glEnd();

                glEnable(GL_ALPHA_TEST);
                glShadeModel(GL_FLAT);
                GLUtil.endBlend();
                glEnable(GL_TEXTURE_2D);
            }
        }
    }

    public boolean canMinimalBobbing(){
        return this.isEnabled();
    }
}
