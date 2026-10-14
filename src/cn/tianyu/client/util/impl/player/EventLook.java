package cn.tianyu.client.util.impl.player;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import lombok.Getter;
import lombok.Setter;
import org.lwjglx.util.vector.Vector2f;

/**
 * @Author: CN_tianyu
 */
@Getter
@Setter
public class EventLook extends EventCancellable implements Event {
    private Vector2f rotation;
    public EventLook(Vector2f rotation) {
        this.rotation = rotation;
    }
}
