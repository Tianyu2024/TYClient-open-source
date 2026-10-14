package cn.tianyu.client.event.events.player;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: CN_tianyu
 */
@Getter
@Setter
public class EventJump extends EventCancellable implements Event {
    public float motion;
    public float yaw;

    public EventJump(float yaw, float motion) {
        this.yaw = yaw;
        this.motion = motion;
    }

}