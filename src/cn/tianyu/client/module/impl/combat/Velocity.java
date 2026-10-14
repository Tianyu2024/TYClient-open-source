package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.player.EventAttack;
import cn.tianyu.client.event.events.player.EventVelocity;
import cn.tianyu.client.event.events.world.EventWorldLoad;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.util.impl.render.Vector3d;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.viamcp.utils.AttackOrder;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.ChatComponentText;

/**
 * @Author: CN_tianyu
 */
public class Velocity extends Module {

    public Velocity() {
        super("Velocity", "Velocity", Category.combat);
    }

    private final ModeValue mode = new ModeValue("Mode", "JumpRest", "GrimAC", "Watchdog", "JumpRest", "LegitJumpRest", "AttackReduce");
    private final BooleanValue invalidEntity = new BooleanValue("Attack Invalid Entity", true);
    private final BooleanValue debug = new BooleanValue("Debug", true);
    public boolean shouldVelocity;
    private Entity target;
    private boolean canelsprint = false;

    @EventTarget
    public void onMotion(EventMotion e){
        if(e.isPre()){
            if(canelsprint) {
                mc.thePlayer.setSprinting(false);
            }
        }
    }

    @EventTarget
    public void onReceive(EventPacket event) {
        if (!isEnabled() || event.getState() == EventPacket.EventState.SEND) return;

        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) event.getPacket();

            if (s12.getEntityID() == mc.thePlayer.getEntityId()) {

                if (mode.getValue().equals("AttackReduce")) {
                    this.setSuffix("AttackReduce");
                    canelsprint = true;
                    mc.thePlayer.jump();
                    if(!mc.thePlayer.onGround) {
                        s12.motionX = (int)(s12.motionX * 0.6D);
                        s12.motionZ = (int)(s12.motionZ * 0.6D);
                    }
                    canelsprint = false;
                    return;
                }

                if (mode.getValue().equals("JumpRest")) {
                    this.setSuffix("JumpRest");

                    if (mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWeb) {

                        s12.motionY = 3360;

                        s12.motionX = (int)(s12.motionX * 0.15D);
                        s12.motionZ = (int)(s12.motionZ * 0.15D);

                        mc.thePlayer.jump();

                        if (debug.getValue()) {
                            mc.thePlayer.addChatMessage(new ChatComponentText("[JumpRest] Packet-Level Reset Applied Perfectly."));
                        }
                    }
                    return;
                }

                if (mode.getValue().equals("LegitJumpRest")) {
                    this.setSuffix("LegitJumpRest");

                    if (mc.thePlayer.onGround && mc.thePlayer.hurtTime >= 0) {
                        mc.thePlayer.setSprinting(false);
                        mc.thePlayer.jump();
                        mc.thePlayer.setSprinting(true);
                    }
                    return;
                }

                if (mode.getValue().equals("GrimAC")) {
                    this.setSuffix("GrimAC");
                    double strength = new Vector3d(s12.getMotionX(), s12.getMotionY(), s12.getMotionZ()).length();
                    if (!mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWeb) {
                        target = getNearTarget();
                        if (target == null) return;
                        if (mc.thePlayer.getDistanceToEntity(target) > 3.3F) {
                            reset();
                            return;
                        }
                        shouldVelocity = true;
                    }
                    return;
                }

                if (mode.getValue().equals("Watchdog")) {
                    this.setSuffix("Watchdog");
                    s12.motionX *= 0;
                    s12.motionZ *= 0;
                    s12.motionY *= 0;
                }
            }
        }
    }

    @EventTarget
    public void onWorld(EventWorldLoad event) {
        if (this.mode.getValue().equals("GrimAC") || this.mode.getValue().equals("AttackReduce")) {
            this.reset();
        }
    }

    private Entity getNearTarget() {
        Entity target = null;
        EntityLivingBase clientTarget = KillAura.target;
        if (clientTarget != null) {
            return clientTarget;
        } else {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (!entity.equals(mc.thePlayer) && !entity.isDead && invalidEntity.getValue()) {
                    if (entity instanceof EntityArrow) {
                        EntityArrow entityArrow = (EntityArrow) entity;
                        if (entityArrow.getTicksInGround() <= 0) {
                            target = entityArrow;
                        }
                    }
                    if (entity instanceof EntitySnowball || entity instanceof EntityEgg ||
                            entity instanceof EntityTNTPrimed || entity instanceof EntityFishHook) {
                        target = entity;
                    }
                }
            }
        }
        return target;
    }

    @EventTarget
    public void onVelocity(EventVelocity event) {
        if (Velocity.mc.thePlayer == null) return;

        if (this.mode.getValue().equals("GrimAC") && this.shouldVelocity) {
            if ((double) Velocity.mc.thePlayer.getDistanceToEntity(this.target) > 3.0) {
                this.reset();
                return;
            }
            if (!mc.thePlayer.serverSprintState) {
                PacketUtils.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                mc.thePlayer.setSprinting(true);
            }
            TYClient.instance.eventManager.callEvent(new EventAttack(target, true));
            TYClient.instance.eventManager.callEvent(new EventAttack(target, false));
            for (int i = 0; i < 5; i++) {
                AttackOrder.sendFixedAttackByPacket(mc.thePlayer, target);
            }
            if (!mc.thePlayer.serverSprintState) {
                PacketUtils.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }
            event.setReduceAmount(0.07776D);
            this.shouldVelocity = false;
        }
    }

    private void reset() {
        this.shouldVelocity = false;
        this.target = null;
        canelsprint = false;
    }
}