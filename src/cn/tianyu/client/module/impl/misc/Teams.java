package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.player.PlayerUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Objects;

/**
 * @Author: CN_tianyu
 */
public class Teams extends Module {
    public Teams(){
        super("Teams","放置误伤队友", Category.misc);
    }

    private static final BooleanValue armorValue = new BooleanValue("ArmorColor", true);
    private static final BooleanValue colorValue = new BooleanValue("Color", true);
    private static final BooleanValue scoreboardValue = new BooleanValue("ScoreboardTeam", true);

    public static boolean isSameTeam(Entity entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            if (Objects.requireNonNull(TYClient.instance.moduleManager.getModule(Teams.class)).getState()) {
                return (armorValue.getValue() && PlayerUtil.armorTeam(entityPlayer)) ||
                        (colorValue.getValue() && PlayerUtil.colorTeam(entityPlayer)) ||
                        (scoreboardValue.getValue() && PlayerUtil.scoreTeam(entityPlayer));
            }
            return false;
        }
        return false;
    }
}
