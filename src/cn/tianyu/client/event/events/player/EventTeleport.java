package cn.tianyu.client.event.events.player;

import cn.tianyu.client.event.Event;
import cn.tianyu.client.event.events.EventCancellable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * @Author: CN_tianyu
 */

@Getter
@Setter
@AllArgsConstructor
public class EventTeleport extends EventCancellable implements Event {
    private C03PacketPlayer response;
    private double posX;
    private double posY;
    private double posZ;
    private float yaw;
    private float pitch;
}
