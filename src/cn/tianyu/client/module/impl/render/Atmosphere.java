package cn.tianyu.client.module.impl.render;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

/**
 * @Author: CN_tianyu
 */
public class Atmosphere extends Module {

    private final NumberValue time = new NumberValue("Time", 18000, 0, 24000, 1000);


    public Atmosphere() {
        super("Atmosphere","气氛渲染", Category.render);
    }
    public static boolean overrideSnow = false;


    @Override
    public void onDisable(){
        overrideSnow = false;
        super.onDisable();
    }


    @EventTarget
    public void onTick(EventTick event) {
        if (mc.theWorld != null) {

            mc.theWorld.setWorldTime(time.getValue().longValue());

//            WorldInfo worldinfo = mc.theWorld.getWorldInfo();
//            if (mc.isSingleplayer()) {
//                World world = MinecraftServer.getServer().worldServers[0];
//                worldinfo = world.getWorldInfo();
//            }
//            worldinfo.setCleanWeatherTime(0);
//            worldinfo.setRainTime(Integer.MAX_VALUE);
//            worldinfo.setThunderTime(Integer.MAX_VALUE);
//            worldinfo.setRaining(true);
//            worldinfo.setThundering(false);
            mc.theWorld.setRainStrength(1);
            mc.theWorld.setThunderStrength(0);
        }
        overrideSnow = true;
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getPacket() instanceof S03PacketTimeUpdate) {
            event.setCancelled(true);
        }
    }
}
