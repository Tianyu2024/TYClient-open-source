package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * @author CN_tianyu
 */

public class FullBright extends Module {
    public FullBright() {
        super("FullBright", "永久夜视", Category.useful);
    }

    @EventTarget
    public void onTick(EventTick event) {
        mc.thePlayer.addPotionEffect(new PotionEffect(Potion.nightVision.id, 114514, 5));
    }

    @Override
    public void onDisable() {
        if(mc.thePlayer != null){
            if (mc.thePlayer.isPotionActive(Potion.nightVision)) {
                mc.thePlayer.removePotionEffectClient(Potion.nightVision.id);
            }
        }
    }
}