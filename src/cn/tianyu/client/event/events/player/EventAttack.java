package cn.tianyu.client.event.events.player;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

/**
 * @Author: CN_tianyu
 */
@Getter
public class EventAttack extends EventCancellable implements Event {
    private final boolean pre;
    @Setter
    private Entity target;

    public EventAttack(Entity entity, boolean pre) {
        this.target = entity;
        this.pre = pre;
    }

    public boolean isPost() {
        return !pre;
    }

    public EntityLivingBase get(){
        return (EntityLivingBase)target;
    }
}
