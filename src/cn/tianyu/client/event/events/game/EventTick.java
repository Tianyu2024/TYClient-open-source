package cn.tianyu.client.event.events.game;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;

/**
 * @Author: CN_tianyu
 */
public class EventTick extends EventCancellable implements Event {
    private final long tickTime;

    public EventTick(long tickTime, EventType type) {
        this.tickTime = tickTime;
        this.type = type;
    }

    public long getTickTime() {
        return tickTime;
    }

    private final EventType type;

    public EventType getType() {
        return this.type;
    }

    public enum EventType {
        PRE, POST
    }
}
