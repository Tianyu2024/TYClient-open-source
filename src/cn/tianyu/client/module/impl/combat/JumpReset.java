package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.player.EventVelocity;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.util.impl.client.RandomUtils;
import cn.tianyu.client.util.impl.player.MoveUtil;
import cn.tianyu.client.util.impl.player.MovementUtil;
import cn.tianyu.client.util.impl.player.PlayerUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.potion.Potion;

/**
 * @Author: haedus client dev
 */
public class JumpReset extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private BooleanValue debug;


    public JumpReset() {
        super("JumpReset", "Jumps on knockback to reduce velocity.", Category.combat);
        this.debug = new BooleanValue("Debug", false);
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if (e.getEventType() == EventPacket.EventState.RECEIVE) {
            if (e.getPacket() instanceof S12PacketEntityVelocity) {
                if (((S12PacketEntityVelocity) e.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
                    if(mc.thePlayer.onGround) {
                        mc.thePlayer.jump();

                        if(debug.getValue()){
                            DebugUtil.log(mc.thePlayer.hurtTime + mc.thePlayer.jumpTicks);
                        }
                    }
                }
            }
        }
    }
}
