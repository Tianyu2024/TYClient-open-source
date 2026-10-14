package cn.tianyu.client.event.events.Packet;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

/**
 * @Author: CN_tianyu
 */

@Getter
@Setter
@AllArgsConstructor
public class EventPacket extends EventCancellable implements Event {
    public final EventState eventType;
    public Packet packet;
    public EventState state;
    public final INetHandler netHandler;

    public EventPacket(EventState eventType, Packet packet, INetHandler netHandler) {
        this.eventType = eventType;
        this.packet = packet;
        this.netHandler = netHandler;
    }

    public enum EventState {
        SEND,
        RECEIVE
    }
}
