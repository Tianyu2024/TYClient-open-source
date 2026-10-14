package cn.tianyu.client.event.events.Packet;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import net.minecraft.network.Packet;

/**
 * @Author: CN_tianyu
 */
public class EventHigherPacketSend extends EventCancellable implements Event {
    public Packet packet;

    public EventHigherPacketSend(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return this.packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}
