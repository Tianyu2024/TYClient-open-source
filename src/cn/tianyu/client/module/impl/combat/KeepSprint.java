package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.event.EventPriority;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.world.EventKeepSprint;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;

/**
 * @Author: CN_tianyu
 */
public class KeepSprint extends Module {
    public KeepSprint() {
        super("KeepSprint", "保持疾跑不会因为攻击打断", Category.combat);
    }

    @EventTarget
    public void onKeepSprintEvent(EventKeepSprint event) {
        event.setCancelled(true);
    }
}
