package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.render.EventRender3D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.annotations.Animation;
import cn.tianyu.client.util.annotations.Direction;
import cn.tianyu.client.util.impl.render.ColorUtil;
import cn.tianyu.client.util.impl.render.DecelerateAnimation;
import cn.tianyu.client.util.impl.render.GLUtil;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * @Author: CN_tianyu
 */

@DefaultEnabled
public class JumpCircle extends Module {
    private static final NumberValue radius = new NumberValue("Radius", 2, 1, 5, 0.25);
    public JumpCircle(){
        super("JumpCircle","JumpCircle", Category.render);
    }
    private boolean inAir = false;

    private final List<Circle> circles = new ArrayList<>();
    private final List<Circle> toRemove = new ArrayList<>();

    @EventTarget
    public void onMotionEvent(EventMotion event) {
        if (event.isPre()) {
            if (!event.isGround()) {
                inAir = true;
            } else if (event.isGround() && inAir) {
                circles.add(new Circle(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
                inAir = false;
            }
        }
    }

    @EventTarget
    public void onRender3DEvent(EventRender3D event) {
        for (Circle circle : circles) {
            circle.drawCircle();
            if (circle.fadeAnimation != null && circle.fadeAnimation.finished(Direction.BACKWARDS)) {
                toRemove.add(circle);
            }
        }

        for (Circle circle : toRemove) {
            circles.remove(circle);
        }


    }


    private static class Circle {
        private final float x, y, z;
        private final Animation expandAnimation;
        private final Animation fadeAnimation;


        public Circle(double x, double y, double z) {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            this.fadeAnimation = new DecelerateAnimation(600, 1);
            this.expandAnimation = new DecelerateAnimation(1000, radius.getValue());
        }


        public void drawCircle() {
            if (expandAnimation.getOutput() > (radius.getValue() * .7f)) {
                fadeAnimation.setDirection(Direction.BACKWARDS);
            }


            glPushMatrix();
            RenderUtil.setAlphaLimit(0);

            double animation = expandAnimation.getOutput();
            double fade = fadeAnimation.getOutput();
            GLUtil.setup2DRendering();
            glDisable(GL_DEPTH_TEST);
            glDepthMask(false);
            glShadeModel(GL_SMOOTH);
            double pi2 = MathHelper.PI2;

            double xVal = x - mc.getRenderManager().renderPosX;
            double yVal = y - mc.getRenderManager().renderPosY;
            double zVal = z - mc.getRenderManager().renderPosZ;

            glBegin(GL_TRIANGLE_STRIP);

            int color1 = new Color(117, 174, 232).getRGB();
            int color2 = new Color(0, 101, 255, 255).getRGB();


            float newAnim = (float) Math.max(0, animation - (.3f * (animation / expandAnimation.getEndPoint())));

            for (int i = 0; i <= 90; ++i) {
                float value = (float) Math.sin((i * 4) * (MathHelper.PI / 180));
                int color = ColorUtil.interpolateColor(color1, color2, Math.abs(value));
                RenderUtil.color(color, fade * .6f);
                glVertex3d(xVal + animation * Math.cos(i * pi2 / 45), yVal, zVal + animation * Math.sin(i * pi2 / 45));


                RenderUtil.color(color, fade * .15f);
                glVertex3d(xVal + newAnim * Math.cos(i * pi2 / 45), yVal, zVal + newAnim * Math.sin(i * pi2 / 45));
            }

            glEnd();


            glShadeModel(GL_FLAT);
            glDepthMask(true);
            glEnable(GL_DEPTH_TEST);
            GLUtil.end2DRendering();
            glPopMatrix();
            RenderUtil.resetColor();
            RenderUtil.color(-1);

        }

    }
}
