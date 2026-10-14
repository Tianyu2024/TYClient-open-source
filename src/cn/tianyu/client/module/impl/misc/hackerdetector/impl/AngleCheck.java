package cn.tianyu.client.module.impl.misc.hackerdetector.impl;

import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.module.impl.misc.hackerdetector.Check;
import net.minecraft.entity.player.EntityPlayer;

public class AngleCheck extends Check {

    @Override
    public void onUpdate(EntityPlayer player) {
        if (Math.abs(player.rotationYaw - player.prevRotationYaw) > 50 && player.swingProgress != 0F) {
            flag(player, "Too fast rotate speed");
        }

        if (player.rotationPitch > 90 || player.rotationPitch < -90) {
            flag(player, "Invalid rotation pitch");
        }
    }

    @Override
    public String getName() {
        return "Angle";
    }

    @Override
    public void onPacketReceive(EventPacket event, EntityPlayer player) {

    }
}
