package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.misc.HackerDetector;
import cn.tianyu.client.module.impl.misc.Teams;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.impl.client.MathUtils;
import cn.tianyu.client.util.impl.player.RotationUtil;
import cn.tianyu.client.util.impl.render.ColorUtil;
import cn.tianyu.client.util.impl.render.GLUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * @Author: CN_tianyu
 */
public class Indicators extends Module {
    public Indicators() {
        super("Indicators","攻击指示器", Category.render);
    }
    private final NumberValue size = new NumberValue("Size", 6, 3, 30, 1);
    private final NumberValue radius = new NumberValue("Radius", 15, 5, 250, 1);
    private final BooleanValue stretch  = new BooleanValue("Stretch", true);
    private final BooleanValue name = new BooleanValue("Name", true);
    private final BooleanValue outline = new BooleanValue("Outline", true);
    private final BooleanValue renderArrows = new BooleanValue("Render Arrows", true);
    private final BooleanValue renderPearls = new BooleanValue("Render Pearls", true);
    private final BooleanValue renderFireballs = new BooleanValue("Render Fireballs", true);
    private final BooleanValue renderPlayers = new BooleanValue("Render Player", false);
    private final BooleanValue playerInfo = new BooleanValue("Player Info", true);
    private final BooleanValue onlyNotInView  = new BooleanValue("Only Not In View", true);

    @EventTarget
    public void onRender2D(EventRender2D event) {
        final ScaledResolution scaledResolution = event.getScaledResolution();
        //FontRenderer fontRenderer = Fonts.psRegular.get(size.getValue() + 3);

        final float hWidth = scaledResolution.getScaledWidth() / 2.0f;
        final float hHeight = scaledResolution.getScaledHeight() / 2.0f;

        final float partialTicks = event.getPartialTicks();

        glTranslatef(hWidth, hHeight, 0);

        if (outline.get()) {
            glEnable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);

            glLineWidth(1);
        }

        final double arrowSize = this.size.getValue();
        final double radius = this.radius.getValue();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if ((!onlyNotInView.get() || onlyNotInView.get() && !RenderUtil.isInViewFrustum(entity))&& (entity instanceof EntityPlayer && entity != mc.thePlayer && renderPlayers.get()
                    || entity instanceof EntityArrow && !((EntityArrow) entity).inGround && renderArrows.get()
                    || entity instanceof EntityFireball && renderFireballs.get() || entity instanceof EntityEnderPearl && renderPearls.get())) {
                final Entity local = mc.thePlayer;

                final float currentRotation = (float) MathUtils.interpolate(local.prevRotationYaw, local.rotationYaw, partialTicks);

                final double currentPosX = MathUtils.interpolate(local.prevPosX, local.posX, partialTicks);
                final double currentPosZ = MathUtils.interpolate(local.prevPosZ, local.posZ, partialTicks);

                final double playerPosX = MathUtils.interpolate(entity.prevPosX, entity.posX, partialTicks);
                final double playerPosZ = MathUtils.interpolate(entity.prevPosZ, entity.posZ, partialTicks);

                final float yawToPlayer = RotationUtil.calculateYawFromSrcToDst(currentRotation, currentPosX, currentPosZ,
                        playerPosX, playerPosZ) - currentRotation;

                glPushMatrix();

                final double rads = Math.toRadians(yawToPlayer);

                final double aspectRatio = stretch.get() ? scaledResolution.getScaledWidth_double() / scaledResolution.getScaledHeight_double() : 1;

                glTranslated(radius * Math.sin(rads) * aspectRatio,
                        radius * -Math.cos(rads), 0);
                glDisable(GL_TEXTURE_2D);

                GLUtil.startBlend();


                int color;

                if (entity instanceof EntityPlayer) {
                    color = getModule(HackerDetector.class).isHacker((EntityPlayer) entity) ? new Color(255, 0, 0).getRGB() : (Teams.isSameTeam(entity) ? new Color(96, 252, 66).getRGB() : new Color(252, 96, 66).getRGB());
                } else if (entity instanceof EntityArrow) {
                    color = new Color(184, 184, 184).getRGB();
                } else if (entity instanceof EntityFireball) {
                    color = new Color(255, 128, 8).getRGB();
                } else {
                    color = new Color(128, 128, 255).getRGB();
                }

                if (name.get()) {
                    glEnable(GL_TEXTURE_2D);
                    final String name = entity.getName();
                    final String displayName = name.equals("entity.ThrownEnderpearl.name") ? "Pearl" : name;
                    final String hacker = entity instanceof EntityPlayer && getModule(HackerDetector.class).isHacker((EntityPlayer) entity) ? "[Hacker] " : "";
                    //FontManager.F22.drawString(hacker + displayName, (float) ((double) -FontManager.F22.getStringWidth(hacker + displayName) / 2), (float) (-arrowSize / 2.0 - FontManager.F22.getHeight() - 4), 0xFFFFFFFF);
                    glDisable(GL_TEXTURE_2D);
                }

                if (playerInfo.get() && playerInfo.get() && entity instanceof EntityPlayer && entity != mc.thePlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    final float health = player.getHealth() / player.getMaxHealth();

                    final int healthColor = ColorUtil.getColorFromPercentage(health);

                    final double barSize = arrowSize + 4;
                    glBegin(GL_QUADS);
                    {
                        // Background
                        RenderUtil.color(0x96000000);
                        addQuadVertices(-barSize / 2.0, arrowSize / 2.0 + 2, barSize, 2);

                        // Colored bar
                        RenderUtil.color(healthColor);
                        final double filled = (barSize - 1) * health;
                        addQuadVertices(-barSize / 2.0 + 0.5, arrowSize / 2.0 + 2 + 0.5, filled, 1);
                    }
                    glEnd();
                }

                glRotatef(yawToPlayer, 0, 0, 1);

                if (outline.get()) {
                    // Draw Outline
                    RenderUtil.color(color | 0xFF000000);

                    glBegin(GL_LINE_LOOP);
                    {
                        addTriangleVertices(arrowSize);
                    }

                    RenderUtil.resetColor();
                    glEnd();
                }

                // Draw Arrow

                glEnable(GL_POLYGON_SMOOTH);
                glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);

                RenderUtil.color(color);

                glBegin(GL_TRIANGLES);
                {
                    addTriangleVertices(arrowSize);
                }

                RenderUtil.resetColor();
                glEnd();

                glDisable(GL_POLYGON_SMOOTH);
                glHint(GL_POLYGON_SMOOTH_HINT, GL_DONT_CARE);

                GLUtil.endBlend();
                glEnable(GL_TEXTURE_2D);

                glPopMatrix();
            }
        }

        if (outline.get()) {
            glDisable(GL_LINE_SMOOTH);
            glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE);
        }

        glTranslatef(-hWidth, -hHeight, 0);
    }

    private static void addQuadVertices(final double x, final double y, final double width, final double height) {
        glVertex2d(x, y);
        glVertex2d(x, y + height);
        glVertex2d(x + width, y + height);
        glVertex2d(x + width, y);
    }

    private static void addTriangleVertices(final double size) {
        glVertex2d(0, -size / 2);
        glVertex2d(-size / 2, size / 2);
        glVertex2d(size / 2, size / 2);
    }
}
