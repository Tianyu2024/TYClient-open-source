package cn.tianyu.client.module.impl.wtf;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.DebugUtil;
import de.gerrygames.viarewind.utils.ChatUtil;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * @Author: CN_tianyu
 */
public class wtffff extends Module {
    public wtffff() {
        super("wtfff","???", Category.client);
    }

    @EventTarget
    public void onTick(EventTick event){
        //mc.thePlayer.addPotionEffect(new PotionEffect(Potion.absorption.id, 114514, 255));
        //mc.thePlayer.addPotionEffect(new PotionEffect(Potion.invisibility.id, 114514, 255));
    }

    @Override
    public void onDisable() {
        if(mc.thePlayer != null){
            mc.thePlayer.sendChatMessage("§k");
//            if (mc.thePlayer.isPotionActive(Potion.invisibility)) {
//                mc.thePlayer.removePotionEffectClient(Potion.invisibility.id);
//            }
//            if (mc.thePlayer.isPotionActive(Potion.absorption)) {
//                mc.thePlayer.removePotionEffectClient(Potion.absorption.id);
//            }
        }
    }
}
