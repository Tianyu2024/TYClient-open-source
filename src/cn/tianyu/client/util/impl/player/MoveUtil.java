package cn.tianyu.client.util.impl.player;

import cn.tianyu.client.Manager.RotationState;
import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.player.EventMove;
import cn.tianyu.client.event.events.world.EventWorldLoad;
import cn.tianyu.client.module.impl.combat.TargetStrafe;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;

import static cn.tianyu.client.TYClient.mc;

public class MoveUtil {
    public static Minecraft mc = Minecraft.getMinecraft();
    public static int inAirTicks, groundTicks, stillTicks;

    public static boolean isForwardPressed() {
        if (MoveUtil.mc.gameSettings.keyBindForward.isKeyDown() != MoveUtil.mc.gameSettings.keyBindBack.isKeyDown())
            return true;
        return MoveUtil.mc.gameSettings.keyBindLeft.isKeyDown() != MoveUtil.mc.gameSettings.keyBindRight.isKeyDown();
    }

    public static int getForwardValue() {
        int forwardValue = 0;
        if (MoveUtil.mc.gameSettings.keyBindForward.isKeyDown()) {
            ++forwardValue;
        }
        if (MoveUtil.mc.gameSettings.keyBindBack.isKeyDown()) {
            --forwardValue;
        }
        return forwardValue;
    }

    public static int getLeftValue() {
        int leftValue = 0;
        if (MoveUtil.mc.gameSettings.keyBindLeft.isKeyDown()) {
            ++leftValue;
        }
        if (MoveUtil.mc.gameSettings.keyBindRight.isKeyDown()) {
            --leftValue;
        }
        return leftValue;
    }

    public static boolean canSprint(final boolean legit) {
        return (legit ? mc.thePlayer.moveForward >= 0.8F
                && !mc.thePlayer.isCollidedHorizontally
                && (mc.thePlayer.getFoodStats().getFoodLevel() > 6 || mc.thePlayer.capabilities.allowFlying)
                && !mc.thePlayer.isPotionActive(Potion.blindness)
                //&& !mc.thePlayer.isUsingItem()
                && !mc.thePlayer.isSneaking()
                : enoughMovementForSprinting());
    }

    public static boolean enoughMovementForSprinting() {
        return Math.abs(mc.thePlayer.moveForward) >= 0.8F || Math.abs(mc.thePlayer.moveStrafing) >= 0.8F;
    }

    public static float adjustYaw(float yaw, float forward, float strafe) {
//        TargetStrafe targetStrafe = (TargetStrafe) TYClient.getModuleManager().getModule(TargetStrafe.class);
//        if (targetStrafe.isEnabled()) {
//            if (!Float.isNaN(targetStrafe.getTargetYaw())) {
//                return targetStrafe.getTargetYaw();
//            }
//        }
        if (forward < 0.0f) {
            yaw += 180.0f;
        }
        if (strafe != 0.0f) {
            float multiplier = forward == 0.0f ? 1.0f : 0.5f * Math.signum(forward);
            yaw += -90.0f * multiplier * Math.signum(strafe);
        }
        return MathHelper.wrapAngleTo180_float(yaw);
    }

    public static float getMoveYaw() {
        return MoveUtil.adjustYaw(RotationState.isActived() ? RotationState.getSmoothedYaw() : MoveUtil.mc.thePlayer.rotationYaw, MoveUtil.mc.thePlayer.movementInput.moveForward, MoveUtil.mc.thePlayer.movementInput.moveStrafe);
    }

    @EventTarget
    public void onWorldLoad(EventWorldLoad e) {
        inAirTicks = 0;
    }

    public static boolean isMoving() {
        return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0F || mc.thePlayer.movementInput.moveStrafe != 0F);
    }
    public static boolean isMoving(EntityLivingBase player) {
        return player != null && (player.moveForward != 0F || player.moveStrafing != 0F);
    }

    public static double getSpeed() {
        return MoveUtil.getSpeed(MoveUtil.mc.thePlayer.motionX, MoveUtil.mc.thePlayer.motionZ);
    }

    public static double getSpeed(double motionX, double motionZ) {
        return Math.hypot(motionX, motionZ);
    }

    public static double getSpeed(EntityPlayer player) {
        return Math.sqrt(player.motionX * player.motionX + player.motionZ * player.motionZ);
    }
    public static double speed() {
        return Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ);
    }

    public static boolean isOnGround(double height) {
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, -height, 0.0D)).isEmpty();
    }



    public static void setMotion2(double d, float f) {
        mc.thePlayer.motionX = -Math.sin(Math.toRadians(f)) * d;
        mc.thePlayer.motionZ = Math.cos(Math.toRadians(f)) * d;
    }
    public static void setMotion(double speed) {
        setMotion(speed, mc.thePlayer.rotationYaw);
    }

    public static void setMotion(double speed, float yaw) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        if ((forward == 0.0D) && (strafe == 0.0D)) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
        } else {
            if (forward != 0.0D) {
                if (strafe > 0.0D) {
                    yaw += (forward > 0.0D ? -45 : 45);
                } else if (strafe < 0.0D) {
                    yaw += (forward > 0.0D ? 45 : -45);
                }
                strafe = 0.0D;
                if (forward > 0.0D) {
                    forward = 1;
                } else if (forward < 0.0D) {
                    forward = -1;
                }
            }
            mc.thePlayer.motionX = forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F));
            mc.thePlayer.motionZ = forward * speed * Math.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F));
        }
    }

    public static float getDirection(final float yaw) {
        return getDirection(yaw, mc.thePlayer.movementInput.moveForward, mc.thePlayer.movementInput.moveStrafe);
    }

    public static double getDirection() {
        float rotationYaw = mc.thePlayer.rotationYaw;
        if (mc.thePlayer.moveForward < 0.0f) {
            rotationYaw += 180.0f;
        }
        float forward = 1.0f;
        if (mc.thePlayer.moveForward < 0.0f) {
            forward = -0.5f;
        } else if (mc.thePlayer.moveForward > 0.0f) {
            forward = 0.5f;
        }
        if (mc.thePlayer.moveStrafing > 0.0f) {
            rotationYaw -= 90.0f * forward;
        }
        if (mc.thePlayer.moveStrafing < 0.0f) {
            rotationYaw += 90.0f * forward;
        }
        return Math.toRadians(rotationYaw);
    }

    public static float getDirection(float yaw, final float forward, final float strafe) {
        if (forward != 0) {
            if (strafe < 0) {
                yaw += forward < 0 ? 135 : 45;
            } else if (strafe > 0) {
                yaw -= forward < 0 ? 135 : 45;
            } else if (strafe == 0 && forward < 0) {
                yaw -= 180;
            }
        } else {
            if (strafe < 0) {
                yaw += 90;
            } else if (strafe > 0) {
                yaw -= 90;
            }
        }

        return yaw;
    }


    public static void strafe(final EventMove moveEvent, final double speed, final double direction) {
        if (!isMoving()) return;
        moveEvent.setX(mc.thePlayer.motionX = -Math.sin(direction) * speed);
        moveEvent.setZ(mc.thePlayer.motionZ = Math.cos(direction) * speed);
    }

    public static double strafe(final double d) {
        if (!isMoving())
            return mc.thePlayer.rotationYaw;

        final double yaw = getDirection1();
        mc.thePlayer.motionX = -MathHelper.sin((float) yaw) * d;
        mc.thePlayer.motionZ = MathHelper.cos((float) yaw) * d;

        return yaw;
    }
    public static void strafe(double speed, float yaw) {
        if (!isMoving()) {
            return;
        }
        yaw = (float)Math.toRadians(yaw);
        mc.thePlayer.motionX = (double)(-MathHelper.sin(yaw)) * speed;
        mc.thePlayer.motionZ = (double)MathHelper.cos(yaw) * speed;
    }

    public static void jump() {
        double jumpY = (double) mc.thePlayer.getJumpUpwardsMotion();

        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            jumpY += (double) ((float) (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F);
        }

        mc.thePlayer.motionY = (mc.thePlayer.motionY = jumpY);
    }

    public static double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }


    public static double getDirection1() {
        float yaw = mc.thePlayer.rotationYaw;

        if (mc.thePlayer.moveForward < 0.0F) {
            yaw += 180F;
        }

        float forward = 1.0F;
        if (mc.thePlayer.moveForward < 0.0F) {
            forward = -0.5F;
        } else if (mc.thePlayer.moveForward > 0.0F) {
            forward = 0.5F;
        }

        if (mc.thePlayer.moveStrafing > 0.0F) {
            yaw -= 90F * forward;
        } else if (mc.thePlayer.moveStrafing < 0.0F) {
            yaw += 90F * forward;
        }

        return Math.toRadians(yaw);
    }

    public static double direction() {
        float rotationYaw = mc.thePlayer.movementYaw;

        if (mc.thePlayer.moveForward < 0) {
            rotationYaw += 180;
        }

        float forward = 1;

        if (mc.thePlayer.moveForward < 0) {
            forward = -0.5F;
        } else if (mc.thePlayer.moveForward > 0) {
            forward = 0.5F;
        }

        if (mc.thePlayer.moveStrafing > 0) {
            rotationYaw -= 70 * forward;
        }

        if (mc.thePlayer.moveStrafing < 0) {
            rotationYaw += 70 * forward;
        }

        return Math.toRadians(rotationYaw);
    }

    public static void stop() {
        mc.thePlayer.motionX = 0;
        mc.thePlayer.motionZ = 0;
    }

    public static void stopXYZ() {
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
    }

    public static void pause(EventMove e) {
        setSpeedEvent(e, 0);
        setSpeed(0);
    }

    public static void setSpeedEvent(EventMove event, double speed) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (forward > 0.0 ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            event.setX(forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f)));
            event.setZ(forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f)));
        }
    }

    public static double getBaseMoveSpeed(EntityPlayer player) {
        var baseSpeed = 0.2873;
        if (player.isPotionActive(Potion.moveSpeed)) {
            int amplifier = player.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= 1.0 + 0.2 * (amplifier + 1);
        }
        return baseSpeed;
    }

    public static void setSpeed(final double speed) {
        mc.thePlayer.motionX = -Math.sin(getDirection2()) * speed;
        mc.thePlayer.motionZ = Math.cos(getDirection2()) * speed;
    }

    public static void setSpeed(double speed, float yaw) {
        MoveUtil.mc.thePlayer.motionX = -Math.sin(Math.toRadians(yaw)) * speed;
        MoveUtil.mc.thePlayer.motionZ = Math.cos(Math.toRadians(yaw)) * speed;
    }

    public static float getDirection2() {
        float yaw = mc.thePlayer.rotationYaw;
        final float forward = mc.thePlayer.moveForward;
        final float strafe = mc.thePlayer.moveStrafing;
        yaw += ((forward < 0.0f) ? 180 : 0);
        if (strafe < 0.0f) {
            yaw += ((forward < 0.0f) ? -45.0f : ((forward == 0.0f) ? 90.0f : 45.0f));
        }
        if (strafe > 0.0f) {
            yaw -= ((forward < 0.0f) ? -45.0f : ((forward == 0.0f) ? 90.0f : 45.0f));
        }
        return yaw * 0.017453292f;
    }

    @EventTarget
    public void onMotion(EventMotion e){
        inAirTicks = mc.thePlayer.onGround ? 0 : ++inAirTicks;
        groundTicks = !mc.thePlayer.onGround ? 0 : ++groundTicks;
        stillTicks = isMoving() ? 0 : ++stillTicks;
    }

    public static float getRawDirection() {
        return getRawDirectionRotation(mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveStrafe, mc.thePlayer.movementInput.moveForward);
    }
    public static float getRawDirectionRotation(float yaw, float pStrafe, float pForward) {
        float rotationYaw = yaw;

        if (pForward < 0F)
            rotationYaw += 180F;

        float forward = 1F;
        if (pForward < 0F)
            forward = -0.5F;
        else if (pForward > 0F)
            forward = 0.5F;

        if (pStrafe > 0F)
            rotationYaw -= 90F * forward;

        if (pStrafe < 0F)
            rotationYaw += 90F * forward;

        return rotationYaw;
    }

    public static int getSpeedLevel() {
        int speedLevel = 0;
        if (MoveUtil.mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            speedLevel = (MoveUtil.mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        }
        return speedLevel;
    }

    public static void fixStrafe(float targetYaw) {
        float angle = MathHelper.wrapAngleTo180_float(MoveUtil.adjustYaw(MoveUtil.mc.thePlayer.rotationYaw, MoveUtil.getForwardValue(), MoveUtil.getLeftValue()) - targetYaw + 22.5f);
        switch ((int) (angle + 180.0f) / 45 % 8) {
            case 0: {
                MoveUtil.mc.thePlayer.movementInput.moveForward = -1.0f;
                MoveUtil.mc.thePlayer.movementInput.moveStrafe = 0.0f;
                break;
            }
            case 1: {
                MoveUtil.mc.thePlayer.movementInput.moveForward = -1.0f;
                MoveUtil.mc.thePlayer.movementInput.moveStrafe = 1.0f;
                break;
            }
            case 2: {
                MoveUtil.mc.thePlayer.movementInput.moveForward = 0.0f;
                MoveUtil.mc.thePlayer.movementInput.moveStrafe = 1.0f;
                break;
            }
            case 3: {
                MoveUtil.mc.thePlayer.movementInput.moveForward = 1.0f;
                MoveUtil.mc.thePlayer.movementInput.moveStrafe = 1.0f;
                break;
            }
            case 4: {
                MoveUtil.mc.thePlayer.movementInput.moveForward = 1.0f;
                MoveUtil.mc.thePlayer.movementInput.moveStrafe = 0.0f;
                break;
            }
            case 5: {
                MoveUtil.mc.thePlayer.movementInput.moveForward = 1.0f;
                MoveUtil.mc.thePlayer.movementInput.moveStrafe = -1.0f;
                break;
            }
            case 6: {
                MoveUtil.mc.thePlayer.movementInput.moveForward = 0.0f;
                MoveUtil.mc.thePlayer.movementInput.moveStrafe = -1.0f;
                break;
            }
            case 7: {
                MoveUtil.mc.thePlayer.movementInput.moveForward = -1.0f;
                MoveUtil.mc.thePlayer.movementInput.moveStrafe = -1.0f;
                break;
            }
        }
        if (MoveUtil.mc.thePlayer.movementInput.sneak) {
            MoveUtil.mc.thePlayer.movementInput.moveForward *= 0.3f;
            MoveUtil.mc.thePlayer.movementInput.moveStrafe *= 0.3f;
        }
    }
}
