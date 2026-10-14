package cn.tianyu.client.event.events.render;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;

/**
 * @Author: CN_tianyu
 */
public class EventRenderWorldLast extends EventCancellable implements Event {
    private final float partialTicks;

    public EventRenderWorldLast(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}