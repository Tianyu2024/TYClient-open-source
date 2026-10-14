package cn.tianyu.client.value.impl;

import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.util.DrawUtil;
import cn.tianyu.client.value.Value;
import net.minecraft.util.MathHelper;
import org.checkerframework.checker.units.qual.C;

import java.awt.*;

/**
 * @Author: CN_tianyu
 */
public class ColorValue extends Value<Color> {
    protected boolean alphaChangeable = false;
    protected boolean enabledRainbow = false;
    protected boolean rainbowChangeable = false;

    private boolean picking;
    private static final int SIZE = 12;
    private int defaultColor;

    public ColorValue(String name, Color defaultColor) {
        super(name);
        setValue(defaultColor);
        this.defaultColor = defaultColor.getRGB();
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        setPosition(x, y);

        // 显示名称
        FontManager.font18.drawString(getName(), x + 5, y + 3, Color.WHITE.getRGB());

        // 颜色预览框
        DrawUtil.drawRect(x + 80, y, SIZE, SIZE, getValue().getRGB());
        DrawUtil.drawRectOutline(x + 80, y, SIZE, SIZE, 1,
                picking ? 0xFF00FF00 : 0xFFAAAAAA);

        // 颜色选择器（展开时）
        if (picking) {
            DrawUtil.drawRect(x + 80 + SIZE + 5, y - 30, 100, 60, new Color(30, 30, 30).getRGB());

            // 色相条
            for (int i = 0; i < 100; i++) {
                float hue = i / 100f;
                DrawUtil.drawRect(x + 80 + SIZE + 5 + i, y - 30, 1, 15,
                        Color.HSBtoRGB(hue, 1f, 1f));
            }

            // 饱和度/亮度面板
            float[] hsb = Color.RGBtoHSB(
                    getValue().getRed(),
                    getValue().getGreen(),
                    getValue().getBlue(), null);

            for (int sx = 0; sx < 60; sx++) {
                for (int sy = 0; sy < 30; sy++) {
                    float saturation = sx / 60f;
                    float brightness = 1f - sy / 30f;
                    DrawUtil.drawRect(
                            x + 80 + SIZE + 5 + sx,
                            y - 15 + sy,
                            1, 1,
                            Color.HSBtoRGB(hsb[0], saturation, brightness)
                    );
                }
            }

            // 当前颜色标记
            int markerX = (int)(hsb[1] * 60) + x + 80 + SIZE + 5;
            int markerY = (int)((1f - hsb[2]) * 30) + y - 15;
            DrawUtil.drawRectOutline(markerX - 2, markerY - 2, 5, 5, 1, 0xFFFFFFFF);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        // 点击颜色框
        if (mouseX >= x + 80 && mouseX <= x + 80 + SIZE &&
                mouseY >= y && mouseY <= y + SIZE) {
            picking = !picking;
            return;
        }

        // 颜色选择逻辑
        if (picking) {
            if (mouseX >= x + 80 + SIZE + 5 && mouseX <= x + 80 + SIZE + 5 + 100) {
                if (mouseY >= y - 30 && mouseY <= y - 15) {
                    // 选择色相
                    float hue = (mouseX - (x + 80 + SIZE + 5)) / 100f;
                    float[] hsb = Color.RGBtoHSB(
                            getValue().getRed(),
                            getValue().getGreen(),
                            getValue().getBlue(),
                            null
                    );
                    setValue(new Color(Color.HSBtoRGB(hue, hsb[1], hsb[2])));
                }
                else if (mouseY >= y - 15 && mouseY <= y + 15) {
                    // 选择饱和度/亮度
                    float saturation = MathHelper.clamp_float(
                            (mouseX - (x + 80 + SIZE + 5)) / 60f, 0f, 1f);
                    float brightness = 1f - MathHelper.clamp_float(
                            (mouseY - (y - 15)) / 30f, 0f, 1f);
                    float[] hsb = Color.RGBtoHSB(
                            getValue().getRed(),
                            getValue().getGreen(),
                            getValue().getBlue(),
                            null
                    );
                    setValue(new Color(Color.HSBtoRGB(hsb[0], saturation, brightness)));
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        // 点击外部区域关闭选择器
        if (picking && !(mouseX >= x + 80 && mouseX <= x + 80 + SIZE + 5 + 100 &&
                mouseY >= y - 30 && mouseY <= y + 15)) {
            picking = false;
        }
    }

    public int getColorInt() {
        return getValue().getRGB();
    }

    public int getRed() {
        return getValue().getRed();
    }
    public int getGreen() {
        return getValue().getGreen();
    }
    public int getBlue() {
        return getValue().getBlue();
    }

    public Color getColorC() {
        return new Color(defaultColor);
    }

    public void setColor(int color) {
        this.setValue(new Color(color));
        this.value = new Color(color);
    }


    public boolean isAlphaChangeable() {
        return this.alphaChangeable;
    }

    public void setAlphaChangeable(boolean alphaChangeable) {
        this.alphaChangeable = alphaChangeable;
    }


    public void setRainbowEnabled(boolean enabledRainbow) {
        this.enabledRainbow = !this.rainbowChangeable ? false : enabledRainbow;
    }

    public boolean isEnabledRainbow() {
        return this.enabledRainbow;
    }
}