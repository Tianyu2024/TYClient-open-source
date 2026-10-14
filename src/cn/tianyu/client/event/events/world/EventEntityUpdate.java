package cn.tianyu.client.event.events.world;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;

/**
 * @Author: CN_tianyu
 */


@AllArgsConstructor
@Getter
public class EventEntityUpdate extends EventCancellable implements Event {
    public final EntityLivingBase entity;
}
