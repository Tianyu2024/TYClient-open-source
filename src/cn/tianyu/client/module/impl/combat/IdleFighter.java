package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.EventManager;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.player.EventAttack;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.MathUtil;
import cn.tianyu.client.util.impl.player.RotationUtil;
import cn.tianyu.client.util.impl.player.TimerUtil;
import cn.tianyu.client.value.impl.NumberValue;
import cn.tianyu.client.viamcp.utils.AttackOrder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.checkerframework.checker.units.qual.K;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @Author: CN_tianyu
 */
public class IdleFighter extends Module {
    public IdleFighter() {
        super("IdleFighter", " ", Category.combat);
    }

    private final List<EntityLivingBase> targets = new ArrayList<>();

    private final NumberValue minCPS = new NumberValue("Min CPS", 10, 20, 1, 1);
    private final NumberValue maxCPS = new NumberValue("Max CPS", 10, 20, 1, 1);

    private final NumberValue reach = new NumberValue("Reach", 3, 3, 6, 0.1);

    private final TimerUtil attackTimer = new TimerUtil();

    @EventTarget
    public void onMotionEvent(EventMotion event) {
        if (minCPS.getValue() > maxCPS.getValue()) {
            minCPS.setValue(minCPS.getValue() - 1);
        }

        mc.gameSettings.keyBindForward.pressed = KillAura.target != null && !(mc.thePlayer.getDistanceToEntity(KillAura.target) <= reach.getValue());
        mc.gameSettings.keyBindJump.pressed = mc.thePlayer.isCollidedHorizontally || mc.thePlayer.isInWater();

        if (event.isPre()) {
            sortTargets();
            if (!targets.isEmpty()) {
                KillAura.target = targets.get(0);
                final float[] rotations = RotationUtil.getRotationsT(
                        KillAura.target.posX,
                        KillAura.target.posY,
                        KillAura.target.posZ);
                mc.thePlayer.rotationYaw = rotations[0];
                mc.thePlayer.rotationPitch = rotations[1];

                if (mc.thePlayer.getDistanceToEntity(KillAura.target) <= reach.getValue() && attackTimer.hasTimeElapsed(
                        1000 / MathUtil.getRandomInRange(minCPS.getValue(), maxCPS.getValue()))) {
                    EventAttack attackEvent = new EventAttack(KillAura.target, false);
                    TYClient.instance.eventManager.callEvent(attackEvent);

                    if (!attackEvent.isCancelled()) {
                        AttackOrder.sendFixedAttack(mc.thePlayer, KillAura.target);
                    }
                    attackTimer.reset();
                }
            } else {
                KillAura.target = null;
            }
        }
    }

    @Override
    public void onDisable() {
        KillAura.target = null;
        mc.gameSettings.keyBindForward.pressed = false;
        targets.clear();
        super.onDisable();
    }

    private void sortTargets() {
        targets.clear();
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                if (KillAura.isValid(entity) && mc.thePlayer != entityLivingBase
                        //&& !FriendCommand.isFriend(entityLivingBase.getName())
                ) {
                    targets.add(entityLivingBase);
                }
            }
        }
        targets.sort(Comparator.comparingDouble(mc.thePlayer::getDistanceToEntity));
    }

}
