package cn.tianyu.client.event.events.world;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import net.minecraft.client.multiplayer.WorldClient;

/**
 * @Author: CN_tianyu
 */
public class EventWorldLoad extends EventCancellable implements Event {
    private final WorldClient world;

    public EventWorldLoad(WorldClient world) {
        this.world = world;
    }

    public WorldClient getWorld() {
        return world;
    }
}
