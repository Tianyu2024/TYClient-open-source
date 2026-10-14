package cn.tianyu.client.Manager;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;

import static cn.tianyu.client.TYClient.mc;

/**
 * @Author: CN_tianyu
 */
public class FallDistanceManager {
    public static float distance;
    private float lastDistance;

    @EventTarget
    private void onMotion(EventMotion event) {
        if (event.isPre()) {
            float fallDistance = TYClient.mc.thePlayer.fallDistance;
            if (fallDistance == 0.0f) {
                distance = 0.0f;
            }
            distance += fallDistance - this.lastDistance;
            this.lastDistance = fallDistance;
        }
    }
}
