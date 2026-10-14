package cn.tianyu.client.event.events.render;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

/**
 * @Author: CN_tianyu
 */
public class EventRenderEntity extends EventCancellable implements Event {
    public enum State {
        PRE(),
        POST()
    }

    Entity entity;
    State state;

    public EventRenderEntity(final Entity entity, final State state) {
        this.entity = entity;
        this.state = state;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(final EntityLivingBase entity) {
        this.entity = entity;
    }

    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
    }
}
