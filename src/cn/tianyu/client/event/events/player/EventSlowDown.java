package cn.tianyu.client.event.events.player;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: CN_tianyu
 */

@Setter
@Getter
@AllArgsConstructor
public class EventSlowDown extends EventCancellable implements Event {
    private float strafe;
    private float forward;
    private boolean sprinting;
}
