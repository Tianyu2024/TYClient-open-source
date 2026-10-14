package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.player.EventAttack;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.player.MoveUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0BPacketEntityAction;

/**
 * @Author: CN_tianyu
 */
public class MoreKB extends Module {
    public MoreKB() {
        super("MoreKB","更多击退", Category.combat);
    }

    private final ModeValue mode = new ModeValue("Mode", "Legit Fast", "Packet", "Legit Fast");
    private final BooleanValue onlyGround = new BooleanValue("Only Ground", true);
    public int ticks;

    private EntityLivingBase target = null;

    @EventTarget
    public void onAttack(EventAttack event) {
        if (event.getTarget() != null && event.getTarget() instanceof EntityLivingBase) {
            target = (EntityLivingBase) event.getTarget();
            ticks = 2;
        }
    }

    @Override
    public void onUpdate() {
        if (target != null && MoveUtil.isMoving()) {
            if ((onlyGround.get() && mc.thePlayer.onGround || !onlyGround.get())) {
                switch (mode.getValue()) {
                    case "Legit Fast":
                        mc.thePlayer.sprintingTicksLeft = 0;
                        break;
                    case "Packet":
                        sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                        sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                        break;
                }
            }
            target = null;
        }
    }
}
