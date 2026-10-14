package cn.tianyu.client.event.events.player;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;

/**
 * @Author: CN_tianyu
 */
public class EventPlace extends EventCancellable implements Event {

    private boolean shouldRightClick;
    private int slot;

    public EventPlace(final int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }

    public void setSlot(final int slot) {
        this.slot = slot;
    }

    public boolean isShouldRightClick() {
        return this.shouldRightClick;
    }

    public void setShouldRightClick(final boolean shouldRightClick) {
        this.shouldRightClick = shouldRightClick;
    }

}
