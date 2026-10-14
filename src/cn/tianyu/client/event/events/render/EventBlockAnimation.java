package cn.tianyu.client.event.events.render;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import lombok.Getter;

/**
 * @Author: CN_tianyu
 */

@Getter
public class EventBlockAnimation extends EventCancellable implements Event {

    private float equipProgress, swingProgress, pitch, yaw;

    public EventBlockAnimation(float equipProgress, float swingProgress, float pitch, float yaw) {
        this.equipProgress = equipProgress;
        this.swingProgress = swingProgress;
        this.pitch = pitch;
        this.yaw = yaw;
    }
}
