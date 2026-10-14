package cn.tianyu.client.module.impl.misc.hackerdetector.impl;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.module.impl.misc.hackerdetector.Check;
import cn.tianyu.client.util.impl.player.MoveUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S14PacketEntity;


public class VelocityCheck extends Check {

    @Override
    public String getName() {
        return "Velocity";
    }

    @Override
    public void onPacketReceive(EventPacket event, EntityPlayer player) {

        if (event.getPacket() instanceof S14PacketEntity) {
            if (MoveUtil.getSpeed(player) == 0.0 && player.hurtResistantTime > 6 && player.hurtResistantTime < 1 && !TYClient.mc.theWorld.checkBlockCollision(player.getEntityBoundingBox().expand(0.05, 0.0, 0.05))) {
                flag(player, "Invalid velocity");
            }
        }
    }

    @Override
    public void onUpdate(EntityPlayer player) {

    }
}

