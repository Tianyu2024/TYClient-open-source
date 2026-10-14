package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.event.EventPriority;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.player.EventVelocity;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.value.impl.NumberValue;
import jdk.jfr.Enabled;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Author: CN_tianyu
 */
public class KnowBackDelay extends Module {
    private final NumberValue delayTime = new NumberValue("Delay Time", 200, 0, 1000, 10);
    private final Queue<TimedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private boolean isActive;
    private boolean blink;

    public KnowBackDelay() {
        super("KnowBackDelay", "延迟收到的击退", Category.combat);
    }

    // 数据包封装类，支持所有类型
    private static class TimedPacket {
        final Packet<?> packet;
        final long timestamp;
        TimedPacket(Packet<?> packet, long timestamp) {
            this.packet = packet;
            this.timestamp = timestamp;
        }
        boolean elapsed(int delayMs) {
            return System.currentTimeMillis() - timestamp >= delayMs;
        }
    }

    @Override
    public void onEnable() {
        isActive = true;
        blink = false;
        packetQueue.clear();
    }

    @Override
    public void onDisable() {
        isActive = false;
        flush();  //禁用时释放所有包
    }

    @EventPriority(0)
    @EventTarget
    public void onPacket(EventPacket event) {
        if (!isActive) return;
        if (event.getEventType() != EventPacket.EventState.RECEIVE) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Packet<?> packet = event.getPacket();

        // 检测到击退包时开启 blink
        if (!blink && packet instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity velocity = (S12PacketEntityVelocity) packet;
            if (velocity.getEntityID() == mc.thePlayer.getEntityId()) {
                blink = true;
            }
        }

        // blink 开启后，拦截所有包
        if (blink) {
            event.setCancelled(true);
            packetQueue.add(new TimedPacket(packet, System.currentTimeMillis()));
        }
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (!isActive) return;

        setSuffix(delayTime.getValue().intValue() + "ms");

        // 如果玩家不再受伤，逐渐释放
        if (blink && mc.thePlayer.hurtTime == 0) {
            long delayMs = delayTime.getValue().longValue();
            while (!packetQueue.isEmpty()) {
                TimedPacket entry = packetQueue.peek();
                if (entry != null && entry.elapsed((int) delayMs)) {
                    packetQueue.poll();
                    processPacketSilent(entry.packet);
                } else {
                    break;
                }
            }
            // 队列清空后关闭 blink
            if (packetQueue.isEmpty()) {
                blink = false;
            }
        }
    }

    // 释放所有积压包
    private void flush() {
        if (packetQueue.isEmpty()) return;
        TimedPacket entry;
        while ((entry = packetQueue.poll()) != null) {
            processPacketSilent(entry.packet);
        }
        blink = false;
    }

    @SuppressWarnings("unchecked")
    private void processPacketSilent(Packet<?> packet) {
        try {
            if (mc.getNetHandler() != null) {
                ((Packet<INetHandlerPlayClient>) packet).processPacket(mc.getNetHandler());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}