package cn.tianyu.client.event.events.game;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import cn.tianyu.client.util.impl.client.DebugUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: CN_tianyu
 */

@Getter
@Setter
public class EventMotion extends EventCancellable implements Event {
    public EventState eventState;
    private double x, y, z;
    private float yaw, pitch;
    private boolean ground;
    private boolean modified;

    public EventMotion(double x, double y, double z, float yaw, float pitch, boolean ground, EventState eventState) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.ground = ground;
        this.eventState = eventState;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        this.modified = true;
    }

    public void setPitch(float pitch) {
        if (Math.abs(pitch) > 90) {
            DebugUtil.log("WARNING: PITCH IS " + pitch);
        }
        this.pitch = pitch;
        this.modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    public boolean isPre() {
        return this.eventState == EventState.PRE;
    }

    public boolean isPost() {
        return this.eventState == EventState.POST;
    }

    public enum EventState {
        PRE,
        POST
    }

}
