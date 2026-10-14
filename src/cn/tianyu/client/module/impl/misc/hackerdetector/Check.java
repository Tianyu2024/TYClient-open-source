package cn.tianyu.client.module.impl.misc.hackerdetector;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.gui.notification.NotificationManager;
import cn.tianyu.client.gui.notification.NotificationType;
import cn.tianyu.client.module.impl.misc.HackerDetector;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.util.impl.player.TimerUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

public abstract class Check{

    public TimerUtil flagTimer = new TimerUtil();
    public int time = 2;

    public abstract String getName();

    public abstract void onPacketReceive(EventPacket event, EntityPlayer player);

    public abstract void onUpdate(EntityPlayer player);

    public void onMotion(EntityPlayer player, double x, double y, double z) {
    }

    public void flag(EntityPlayer player, String verbose) {
        if (flagTimer.hasTimeElapsed(time * 1000L)) {
            DebugUtil.log(player.getName() + EnumChatFormatting.WHITE + " detected for " + EnumChatFormatting.GRAY + getName() + EnumChatFormatting.WHITE + ", " + EnumChatFormatting.WHITE + verbose);
            NotificationManager.post(NotificationType.WARNING, player.getName() + EnumChatFormatting.WHITE + " detected for " + EnumChatFormatting.GRAY + getName() + EnumChatFormatting.WHITE, verbose, 2);
            TYClient.getModuleManager().getModule(HackerDetector.class).mark(player);
            flagTimer.reset();
        }
    }
}