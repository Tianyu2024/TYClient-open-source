package cn.tianyu.client.module.impl.useful;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.player.EventAttack;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.entity.Entity;

/**
 * @Author: CN_tianyu
 */
@DefaultEnabled
public class Particles extends Module {
    private final NumberValue amount = new NumberValue("Amount", 1, 1, 30, 1);
    public Particles (){
        super("Particles","粒子", Category.useful);
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        final Entity entity = event.getTarget();
        for (int i = 0; i < amount.getValue(); i++) {
            mc.thePlayer.onEnchantmentCritical(entity);
        }
    }
}
