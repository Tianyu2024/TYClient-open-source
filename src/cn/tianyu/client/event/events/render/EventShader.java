package cn.tianyu.client.event.events.render;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;

/**
 * @Author: CN_tianyu
 */
public class EventShader extends EventCancellable implements Event {
    private final boolean bloom;

    public EventShader(boolean bloom) {
        this.bloom = bloom;
    }

    public boolean isBloom() {
        return this.bloom;
    }
}
