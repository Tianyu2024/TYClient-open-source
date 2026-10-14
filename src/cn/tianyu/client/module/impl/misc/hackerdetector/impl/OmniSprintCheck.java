package cn.tianyu.client.module.impl.misc.hackerdetector.impl;

import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.module.impl.misc.hackerdetector.Check;
import net.minecraft.entity.player.EntityPlayer;


public class OmniSprintCheck extends Check {
    @Override
    public String getName() {
        return "Omni Sprint";
    }

    @Override
    public void onPacketReceive(EventPacket event, EntityPlayer player) {

    }

    @Override
    public void onUpdate(EntityPlayer player) {
        if (player.isSprinting() && (player.moveForward < 0.0f || player.moveForward == 0.0f && player.moveStrafing != 0.0f)) {
            flag(player, "Sprinting when moving backward");
        }
    }
}
