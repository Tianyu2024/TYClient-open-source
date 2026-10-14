package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.gui.notification.NotificationManager;
import cn.tianyu.client.gui.notification.NotificationType;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.EnumChatFormatting;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Author: CN_tianyu
 * 缓冲击退模块 - 延迟释放速度包
 */
public class AntiKb extends Module {
    public AntiKb() {
        super("AntiKb", "缓冲速度", Category.combat);
    }

    // 模式：Buffer（缓冲击退）
    public final ModeValue mode = new ModeValue("Mode", "Buffer", "Buffer", "Instant", "Cancel");

    // 缓冲释放间隔（毫秒）：每隔多少毫秒释放一次
    private final NumberValue releaseInterval = new NumberValue("Release Interval", 100, 10, 500, 10);

    // 每次释放的百分比（0~100%）
    private final NumberValue releasePercent = new NumberValue("Release Percent", 25, 5, 100, 1);

    // 水平击退吸收百分比（0~100%）
    private final NumberValue horizontalAbsorb = new NumberValue("Horizontal Absorb", 90, 0, 100, 1);

    // 垂直击退吸收百分比（0~100%）
    private final NumberValue verticalAbsorb = new NumberValue("Vertical Absorb", 70, 0, 100, 1);

    // 仅对玩家攻击生效
    private final BooleanValue onlyPlayerDamage = new BooleanValue("Only Player Damage", true);

    // 周围无玩家时一次性释放
    private final BooleanValue releaseWhenAlone = new BooleanValue("Release When Alone", true);

    // 缓存的速度包队列（存储被缓存的速度包）
    private final Queue<DelayedVelocityPacket> delayedPackets = new ConcurrentLinkedQueue<>();

    // 当前是否在缓冲中
    private boolean isActive;
    private long lastReleaseTime;

    /**
     * 延迟速度包结构
     */
    private static class DelayedVelocityPacket {
        final int entityId;
        final int motionX;
        final int motionY;
        final int motionZ;
        final long timestamp;

        DelayedVelocityPacket(int entityId, int motionX, int motionY, int motionZ, long timestamp) {
            this.entityId = entityId;
            this.motionX = motionX;
            this.motionY = motionY;
            this.motionZ = motionZ;
            this.timestamp = timestamp;
        }
    }

    @EventTarget
    public void onEnable() {
        isActive = true;
        delayedPackets.clear();
        lastReleaseTime = 0;
    }

    @Override
    public void onDisable() {
        isActive = false;
        // 禁用时释放所有缓存的包
        releaseAllPackets();
        delayedPackets.clear();
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (!isActive) return;

        // 【关键修复】如果是自己发送的包，跳过处理防止递归
        if (event.getState() == EventPacket.EventState.SEND) return;

        Packet<?> packet = event.getPacket();

        // 拦截速度包（击退包）
        if (packet instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity vp = (S12PacketEntityVelocity) packet;
            if (vp.getEntityID() == mc.thePlayer.getEntityId()) {
                // 解析速度值
                double motionX = vp.getMotionX() / 8000.0;
                double motionY = vp.getMotionY() / 8000.0;
                double motionZ = vp.getMotionZ() / 8000.0;

                // 判断是否为玩家攻击（Y轴击退通常 > 0.08）
                if (onlyPlayerDamage.getValue() && motionY < 0.08) {
                    return; // 非玩家攻击，放行
                }

                // 计算要吸收（缓冲）的速度
                double hAbsorb = horizontalAbsorb.getValue().doubleValue() / 100.0;
                double vAbsorb = verticalAbsorb.getValue().doubleValue() / 100.0;

                // 需要缓冲的部分（被吸收）
                int bufferX = (int) ((motionX * hAbsorb) * 8000);
                int bufferY = (int) ((motionY * vAbsorb) * 8000);
                int bufferZ = (int) ((motionZ * hAbsorb) * 8000);

                // 立即释放的部分（未被吸收）
                int remainX = (int) ((motionX * (1.0 - hAbsorb)) * 8000);
                int remainY = (int) ((motionY * (1.0 - vAbsorb)) * 8000);
                int remainZ = (int) ((motionZ * (1.0 - hAbsorb)) * 8000);

                // 将缓冲部分加入队列
                if (Math.abs(bufferX) > 10 || Math.abs(bufferY) > 10 || Math.abs(bufferZ) > 10) {
                    delayedPackets.add(new DelayedVelocityPacket(
                            vp.getEntityID(),
                            bufferX,
                            bufferY,
                            bufferZ,
                            System.currentTimeMillis()
                    ));
                }

                // 立即发送剩余部分
                if (Math.abs(remainX) > 10 || Math.abs(remainY) > 10 || Math.abs(remainZ) > 10) {
                    S12PacketEntityVelocity remainPacket = new S12PacketEntityVelocity(
                            vp.getEntityID(),
                            remainX,
                            remainY,
                            remainZ
                    );
                    // 直接发送给服务器（使用 sendPacketNoEvent 避免递归）
                    if (mc.getNetHandler() != null) {
                        sendPacketNoEvent(remainPacket);
                    }
                }

                // 取消原始速度包
                event.setCancelled(true);
            }
        }
    }

    @Override
    public void onUpdate() {
        setSuffix(getDisplayInfo());

        if (!isActive || delayedPackets.isEmpty()) return;

        long now = System.currentTimeMillis();

        // 检查周围是否有玩家
        boolean hasNearbyPlayer = hasPlayerNearby();

        // 如果周围没有玩家且开启了"无人时释放"，一次性释放所有缓冲
        if (releaseWhenAlone.getValue() && !hasNearbyPlayer) {
            releaseAllPackets();
            return;
        }

        // 定时释放：到达释放间隔时释放一批
        if (now - lastReleaseTime >= releaseInterval.getValue().longValue()) {
            releaseBatch();
            lastReleaseTime = now;
        }
    }

    /**
     * 释放一批缓冲的速度包
     */
    private void releaseBatch() {
        if (delayedPackets.isEmpty()) return;

        // 计算本次释放的数量（按百分比）
        int total = delayedPackets.size();
        int releaseCount = Math.max(1, (int) (total * (releasePercent.getValue().doubleValue() / 100.0)));

        // 释放指定数量的包
        for (int i = 0; i < releaseCount && !delayedPackets.isEmpty(); i++) {
            DelayedVelocityPacket entry = delayedPackets.poll();
            if (entry != null) {
                sendVelocityPacket(entry);
            }
        }
    }

    /**
     * 一次性释放所有缓冲包
     */
    private void releaseAllPackets() {
        if (delayedPackets.isEmpty()) return;

        int released = 0;
        while (!delayedPackets.isEmpty()) {
            DelayedVelocityPacket entry = delayedPackets.poll();
            if (entry != null) {
                sendVelocityPacket(entry);
                released++;
            }
        }

        if (released > 0) {
            NotificationManager.post(NotificationType.INFO,
                    EnumChatFormatting.GREEN + "AntiKb",
                    "Released all " + released + " buffered packets!", 1);
        }
    }

    /**
     * 发送单个速度包
     */
    private void sendVelocityPacket(DelayedVelocityPacket entry) {
        if (mc.getNetHandler() == null) return;

        // 检查玩家是否还存活
        if (mc.thePlayer == null || mc.thePlayer.isDead) {
            return;
        }

        S12PacketEntityVelocity packet = new S12PacketEntityVelocity(
                entry.entityId,
                entry.motionX,
                entry.motionY,
                entry.motionZ
        );
        // 使用 sendPacketNoEvent 避免递归
        sendPacketNoEvent(packet);
    }

    /**
     * 发送包但不触发事件（绕过事件系统防止递归）
     */
    private void sendPacketNoEvent(Packet<?> packet) {
        if (mc.getNetHandler() == null) return;
        // 直接调用 NetworkManager 的 sendPacket 方法
        // 这会绕过 EventManager 的拦截
        mc.getNetHandler().getNetworkManager().sendPacket(packet);
    }

    /**
     * 检查周围是否有玩家
     */
    private boolean hasPlayerNearby() {
        if (mc.theWorld == null || mc.thePlayer == null) return false;

        double checkRange = 8.0; // 检测范围8格
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
                double dist = mc.thePlayer.getDistanceToEntity(entity);
                if (dist < checkRange) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取当前缓冲的包数量
     */
    private int getBufferedCount() {
        return delayedPackets.size();
    }

    /**
     * 获取当前缓冲的总速度值（近似）
     */
    private double getBufferedSpeed() {
        double totalX = 0, totalY = 0, totalZ = 0;
        for (DelayedVelocityPacket p : delayedPackets) {
            totalX += p.motionX / 8000.0;
            totalY += p.motionY / 8000.0;
            totalZ += p.motionZ / 8000.0;
        }
        return Math.sqrt(totalX * totalX + totalY * totalY + totalZ * totalZ);
    }

    public String getDisplayInfo() {
        if (!isActive) return "Off";
        if (delayedPackets.isEmpty()) return "Ready";
        return String.format("Buf: %d | %.2f",
                getBufferedCount(),
                getBufferedSpeed()
        );
    }
}