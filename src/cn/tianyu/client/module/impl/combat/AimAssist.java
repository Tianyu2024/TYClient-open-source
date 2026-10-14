package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.gui.notification.Notification;
import cn.tianyu.client.gui.notification.NotificationManager;
import cn.tianyu.client.gui.notification.NotificationType;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.misc.AntiBot;
import cn.tianyu.client.module.impl.misc.Teams;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import com.yumegod.obfuscation.Native;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.MathHelper;
import org.lwjglx.util.vector.Vector2f;

import java.util.Objects;
import java.util.Random;

/**
 * @Author: CN_tianyu
 */
@Native
public class AimAssist extends Module {
    public AimAssist() {
        super("AimAssist", "自瞄", Category.combat);
    }

    public static ModeValue mode = new ModeValue("Mode", "normal" ,"normal", "silent");
    public static NumberValue speed = new NumberValue("Speed", 15.0, 1.0, 100.0, 1.0);
    public static NumberValue fov = new NumberValue("FOV", 90.0, 15.0, 380.0, 1.0);
    public static NumberValue range = new NumberValue("Range", 4.5, 3.0, 6.0, 0.1);
    public static BooleanValue clickAim = new BooleanValue("Click", true);
    public static BooleanValue weaponOnly = new BooleanValue("Weapon only", false);
    public static BooleanValue legitMode = new BooleanValue("Legit Mode", true);
    public static BooleanValue stopbreak = new BooleanValue("stop break", true);
    //public static BooleanValue kkcmode = new BooleanValue("kkcmode", false);


    private final Random random = new Random();
    private int lastTargetId = -1;
    private double randomOffsetX = 0;
    private double randomOffsetY = 0;
    private double randomOffsetZ = 0;
    private int ticksSinceTargetChange = 0;

    private float updateRotation(float current, float target, float baseSpeed) {
        float diff = MathHelper.wrapAngleTo180_float(target - current);

        if (legitMode.getValue()) {
            // 引入更自然的随机高斯抖动
            float jitter = (float) (random.nextGaussian() * (baseSpeed * 0.12f));
            baseSpeed = Math.max(0.3f, baseSpeed + jitter);

            if (Math.abs(diff) < 1.0f && random.nextFloat() < 0.1f) {
                diff += (diff > 0 ? 0.15f : -0.15f);
            }
        }

        if (diff > baseSpeed) diff = baseSpeed;
        if (diff < -baseSpeed) diff = -baseSpeed;

        float nextRotation = current + diff;

        if (legitMode.getValue()) {
            // 精确还原游戏原生的鼠标移动最大公约数(GCD)过滤，防止因浮点数不合规范被拦截
            float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float f1 = f * f * f * 8.0F;
            float f2 = f1 * 0.15F;
            nextRotation = current + Math.round((nextRotation - current) / f2) * f2;
        }

        return nextRotation;
    }

    @Native
    public float[] getRotationsToHead(final Entity target) {
        if (target == null) return null;

        if (target.getEntityId() != lastTargetId || ticksSinceTargetChange % 6 == 0) {
            if (legitMode.getValue()) {
                // 动态根据距离缩放偏移量，防止近距离时准星偏移过大显得极其不自然
                double distance = mc.thePlayer.getDistanceToEntity(target);
                double scale = distance > 2.0 ? 0.12 : 0.05;
                randomOffsetX = (random.nextDouble() - 0.5) * scale;
                randomOffsetY = (random.nextDouble() - 0.4) * scale;
                randomOffsetZ = (random.nextDouble() - 0.5) * scale;
            } else {
                randomOffsetX = 0;
                randomOffsetY = 0;
                randomOffsetZ = 0;
            }
            lastTargetId = target.getEntityId();
        }

        final double diffX = (target.posX + randomOffsetX) - mc.thePlayer.posX;
        final double diffZ = (target.posZ + randomOffsetZ) - mc.thePlayer.posZ;
        double diffY;

        if (target instanceof EntityLivingBase) {
            final EntityLivingBase living = (EntityLivingBase) target;
            // 预测修复：计算对方和自己的相对动态高度差
            double targetY = living.posY + living.getEyeHeight() - 0.1 + randomOffsetY;
            diffY = targetY - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        } else {
            diffY = (target.getEntityBoundingBox().minY + target.getEntityBoundingBox().maxY) / 2.0 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        }

        final double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0 / Math.PI));

        return new float[]{ yaw, MathHelper.clamp_float(pitch, -90.0f, 90.0f) };
    }

    public boolean isInFov(final Entity entity, float fovDeg) {
        float[] rots = getRotationsToHead(entity);
        if (rots == null) return false;

        float yawDiff = MathHelper.wrapAngleTo180_float(rots[0] - mc.thePlayer.rotationYaw);
        float pitchDiff = MathHelper.wrapAngleTo180_float(rots[1] - mc.thePlayer.rotationPitch);

        return Math.abs(yawDiff) <= fovDeg * 0.5 && Math.abs(pitchDiff) <= fovDeg * 0.5;
    }

    @Native
    @EventTarget
    @SuppressWarnings("unused")
    public void onMotion(EventMotion event) {
        setSuffix(String.format("Speed:%s Fov:%s", speed.getValue(), fov.getValue()));

        if (!isEnable()) return;

        if (event.getEventState() != EventMotion.EventState.PRE) return;

        ticksSinceTargetChange++;

        if (weaponOnly.getValue()) {
            if (mc.thePlayer.getCurrentEquippedItem() == null) return;
            if (!(mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword) &&
                    !(mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemAxe)) {
                return;
            }
        }

        if (clickAim.getValue() && !mc.gameSettings.keyBindAttack.isKeyDown()) {
            return;
        }

        final Entity target = this.getTarget();
        if (target != null) {
            float[] targetRots = getRotationsToHead(target);
            if (targetRots != null) {
                float currentSpeed = speed.getValue().floatValue() * 0.35f;

                double dist = mc.thePlayer.getDistanceToEntity(target);
                // 玩家处于移动状态下或者距离极近时，平滑减速，防止旋转速率过高产生不合理的反作弊判定
                if (legitMode.getValue()) {
                    if (dist < 2.5) {
                        currentSpeed *= 0.6f;
                    }
                    if (mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0) {
                        currentSpeed *= 0.85f; // 移动时自瞄平滑度衰减，模拟手部控枪生理逻辑
                    }
                }

                float calculatedYaw = updateRotation(mc.thePlayer.rotationYaw, targetRots[0], currentSpeed);
                float calculatedPitch = updateRotation(mc.thePlayer.rotationPitch, targetRots[1], currentSpeed);

                if(Objects.equals(mode.getValue(), "normal")){
                    mc.thePlayer.rotationYaw = calculatedYaw;
                    mc.thePlayer.rotationPitch = calculatedPitch;
                } else {
                    TYClient.rotationManager.setRotation(new Vector2f(
                            calculatedYaw,
                            calculatedPitch)
                            ,180
                            , true);
                }
            }
        }
    }

    public Entity getTarget() {
        Entity closestTarget = null;
        double closestDist = Double.MAX_VALUE;
        float fovVal = fov.getValue().floatValue();

        if(mc.theWorld.loadedEntityList == null){
            return null;
        }

        for (final Entity ent : mc.theWorld.loadedEntityList) {
            if (ent == mc.thePlayer || !ent.isEntityAlive() || ent.isInvisible()) continue;

            double dist = mc.thePlayer.getDistanceToEntity(ent);
            if (dist > range.getValue()) continue;

            if (!(ent instanceof EntityLivingBase)) continue;
            if (AntiBot.INSTANCE.isBot((EntityLivingBase) ent)) continue;
            if (!isInFov(ent, fovVal)) continue;

            if (dist < closestDist) {
                closestDist = dist;
                closestTarget = ent;
            }
        }
        if(Teams.isSameTeam(closestTarget)){
            return null;
        }else {
            return closestTarget;
        }
    }

    @EventTarget
    public void onPacket(EventPacket e){
        if(stopbreak.getValue()) {
            if(mc.theWorld == null){
                return;
            }
            if(this.getTarget() == null){
                return;
            }
//            if(e.getState() != EventPacket.EventState.SEND){
//                return;
//            }
            if(e.getPacket() instanceof C07PacketPlayerDigging c07PacketPlayerDigging){
                if(c07PacketPlayerDigging.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK){
                    NotificationManager.post(NotificationType.SUCCESS,"Debug", "stop breaking!");
                    e.setCancelled(true);
                }
            }
        }
    }
}