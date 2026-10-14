package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.render.EventRenderEntity;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.misc.Teams;
import cn.tianyu.client.util.impl.player.PlayerUtil;
import cn.tianyu.client.util.impl.player.PlayerUtils;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;

/**
 * @Author: CN_tianyu
 */
public class Chams extends Module {
    public Chams() {
        super("Chams", "", Category.render);
    }

    private static final Minecraft mc = Minecraft.getMinecraft();
    public final BooleanValue players = new BooleanValue("players", true);
    public final BooleanValue friends = new BooleanValue("friends", true);
    public final BooleanValue enemiess = new BooleanValue("enemies", true);
    public final BooleanValue bosses = new BooleanValue("bosses", false);
    public final BooleanValue mobs = new BooleanValue("mobs", false);
    public final BooleanValue creepers = new BooleanValue("creepers", false);
    public final BooleanValue enderman = new BooleanValue("endermen", false);
    public final BooleanValue blaze = new BooleanValue("blazes", false);
    public final BooleanValue animals = new BooleanValue("animals", false);
    public final BooleanValue self = new BooleanValue("self", false);
    public final BooleanValue bots = new BooleanValue("bots", false);

    private boolean shouldRenderChams(EntityLivingBase entityLivingBase) {
        if (entityLivingBase.deathTime > 0) {
            return false;
        } else if (mc.getRenderViewEntity().getDistanceToEntity(entityLivingBase) > 512.0F) {
            return false;
        } else if (entityLivingBase instanceof EntityPlayer) {
            if (entityLivingBase != mc.thePlayer && entityLivingBase != mc.getRenderViewEntity()) {
                if (PlayerUtil.isBot((EntityPlayer) entityLivingBase)) {
                    return this.bots.getValue();
                } else if (PlayerUtil.isFriend((EntityPlayer) entityLivingBase)) {
                    return this.friends.getValue();
                } else {
                    return PlayerUtil.isTarget((EntityPlayer) entityLivingBase) ? this.enemiess.getValue() : this.players.getValue();
                }
            } else {
                return this.self.getValue() && mc.gameSettings.thirdPersonView != 0;
            }
        } else if (entityLivingBase instanceof EntityDragon || entityLivingBase instanceof EntityWither) {
            return !entityLivingBase.isInvisible() && this.bosses.getValue();
        } else if (!(entityLivingBase instanceof EntityMob) && !(entityLivingBase instanceof EntitySlime)) {
            return (entityLivingBase instanceof EntityAnimal
                    || entityLivingBase instanceof EntityBat
                    || entityLivingBase instanceof EntitySquid
                    || entityLivingBase instanceof EntityVillager) && this.animals.getValue();
        } else if (entityLivingBase instanceof EntityCreeper) {
            return this.creepers.getValue();
        } else if (entityLivingBase instanceof EntityEnderman) {
            return this.enderman.getValue();
        } else {
            return entityLivingBase instanceof EntityBlaze ? this.blaze.getValue() : this.mobs.getValue();
        }
    }

    @EventTarget
    public void onRenderLiving(EventRenderEntity event) {
        if (this.isEnabled()) {
            if (this.shouldRenderChams((EntityLivingBase) event.getEntity())) {
                switch (event.getState()) {
                    case PRE:
                        GL11.glEnable(32823);
                        GL11.glPolygonOffset(1.0F, -2500000.0F);
                        break;
                    case POST:
                        GL11.glPolygonOffset(1.0F, 2500000.0F);
                        GL11.glDisable(32823);
                }
            }
        }
    }
}
