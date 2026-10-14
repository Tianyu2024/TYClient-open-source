package cn.tianyu.client.event.events.game;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import lombok.Getter;

/**
 * @Author: CN_tianyu
 */
@Getter
public class EventKey extends EventCancellable implements Event {
    private int key;

    public EventKey(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
