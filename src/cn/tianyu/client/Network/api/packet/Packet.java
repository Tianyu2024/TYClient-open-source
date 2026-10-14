package cn.tianyu.client.Network.api.packet;

import cn.tianyu.client.Network.api.packet.Packets.I01Devmsg;
import cn.tianyu.client.Network.api.packet.Packets.I02Usermsg;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: CN_tianyu
 */
public class Packet {
    @Getter
    private final List<Packet> packet = new ArrayList<>();
    @Getter
    @Setter
    public String PackerType;

    public void addModule(Packet packet){
        this.packet.add(packet);
    }

    public void init(){
        addModule(new I01Devmsg());
        addModule(new I02Usermsg());
    }
}
