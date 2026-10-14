package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.player.EventAttack;
import cn.tianyu.client.event.events.player.EventStrafe;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.movement.Speed;
import cn.tianyu.client.module.impl.world.Stuck;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.util.impl.player.MoveUtil;
import cn.tianyu.client.util.impl.player.TimerUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C18PacketSpectate;
import net.minecraft.util.BlockPos;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author: CN_tianyu
 */
public class Criticals extends Module {

    public Criticals(){
        super("Criticals","刀爆", Category.combat);
    }


    private final ModeValue mode = new ModeValue("Mode", "Jump", "AutoFreeze", "AutoSpeed", "Jump");
    private boolean attacking;
    public boolean stuckEnabled;

    @Override
    public void onEnable() {
        stuckEnabled = false;
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        if (mc.thePlayer.onGround && event.getTarget() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) event.getTarget();
            if(entity.hurtTime == 9) {
                mc.thePlayer.onCriticalHit(entity);
            }
            attacking = true;
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        setSuffix(mode.getValue());
        switch (mode.getValue()) {
            case "AutoFreeze":
                if (getModule(KillAura.class).target != null && mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                }
                if (mc.thePlayer.fallDistance > 0) {
                    getModule(Stuck.class).setEnable(true);
                    stuckEnabled = true;
                }
                if (getModule(KillAura.class).target == null && stuckEnabled) {
                    getModule(Stuck.class).setEnable(false);
                    stuckEnabled = false;
                }
                break;
            case "AutoSpeed":
                if (getModule(KillAura.class).target != null) {
                    if (!TYClient.moduleManager.getModule(Speed.class).isEnable()) {
                        getModule(Speed.class).setEnable(true);
                    } else {
                        if (!MoveUtil.isMoving() && mc.thePlayer.onGround) {
                            mc.thePlayer.jump();
                        }
                    }
                } else {
                    if (isEnabled(Speed.class)) {
                        getModule(Speed.class).setEnable(false);
                    }
                }
                break;
        }
    }

    @EventTarget
    public void onStrafe(EventStrafe event){
        if(mode.is("Jump") && attacking && mc.thePlayer.onGround){
            mc.thePlayer.jump();
            attacking = false;
        }
    }
}
