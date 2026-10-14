package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.player.RotationUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static cn.tianyu.client.TYClient.mc;

/**
 * @Author: CN_tianyu
 */
public class HypixelKillAura extends Module {

    private final Random random = new Random();

    private final NumberValue range = new NumberValue("Range", 3.8, 2.5, 5.0, 0.1);
    private final NumberValue fov = new NumberValue("FOV", 180.0, 30.0, 360.0, 5.0);
    private final NumberValue cps = new NumberValue("CPS", 12, 5, 20, 1);
    private final NumberValue rotationSpeed = new NumberValue("Rotation Speed", 3.0, 0.5, 8.0, 0.1);
    private final BooleanValue silentRotation = new BooleanValue("Silent Rotation", true);
    private final BooleanValue autoBlock = new BooleanValue("AutoBlock", true);
    private final BooleanValue swing = new BooleanValue("Swing", true);
    private final BooleanValue onlyWeapon = new BooleanValue("Only Weapon", true);

    private EntityLivingBase target;
    private long lastAttackTime = 0;
    private int attackCounter = 0;
    private boolean blocking = false;

    public HypixelKillAura() {
        super("HypixelKillAura", "Hypixel杀戮光环", Category.combat);
    }

    @Override
    public void onEnable() {
        target = null;
        blocking = false;
        attackCounter = 0;
    }

    @Override
    public void onDisable() {
        releaseBlock();
        target = null;
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (onlyWeapon.getValue() && !isHoldingWeapon()) {
            target = null;
            releaseBlock();
            return;
        }

        target = findTarget();

        if (target != null) {
            handleAttack();
            handleBlock();
        } else {
            releaseBlock();
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (target == null) return;

        float[] rotations = RotationUtil.getRotations(target);
        float yaw = rotations[0];
        float pitch = rotations[1];

        if (random.nextBoolean()) {
            yaw += (random.nextFloat() - 0.5f) * 1.5f;
        }
        if (random.nextBoolean()) {
            pitch += (random.nextFloat() - 0.5f) * 0.8f;
        }

        float currentYaw = mc.thePlayer.rotationYaw;
        float currentPitch = mc.thePlayer.rotationPitch;

        float yawDiff = normalizeAngle(yaw - currentYaw);
        float pitchDiff = normalizeAngle(pitch - currentPitch);

        float speed = rotationSpeed.getValue().floatValue();

        if (Math.abs(yawDiff) > speed) {
            yaw = currentYaw + (yawDiff > 0 ? speed : -speed);
        }
        if (Math.abs(pitchDiff) > speed * 0.3f) {
            pitch = currentPitch + (pitchDiff > 0 ? speed * 0.3f : -speed * 0.3f);
        }

        if (silentRotation.getValue()) {
            event.setYaw(yaw);
            event.setPitch(pitch);
        } else {
            mc.thePlayer.rotationYaw = yaw;
            mc.thePlayer.rotationPitch = pitch;
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getEventType() != EventPacket.EventState.SEND) return;
        if (!silentRotation.getValue()) return;
        if (target == null) return;
        if (event.getPacket() instanceof C03PacketPlayer) {
            C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();
            if (packet.isMoving() || packet.getRotating()) {
                float[] rots = RotationUtil.getRotations(target);
                // 只修改旋转包
                if (packet instanceof C03PacketPlayer.C05PacketPlayerLook ||
                        packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
                    // 由于不能直接修改包，使用事件取消并重新发送
                    event.setCancelled(true);
                    if (packet instanceof C03PacketPlayer.C05PacketPlayerLook) {
                        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(
                                rots[0], rots[1], mc.thePlayer.onGround
                        ));
                    } else {
                        C03PacketPlayer.C06PacketPlayerPosLook posLook = (C03PacketPlayer.C06PacketPlayerPosLook) packet;
                        mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(
                                posLook.getPositionX(), posLook.getPositionY(), posLook.getPositionZ(),
                                rots[0], rots[1], mc.thePlayer.onGround
                        ));
                    }
                }
            }
        }
    }

    private void handleAttack() {
        if (target == null) return;

        long currentTime = System.currentTimeMillis();
        long interval = 1000 / cps.getValue().intValue();

        if (currentTime - lastAttackTime >= interval) {
            performAttack();
            lastAttackTime = currentTime;
            attackCounter++;
        }
    }

    private void performAttack() {
        if (target == null) return;

        // 随机跳过攻击（模拟人类）
        if (attackCounter % 7 == 0 && random.nextInt(3) == 0) return;

        mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));

        if (swing.getValue()) {
            mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
        }
    }

    private void handleBlock() {
        if (!autoBlock.getValue()) {
            releaseBlock();
            return;
        }

        if (target == null) {
            releaseBlock();
            return;
        }

        if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof net.minecraft.item.ItemSword)) {
            releaseBlock();
            return;
        }

        if (mc.thePlayer.hurtTime > 0 || target.hurtTime > 0) {
            block();
        } else {
            if (random.nextInt(3) == 0) {
                block();
            } else {
                releaseBlock();
            }
        }
    }

    private void block() {
        if (!blocking) {
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(
                    new BlockPos(-1, -1, -1), 255,
                    mc.thePlayer.getHeldItem(), 0, 0, 0
            ));
            blocking = true;
        }
    }

    private void releaseBlock() {
        if (blocking) {
            mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                    BlockPos.ORIGIN, EnumFacing.DOWN
            ));
            blocking = false;
        }
    }

    private EntityLivingBase findTarget() {
        List<EntityLivingBase> targets = new ArrayList<>();
        double fovValue = fov.getValue();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityPlayer)) continue;
            EntityPlayer player = (EntityPlayer) entity;
            if (player == mc.thePlayer || player.isDead) continue;
            if (!mc.thePlayer.canEntityBeSeen(player)) continue;

            double dist = mc.thePlayer.getDistanceToEntity(player);
            if (dist > range.getValue() || dist < 0.5) continue;

            if (!isInFOV(player, fovValue)) continue;

            targets.add(player);
        }

        if (targets.isEmpty()) return null;

        targets.sort(Comparator.comparingDouble(e -> mc.thePlayer.getDistanceToEntity(e)));

        // 随机选择附近的敌人（防锁定检测）
        int size = Math.min(targets.size(), 3);
        return targets.get(random.nextInt(size));
    }

    private boolean isInFOV(Entity entity, double fovValue) {
        float[] rots = RotationUtil.getRotations(entity);
        float yawDiff = normalizeAngle(rots[0] - mc.thePlayer.rotationYaw);
        return Math.abs(yawDiff) <= fovValue / 2;
    }

    private boolean isHoldingWeapon() {
        if (mc.thePlayer.getHeldItem() == null) return false;
        return mc.thePlayer.getHeldItem().getItem() instanceof net.minecraft.item.ItemSword ||
                mc.thePlayer.getHeldItem().getItem() instanceof net.minecraft.item.ItemAxe;
    }

    private float normalizeAngle(float angle) {
        angle %= 360.0f;
        if (angle >= 180.0f) angle -= 360.0f;
        if (angle < -180.0f) angle += 360.0f;
        return angle;
    }
}