package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.util.impl.player.TimerUtil;
import cn.tianyu.client.value.impl.NumberValue;

@DefaultEnabled
public class MemoryFix extends Module {
    private final NumberValue delay = new NumberValue("Delay", 120.0F, 10.0F, 600.0F, 10.0F);
    private final NumberValue limit = new NumberValue("Limit", 80.0F, 20.0F, 95.0F, 1.0F);
    public TimerUtil timer = new TimerUtil();

    public MemoryFix() {
        super("MemoryFix","内存溢出修复", Category.misc);
    }

    @EventTarget
    public void onTick(EventTick event) {
        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long usedMem = totalMem - freeMem;
        float pct = (float) (usedMem * 100L / maxMem);
        if (timer.hasReached(delay.getValue() * 1000.0D) && limit.getValue() <= (double) pct) {
            Runtime.getRuntime().gc();
            timer.reset();
        }
    }
}
