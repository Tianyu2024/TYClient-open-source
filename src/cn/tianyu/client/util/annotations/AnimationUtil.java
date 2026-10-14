package cn.tianyu.client.util.annotations;

import cn.tianyu.client.TYClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3i;

public class AnimationUtil {
    public static long deltaTime = 500L;

    public static float calculateCompensation(final float target, float current, long delta, final int speed) {
        final float diff = current - target;
        if (delta < 1L) {
            delta = 1L;
        }
        double v = (speed * delta / 16L < 0.25) ? 0.5 : (speed * delta / 16L);
        if (diff > speed) {
            current -= (float) v;
            if (current < target) {
                current = target;
            }
        } else if (diff < -speed) {
            current += (float) v;
            if (current > target) {
                current = target;
            }
        } else {
            current = target;
        }
        return current;
    }

    public static float clamp(final float number, final float min, final float max) {
        return number < min ? min : Math.min(number, max);
    }

    public double animateNoFast(double value, double target, double speed) {
        if (deltaTime <= 1L) {
            deltaTime = 500L;
        }
        speed = speed * (Double)4.0 / 4.0;
        double increment = speed * (double)deltaTime / 500.0;
        double returnValue = value + (target - value) * increment / 200.0;
        if (Math.abs(target - returnValue) < 0.05 * (4.0 / (Double)4.0)) {
            return target;
        }
        return returnValue;
    }

    public static float animate(float value, float target, float speed) {
        float increment;
        float returnValue;
        if (deltaTime <= 1L) {
            deltaTime = 500L;
        }
        if ((double)Math.abs(target - (returnValue = value + (target - value) * (increment = (speed *= 70.0f) * (float)deltaTime / 500.0f) / 200.0f)) < 0.05 * (4.0 / (Double)4.0)) {
            return target;
        }
        return returnValue;
    }
    public static float animateSmooth(float current, float target, float speed) {
        boolean larger;
        if (current == target) {
            return current;
        }
        boolean bl = larger = target > current;
        if (speed < 0.0f) {
            speed = 0.0f;
        } else if (speed > 1.0f) {
            speed = 1.0f;
        }
        double dif = Math.max((double)target, (double)current) - Math.min((double)target, (double)current);
        double factor = dif * (double)speed;
        if (factor < 0.1) {
            factor = 0.1;
        }
        if (larger) {
            if ((current += (float)factor) >= target) {
                current = target;
            }
        } else if (target < current && (current -= (float)factor) <= target) {
            current = target;
        }
        return current;
    }

    public static float getAnimationStateFlux(float animation, final float finalState, final float speed) {
        final float add = (float) (deltaTime * (speed / 1000f));
        if (animation < finalState) {
            if (animation + add < finalState) {
                animation += add;
            } else {
                animation = finalState;
            }
        } else if (animation - add > finalState) {
            animation -= add;
        } else {
            animation = finalState;
        }
        return animation;
    }

    public static float processFPS(final float defV) {
        final float defF = 1000;
        int limitFPS = Math.abs(TYClient.mc.getDebugFPS());
        return defV / (limitFPS <= 0 ? 1 : limitFPS / defF);
    }

    public static float moveUDFaster(final float current, final float end) {
        float smoothSpeed = processFPS(0.025F);
        float minSpeed = processFPS(0.023F);
        final boolean larger = end > current;
        if (smoothSpeed < 0.0f) {
            smoothSpeed = 0.0f;
        } else if (smoothSpeed > 1.0f) {
            smoothSpeed = 1.0f;
        }
        if (minSpeed < 0.0f) {
            minSpeed = 0.0f;
        } else if (minSpeed > 1.0f) {
            minSpeed = 1.0f;
        }
        float movement = (end - current) * smoothSpeed;
        if (movement > 0) {
            movement = Math.max(minSpeed, movement);
            movement = Math.min(end - current, movement);
        } else if (movement < 0) {
            movement = Math.min(-minSpeed, movement);
            movement = Math.max(end - current, movement);
        }
        if (larger) {
            if (end <= current + movement) {
                return end;
            }
        } else {
            if (end >= current + movement) {
                return end;
            }
        }
        return current + movement;
    }
}
