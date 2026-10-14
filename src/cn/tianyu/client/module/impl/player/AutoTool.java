package cn.tianyu.client.module.impl.player;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;

/**
 * @Author: CN_tianyu
 */
public class AutoTool extends Module {
    public AutoTool(){
        super("AutoTool","自动选择工具", Category.player);
    }

    private int oldSlot;
    private int tick;

    @EventTarget
    public void onClick(EventMotion event) {
        if(event.isPre()){
            if (mc.playerController.isBreakingBlock()) {
                tick++;

                if (tick == 1) {
                    oldSlot = mc.thePlayer.inventory.currentItem;
                }

                mc.thePlayer.updateTool(mc.objectMouseOver.getBlockPos());
            } else if (tick > 0) {
                mc.thePlayer.inventory.currentItem = oldSlot;

                tick = 0;
            }
        }
    }
}
