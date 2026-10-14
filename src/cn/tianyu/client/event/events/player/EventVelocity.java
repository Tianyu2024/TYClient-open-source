package cn.tianyu.client.event.events.player;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: CN_tianyu
 */
@Getter
@Setter
@AllArgsConstructor
public class EventVelocity extends EventCancellable implements Event {
    private double reduceAmount;
}
