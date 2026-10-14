package cn.tianyu.client.util.impl.client;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

/**
 * @Author: CN_tianyu
 */
public class ServerUtil {

    private static Minecraft mc = Minecraft.getMinecraft();

    public static boolean isInLobby(){
        return checkEmerald() && checkCompass();
    }

    public static boolean checkCompass(){
        boolean compass = false;
        for (int i = 0; i < 9; i++) {
            final ItemStack stackInSlot = mc.thePlayer.inventory.getStackInSlot(i);

            if (stackInSlot != null && stackInSlot.getUnlocalizedName().toLowerCase().contains("compass")) {
                compass = true;
            }
        }
        return compass;
    }

    public static boolean checkEmerald(){
        boolean emerald = false;
        for (int i = 0; i < 9; i++) {
            final ItemStack stackInSlot = mc.thePlayer.inventory.getStackInSlot(i);

            if (stackInSlot != null && stackInSlot.getUnlocalizedName().toLowerCase().contains("emerald")) {
                emerald = true;
            }
        }
        return emerald;
    }
}
