package cn.tianyu.client.util.impl.render;

import cn.tianyu.client.util.impl.client.Pair;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.text.NumberFormat;

import static cn.tianyu.client.module.impl.client.HUD.mainColor;
import static cn.tianyu.client.util.impl.client.MathUtil.interpolateInt;

public class ColorUtil {


    public static final int WHITE = Color.WHITE.getRGB();

    public static int rainbowDraw(long speed, long... delay) {
        long time = System.currentTimeMillis() + (delay.length > 0 ? delay[0] : 0L);
        return Color.getHSBColor((float) (time % (15000L / speed)) / (15000.0F / (float) speed), 1.0F, 1.0F)
                .getRGB();
    }

    public static Color injectAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
    public static int swapAlpha(int color, float alpha) {
        int f = color >> 16 & 0xFF;
        int f1 = color >> 8 & 0xFF;
        int f2 = color & 0xFF;
        return ColorUtil.getColor(f, f1, f2, (int) alpha);
    }

    public static int getRainbow(int counter) {
        return Color.HSBtoRGB(getRainbowHSB(counter)[0], getRainbowHSB(counter)[1], getRainbowHSB(counter)[2]);
    }

    public static float[] getRainbowHSB(int counter) {
        final int width = 20;

        double rainbowState = Math.ceil(System.currentTimeMillis() - (long) counter * width) / 8;
        rainbowState %= 360;

        float hue = (float) (rainbowState / 360);
        float saturation = 1;
        float brightness = 255;

        return new float[]{hue, saturation, brightness};
    }

    public static Color getBlendColor(final double current, final double max) {
        final long base = Math.round(max / 5.0);
        if (current >= base * 5L) {
            return new Color(15, 255, 15);
        }
        if (current >= base * 4L) {
            return new Color(165, 255, 0);
        }
        if (current >= base * 3L) {
            return new Color(255, 190, 0);
        }
        if (current >= base * 2L) {
            return new Color(255, 90, 0);
        }
        return new Color(255, 0, 0);
    }

    public static int applyOpacity(int color, float opacity) {
        Color old = new Color(color);
        return applyOpacity(old, opacity).getRGB();
    }

    //Opacity value ranges from 0-1
    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static Color getRandomColor() {
        return new Color(Color.HSBtoRGB((float)Math.random(), (float)(0.5 + Math.random() / 2.0), (float)(0.5 + Math.random() / 2.0)));
    }

    public static int overwriteAlphaComponent(final int colour, final int alphaComponent) {
        final int red = colour >> 16 & 0xFF;
        final int green = colour >> 8 & 0xFF;
        final int blue = colour & 0xFF;
        return (alphaComponent & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
    }

    public static int getColor(final int red, final int green, final int blue) {
        return getColor(red, green, blue, 255);
    }

    public static int getColor(final int brightness) {
        return getColor(brightness, brightness, brightness, 255);
    }

    public static int getColor(final int brightness, final int alpha) {
        return getColor(brightness, brightness, brightness, alpha);
    }

    public static int getColor(final int red, final int green, final int blue, final int alpha) {
        int color = MathHelper.clamp_int(alpha, 0, 255) << 24;
        color |= MathHelper.clamp_int(red, 0, 255) << 16;
        color |= MathHelper.clamp_int(green, 0, 255) << 8;
        color |= MathHelper.clamp_int(blue, 0, 255);
        return color;
    }

    public static Color tripleColor(final int rgbValue) {
        return tripleColor(rgbValue, 1.0f);
    }

    public static Color tripleColor(final int rgbValue, float alpha) {
        alpha = Math.min(1.0f, Math.max(0.0f, alpha));
        return new Color(rgbValue, rgbValue, rgbValue, (int)(255.0f * alpha));
    }

    public static int removeAlphaComponent(final int colour) {
        final int red = colour >> 16 & 0xFF;
        final int green = colour >> 8 & 0xFF;
        final int blue = colour & 0xFF;
        return (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
    }

    public static Color fade(int speed, int index, Color color, float alpha) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;

        Color colorHSB = new Color(Color.HSBtoRGB(hsb[0], hsb[1], angle / 360f));

        return new Color(colorHSB.getRed(), colorHSB.getGreen(), colorHSB.getBlue(), Math.max(0, Math.min(255, (int) (alpha * 255))));
    }

    public static int darker(int color, float factor) {
        int r = (int) ((color >> 16 & 0xFF) * factor);
        int g = (int) ((color >> 8 & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        int a = color >> 24 & 0xFF;
        return (r & 0xFF) << 16 | (g & 0xFF) << 8 | b & 0xFF | (a & 0xFF) << 24;
    }

    public static Color blendColors(final float[] fractions, final Color[] colors, final float progress) {
        if (fractions == null) {
            throw new IllegalArgumentException("Fractions can't be null");
        }
        if (colors == null) {
            throw new IllegalArgumentException("Colours can't be null");
        }
        if (fractions.length == colors.length) {
            final int[] indicies = getFractionIndicies(fractions, progress);
            final float[] range = { fractions[indicies[0]], fractions[indicies[1]] };
            final Color[] colorRange = { colors[indicies[0]], colors[indicies[1]] };
            final float max = range[1] - range[0];
            final float value = progress - range[0];
            final float weight = value / max;
            final Color color = blend(colorRange[0], colorRange[1], 1.0f - weight);
            return color;
        }
        throw new IllegalArgumentException("Fractions and colours must have equal number of elements");
    }

    public static Color blend(final Color color1, final Color color2, final double ratio) {
        final float r = (float)ratio;
        final float ir = 1.0f - r;
        final float[] rgb1 = new float[3];
        final float[] rgb2 = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        float red = rgb1[0] * r + rgb2[0] * ir;
        float green = rgb1[1] * r + rgb2[1] * ir;
        float blue = rgb1[2] * r + rgb2[2] * ir;
        if (red < 0.0f) {
            red = 0.0f;
        }
        else if (red > 255.0f) {
            red = 255.0f;
        }
        if (green < 0.0f) {
            green = 0.0f;
        }
        else if (green > 255.0f) {
            green = 255.0f;
        }
        if (blue < 0.0f) {
            blue = 0.0f;
        }
        else if (blue > 255.0f) {
            blue = 255.0f;
        }
        Color color3 = null;
        try {
            color3 = new Color(red, green, blue);
        }
        catch (IllegalArgumentException exp) {
            final NumberFormat nf = NumberFormat.getNumberInstance();
            System.out.println(nf.format(red) + "; " + nf.format(green) + "; " + nf.format(blue));
            exp.printStackTrace();
        }
        return color3;
    }

    public static int[] getFractionIndicies(final float[] fractions, final float progress) {
        final int[] range = new int[2];
        int startPoint;
        for (startPoint = 0; startPoint < fractions.length && fractions[startPoint] <= progress; ++startPoint) {}
        if (startPoint >= fractions.length) {
            startPoint = fractions.length - 1;
        }
        range[0] = startPoint - 1;
        range[1] = startPoint;
        return range;
    }

    public static int interpolateColor(int color1, int color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        Color cColor1 = new Color(color1);
        Color cColor2 = new Color(color2);
        return interpolateColorC(cColor1, cColor2, amount).getRGB();
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount),
                interpolateInt(color1.getGreen(), color2.getGreen(), amount),
                interpolateInt(color1.getBlue(), color2.getBlue(), amount),
                interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static int getColorFromPercentage(float percentage) {
        return Color.HSBtoRGB(Math.min(1.0F, Math.max(0.0F, percentage)) / 3, 0.9F, 0.9F);
    }

    public static int fadeTo(int startColour, int endColour, double progress) {
        double invert = 1.0 - progress;
        int r = (int) ((startColour >> 16 & 0xFF) * invert +
                (endColour >> 16 & 0xFF) * progress);
        int g = (int) ((startColour >> 8 & 0xFF) * invert +
                (endColour >> 8 & 0xFF) * progress);
        int b = (int) ((startColour & 0xFF) * invert +
                (endColour & 0xFF) * progress);
        int a = (int) ((startColour >> 24 & 0xFF) * invert +
                (endColour >> 24 & 0xFF) * progress);
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }

}
