package cn.tianyu.client.Manager;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventPriority;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.player.EventJump;
import cn.tianyu.client.event.events.player.EventMoveInput;
import cn.tianyu.client.event.events.player.EventStrafe;
import cn.tianyu.client.util.impl.client.MathUtils;
import cn.tianyu.client.util.impl.player.EventLook;
import cn.tianyu.client.util.impl.player.Rotation;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MathHelper;
import org.lwjglx.util.vector.Vector2f;

import static cn.tianyu.client.util.impl.player.MoveUtil.direction;

public class RotationManager {
    private final Minecraft mc = Minecraft.getMinecraft();

    public Vector2f rotation;
    public Vector2f targetRotation;
    public Vector2f lastServerRotation;

    public Vector2f currentRotation = null;
    public Vector2f serverRotation = new Vector2f(0, 0);

    private float rotationSpeed;
    private boolean enabled;
    private boolean movementFix;
    private boolean strict;
    private boolean smoothlyReset;
    private int keepLength;

    public RotationManager() {
        this.rotation = new Vector2f(0, 0);
        this.targetRotation = new Vector2f(0, 0);
        this.lastServerRotation = new Vector2f(0, 0);
        this.serverRotation = new Vector2f(0, 0);
    }

    public Vector2f getRotation() {
        return currentRotation != null ? currentRotation : rotation;
    }

    public boolean shouldRotate() {
        return enabled && (currentRotation != null || rotation != null);
    }

    public void setRotation(Vector2f rotation, float rotationSpeed, boolean movementFix, boolean strict) {
        Vector2f fixed = applyGCDFix(serverRotation, new Vector2f(rotation.getX(), rotation.getY()));

        this.currentRotation = fixed;
        this.rotation = fixed;
        this.targetRotation = new Vector2f(rotation.getX(), rotation.getY());

        this.rotationSpeed = Math.max(1, Math.min(360, rotationSpeed));
        this.movementFix = movementFix;
        this.strict = strict;
        this.smoothlyReset = false;
        this.enabled = true;
        this.keepLength = 0;
    }

    public void setRotation(Vector2f rotation, float rotationSpeed, boolean movementFix, boolean strict, int keepLength) {
        Vector2f fixed = applyGCDFix(serverRotation, new Vector2f(rotation.getX(), rotation.getY()));

        this.currentRotation = fixed;
        this.rotation = fixed;
        this.targetRotation = new Vector2f(rotation.getX(), rotation.getY());

        this.rotationSpeed = Math.max(1, Math.min(360, rotationSpeed));
        this.movementFix = movementFix;
        this.strict = strict;
        this.smoothlyReset = false;
        this.enabled = true;
        this.keepLength = keepLength;
    }

    public void setRotation(Vector2f rotation, float rotationSpeed, boolean movementFix) {
        setRotation(rotation, rotationSpeed, movementFix, false);
    }

    public void setRotation(Rotation rotation, float rotationSpeed, boolean movementFix) {
        setRotation(rotation.toVec2f(), rotationSpeed, movementFix, false);
    }

    public void resetRotation() {
        this.keepLength = 0;
        this.enabled = false;
        this.currentRotation = null;
        this.movementFix = false;
        this.smoothlyReset = false;
    }

    public void resetSmoothly() {
        this.smoothlyReset = true;
        this.enabled = false;
        if (this.rotationSpeed == 0) {
            this.rotationSpeed = 180;
        }
    }

    public double getRotationDifference(final Rotation rotation) {
        return serverRotation == null ? 0D : getRotationDifference(rotation, serverRotation);
    }

    public float getAngleDifference(final float a, final float b) {
        return ((((a - b) % 360F) + 540F) % 360F) - 180F;
    }

    public double getRotationDifference(final Rotation a, final Vector2f b) {
        return Math.hypot(getAngleDifference(a.getYaw(), b.getX()), a.getPitch() - b.getY());
    }

    public boolean isModifying() {
        return enabled;
    }

    public boolean isMovementFix() {
        return movementFix;
    }

    @EventTarget
    @EventPriority(8888)
    public void onTick(EventTick event) {
        if (enabled && currentRotation != null) {
            keepLength--;
            if (keepLength <= 0) {
                enabled = false;
            }
        }
    }

    @EventTarget
    @EventPriority(8888)
    public void onUpdate(EventUpdate event) {
        if (!enabled && currentRotation != null) {
            float distance = getRotationDifference(currentRotation, new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch));

            if (!smoothlyReset || distance < 1) {
                resetRotation();
                return;
            }

            if (distance > 0) {
                currentRotation = smooth(
                        currentRotation != null ? currentRotation : serverRotation,
                        new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch),
                        rotationSpeed
                );
                rotation = currentRotation;
            }
        }
    }

    @EventTarget
    @EventPriority(8888)
    public void onMotion(EventMotion event) {
        if ((event.isPost() || !smoothlyReset) && currentRotation != null) {
            float distance = getRotationDifference(currentRotation, new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch));

            if (!enabled) {
                if (!smoothlyReset || distance < 1) {
                    resetRotation();
                    return;
                }

                if (distance > 0) {
                    currentRotation = smooth(
                            currentRotation != null ? currentRotation : serverRotation,
                            new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch),
                            rotationSpeed
                    );
                    rotation = currentRotation;
                }
            }
        }

        if (event.isPre() && currentRotation != null && enabled) {
            float yaw = currentRotation.getX();
            float pitch = MathHelper.clamp_float(currentRotation.getY(), -90, 90);

            event.setYaw(yaw);
            event.setPitch(pitch);

            mc.thePlayer.rotationYawHead = yaw;
            mc.thePlayer.renderYawOffset = yaw;
        }
    }

    @EventTarget
    @EventPriority(8888)
    public void onPacket(EventPacket event) {
        if (event.getEventType() != EventPacket.EventState.SEND) return;

        if (event.getPacket() instanceof C03PacketPlayer) {
            C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();

            if (currentRotation != null && (currentRotation.getX() != serverRotation.getX() || currentRotation.getY() != serverRotation.getY())) {
                packet.yaw = currentRotation.getX();
                packet.pitch = currentRotation.getY();
                packet.rotating = true;
            }

            if (packet.rotating) {
                serverRotation = new Vector2f(packet.yaw, packet.pitch);
                lastServerRotation = serverRotation;
            }
        }
    }

    @EventTarget
    @EventPriority(8888)
    public void onLook(EventLook event) {
        if (shouldRotate() && currentRotation != null) {
            event.setRotation(currentRotation);
        }
    }

    @EventTarget
    @EventPriority(8888)
    public void onMoveInput(EventMoveInput event) {
        if (!shouldRotate() || !movementFix) return;
        fixMovement(event, currentRotation.getX());
    }

    @EventTarget
    @EventPriority(8888)
    public void onStrafe(EventStrafe event) {
        if (!shouldRotate() || movementFix == false) return;
        event.setYaw(currentRotation.getX());
    }

    @EventTarget
    @EventPriority(8888)
    public void onJump(EventJump event) {
        if (shouldRotate() && movementFix) {
            event.setYaw(currentRotation.getX());
        }
    }

    public static void fixMovement(EventMoveInput moveInputEvent, float var1) {
        float f = moveInputEvent.getForward();
        float strafe = moveInputEvent.getStrafe();
        double d0 = MathHelper.wrapAngleTo180_double(Math.toDegrees(direction(TYClient.mc.thePlayer.rotationYaw, f, strafe)));
        if (f != 0.0F || strafe != 0.0F) {
            float f2 = 0.0F;
            float f3 = 0.0F;
            float f4 = Float.MAX_VALUE;

            for (float f5 = -1.0F; f5 <= 1.0F; f5++) {
                for (float f6 = -1.0F; f6 <= 1.0F; f6++) {
                    if (f6 != 0.0F || f5 != 0.0F) {
                        double d1 = MathHelper.wrapAngleTo180_double(Math.toDegrees(direction(var1, f5, f6)));
                        double d2 = MathUtils.n(d0, d1);
                        if (d2 < f4) {
                            f4 = (float)d2;
                            f2 = f5;
                            f3 = f6;
                        }
                    }
                }
            }

            if (f3 == -1.0F) {
            } else {
            }

            moveInputEvent.setForward(f2);
            moveInputEvent.setStrafe(f3);
        }
    }

    private Vector2f smooth(Vector2f from, Vector2f to, float speed) {
        speed /= 180;

        float yawDiff = MathHelper.wrapAngleTo180_float(to.getX() - from.getX());
        float pitchDiff = to.getY() - from.getY();

        float yaw = from.getX() + yawDiff * speed;
        float pitch = from.getY() + pitchDiff * speed;

        yaw = fixGCD(yaw, from.getX());
        pitch = MathHelper.clamp_float(fixGCD(pitch, from.getY()), -90, 90);

        return new Vector2f(yaw, pitch);
    }

    private Vector2f applyGCDFix(Vector2f prevRotation, Vector2f currentRotation) {
        float sens = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        double gcd = sens * sens * sens * 8.0f * 0.15d;
        float yaw = prevRotation.getX() + (float) (Math.round((currentRotation.getX() - prevRotation.getX()) / gcd) * gcd);
        float pitch = prevRotation.getY() + (float) (Math.round((currentRotation.getY() - prevRotation.getY()) / gcd) * gcd);
        return new Vector2f(yaw, pitch);
    }

    private float fixGCD(float target, float previous) {
        float sens = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        float multiplier = sens * sens * sens * 1.2f;
        float diff = target - previous;
        return previous + (float) (Math.round(diff / multiplier) * multiplier);
    }

    private float getRotationDifference(Vector2f a, Vector2f b) {
        return (float) Math.hypot(
                Math.abs(getAngleDifference(a.getX(), b.getX())),
                Math.abs(a.getY() - b.getY())
        );
    }
}