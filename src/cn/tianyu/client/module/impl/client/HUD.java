package cn.tianyu.client.module.impl.client;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.render.EventPostRender;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.font.RapeMasterFontManager;
import cn.tianyu.client.gui.Login.ShitLoginScreen;
import cn.tianyu.client.gui.notification.Notification;
import cn.tianyu.client.gui.notification.NotificationManager;
import cn.tianyu.client.gui.ui.UiModule;
import cn.tianyu.client.gui.ui.modules.ModuleList;
import cn.tianyu.client.gui.ui.modules.TargetHud;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.api.DefaultEnabled;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.annotations.Animation;
import cn.tianyu.client.util.annotations.Direction;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.util.impl.render.ColorUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ColorValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.tianyu.client.font.FontManager.*;

/**
 * @author CN_tianyu
 */

@DefaultEnabled
public class HUD extends Module {
    private long startTime;
    private float hueSpeed = 0.0005f;
    public static final BooleanValue importantModules = new BooleanValue("Arraylist-Important", true);
    public static final BooleanValue hLine = new BooleanValue("Arraylist-HLine", true);
    public static final NumberValue height = new NumberValue("Arraylist-Height", 11.0, 9.0, 20.0, 1.0);
    public static final BooleanValue background = new BooleanValue("Arraylist-Background", true);
    public static final NumberValue backgroundAlpha = new NumberValue("Arraylist-Background Alpha", 0.35, 0.01, 1.0, 0.01);
    //public static final ModeValue animation = new ModeValue("Arraylist-Animation", ModuleList.ANIM.ScaleIn, ModuleList.ANIM.values());

    public static BooleanValue ClientHUD = new BooleanValue("Client HUD", true);
    public static BooleanValue ArmorHUD = new BooleanValue("Armor HUD", true);

    public static BooleanValue TargetHUD = new BooleanValue("TargetHUD", true);
    public static BooleanValue ArraylistHUD = new BooleanValue("Arraylist HUD", true);
    public static ColorValue mainColor = new ColorValue("Main Color", new java.awt.Color(0, 166, 255));
    public int offsetValue = 0;
    public List<Module> modules;

    public HUD() {
        super("HUD", "仿外挂界面", Category.client);
        this.startTime = System.currentTimeMillis();
    }

    public static java.awt.Color color(int tick) {
        java.awt.Color textColor;
        textColor = ColorUtil.fade(5, tick * 20, new java.awt.Color(mainColor.getColorInt()), 1);
        return textColor;
    }



    @EventTarget
    public void onPostRender(EventPostRender e){
        Minecraft minecraft = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(minecraft);
        drawArmor(scaledResolution);
    }

    public void drawNotifications() {
        ScaledResolution sr = new ScaledResolution(mc);
        float yOffset = 0;
        int notificationHeight = 0, notificationWidth = 0, actualOffset;

        NotificationManager.setToggleTime(2f);

        for (Notification notification : NotificationManager.getNotifications()) {
            Animation animation = notification.getAnimation();
            animation.setDirection(notification.getTimerUtil().hasTimeElapsed((long) notification.getTime()) ? Direction.BACKWARDS : Direction.FORWARDS);

            if (animation.finished(Direction.BACKWARDS)) {
                NotificationManager.getNotifications().remove(notification);
                continue;
            }

            float x, y;
            animation.setDuration(200);
            actualOffset = 3;
//            switch (notiMode.getValue()) {
//                case "Xylitol":
//                    notificationHeight = 32;
//                    notificationWidth = Math.max(font20.getStringWidth(notification.getDescription()), font20.getStringWidth(notification.getTitle())) + 15;
//                    break;
//                case "Lettuce":
//                    notificationHeight = 23;
//                    notificationWidth = font20.getStringWidth(notification.getDescription()) + 25;
//                    break;
//                case "Clear":
//                    notificationHeight = 23;
//                    notificationWidth = font20.getStringWidth(notification.getDescription()) + 15;
//                    break;
//                case "Simple":
//                    notificationHeight = 6 + (axBold20.getHeight() - 2) + 2 + (font18.getHeight() - 2);
//                    notificationWidth = font18.getStringWidth(notification.getDescription()) + 20;
//                    break;
//            }

            notificationHeight = 25;
            RapeMasterFontManager tahomaFont18 = FontManager.xyfont18;
            RapeMasterFontManager tahomaFont14 = xyfont14;
            notificationWidth = (int) Math.max(tahomaFont18.getStringWidth(notification.getTitle()), tahomaFont14.getStringWidth(notification.getDescription())) + 30;

            x = (float) (sr.getScaledWidth() - (notificationWidth) * animation.getOutput());
            y = sr.getScaledHeight() - (yOffset + 18 + offsetValue + notificationHeight + 15);
            //switch (notiMode.getValue()) {
//                case "Lettuce":
//                    notification.drawLettuce(x, y, notificationWidth, notificationHeight);
//                    break;
//                case "Xylitol":
//                    notification.drawXylitol(x, y, notificationWidth, notificationHeight);
//                    break;
//                case "Clear":
//                    notification.drawClear(x, y, notificationWidth, notificationHeight);
//                    break;
                //case "Simple":
                    //notification.drawSimple(x, y, notificationWidth, notificationHeight);
                    //break;
            //}
            notification.drawExhibition(x, y, notificationWidth, notificationHeight);

            yOffset += (notificationHeight + actualOffset) * animation.getOutput();
        }
    }

    static String username;
    public static boolean L = true;

    @EventTarget
    public void onRender2D(EventRender2D e) {
        Minecraft minecraft = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(minecraft);
        int screenWidth = scaledResolution.getScaledWidth();

        //String user = DebugUtil.getPlayerName();
        //String sb = TYClient.NAME + " | " + (DebugUtil.isDev() ? "Dev":"User") + " | "+ user;
        //loat sx = (float) screenWidth / 2 - (float) tenacityBoldFont20.getStringWidth(sb) / 2;
        //RoundedUtils.drawRound(sx - 10,40,tenacityBoldFont20.getStringWidth(sb) + sx + 10,40,6,new java.awt.Color(0, 0, 0));
        //tenacityBoldFont20.drawString(sb,sx,30, java.awt.Color.WHITE.getRGB());
        if(ClientHUD.getValue()){
            int s = 0;
            int y = 0;
            xyfont14.drawString(TYClient.NAME + TYClient.VERSION +
                            "| " + DebugUtil.getUsername() + " FPS:" + Minecraft.getDebugFPS(),
                    s,y , new Color(255, 255, 255, 255).getRGB());
        }

        UiModule Thud = TYClient.instance.uiManager.getModule(TargetHud.class);
        Thud.setState(TargetHUD.getValue());
        UiModule Arraylist = TYClient.instance.uiManager.getModule(ModuleList.class);
        Arraylist.setState(ArraylistHUD.getValue());

        //sb = isDevrender();
        username = ShitLoginScreen.User;
        //tenacityBoldFont20.drawString("TYClient " + TYClient.VERSION + " | "+ sb + username,0,0,new java.awt.Color(255, 255, 255).getRGB());
        drawNotifications();
    }

    private void drawArmor(ScaledResolution sr) {
        if (ArmorHUD.get()) {
            List<ItemStack> equipment = new ArrayList<>();
            boolean inWater = mc.thePlayer.isEntityAlive() && mc.thePlayer.isInsideOfMaterial(Material.water);
            int x = -74;
            ItemStack armorPiece;

            for (int i = 3; i >= 0; i--) {
                if ((armorPiece = mc.thePlayer.inventory.armorInventory[i]) != null) {
                    equipment.add(armorPiece);
                }
            }
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            if (heldItem != null && heldItem.getItem() != null) {
                equipment.add(heldItem);
            }
            Collections.reverse(equipment);

            // 绘制盔甲和附魔效果
            for (ItemStack itemStack : equipment) {
                armorPiece = itemStack;
                RenderHelper.enableGUIStandardItemLighting();
                x += 15;
                if (armorPiece == mc.thePlayer.getHeldItem()) {
                    x -= 15; // 如果是手持物品，调整位置，与盔甲相隔一定距离
                }
                GlStateManager.pushMatrix();
                GlStateManager.disableAlpha();
                GlStateManager.clear(256);
                mc.getRenderItem().zLevel = -200.0F;
                int s = mc.thePlayer.capabilities.isCreativeMode ? 15 : 0;
                mc.getRenderItem().renderItemAndEffectIntoGUI(armorPiece, -x + sr.getScaledWidth() / 2 - 4,
                        (int) (sr.getScaledHeight() - (inWater ? 65 : 55) + s - 5 - (float) (16 * GuiChat.openingAnimation.getOutput())));

                // 如果物品数量大于1，渲染物品数量
                if (armorPiece.stackSize > 1) {
                    String count = String.valueOf(armorPiece.stackSize);
                    mc.fontRendererObj.drawStringWithShadow(count,
                            -x + sr.getScaledWidth() / 2 - 4 + 19 - 2 - mc.fontRendererObj.getStringWidth(count),
                            sr.getScaledHeight() - (inWater ? 65 : 55) + s - 5 - (float) (16 * GuiChat.openingAnimation.getOutput()) + 9, 16777215);
                }

                mc.getRenderItem().zLevel = 0.0F;
                GlStateManager.disableBlend();
                GlStateManager.disableDepth();
                GlStateManager.disableLighting();
                GlStateManager.enableDepth();
                GlStateManager.enableAlpha();
                GlStateManager.popMatrix();
                armorPiece.getEnchantmentTagList();

                // 绘制附魔效果
                RenderUtil.drawExhiEnchants(armorPiece, -x + sr.getScaledWidth() / 2 - 4, sr.getScaledHeight() - (inWater ? 65 : 55) + s - 5 - (float)(16 * GuiChat.openingAnimation.getOutput()));
            }
        }
    }

}