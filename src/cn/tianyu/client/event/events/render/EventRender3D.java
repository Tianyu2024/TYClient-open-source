package cn.tianyu.client.event.events.render;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;

/**
 * @Author: CN_tianyu
 */
@Getter
public class EventRender3D extends EventCancellable implements Event {
    private float ticks;
    private float partialTicks;
    private ScaledResolution scaledResolution;
    public EventRender3D(float ticks) {
        this.ticks = ticks;
    }
    public void Render3DEvent(ScaledResolution scaledResolution, float partialTicks) {
        this.scaledResolution = scaledResolution;
        this.partialTicks = partialTicks;
    }
    public float getPartialTicks() {
        return partialTicks;
    }
}
