package cn.tianyu.client.util.impl.client;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.EntityLivingBase;

import static cn.tianyu.client.TYClient.mc;

/**
 * @Author: CN_tianyu
 */
public class TabUtil {
    public static boolean inTab(EntityLivingBase entity) {
        for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap())
            if (info != null && info.getGameProfile() != null && info.getGameProfile().getName().contains(entity.getName()))
                return true;

        return false;
    }

    public static boolean inTab(String name) {
        for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap())
            if (info != null && info.getGameProfile() != null && info.getGameProfile().getName().contains(name))
                return true;

        return false;
    }
}
