package cn.tianyu.client.gui.ui.modules;


import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.event.events.render.EventShader;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.gui.ui.UiModule;
import cn.tianyu.client.module.impl.client.HUD;
import cn.tianyu.client.module.impl.combat.KillAura;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.annotations.*;
import cn.tianyu.client.util.impl.render.RoundedUtils;
import cn.tianyu.client.util.impl.render.StencilUtil;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;

import java.awt.*;

import static cn.tianyu.client.util.impl.render.ColorUtil.interpolateColorC;

public class TargetHud
extends UiModule {
    public static TargetHud instance;
    private EntityLivingBase target;
    private final Animation animation = new EaseBackIn(500, 1.0, 1.5f);

    public TargetHud() {
        super("TargetHud", 50.0, 50.0, 200.0, 120.0);
        instance = this;
    }

    @EventTarget
    public void onTick(EventTick event) {
        block6: {
            block7: {
                KillAura aura = TYClient.instance.moduleManager.getModule(KillAura.class);
                if (!aura.getState()) {
                    this.animation.setDirection(Direction.BACKWARDS);
                }
                if (KillAura.target != null) {
                    this.target = KillAura.target;
                    this.animation.setDirection(Direction.FORWARDS);
                }
                if (!aura.getState()) break block6;
                if (KillAura.target == null) break block7;
                if (this.target == KillAura.target) break block6;
            }
            this.animation.setDirection(Direction.BACKWARDS);
        }
        if (TargetHud.mc.currentScreen instanceof GuiChat) {
            this.animation.setDirection(Direction.FORWARDS);
            this.target = TargetHud.mc.thePlayer;
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if(mc.thePlayer != null){
            float x2 = (float)this.getPosX();
            float y2 = (float)this.getPosY();
            RenderUtil.scaleStart((float)((double)x2 + this.width / 2.0), (float)((double)y2 + this.height / 2.0), (float)this.animation.getOutput());
            if(target != null){
                this.render(x2, y2, 1.0f, this.target, false);
            }
            RenderUtil.scaleEnd();
        }
    }

    @EventTarget
    public void shader(EventShader event) {
        float x2 = (float)this.getPosX();
        float y2 = (float)this.getPosY();
        if (event.isBloom()) {
            return;
        }
        RenderUtil.scaleStart((float)((double)x2 + this.width / 2.0), (float)((double)y2 + this.height / 2.0), (float)this.animation.getOutput());
        if(target != null){
            this.render(x2, y2, 1.0f, this.target, true);
        }
        RenderUtil.scaleEnd();
    }

    public void render(float x2, float y2, float alpha, EntityLivingBase target, boolean blur) {
        float getMaxHel = target.getMaxHealth() < 20.0f ? target.getMaxHealth() : 20.0f;
        this.setWidth(Math.max(26.0f + getMaxHel * 3.0f + (float)FontManager.arial18.getStringWidth(target.getName()) - 34.0f, 28.0f + getMaxHel * 3.0f));
        this.setHeight(36.0);
        if (blur) {
            //RoundRect.drawRoundRect(x2, y2, x2 + Math.max(39.0f + getMaxHel * 3.0f, (float)(39 + FontManager.arial18.getStringWidth(target.getName()))) + 2.0f, y2 + 36.0f, 5.0f, new Color(0, 0, 0).getRGB());
            RoundedUtils.drawRound(x2, y2, Math.max(39.0f + getMaxHel * 3.0f, (float)(39 + FontManager.arial18.getStringWidth(target.getName()))) + 2.0f, 36.0f, 5.0f, new Color(0, 0, 0));
            return;
        }
        //RoundRect.drawRoundRect(x2, y2, x2 + Math.max(39.0f + getMaxHel * 3.0f,(float)(39 + FontManager.arial18.getStringWidth(target.getName()))) + 2.0f, y2 + 36.0f, 5.0f, new Color(0, 0, 0, 100).getRGB());
        RoundedUtils.drawRound(x2, y2, Math.max(39.0f + getMaxHel * 3.0f, (float)(39 + FontManager.arial18.getStringWidth(target.getName()))) + 2.0f, 36.0f, 5.0f, new Color(0, 0, 0, 100));
        if (target instanceof AbstractClientPlayer) {
            this.drawBigHeadRound(x2 + 3.0f, y2 + 3.0f, 30.0f, 30.0f, (AbstractClientPlayer)target);
        }
        FontManager.arial18.drawString(target.getName(), x2 + 36.0f, y2 + 6.0f, new Color(255, 255, 255).getRGB());
        double nmsl = (double)target.getHealth() - Math.floor(target.getHealth()) >= 0.5 ? 0.5 : 0.0;
        FontManager.arial14.drawString(Math.floor(target.getHealth()) + nmsl + " HP", x2 + 36.0f, y2 + 6.0f + (float)FontManager.arial18.getHeight(), new Color(255, 255, 255).getRGB());
        target.animatedHealthBar = AnimationUtil.animate(target.animatedHealthBar, target.getHealth(), 0.15f);
        //RoundRect.drawRoundRect(x2 + 36.0f, y2 + 16.0f + (float) FontManager.arial16.getHeight(),x2 + 36.0f +  Math.max(getMaxHel * 3.0f, (float)FontManager.arial18.getStringWidth(target.getName())), y2 + 16.0f + (float) FontManager.arial16.getHeight() + 5.0f, 2.5f, new Color(0, 0, 0, 200).getRGB());
        //RoundRect.drawRoundRect(x2 + 36.0f, y2 + 16.0f + (float)FontManager.arial16.getHeight(), x2 + 36.0f + target.animatedHealthBar / target.getMaxHealth() * Math.max(getMaxHel * 3.0f, (float)FontManager.arial18.getStringWidth(target.getName())),  y2 + 16.0f + (float)FontManager.arial16.getHeight() + 5.0f, 2.5f, TYClient.instance.getMainColor().getRGB());
        RoundedUtils.drawRound(x2 + 36.0f, y2 + 16.0f + (float) FontManager.arial16.getHeight(), Math.max(getMaxHel * 3.0f, (float)FontManager.arial18.getStringWidth(target.getName())), 5.0f, 2.5f, new Color(0, 0, 0, 200));
        RoundedUtils.drawRound(x2 + 36.0f, y2 + 16.0f + (float)FontManager.arial16.getHeight(), target.animatedHealthBar / target.getMaxHealth() * Math.max(getMaxHel * 3.0f, (float)FontManager.arial18.getStringWidth(target.getName())), 5.0f, 2.5f, HUD.color(8));
    }

    protected void drawBigHead(float x2, float y2, float width, float height, AbstractClientPlayer player) {
        double offset = -(player.hurtTime * 23);
        RenderUtil.glColor(new Color(255, (int)(255.0 + offset), (int)(255.0 + offset)).getRGB());
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        mc.getTextureManager().bindTexture(player.getLocationSkin());
        Gui.drawScaledCustomSizeModalRect((int) x2, (int) y2, 8.0f, 8.0f, 8, 8, (int)width, (int)height, 64.0f, 64.0f);
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    protected void drawBigHeadRound(float x2, float y2, float width, float height, AbstractClientPlayer player) {
        StencilUtil.initStencilToWrite();
        RenderUtil.renderRoundedRect(x2, y2, width, height, 4.0f, -1);
        StencilUtil.readStencilBuffer(1);
        RenderUtil.color(-1);
        this.drawBigHead(x2, y2, width, height, player);
        StencilUtil.uninitStencilBuffer();
        GlStateManager.disableBlend();
    }

//    private Color[] getColors(float offset, float rectHeight, float animationValue) {
//        Color color = color1.getValue();
//        Color secondColor = color2.getValue();
//
//        switch (colorMode.getValue()) {
//            case "Breathe", "Static" -> {
//                color = ColorUtil.getColorFromIndex(4, 0, color1.getValue(), color2.getValue(), false);
//                secondColor = colorMode.getValue().equals("Static") ? color : secondColor;
//            }
//            case "Fade" -> {
//                int colorOffset1 = (int) -(offset - (rectHeight * animationValue) / 2) * 2;
//                int colorOffset2 = (int) -(offset + (rectHeight * animationValue) / 2) * 2;
//                color = ColorUtil.getColorFromIndex(4, colorOffset1, color1.getValue(), color2.getValue(), false);
//                secondColor = ColorUtil.getColorFromIndex(4, colorOffset2, color1.getValue(), color2.getValue(), false);
//            }
//        }
//        return new Color[]{color, secondColor};
//    }
//    public static Color getColorFromIndex(int speed, int index, Color start, Color end, boolean trueColor) {
//        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
//        angle = (angle >= 180 ? 360 - angle : angle) * 2;
//        return interpolateColorC(start, end, angle / 360f);
//    }
}

