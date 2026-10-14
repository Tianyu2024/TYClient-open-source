package cn.tianyu.client.module.impl.misc.hackerdetector.impl;

import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.module.impl.misc.hackerdetector.Check;
import cn.tianyu.client.util.impl.player.MoveUtil;
import cn.tianyu.client.util.impl.player.TimerUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;

public class LegitScaffoldCheck extends Check {
    private final TimerUtil timer = new TimerUtil();
    private int sneakFlag;

    @Override
    public String getName() {
        return "Legit Scaffold";
    }

    @Override
    public void onPacketReceive(EventPacket event, EntityPlayer player) {

    }

    @Override
    public void onUpdate(EntityPlayer player) {
        if (player.isSneaking()) {
            timer.reset();
            sneakFlag += 1;
        }

        if (timer.hasTimeElapsed(140)) {
            sneakFlag = 0;
        }
        if (player.rotationPitch > 75 && player.rotationPitch < 90 && player.isSwingInProgress) {
            if (player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemBlock) {
                if (MoveUtil.getSpeed(player) >= 0.10 && player.onGround && sneakFlag > 5) {
                    flag(player, "Sneak too fast");
                }
                if (MoveUtil.getSpeed(player) >= 0.21 && !player.onGround && sneakFlag > 5) {
                    flag(player, "Sneak too fast");
                }
            }
        }
    }


}

