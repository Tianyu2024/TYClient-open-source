package cn.tianyu.client.event.events.player;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;

/**
 * @Author: CN_tianyu
 */
public class EventSwapItem extends EventCancellable implements Event {
    private final int slot;
    private final int offset;

    public EventSwapItem(int slot, int offset) {
        this.slot = slot;
        this.offset = Math.min(Math.max(offset, -1), 1);
    }

    public int setSlot(int integer) {
        return this.slot >= 0 && this.slot <= 8 ? this.slot : Math.floorMod(integer - this.offset, 9);
    }
}
