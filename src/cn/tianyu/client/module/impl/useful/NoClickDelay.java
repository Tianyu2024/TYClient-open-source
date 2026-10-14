package cn.tianyu.client.module.impl.useful;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;

import static cn.tianyu.client.TYClient.mc;

/**
 * @Author: CN_tianyu
 */

@DefaultEnabled
public class NoClickDelay extends Module {
    public NoClickDelay(){
        super("NoClickDelay","NoClickDelay", Category.useful);
    }
    @EventTarget
    public void onMotion(EventMotion e) {
        if (mc.thePlayer != null && mc.theWorld != null){
            mc.leftClickCounter = 0;
        }
    }
}
