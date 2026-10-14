package cn.tianyu.client.event.events.player;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;

/**
 * @Author: CN_tianyu
 */
public class EventSafeWalk extends EventCancellable implements Event {
    private boolean safeWalk;

    public EventSafeWalk(boolean safeWalk) {
        this.safeWalk = safeWalk;
    }

    public boolean isSafeWalk() {
        return this.safeWalk;
    }

    public void setSafeWalk(boolean safeWalk) {
        this.safeWalk = safeWalk;
    }
}
