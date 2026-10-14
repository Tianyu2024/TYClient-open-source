package cn.tianyu.client.Network;

import cn.tianyu.client.Network.IRC.IRC;
import cn.tianyu.client.Network.api.packet.Packet;
import cn.tianyu.client.TYClient;
import cn.tianyu.client.util.impl.client.DebugUtil;
import com.yumegod.obfuscation.Native;
import com.yumegod.obfuscation.StringObfuscate;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: CN_tianyu
 */

@Native
@StringObfuscate
public class NetWorkManager {
    @Getter
    @Setter
    public String uuid;
    @Getter
    public String username;
    @Getter
    public String password;
    @Getter
    public String Serverip = "就不告诉你啦啦啦，自己测试用debugip";
    @Getter
    public String DebugServerip = "127.0.0.1";

    public static NetWorkManager instance;
    public Packet packet = new Packet();

    @Getter
    public String thisClientName = TYClient.NAME;

    public NetWorkManager(String uuid,String username,String password){
        this.uuid = uuid;
        this.username = username;
        this.password = password;
    }

    public NetWorkManager(){
        instance = this;
    }

    @Native
    public void sendPacket(Packet packet,String m) {
        if(packet.getPackerType() == "I01"){
            IRC.send("TYD" + DebugUtil.getUsername() + "." + m);
        }else if(packet.getPackerType() == "I02"){
            IRC.send("TYC" + DebugUtil.getUsername() + "." + m);
        }
    }
}
