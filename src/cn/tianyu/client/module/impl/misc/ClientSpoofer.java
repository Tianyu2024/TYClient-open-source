package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.ModeValue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

/**
 * @Author: CN_tianyu
 */
public class ClientSpoofer extends Module {
    public ClientSpoofer() {
        super("ClientSpoofer","客户端冒充", Category.misc);
    }

    public final ModeValue mode = new ModeValue("Mode", "Lunar", "Feather" ,"Lunar");

    @EventTarget
    public void onPacket(EventPacket packetEvent) {
        if (packetEvent.getPacket() instanceof C17PacketCustomPayload) {
            C17PacketCustomPayload packet = (C17PacketCustomPayload) packetEvent.getPacket();
            String data;
            switch (mode.getValue()) {
                case "Lunar":
                    data = "lunarclient:v2.14.5-2411";
                case "Feather":
                    data = "Feather Forge";
                default: data = "";
            };

            ByteBuf byteBuf = Unpooled.wrappedBuffer(data.getBytes());
            PacketBuffer buffer = new PacketBuffer(Unpooled.wrappedBuffer(byteBuf));

            packet.setData(buffer);
        }
    }
}
