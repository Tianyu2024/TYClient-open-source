package cn.tianyu.client.TianyuOptiFine.betterfps.impl;

/**
 * @Author CN_tianyu
 */
public final class FastMath {

    /* ===========================
     *      Trigonometry
     * =========================== */

    private static final int SIN_BITS = 16;
    private static final int SIN_COUNT = 1 << SIN_BITS;
    private static final int SIN_MASK = SIN_COUNT - 1;

    private static final float PI = (float) Math.PI;
    private static final float HALF_PI = PI * 0.5f;
    private static final float TWO_PI = PI * 2.0f;

    private static final float RAD_TO_INDEX = SIN_COUNT / TWO_PI;

    private static final float[] SIN_TABLE = new float[SIN_COUNT];

    static {
        for (int i = 0; i < SIN_COUNT; i++) {
            SIN_TABLE[i] = (float) Math.sin(i * TWO_PI / SIN_COUNT);
        }

        // 精确修正四个特殊角
        SIN_TABLE[0] = 0.0f;
        SIN_TABLE[SIN_COUNT / 4] = 1.0f;
        SIN_TABLE[SIN_COUNT / 2] = 0.0f;
        SIN_TABLE[(SIN_COUNT * 3) / 4] = -1.0f;
    }

    /**
     * 高精度快速 sin
     */
    public static float sin(float rad) {

        float index = rad * RAD_TO_INDEX;

        int base = (int) index;

        if (index < base) {
            base--;
        }

        float frac = index - base;

        int idx0 = base & SIN_MASK;
        int idx1 = (base + 1) & SIN_MASK;

        float s0 = SIN_TABLE[idx0];
        float s1 = SIN_TABLE[idx1];

        return s0 + (s1 - s0) * frac;
    }

    /**
     * 高精度快速 cos
     */
    public static float cos(float rad) {
        return sin(rad + HALF_PI);
    }

    /**
     * double版本
     */
    public static double sin(double rad) {
        return sin((float) rad);
    }

    /**
     * double版本
     */
    public static double cos(double rad) {
        return cos((float) rad);
    }

    /* ===========================
     *      Fast sqrt
     * =========================== */

    private static final float THREE_HALVES = 1.5f;
    private static final int IEEE_MAGIC = 0x5f375a86;

    /**
     * 快速平方根
     */
    public static float sqrt(float x) {
        return x * invSqrt(x);
    }

    /**
     * 快速反平方根
     */
    public static float invSqrt(float x) {

        if (x <= 0.0f)
            return 0.0f;

        float xHalf = 0.5f * x;

        int i = Float.floatToIntBits(x);
        i = IEEE_MAGIC - (i >> 1);

        x = Float.intBitsToFloat(i);

        // 两次牛顿迭代，精度明显提高
        x *= THREE_HALVES - xHalf * x * x;
        x *= THREE_HALVES - xHalf * x * x;

        return x;
    }

    /* ===========================
     *      atan2
     * =========================== */

    public static float atan2(float y, float x) {

        if (x == 0.0f) {

            if (y > 0.0f)
                return HALF_PI;

            if (y < 0.0f)
                return -HALF_PI;

            return 0.0f;
        }

        float absY = Math.abs(y) + 1.0e-10f;

        float angle;

        if (x >= 0.0f) {

            float r = (x - absY) / (x + absY);
            angle = 0.78539816339f;
            angle += (0.1963f * r * r - 0.9817f) * r;

        } else {

            float r = (x + absY) / (absY - x);
            angle = 2.35619449019f;
            angle += (0.1963f * r * r - 0.9817f) * r;
        }

        return y < 0 ? -angle : angle;
    }

    /* ===========================
     *      Utils
     * =========================== */

    public static int floor(float value) {

        int i = (int) value;

        return value < i ? i - 1 : i;
    }

    public static int ceil(float value) {

        int i = (int) value;

        return value > i ? i + 1 : i;
    }

    public static float toRadians(float degrees) {
        return degrees * 0.01745329251994329576923690768489f;
    }

    public static float toDegrees(float radians) {
        return radians * 57.295779513082320876798154814105f;
    }

    /**
     * 限制角度到[-PI, PI]
     */
    public static float wrapRadians(float rad) {

        rad %= TWO_PI;

        if (rad > PI)
            rad -= TWO_PI;

        if (rad < -PI)
            rad += TWO_PI;

        return rad;
    }

    /**
     * 限制角度到[0,360)
     */
    public static float wrapDegrees(float deg) {

        deg %= 360.0f;

        if (deg < 0)
            deg += 360.0f;

        return deg;
    }

    private FastMath() {
    }

}