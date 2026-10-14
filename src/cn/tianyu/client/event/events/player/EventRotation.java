package cn.tianyu.client.event.events.player;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;

/**
 * @Author: CN_tianyu
 */
public class EventRotation extends EventCancellable implements Event {
    private float yaw;
    private float pitch;

    public EventRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }

    public EventRotation getAngle() {
        yaw = yaw % 360.0f;
        pitch = pitch % 360.0f;
        while (yaw <= -180.0f) {
            yaw = yaw + 360.0f;
        }
        while (pitch <= -180.0f) {
            pitch = pitch + 360.0f;
        }
        while (yaw > 180.0f) {
            yaw = yaw - 360.0f;
        }
        while (pitch > 180.0f) {
            pitch = pitch - 360.0f;
        }
        return this;
    }
}
