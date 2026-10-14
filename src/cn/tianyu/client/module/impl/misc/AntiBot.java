package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S14PacketEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: CN_tianyu
 */
public class AntiBot extends Module {
    public AntiBot() {
        super("AntiBot","防假人", Category.misc);
        INSTANCE = this;
    }

    public static AntiBot INSTANCE;
    private static final BooleanValue tab = new BooleanValue("Tab", true);
    private BooleanValue checkOnGround = new BooleanValue("OnGround",false);
    private BooleanValue checkSwing = new BooleanValue("Swing",false);
    private final List<Integer> hasBeenGround = new ArrayList<Integer>();
    private final List<Integer> hasSwing = new ArrayList<Integer>();

    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getPacket() instanceof S14PacketEntity) {
            Entity swingEntity;
            S14PacketEntity s14Packet = (S14PacketEntity)event.getPacket();
            Entity entity = s14Packet.getEntity(AntiBot.mc.theWorld);
            if (entity instanceof EntityPlayer) {
                EntityPlayer entityPlayer = (EntityPlayer)entity;
                if (entity.onGround) {
                    this.hasBeenGround.add(entityPlayer.getEntityId());
                }
            }
            if (event.getPacket() instanceof S0BPacketAnimation && (swingEntity = AntiBot.mc.theWorld.getEntityByID(((S0BPacketAnimation)event.getPacket()).entityId)) instanceof EntityPlayer) {
                this.hasSwing.add(swingEntity.getEntityId());
            }
        }
    }

    private boolean inTab(EntityLivingBase entity) {
        for (NetworkPlayerInfo item : mc.getNetHandler().getPlayerInfoMap()) {
            if (item == null || item.getGameProfile() == null || !item.getGameProfile().getName().contains(entity.getName()))
                continue;
            return true;
        }
        return false;
    }

    public boolean isBot(EntityLivingBase entityLivingBase) {
        if (!this.getState()) {
            return false;
        }
        if (tab.get() && !inTab(entityLivingBase)) {
            return true;
        }
        if (this.checkOnGround.get() && !this.hasBeenGround.contains(entityLivingBase.getEntityId())) {
            return true;
        }
        return this.checkSwing.get() && !this.hasSwing.contains(entityLivingBase.getEntityId());
    }
}
