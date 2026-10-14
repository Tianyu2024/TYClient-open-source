package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.gui.notification.NotificationManager;
import cn.tianyu.client.gui.notification.NotificationType;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

/**
 * @Author: CN_tianyu
 */
public class Speed extends Module {
    private final ModeValue mode = new ModeValue("Mode", "Vanilla", new String[]{"Vanilla", "Secure"});
    private final NumberValue vanillaSpeed = new NumberValue("Speed", 1.0, 0.1, 5.0, 0.1);
    private final BooleanValue flagcheek = new BooleanValue("Flag Cheek", true);
    private final BooleanValue sb = new BooleanValue("sb", true);

    public Speed() {
        super("Speed", "Bhop", Category.movement);
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        if (!isMoving())
            return;

        if (mode.getValue().equals("Vanilla")) {
            setSpeed(vanillaSpeed.getValue() / 4.0F);
            if (mc.thePlayer.onGround) {
                //mc.thePlayer.jump();
            }
        }

        if (mode.getValue().equals("Secure")) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
                setSpeed(0.28F);
            } else {
                setSpeed(0.31F);
                if (mc.thePlayer.motionY > 0) {
                    mc.thePlayer.motionY *= 0.96F;
                }
            }
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof S08PacketPlayerPosLook && flagcheek.getValue()) {
            NotificationManager.post(NotificationType.WARNING, "Flag Detector",
                    "Speed disabled due to " +
                            (mc.thePlayer == null || mc.thePlayer.ticksExisted < 5
                                    ? "world change"
                                    : "lagback"), 1.5F);
            this.toggle();
        }
    }

    private boolean isMoving() {
        return mc.thePlayer.movementInput.moveForward != 0.0F || mc.thePlayer.movementInput.moveStrafe != 0.0F;
    }

    private void setSpeed(double speed) {
        float yaw = mc.thePlayer.rotationYaw;
        float forward = mc.thePlayer.movementInput.moveForward;
        float strafe = mc.thePlayer.movementInput.moveStrafe;

        if (forward != 0.0F && strafe != 0.0F) {
            speed *= Math.sin(Math.PI / 4);
        }

        double motX = -Math.sin(Math.toRadians(yaw)) * speed;
        double motZ = Math.cos(Math.toRadians(yaw)) * speed;

        if (forward != 0.0F) {
            mc.thePlayer.motionX = motX * forward;
            mc.thePlayer.motionZ = motZ * forward;
        }

        if (strafe != 0.0F) {
            mc.thePlayer.motionX = motX * strafe * -1;
            mc.thePlayer.motionZ = motZ * strafe;
        }
    }
}