package cn.tianyu.client.event.events.player;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import cn.tianyu.client.util.impl.player.MoveUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: CN_tianyu
 */
@Getter
@Setter
public class EventStrafe extends EventCancellable implements Event {
    public float strafe;
    public float forward;
    public float friction;
    public float yaw;

    public EventStrafe(float Strafe, float Forward, float Friction, float Yaw) {
        this.strafe = Strafe;
        this.forward = Forward;
        this.friction = Friction;
        this.yaw = Yaw;
    }

    public void setSpeed(final double speed) {
        setFriction((float) (getForward() != 0 && getStrafe() != 0 ? speed * 0.98F : speed));
        MoveUtil.stop();
    }
}
