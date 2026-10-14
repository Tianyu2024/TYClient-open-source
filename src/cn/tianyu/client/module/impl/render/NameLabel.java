//package cn.tianyu.client.module.impl.render;
//
//import cn.tianyu.client.TYClient;
//import cn.tianyu.client.module.Category;
//import cn.tianyu.client.module.Module;
//import cn.tianyu.client.module.api.DefaultEnabled;
//import cn.tianyu.client.module.impl.misc.HackerDetector;
//import cn.tianyu.client.util.RenderUtil;
//import cn.tianyu.client.util.impl.client.DebugUtil;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.entity.EntityPlayerSP;
//import net.minecraft.client.gui.inventory.GuiInventory;
//import net.minecraft.entity.Entity;
//import net.minecraft.entity.EntityLivingBase;
//import net.minecraft.entity.player.EntityPlayer;
//import net.minecraft.util.EnumChatFormatting;
//
////@DefaultEnabled
//public class NameLabel extends Module {
//
//    private static boolean isOriginalNameTagEnabled = true;
//
//    public NameLabel() {
//        super("NameLabel", "名字标签", Category.render);
//    }
//
//    public static boolean isOriginalNameTagEnabled() {
//        return isOriginalNameTagEnabled;
//    }
//
//    @Override
//    public void onEnable() {
//        super.onEnable();
//        isOriginalNameTagEnabled = false;
//    }
//
//    @Override
//    public void onRender(float partialTicks) {
//        Minecraft mc = Minecraft.getMinecraft();
//
//        EntityPlayerSP player = mc.thePlayer;
//
//        if (mc.currentScreen instanceof GuiInventory) {
//            renderPlayerName(player, partialTicks);
//        } else if (mc.gameSettings.thirdPersonView == 1 || mc.gameSettings.thirdPersonView == 2) {
//            renderPlayerName(player, partialTicks);
//        }
//
//        // 渲染其他实体的名字
//        for (Entity entity : mc.theWorld.loadedEntityList) {
//            if (entity instanceof EntityLivingBase) {
//                if (entity instanceof EntityPlayerSP) {
//                    continue; // 跳过玩家自己
//                }
//
//                EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
//                LiveUser liveUser = null;
//
//                //
//                if (liveUser != null) {
//                    String displayName =  EnumChatFormatting.RESET + player.getDisplayName().getFormattedText();
//                    RenderUtil.renderClientLabel(entityLivingBase, displayName, partialTicks);
//                } else{
//                    HackerDetector hackerDetector = (HackerDetector) TYClient.moduleManager.getByClass(HackerDetector.class);
//                    boolean hacker = hackerDetector.isEnabled() && hackerDetector.isHacker(player);
//                    if(hacker){
//                        String displayName = "[SB-Hacker] " + entityLivingBase.getDisplayName().getFormattedText();
//                        RenderUtil.renderLabel(entityLivingBase, displayName, partialTicks);
//                    }else{
//                        String displayName = entityLivingBase.getDisplayName().getFormattedText();
//                        RenderUtil.renderLabel(entityLivingBase, displayName, partialTicks);
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * render this player's name
//     */
//    private void renderPlayerName(EntityPlayerSP player, float partialTicks) {
//        if (player != null) {
//            String displayName = player.getDisplayName().getFormattedText();
//            RenderUtil.renderClientLabel(player, LiveComponent.getLiveUserDisplayName(LiveClient.INSTANCE.liveUser) + " " + EnumChatFormatting.WHITE + displayName, partialTicks);
//        }
//    }
//
//    @Override
//    public void onDisable() {
//        super.onDisable();
//        isOriginalNameTagEnabled = true;
//    }
//}