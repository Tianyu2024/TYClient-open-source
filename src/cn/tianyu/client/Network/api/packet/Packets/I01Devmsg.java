package cn.tianyu.client.Network.api.packet.Packets;

import cn.tianyu.client.Network.IRC.IRC;
import cn.tianyu.client.Network.api.packet.Packet;

/**
 * @Author: CN_tianyu
 */
public class I01Devmsg extends Packet {

    public I01Devmsg() {
        this.setPackerType("I01");
    }
}
