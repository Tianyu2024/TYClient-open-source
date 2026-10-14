package cn.tianyu.client.module.impl.player;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.movement.SafeWalk;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

/**
 * @Author: CN_tianyu
 */
public class ScaffoldAim extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final BooleanValue setLook = new BooleanValue("set-look", true);
    public final BooleanValue onSneak = new BooleanValue("on-sneak", true);
    public final BooleanValue workWithSafeWalk = new BooleanValue("work-with-safewalk", false);
    public final NumberValue waitFor = new NumberValue("wait-time", 150, 0, 5000, 25);
    public final NumberValue glideTime = new NumberValue("glide-speed", 500, 1, 201, 5);
    public final ModeValue assistMode = new ModeValue("assist-mode", "GodBridge", "GODBRIDGE", "MOONWALK", "BREEZILY", "NORMAL");
    public final NumberValue assistRange = new NumberValue("assist-range", 18.0D, 1.0D, 40.0D, 1.0D);

    private boolean waitingForAim;
    private boolean gliding;
    private long startWaitTime;
    private final float[] godbridgePos = {75.6f, -315, -225, -135, -45, 0, 45, 135, 225, 315};
    private final float[] moonwalkPos = {79.6f, -340, -290, -250, -200, -160, -110, -70, -20, 0, 20, 70, 110, 160, 200, 250, 290, 340};
    private final float[] breezilyPos = {79.9f, -360, -270, -180, -90, 0, 90, 180, 270, 360};
    private final float[] normalPos = {78f, -315, -225, -135, -45, 0, 45, 135, 225, 315};
    private double speedYaw;
    private double speedPitch;
    private float waitingForYaw;
    private float waitingForPitch;

    public ScaffoldAim() {
        super("ScaffoldAim", "自动瞄准方块", Category.player);
    }

    @Override
    public void onEnable() {
        waitingForAim = false;
        gliding = false;
        speedYaw = 0.0D;
        speedPitch = 0.0D;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        waitingForAim = false;
        gliding = false;
        speedYaw = 0.0D;
        speedPitch = 0.0D;
    }

    public static boolean isPlayerInGame() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    public static boolean playerOverAir() {
        if (!isPlayerInGame()) {
            return false;
        }

        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;

        BlockPos pos1 = new BlockPos(
                MathHelper.floor_double(x),
                MathHelper.floor_double(y - 1.0D),
                MathHelper.floor_double(z)
        );

        BlockPos pos2 = new BlockPos(
                MathHelper.floor_double(x),
                MathHelper.floor_double(y - 2.0D),
                MathHelper.floor_double(z)
        );

        return mc.theWorld.isAirBlock(pos1) || mc.theWorld.isAirBlock(pos2);
    }

    @EventTarget
    public void onRenderTick(EventRender2D event) {
        setSuffix(assistMode.getValue());

        if (!isPlayerInGame()) {
            return;
        }

        EntityPlayerSP player = mc.thePlayer;
        ItemStack held = player.getHeldItem();

        if (held == null) {
            return;
        }

        Item item = held.getItem();

        if (item instanceof ItemFishingRod) {
            return;
        }

        if (!mc.gameSettings.keyBindUseItem.isKeyDown()) {
            waitingForAim = false;
            gliding = false;
            return;
        }

        Module safeWalk = TYClient.moduleManager.getModule(SafeWalk.class);

        if (safeWalk != null && safeWalk.isEnabled() && !workWithSafeWalk.getValue()) {
            return;
        }

        if (!playerOverAir()) {
            waitingForAim = false;
            gliding = false;
            return;
        }

        if (onSneak.getValue() && !player.isSneaking()) {
            waitingForAim = false;
            gliding = false;
            return;
        }

        if (gliding) {
            updateRotation(player);

            if (Math.abs(wrapAngleTo180(player.rotationYaw) - wrapAngleTo180(waitingForYaw)) < 0.5F
                    && Math.abs(player.rotationPitch - waitingForPitch) < 0.5F) {
                player.rotationYaw = waitingForYaw;
                player.rotationPitch = waitingForPitch;
                gliding = false;
            }
        }

        if (gliding) {
            return;
        }

        float yaw = wrapAngleTo360(player.rotationYaw);
        float pitch = player.rotationPitch;
        float range = assistRange.getValue().floatValue();

        boolean found = false;

        switch (assistMode.getValue()) {
            case "GODBRIDGE":
                found = findAim(godbridgePos, pitch, yaw, range, player);
                break;

            case "MOONWALK":
                found = findAim(moonwalkPos, pitch, yaw, range, player);
                break;

            case "BREEZILY":
                found = findAim(breezilyPos, pitch, yaw, range, player);
                break;

            case "NORMAL":
                found = findAim(normalPos, pitch, yaw, range, player);
                break;
        }

        if (!found) {
            if (!waitingForAim) {
                waitingForAim = true;
                startWaitTime = System.currentTimeMillis();
            }

            if (System.currentTimeMillis() - startWaitTime >= waitFor.getValue().longValue()) {
                waitingForAim = false;
            }
        } else {
            waitingForAim = false;
        }
    }

    private boolean findAim(float[] positions, float pitch, float yaw, float range, EntityPlayerSP player) {
        if (positions.length == 0) {
            return false;
        }

        float targetPitch = positions[0];

        if (!isAngleInRange(pitch, targetPitch, range)) {
            return false;
        }

        for (int i = 1; i < positions.length; i++) {
            float targetYaw = positions[i];

            if (isAngleInRange(yaw, targetYaw, range)) {
                aimAt(targetPitch, targetYaw, player.rotationYaw, player.rotationPitch);
                return true;
            }
        }

        return false;
    }

    private boolean isAngleInRange(float current, float target, float range) {
        return Math.abs(wrapAngleTo180(current - target)) <= range;
    }

    private void aimAt(float pitch, float yaw, float currentYaw, float currentPitch) {
        if (!setLook.getValue()) {
            return;
        }

        this.waitingForYaw = yaw;
        this.waitingForPitch = pitch + getRotationBase(currentPitch);
        this.speedYaw = Math.max(0.1D, glideTime.getValue().doubleValue() / 100.0D);
        this.speedPitch = Math.max(0.1D, glideTime.getValue().doubleValue() / 100.0D);
        this.gliding = true;
        this.waitingForAim = false;
    }

    private void updateRotation(EntityPlayerSP player) {
        float currentYaw = player.rotationYaw;
        float currentPitch = player.rotationPitch;

        float yawDifference = wrapAngleTo180(waitingForYaw - currentYaw);
        float pitchDifference = waitingForPitch - currentPitch;

        if (Math.abs(yawDifference) > speedYaw) {
            player.rotationYaw += yawDifference > 0.0F ? speedYaw : -speedYaw;
        } else {
            player.rotationYaw = waitingForYaw;
        }

        if (Math.abs(pitchDifference) > speedPitch) {
            player.rotationPitch += pitchDifference > 0.0F ? speedPitch : -speedPitch;
        } else {
            player.rotationPitch = waitingForPitch;
        }

        player.rotationPitch = MathHelper.clamp_float(player.rotationPitch, -90.0F, 90.0F);
    }

    private float getRotationBase(float pitch) {
        return (float) (Math.floor((pitch + 180.0F) / 360.0F) * 360.0F);
    }

    private float wrapAngleTo180(float angle) {
        angle %= 360.0F;

        if (angle >= 180.0F) {
            angle -= 360.0F;
        }

        if (angle < -180.0F) {
            angle += 360.0F;
        }

        return angle;
    }

    private float wrapAngleTo360(float angle) {
        angle %= 360.0F;

        if (angle < 0.0F) {
            angle += 360.0F;
        }

        return angle;
    }
}