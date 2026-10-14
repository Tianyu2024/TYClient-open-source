package cn.tianyu.client.module.impl.combat;


import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.util.impl.player.TimerUtil;
import cn.tianyu.client.value.impl.ModeValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AntiAura extends Module {

    private final ModeValue mode = new ModeValue("Mode", "Legit", "Legit", "Rage");
    private final List<Packet<?>> packetsList = new CopyOnWriteArrayList<>();
    private final TimerUtil timer = new TimerUtil();
    private double x, y, z;

    public AntiAura() {
        super("AntiAura", "", Category.combat);
    }

    @EventTarget
    public void onMotionEvent(EventMotion event) {
        this.setSuffix(mode.getValue());
        switch (mode.getValue()) {
            case "Legit":
                if (timer.hasTimeElapsed(50)) {
                    if (!packetsList.isEmpty()) {
                        for (Packet<?> packet : packetsList) {
                            PacketUtils.sendPacketNoEvent(packet);
                            packetsList.remove(packet);
                        }
                    }
                    timer.reset();
                }
                break;
            case "Rage":
                x = mc.thePlayer.posX;
                y = mc.thePlayer.posY;
                z = mc.thePlayer.posZ;
                double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
                double xValue = -Math.sin(yaw) * 2;
                double zValue = Math.cos(yaw) * 2;
                //if (KillAura.attacking) {
                    mc.thePlayer.setPosition(x + xValue, y, z + zValue);
                    if (mc.thePlayer.ticksExisted % 2 == 0) {
                        mc.thePlayer.setPosition(x - xValue, y, z - zValue);
                    }
                //}
                break;
        }
    }


    @EventTarget
    public void onPacketSendEvent(EventPacket e) {
        if(e.getState() != EventPacket.EventState.SEND){
            return;
        }
        if (e.getPacket() instanceof C03PacketPlayer) {
            packetsList.add(e.getPacket());
            e.setCancelled(true);
        }
    }

}
