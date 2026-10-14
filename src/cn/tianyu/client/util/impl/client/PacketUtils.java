package cn.tianyu.client.util.impl.client;

import cn.tianyu.client.module.impl.combat.BackTrack;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.*;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.tianyu.client.TYClient.mc;

/**
 * @Author: CN_tianyu
 */
public class PacketUtils {

    public static PacketUtils INSTANCE;

    static {
        INSTANCE = new PacketUtils();
    }

    public static void sendPacket(Packet<?> packet, boolean silent) {
        if (mc.thePlayer != null) {
            mc.getNetHandler().getNetworkManager().sendPacket(packet);
        }
    }

    public static void sendPacketNoEvent(Packet packet) {
        mc.getNetHandler().getNetworkManager().sendPacketNoEvent(packet);
        //mc.getNetHandler().getNetworkManager().sendPacket(packet);
    }

    public static void sendPacket(Packet packet) {
        sendPacket(packet, false);
    }

    public static boolean isPacketValid(final Packet packet) {
        return !(packet instanceof C00PacketLoginStart) && !(packet instanceof C00Handshake) && !(packet instanceof C00PacketServerQuery) && !(packet instanceof C01PacketPing);
    }

    public static boolean isCPacket(Packet<?> packet) {
        return packet.getClass().getSimpleName().startsWith("C");
    }

    public static void send(Packet<?> packet) {
        if (mc.thePlayer != null) {
            mc.getNetHandler().addToSendQueue(packet);
        }
    }

    public static void queue(final Packet packet) {
        if (packet == null) {
            System.out.println("Packet is null");
            return;
        }

        if (isCPacket(packet)) {
            sendPacketNoEvent(packet);
        } else {
            packet.processPacket(mc.getNetHandler().getNetworkManager().getNetHandler());
        }
    }

    public static void receivePacket(Packet packet) {
        if (packet == null) return;
        if (packet.getClass().getPackage().getName().startsWith("net.minecraft.network.play.server")) {
            packet.processPacket(mc.getNetHandler());
        }
    }

    public PacketData C0A = new PacketData();
    public PacketData C08 = new PacketData();
    public PacketData C07 = new PacketData();
    public PacketData C02 = new PacketData();
    public PacketData C02_INTERACT_AT = new PacketData();
    public PacketData C09 = new PacketData();
    public AtomicInteger playerSlot = new AtomicInteger(-1);
    public AtomicInteger serverSlot = new AtomicInteger(-1);
    private final boolean handleSlots = true;

    public void handlePacket(Packet<?> packet) {
        if (packet instanceof C09PacketHeldItemChange && handleSlots) {
            int slotId = ((C09PacketHeldItemChange) packet).getSlotId();
            this.playerSlot.set(slotId);
            C09.sentCurrentTick.set(true);
        } else if (packet instanceof C02PacketUseEntity) {
            C02.sentCurrentTick.set(true);
            if (((C02PacketUseEntity) packet).getAction() == C02PacketUseEntity.Action.INTERACT_AT) {
                C02_INTERACT_AT.sentCurrentTick.set(true);
            }
        } else if (packet instanceof C07PacketPlayerDigging) {
            C07.sentCurrentTick.set(true);
        } else if (packet instanceof C08PacketPlayerBlockPlacement) {
            C08.sentCurrentTick.set(true);
        } else if (packet instanceof C0APacketAnimation) {
            C0A.sentCurrentTick.set(true);
        }
    }


    public static class PacketData {
        public AtomicBoolean sentLastTick = new AtomicBoolean(false);
        public AtomicBoolean sentCurrentTick = new AtomicBoolean(false);
        private int value;

        public void updateStatesPostUpdate() {
            sentLastTick.set(sentCurrentTick.get());
            sentCurrentTick.set(false);
        }

        public final boolean get() {
            return value != 0;
        }
    }
}
