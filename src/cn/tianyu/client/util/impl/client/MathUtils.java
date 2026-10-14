package cn.tianyu.client.util.impl.client;

import net.minecraft.util.MathHelper;

public class MathUtils {
    public static final float PI = (float)Math.PI;
    public static final float PI2 = ((float)Math.PI * 2F);
    public static final float PId2 = ((float)Math.PI / 2F);
    private static final float[] ASIN_TABLE = new float[65536];

    public static double incValue(double val, double inc) {
        double one = 1.0 / inc;
        return Math.round(val * one) / one;
    }

    public static float asin(float value)
    {
        return ASIN_TABLE[(int)((double)(value + 1.0F) * 32767.5D) & 65535];
    }

    public static float acos(float value)
    {
        return ((float)Math.PI / 2F) - ASIN_TABLE[(int)((double)(value + 1.0F) * 32767.5D) & 65535];
    }

    public static int getAverage(int[] vals)
    {
        if (vals.length <= 0)
        {
            return 0;
        }
        else
        {
            int i = getSum(vals);
            int j = i / vals.length;
            return j;
        }
    }

    public static int getSum(int[] vals)
    {
        if (vals.length <= 0)
        {
            return 0;
        }
        else
        {
            int i = 0;

            for (int j = 0; j < vals.length; ++j)
            {
                int k = vals[j];
                i += k;
            }

            return i;
        }
    }

    public static int roundDownToPowerOfTwo(int val)
    {
        int i = MathHelper.roundUpToPowerOfTwo(val);
        return val == i ? i : i / 2;
    }

    public static boolean equalsDelta(float f1, float f2, float delta)
    {
        return Math.abs(f1 - f2) <= delta;
    }


    public static float roundToFloat(double d)
    {
        return (float)((double)Math.round(d * 1.0E8D) / 1.0E8D);
    }

    static
    {
        for (int i = 0; i < 65536; ++i)
        {
            ASIN_TABLE[i] = (float)Math.asin((double)i / 32767.5D - 1.0D);
        }

        for (int j = -1; j < 2; ++j)
        {
            ASIN_TABLE[(int)(((double)j + 1.0D) * 32767.5D) & 65535] = (float)Math.asin((double)j);
        }
    }

    public static double lerp(double pct, double start, double end) {
        return start + pct * (end - start);
    }

    public static float lerp(float min, float max, float delta) {
        return min + (max - min) * delta;
    }

    public static double randomizeDouble(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public static double n(double var0, double var2) {
        return Math.min(Math.abs(var0 - var2), Math.min(Math.abs(var0 - 360.0) - Math.abs(var2 - 0.0), Math.abs(var2 - 360.0) - Math.abs(var0 - 0.0)));
    }

    public static float randomizeFloat(float min, float max) {
        return (float) randomizeDouble(min, max);
    }

    public static int randomizeInt(float min, float max) {
        return (int) randomizeDouble2(min, max);
    }
    public static float randomizeFloat2(float min, float max) {
        return (float) randomizeDouble(min, max);
    }

    public static double randomizeDouble2(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public static double interpolate(double old,
                                     double now,
                                     float partialTicks) {
        return old + (now - old) * partialTicks;
    }

    public static double randomNumber(double max, double min) {
        return Math.random() * (max - min) + min;
    }

    public static boolean inBetween(double min, double max, double value) {
        return value >= min && value <= max;
    }
}
