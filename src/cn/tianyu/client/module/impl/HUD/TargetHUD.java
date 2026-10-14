package cn.tianyu.client.module.impl.HUD;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.player.EventAttack;
import cn.tianyu.client.gui.ui.modules.TargetHud;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.combat.KillAura;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.impl.client.PacketUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

/**
 * @Author: CN_tianyu
 */
public class TargetHUD extends Module {
    public TargetHUD(){
        super("TargetHUD","TargetHUD", Category.HUD);
    }

    @EventTarget
    public void onA(EventAttack e){
        if(!TYClient.moduleManager.getByClass(KillAura.class).isEnable()){
            KillAura.target = e.get();
        }
        if(e.getTarget().isDead) KillAura.target = null;
    }
}
