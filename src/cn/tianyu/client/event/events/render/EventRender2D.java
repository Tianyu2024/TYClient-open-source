package cn.tianyu.client.event.events.render;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;

/**
 * @Author: CN_tianyu
 */
public class EventRender2D extends EventCancellable implements Event {
    private float partialTicks;
    public final ScaledResolution scaledResolution;

    public EventRender2D(float partialTicks, ScaledResolution scaledResolution) {
        this.partialTicks = partialTicks;
        this.scaledResolution = scaledResolution;
    }

    public float getPartialTicks() {
        return this.partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public ScaledResolution getScaledResolution() {
        return scaledResolution;
    }
}
