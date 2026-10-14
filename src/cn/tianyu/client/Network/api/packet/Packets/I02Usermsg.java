package cn.tianyu.client.Network.api.packet.Packets;

import cn.tianyu.client.Network.IRC.IRC;
import cn.tianyu.client.Network.api.packet.Packet;

/**
 * @Author: CN_tianyu
 */
public class I02Usermsg extends Packet {


    public I02Usermsg(){
        this.setPackerType("I02");
    }
}
