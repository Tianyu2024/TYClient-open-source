package cn.tianyu.client.module.impl.better;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.player.TimerUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;

/**
 * @Author: CN_tianyu
 */
public class MemoryCleaner extends Module {

    private final NumberValue cleanDelay = new NumberValue("CleanDelay(s)", 30, 5, 120, 5);
    private final BooleanValue clearChunks = new BooleanValue("ClearChunks", true);
    private final BooleanValue clearEntities = new BooleanValue("ClearEntities", true);
    private final BooleanValue fullGC = new BooleanValue("FullGC", true);
    private final BooleanValue autoClean = new BooleanValue("AutoClean", true);

    private final TimerUtil timer = new TimerUtil();
    private final Minecraft mc = Minecraft.getMinecraft();

    public MemoryCleaner() {
        super("MemoryCleaner", "强力内存清理", Category.better);
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (!autoClean.get() || !timer.hasReached(cleanDelay.get() * 1000))
            return;

        doClean();
        timer.reset();
    }

    public void doClean() {
        try {
            if (mc.theWorld != null && clearChunks.get()) {
                mc.renderGlobal.loadRenderers();
            }

            if (mc.theWorld != null && clearEntities.get()) {
                mc.theWorld.loadedEntityList.removeIf(entity ->
                        entity == null || entity.isDead || entity instanceof EntityItem
                );
                mc.theWorld.playerEntities.removeIf(entity ->
                        entity == null || entity.isDead
                );
            }

            if (fullGC.get()) {
                System.gc();
                System.runFinalization();
            }

        } catch (Exception e) {
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        timer.reset();
        doClean();
    }
}