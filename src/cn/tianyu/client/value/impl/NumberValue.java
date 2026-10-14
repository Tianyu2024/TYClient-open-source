package cn.tianyu.client.value.impl;

import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.util.DrawUtil;
import cn.tianyu.client.util.annotations.AnimationUtil;
import cn.tianyu.client.util.impl.render.RoundedUtils;
import cn.tianyu.client.value.Value;
import cn.tianyu.client.util.impl.client.MathUtil;
import lombok.Getter;

import java.awt.*;

/**
 * @Author: CN_tianyu
 */
public class NumberValue extends Value<Double> {
    public float animatedNumberValue = 0f;

    public float animatedPercentage;
    public boolean sliding;

    @Getter
    private final double min, max, inc;
    private boolean dragging;
    private double sliderWidth = 90.0;
    private long lastDragTime;

    public NumberValue(String name, double value, double min, double max, double inc) {
        super(name);
        setValue(MathUtil.clamp(value, min, max));
        this.min = min;
        this.max = max;
        this.inc = inc;
        this.animatedNumberValue = 0;

    }

    public NumberValue(String name, double value, double min, double max, double inc, Dependency dependenc) {
        super(name,dependenc);
        setValue(MathUtil.clamp(value, min, max));
        this.min = min;
        this.max = max;
        this.inc = inc;
        this.animatedNumberValue = 0;
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        // 更新坐标（用于后续点击检测）
        setPosition(x, y);

        // 显示名称和当前值
        FontManager.font18.drawString(
                String.format("%s: %.2f", getName(), getValue()),
                x + 5, y + 5, Color.WHITE.getRGB()
        );

        // 滑块背景
        //RoundedUtils.drawRound();
        DrawUtil.drawRect(x + 5, y + 18, (int)sliderWidth, 4, new Color(70, 70, 70).getRGB());

        // 计算进度
        double percentage = (getValue() - min) / (max - min);
        int progressWidth = (int)(sliderWidth * percentage);

        // 进度条
        this.animatedNumberValue = AnimationUtil.animate(this.animatedNumberValue,progressWidth,0.2f);
        RoundedUtils.drawRound(x + 5, y + 18, animatedNumberValue, 4,3.0f, new Color(0, 174, 248));

        // 滑块手柄
        int handleX = x + 5 + progressWidth - 3;
        RoundedUtils.drawRound(handleX, y + 17, 6, 6,2.5f,Color.WHITE);

        // 实时更新拖动（即使鼠标超出范围）
        if (dragging && System.currentTimeMillis() - lastDragTime < 500) {
            updateValue(mouseX);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isHovered(mouseX, mouseY)) {
            dragging = true;
            lastDragTime = System.currentTimeMillis();
            updateValue(mouseX);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        dragging = false;
    }

    private boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x + 5 && mouseX <= x + 5 + sliderWidth &&
                mouseY >= y + 12 && mouseY <= y + 24;
    }

    private void updateValue(int mouseX) {
        // 计算鼠标位置对应的值
        double newValue = min + ((max - min) *
                MathUtil.clamp((mouseX - (x + 5)) / sliderWidth, 0, 1));

        // 对齐到增量倍数
        newValue = Math.round(newValue / inc) * inc;

        // 确保在范围内
        setValue(MathUtil.clamp(newValue, min, max));
        lastDragTime = System.currentTimeMillis();
    }
}