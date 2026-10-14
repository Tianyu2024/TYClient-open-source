package net.minecraft.client.resources;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.module.impl.better.Girl;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class DefaultPlayerSkin {
    /**
     * The default skin for the Steve model.
     */
    private static final ResourceLocation TEXTURE_STEVE = new ResourceLocation("textures/entity/steve.png");

    /**
     * The default skin for the Alex model.
     */
    private static final ResourceLocation TEXTURE_ALEX = new ResourceLocation("textures/entity/alex.png");
    private static final ResourceLocation TEXTURE_ALEX2 = new ResourceLocation("client/skin1.png");

    /**
     * Returns the default skind for versions prior to 1.8, which is always the Steve texture.
     */
    public static ResourceLocation getDefaultSkinLegacy() {
        return TEXTURE_STEVE;
    }

    /**
     * Retrieves the default skin for this player. Depending on the model used this will be Alex or Steve.
     */
    public static ResourceLocation getDefaultSkin(UUID playerUUID) {
        if(TYClient.moduleManager.getModule(Girl.class).isEnable()){
            return isSlimSkin(playerUUID) ? TEXTURE_ALEX2 : TEXTURE_STEVE;
        }else {
            return isSlimSkin(playerUUID) ? TEXTURE_ALEX : TEXTURE_STEVE;
        }
    }

    /**
     * Retrieves the type of skin that a player is using. The Alex model is slim while the Steve model is default.
     */
    public static String getSkinType(UUID playerUUID) {
        return isSlimSkin(playerUUID) ? "slim" : "default";
    }

    /**
     * Checks if a players skin model is slim or the default. The Alex model is slime while the Steve model is default.
     */
    private static boolean isSlimSkin(UUID playerUUID) {
        return (playerUUID.hashCode() & 1) == 1;
    }
}
