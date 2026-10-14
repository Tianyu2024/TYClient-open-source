package cn.tianyu.client.util.impl.player;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

/**
 * @Author: CN_tianyu
 */
public class MovementUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void strafe(float speed) {
        if (!isMoving()) return;

        float yaw = getDirection();
        mc.thePlayer.motionX = -MathHelper.sin(yaw) * speed;
        mc.thePlayer.motionZ = MathHelper.cos(yaw) * speed;
    }

    public static float getDirection() {
        float yaw = mc.thePlayer.rotationYaw;
        if (mc.thePlayer.moveForward < 0) {
            yaw += 180;
        }

        float forward = Math.abs(mc.thePlayer.moveForward);
        if (forward < 0.3f) forward = 0;

        if (mc.thePlayer.moveStrafing > 0) {
            yaw -= 90 * forward;
        } else if (mc.thePlayer.moveStrafing < 0) {
            yaw += 90 * forward;
        }

        return yaw * (float) Math.PI / 180;
    }

    public static boolean isMoving() {
        return mc.thePlayer != null &&
                (mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0);
    }
}
