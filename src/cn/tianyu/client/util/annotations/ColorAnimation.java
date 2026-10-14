package cn.tianyu.client.util.annotations;

import java.awt.*;

/**
 * @Author: CN_tianyu
 */
public class ColorAnimation {
    private Color color;

    private float r;
    private float g;
    private float b;
    private float a;

    public ColorAnimation(Color color) {
        this.color = color;
    }

    public void animateTo(Color color, float speed) {
        this.r = AnimationUtil.animate(this.r, color.getRed(), speed);
        this.g = AnimationUtil.animate(this.g, color.getGreen(), speed);
        this.b = AnimationUtil.animate(this.b, color.getBlue(), speed);
        this.a = AnimationUtil.animate(this.a, color.getAlpha(), speed);

        this.color = new Color((int) r, (int) g, (int) b, (int) a);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
