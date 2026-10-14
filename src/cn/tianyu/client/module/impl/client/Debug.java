package cn.tianyu.client.module.impl.client;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.value.impl.BooleanValue;

/**
 * @Author: CN_tianyu
 */
public class Debug extends Module {
    private boolean PacketEvent;
    public BooleanValue PacketEventDebug = new BooleanValue("PacketEventDebug",true);
    public Debug(){
        super("Debug","Debug", Category.client);
    }
    @Override
    public void onEnable(){
        if(PacketEventDebug.getValue() && !checknull()){
            sendPackerEvent();
            DebugUtil.log(mc.thePlayer.rotationYaw + "  " + mc.thePlayer.rotationPitch);

        }
    }
    @EventTarget
    public void onPacket(EventPacket e){
        PacketEvent = e.isCancelled();
    }
    private void sendPackerEvent(){
        DebugUtil.Clientlog(PacketEvent);
    }
}
