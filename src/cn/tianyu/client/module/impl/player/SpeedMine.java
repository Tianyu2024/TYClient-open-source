package cn.tianyu.client.module.impl.player;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;

/**
 * @Author: CN_tianyu
 */
public class SpeedMine extends Module {
    public final NumberValue speed = new NumberValue("speed", 15,0,100,1);
    public final NumberValue delay = new NumberValue("delay", 0, 0, 4,1);

    public SpeedMine() {
        super("SpeedMine", "快速挖掘", Category.player);
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (this.isEnabled() && event.getEventState() == EventMotion.EventState.PRE) {
            setSuffix(getSuffix());
            if (!mc.playerController.isInCreativeMode()) {
                if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    mc.playerController.setBlockHitDelay((int) Math.min(mc.playerController.getBlockHitDelay(), this.delay.getValue() + 1));
                    if (mc.playerController.isHittingBlock) {
                        float curBlockDamageMP = mc.playerController.getCurBlockDamageMP();
                        float damage = 0.3F * (this.speed.getValue().floatValue() / 100.0F);
                        if (curBlockDamageMP < damage) {
                            mc.playerController.setCurBlockDamageMP(damage);
                        }
                    }
                }
            }
        }
    }

    public String getSuffix() {
        return "%d%%" + this.speed.getValue();
    }
}
