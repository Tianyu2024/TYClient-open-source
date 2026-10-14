package cn.tianyu.client.module.impl.wtf;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.player.EventAttack;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;

/**
 * @Author: CN_tianyu
 */
public class LTap extends Module {
    boolean is;
    public LTap() {
        super("LTap","LTap", Category.client);
    }
    @Override
    public void onEnable(){
        super.onEnable();
        is = true;
    }
    @Override
    public void onDisable(){
        super.onDisable();
        is = false;
    }

    @EventTarget
    public void onAttack(EventAttack event) {
        if (is) {
            mc.thePlayer.sendChatMessage("L");
        }
    }
}
