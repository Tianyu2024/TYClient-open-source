package cn.tianyu.client.module.impl.misc.hackerdetector.impl;

import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.module.impl.misc.hackerdetector.Check;
import net.minecraft.entity.player.EntityPlayer;

public class NoFallCheck extends Check {
    boolean fall;

    @Override
    public String getName() {
        return "No Fall";
    }

    @Override
    public void onPacketReceive(EventPacket event, EntityPlayer player) {

    }

    @Override
    public void onUpdate(EntityPlayer player) {
        if (player.fallDistance > 3) {
            fall = true;
        }
        if (fall && player.fallDistance == 0 && player.hurtTime == 0 && !player.isInWater()) {
            flag(player, "Not taking any damage");
            fall = false;
        }
    }


}

