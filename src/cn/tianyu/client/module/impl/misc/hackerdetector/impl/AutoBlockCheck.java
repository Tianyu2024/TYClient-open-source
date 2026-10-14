package cn.tianyu.client.module.impl.misc.hackerdetector.impl;

import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.module.impl.misc.hackerdetector.Check;
import net.minecraft.entity.player.EntityPlayer;

public class AutoBlockCheck extends Check {
    private int blockingTime;

    @Override
    public String getName() {
        return "Auto Block";
    }

    @Override
    public void onPacketReceive(EventPacket event, EntityPlayer player) {

    }

    @Override
    public void onUpdate(EntityPlayer player) {
        if (player.isBlocking()) ++blockingTime;
        else blockingTime = 0;
        if (blockingTime > 5 && player.isSwingInProgress) {
            flag(player, "Swing when using item or blocking");
        }
    }
}

