package cn.tianyu.client.value.impl;


import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.util.DrawUtil;
import cn.tianyu.client.util.annotations.AnimationUtil;
import cn.tianyu.client.util.impl.render.RoundedUtils;
import cn.tianyu.client.value.Value;
import net.minecraft.client.Minecraft;

import java.awt.*;

/**
 * @Author: CN_tianyu
 */
public class BooleanValue extends Value<Boolean> {
    private boolean hovered;
    private float slideAnim;
    private static final float ANIM_SPEED = 0.18f;
    private final Minecraft mc = Minecraft.getMinecraft();

    public BooleanValue(String name, boolean defaultValue) {
        super(name);
        setValue(defaultValue);
        slideAnim = getValue() ? 1f : 0f;
    }

    public BooleanValue(String name, Boolean value, Dependency dependency) {
        super(name, dependency);
        this.setValue(value);
        slideAnim = getValue() ? 1f : 0f;
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        setPosition(x, y);
        FontManager.font18.drawString(getName(), x + 5, y + 5, Color.WHITE.getRGB());

        int toggleX = x + 90;
        int toggleY = y + 3;
        int toggleWidth = 20;
        int toggleHeight = 14;
        float toggleRadius = toggleHeight / 2f;

        hovered = mouseX >= toggleX && mouseX <= toggleX + toggleWidth
                && mouseY >= toggleY && mouseY <= toggleY + toggleHeight;

        float targetAnim = getValue() ? 1f : 0f;
        slideAnim = AnimationUtil.animate(slideAnim, targetAnim, ANIM_SPEED);

        int bgColor = hovered
                ? (getValue() ? new Color(0, 200, 255).getRGB() : new Color(120, 120, 120).getRGB())
                : (getValue() ? new Color(0, 174, 248).getRGB() : new Color(70, 70, 70).getRGB());

        RoundedUtils.drawRound(toggleX, toggleY, toggleWidth, toggleHeight, toggleRadius, new Color(bgColor));

        float sliderDiameter = toggleHeight - 4;
        float sliderMaxX = toggleX + toggleWidth - sliderDiameter - 2;
        float sliderMinX = toggleX + 2;
        float sliderX = sliderMinX + (sliderMaxX - sliderMinX) * slideAnim;
        float sliderY = toggleY + 2;

        RoundedUtils.drawRound(sliderX, sliderY, sliderDiameter, sliderDiameter, sliderDiameter / 2f, Color.WHITE);

        DrawUtil.drawRectOutline(toggleX, toggleY, toggleWidth, toggleHeight, 1, hovered ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && hovered) {
            mc.thePlayer.playSound("random.click", 0.3f, getValue() ? 0.8f : 1.2f);
            setValue(!getValue());
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
    }
}