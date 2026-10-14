package cn.tianyu.client.event.events.render;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import net.minecraft.util.AxisAlignedBB;

/**
 * @Author: CN_tianyu
 */
public class EventBlockOverlay extends EventCancellable implements Event {
    AxisAlignedBB axisalignedbb;

    public EventBlockOverlay(AxisAlignedBB axisalignedbb) {
        this.axisalignedbb = axisalignedbb;
    }

    public AxisAlignedBB getBB() {
        return this.axisalignedbb;
    }

    public void setBB(AxisAlignedBB axisalignedbb) {
        this.axisalignedbb = axisalignedbb;
    }

}
