package cn.tianyu.client.module.impl.better;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.render.EventRenderEntity;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.util.impl.player.TimerUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

/**
 * @Author: CN_tianyu
 */

@DefaultEnabled
public class FPSBoost extends Module {
    public FPSBoost() {
        super("FPSBoost", "帧数提升", Category.better);
    }

    private final BooleanValue adaptiveRenderDistance = new BooleanValue("AdaptiveRenderDistance", true);
    private final BooleanValue clearFarEntities = new BooleanValue("ClearFarEntities", true);

    private final TimerUtil timer = new TimerUtil();

    @EventTarget
    public void onUpdate(final EventMotion event) {
        if (!event.isPre()) {
            return;
        }

        if (adaptiveRenderDistance.getValue() && timer.hasReached(500)) {
            EntityLivingBase farthest = getFarthest(64);
            if (farthest == null) {
                mc.gameSettings.renderDistanceChunks = 4;
            } else {
                double dist = mc.thePlayer.getDistanceToEntity(farthest);
                int targetDist = Math.max(4, Math.min(10, (int) (dist / 16.0f)));
                mc.gameSettings.renderDistanceChunks = targetDist;
            }
            timer.reset();
        }
    }

    @EventTarget
    public void onRenderPlayer(final EventRenderEntity event) {
        Entity entity = event.getEntity();
        if (entity == null) return;

        if (clearFarEntities.getValue() && entity.getDistanceSqToEntity(mc.thePlayer) > 64*64) {
            event.setCancelled(true);
        }
    }

    private EntityLivingBase getFarthest(final double range) {
        double maxDist = 0.0D;
        EntityLivingBase target = null;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase) || entity == mc.thePlayer)
                continue;

            EntityLivingBase living = (EntityLivingBase) entity;
            double dist = mc.thePlayer.getDistanceSqToEntity(living);

            if (dist <= range * range && dist > maxDist) {
                maxDist = dist;
                target = living;
            }
        }
        return target;
    }
}