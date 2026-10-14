package cn.tianyu.client.util.impl.client;

import java.security.SecureRandom;

/**
 * @Author: CN_tianyu
 */
public class RandomUtils {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static int nextInt(int min, int max) {
        return (int) nextDouble(min, max);
    }

    public static float nextFloat(float min, float max) {
        return (float) nextDouble(min, max);
    }

    public static double nextDouble(double min, double max) {
        return max + (min - max) * secureRandom.nextDouble();
    }

    public static long nextLong(long min, long max) {
        return (long) nextDouble((double) min, (double) (max + 1L));
    }

}
