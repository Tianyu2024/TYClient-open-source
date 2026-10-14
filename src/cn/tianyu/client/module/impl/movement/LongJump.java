package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.gui.notification.NotificationManager;
import cn.tianyu.client.gui.notification.NotificationType;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.EnumChatFormatting;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Author: CN_tianyu
 */
public class LongJump extends Module {
    public LongJump(){
        super("LongJump","远跳", Category.movement);
    }
    public final ModeValue mode = new ModeValue("Mode", "Prediction", "Delay" ,"Prediction");
    private final NumberValue delayTime = new NumberValue("Delay Time", 1000, 0, 5000, 1);

    private final Queue<DelayedPacket> delayedPackets = new ConcurrentLinkedQueue<>();
    private boolean isActive;

    private static class DelayedPacket {
        final Packet<?> packet;
        final long timestamp;

        DelayedPacket(Packet<?> packet, long timestamp) {
            this.packet = packet;
            this.timestamp = timestamp;
        }
    }

    @EventTarget
    public void onEnable() {
        if (getFireball() == -1) {
            NotificationManager.post(NotificationType.WARNING,
                    EnumChatFormatting.RED + "Inventory",
                    "You don't have a Fireball.", 5);
            this.toggle();
            return;
        }

        isActive = true;
        delayedPackets.clear();
    }

    @Override
    public void onDisable() {
        isActive = false;
        delayedPackets.clear();
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (!isActive) return;

        Packet<?> packet = event.getPacket();

        if (mode.is("Prediction")) {
            if (packet instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity velocityPacket = (S12PacketEntityVelocity) event.getPacket();
                if (velocityPacket.getEntityID() == mc.thePlayer.getEntityId()) {
                    delayedPackets.add(new DelayedPacket(packet, System.currentTimeMillis()));
                    event.setCancelled(true);
                }
            }
        }

        if (shouldHideExplosion() && (
                packet instanceof S0EPacketSpawnObject ||
                        packet instanceof S2APacketParticles)) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onUpdate() {
        setSuffix(mode.getValue());
        if (!isActive) return;

        if (mode.is("Prediction")) {
            long currentTime = System.currentTimeMillis();
            while (!delayedPackets.isEmpty()) {
                DelayedPacket entry = delayedPackets.peek();
                if (currentTime - entry.timestamp >= delayTime.getValue()) {
                    releasePacket(entry.packet);
                    delayedPackets.remove();
                } else {
                    break;
                }
            }
        }
    }

    private void releasePacket(Packet<?> packet) {
        if (mc.getNetHandler() != null) {
            mc.getNetHandler().addToSendQueue(packet);
        }
    }

    private boolean shouldHideExplosion() {
        return true;
    }

    private int getFireball() {
        if (mc.thePlayer == null || mc.thePlayer.inventory == null) return -1;

        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == Items.fire_charge) {
                return i;
            }
        }
        return -1;
    }
}