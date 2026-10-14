package net.minecraft.client.gui;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.events.game.EventChat;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.font.RapeMasterFontManager;
import cn.tianyu.client.module.impl.better.BetterChat;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.annotations.AnimationUtil;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.List;

public class GuiNewChat extends Gui {

    private final List<String> messageHistory = new LinkedList<>();
    private final Map<String, Integer> repeatCounter = new HashMap<>();

    private static final Logger logger = LogManager.getLogger();
    private final Minecraft mc;
    private final List<String> sentMessages = Lists.<String>newArrayList();
    private final List<ChatLine> chatLines = Lists.<ChatLine>newArrayList();
    private final List<ChatLine> field_146253_i = Lists.<ChatLine>newArrayList();
    private int scrollPos;
    private boolean isScrolled;

    public float percentComplete;
    public int newLines;
    public int chatPos = 20;
    RapeMasterFontManager chatHDFont;
    public long prevMillis = -1L;

    public GuiNewChat(Minecraft mcIn) {
        this.mc = mcIn;
    }

    public void updatePercentage(long diff) {
        if (percentComplete < 1.0f) {
            percentComplete += 0.004f * diff;
        }
        percentComplete = AnimationUtil.clamp(percentComplete, 0.0f, 1.5f);
    }

    public void drawChat(int p_146230_1_) {
        if (BetterChat.getInstance.isEnabled() && BetterChat.getInstance.animations.is("Height")) {
            if (prevMillis == -1L) {
                prevMillis = System.currentTimeMillis();
                return;
            }
        }
        if (BetterChat.getInstance.enableFont.get()) {
            chatHDFont = FontManager.arial17;//font 17
        } else {
            chatHDFont = null;
        }
        long current = System.currentTimeMillis();
        long diff = current - prevMillis;
        if (BetterChat.getInstance.isEnabled() && BetterChat.getInstance.animations.is("Height")) {
            prevMillis = current;
            updatePercentage(diff);
        }
        float t = percentComplete;
        float percent = 1.0f - (t -= 1.0f) * t * t * t;
        if (BetterChat.getInstance.isEnabled() && BetterChat.getInstance.animations.is("Height")) {
            percent = AnimationUtil.clamp(percent, 0.0f, 1.5f);
        }
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int i = this.getLineCount();
            boolean flag = false;
            int j = 0;
            int k = this.field_146253_i.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (k > 0) {
                if (this.getChatOpen()) {
                    flag = true;
                }

                float f1 = this.getChatScale();
                int l = MathHelper.ceiling_float_int((float) this.getChatWidth() / f1);
                GlStateManager.pushMatrix();
                if (BetterChat.getInstance.isEnabled()) {
                    if (BetterChat.getInstance.animations.is("Height")) {
                        if (!this.isScrolled) {
                            GlStateManager.translate(2.0f, chatPos - (16 * GuiChat.openingAnimation.getOutput())
                                    + (9.0f - 9.0f * percent) * f1, 0.0f);
                        }
                    } else {
                        GlStateManager.translate(2.0F, chatPos - (16 * GuiChat.openingAnimation.getOutput()), 0.0F);
                    }
                } else {
                    GlStateManager.translate(2.0F, chatPos - (16 * GuiChat.openingAnimation.getOutput()), 0.0F);
                }
                //GlStateManager.translate(2.0F, 20.0F, 0.0F);
                GlStateManager.scale(f1, f1, 1.0F);

                for (int i1 = 0; i1 + this.scrollPos < this.field_146253_i.size() && i1 < i; ++i1) {
                    ChatLine chatline = (ChatLine) this.field_146253_i.get(i1 + this.scrollPos);

                    if (chatline != null) {
                        int j1 = p_146230_1_ - chatline.getUpdatedCounter();

                        if (j1 < 200 || flag) {
                            double d0 = (double) j1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 = d0 * 10.0D;
                            d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
                            d0 = d0 * d0;
                            int l1 = (int) (255.0D * d0);

                            if (flag) {
                                l1 = 255;
                            }

                            l1 = (int) ((float) l1 * f);
                            ++j;

                            float x = 0, y = 0;


                            if (l1 > 3) {
                                int i2 = 0;
                                int j2 = -i1 * 9;
                                //RoundedUtils.drawRound(i2, j2 - 9,i2 + i2 + l + 4,j2 + j2 - 9,7.0f,new Color(1,1,1));
                                //drawRect(i2, j2 - 9, i2 + l + 4, j2, l1 / 2 << 24);
                                if (!BetterChat.getInstance.isEnabled()) {
                                    x = i2;
                                    y = j2;
                                    drawRect(x, y - 9, x + l + 4, y, l1 / 2 << 24);
                                } else {
                                    if (BetterChat.getInstance.animations.is("Off") || BetterChat.getInstance.animations.is("Height")) {
                                        x = i2;
                                        y = j2;
                                    }
                                    if (BetterChat.getInstance.backgroundShadow.is("Normal")) {
                                        drawRect(x, y - (BetterChat.getInstance.enableFont.get() ? 11f : 9f), x + l + 4, y - (BetterChat.getInstance.enableFont.get() ? 2f : 0f), l1 / 2 << 24);
                                    } else if (BetterChat.getInstance.backgroundShadow.is("Visuals")) {
                                        String s = chatline.getChatComponent().getFormattedText();
                                        if (BetterChat.getInstance.enableFont.get()) {
                                            RenderUtil.drawCustomImage(x + 1f, y - 17f,
                                                    chatHDFont.getStringWidth(s.replaceAll("§.", "")) + 15, 20,
                                                    chatHDFont.getStringWidth(s.replaceAll("§.", "")) + 17, 23.0f, new ResourceLocation("client/arraylistshadows.png"));
                                        } else {
                                            RenderUtil.drawCustomImage(x, y - 15, mc.fontRendererObj.getStringWidth(s.replaceAll("§.", "")) + 15, 20,
                                                    mc.fontRendererObj.getStringWidth(s.replaceAll("§.", "")) + 17, 23.0f, new ResourceLocation("client/arraylistshadows.png"));

                                        }
                                    }
                                }
                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();

                                // ------------- 头像绘制开始 -------------
                                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                                EntityPlayer player = null;
                                for (EntityPlayer p : mc.theWorld.playerEntities) {
                                    if (s.contains(p.getName())) {
                                        player = p;
                                        break;
                                    }
                                }

                                int headOffset = 0;
                                if (player != null) {
                                    headOffset = 8;
                                    GlStateManager.pushMatrix();
                                    GlStateManager.translate(x, j2 - 8, 0);
                                    GlStateManager.scale(0.75f, 0.75f, 1);
                                    ResourceLocation skin = AbstractClientPlayer.getLocationSkin(player.getName());
                                    AbstractClientPlayer.getDownloadImageSkin(skin, player.getName());
                                    mc.getTextureManager().bindTexture(skin);
                                    drawModalRectWithCustomSizedTexture(0, 0, 8, 8, 8, 8, 64, 64);
                                    GlStateManager.popMatrix();
                                }
                                // ------------- 头像绘制结束 -------------

                                if (!BetterChat.getInstance.isEnabled()) {
                                    mc.fontRendererObj.drawStringWithShadow(s, i2 + headOffset, (j2 - 8), 16777215 + (l1 << 24));
                                } else {
                                    if (BetterChat.getInstance.animations.is("Height")) {
                                        if (i1 <= newLines) {
                                            if (BetterChat.getInstance.enableFont.get()) {
                                                chatHDFont.drawString(
                                                        s, (float) i2 + 3 + headOffset, (j2 - 12),
                                                        16777215 + ((int) (l1 * percent) << 24));
                                            } else {
                                                drawString(s, x + 2f + headOffset, j2 - 9f, 16777215 + ((int) (l1 * percent) << 24));
                                            }
                                        } else {
                                            if (BetterChat.getInstance.enableFont.get()) {
                                                chatHDFont.drawString(s,
                                                        (float) i2 + 3 + headOffset, (float) (j2 - 12), 16777215 + (l1 << 24));
                                            } else {
                                                drawString(s, x + 2f + headOffset, j2 - 9f, 16777215 + (l1 << 24));
                                            }
                                        }
                                    } else if (BetterChat.getInstance.animations.is("Off")) {
                                        if (BetterChat.getInstance.enableFont.get()) {
                                            chatHDFont.drawString(
                                                    s, (float) i2 + 1 + headOffset, (float) (j2 - 12), 16777215 + (l1 << 24));
                                        } else {
                                            drawString(s, x + 2f + headOffset, (j2 - 9f), 16777215 + (l1 << 24));
                                        }
                                    }
                                }
                                //this.mc.fontRendererObj.drawStringWithShadow(s, (float) i2, (float) (j2 - 8), 16777215 + (l1 << 24));
                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();
                            }
                        }
                    }
                }

                if (flag) {
                    int k2 = this.mc.fontRendererObj.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int l2 = k * k2 + k;
                    int i3 = j * k2 + j;
                    int j3 = this.scrollPos * i3 / k;
                    int k1 = i3 * i3 / l2;

                    if (l2 != i3) {
                        int k3 = j3 > 0 ? 170 : 96;
                        int l3 = this.isScrolled ? 13382451 : 3355562;
                        drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                        drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }

    public int drawString(String text, float x, float y, int color) {
        return mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
    }

    /**
     * Clears the chat.
     */
//    public void clearChatMessages() {
//        this.field_146253_i.clear();
//        this.chatLines.clear();
//        this.sentMessages.clear();
//    }
    public void clearChatMessages() {
        this.field_146253_i.clear();
        this.chatLines.clear();
        // 新增以下两行
        this.messageHistory.clear();
        this.repeatCounter.clear();
        this.scrollPos = 0;
    }

    public void printChatMessage(IChatComponent p_146227_1_) {
        this.printChatMessageWithOptionalDeletion2(p_146227_1_, 0);
    }

    /**
     * prints the ChatComponent to Chat. If the ID is not 0, deletes an existing Chat Line of that ID from the GUI
     */
//    public void printChatMessageWithOptionalDeletion(IChatComponent p_146234_1_, int p_146234_2_) {
//        EventChat e = new EventChat(p_146234_1_.getUnformattedText(), p_146234_1_, field_146253_i);
//        TYClient.instance.eventManager.callEvent(e);
//        if (e.isCancelled()) {
//            return;
//        }
//        if (BetterChat.getInstance.animations.is("Height")) {
//            percentComplete = 0.0f;
//        }
//        this.setChatLine(p_146234_1_, p_146234_2_, this.mc.ingameGUI.getUpdateCounter(), false);
//        logger.info("[CHAT] " + p_146234_1_.getUnformattedText());
//    }
    public void printChatMessageWithOptionalDeletion2(IChatComponent chatComponent, int chatLineId) {
        EventChat e = new EventChat(chatComponent.getUnformattedText(), chatComponent, field_146253_i);
        TYClient.instance.eventManager.callEvent(e);
        if (e.isCancelled()) {
            return;
        }
        if (BetterChat.getInstance.animations.is("Height")) {
            percentComplete = 0.0f;
        }

        String rawMessage = chatComponent.getUnformattedText().trim();
        String cleanMessage = rawMessage.replaceAll("§[0-9a-fk-or]", "");

        // 只检查【最后一条】消息是否相同 → 仅相邻折叠
        if (!messageHistory.isEmpty() && messageHistory.get(messageHistory.size() - 1).equals(cleanMessage)) {
            int count = repeatCounter.getOrDefault(cleanMessage, 1) + 1;
            repeatCounter.put(cleanMessage, count);

            // 只删除【最后一条】重复消息，不删历史
            if (!field_146253_i.isEmpty()) {
                ChatLine lastLine = field_146253_i.get(0);
                String lastText = lastLine.getChatComponent().getUnformattedText().trim().replaceAll(" \\[\\d+\\]", "");
                if (lastText.equals(rawMessage)) {
                    field_146253_i.remove(0);
                }
            }

            if (!chatLines.isEmpty()) {
                ChatLine lastChatLine = chatLines.get(0);
                String lastText = lastChatLine.getChatComponent().getUnformattedText().trim().replaceAll(" \\[\\d+\\]", "");
                if (lastText.equals(rawMessage)) {
                    chatLines.remove(0);
                }
            }

            IChatComponent foldedMsg = new ChatComponentText(rawMessage + " [" + count + "]");
            this.addSingleChatLine(foldedMsg, chatLineId, this.mc.ingameGUI.getUpdateCounter());
        } else {
            repeatCounter.put(cleanMessage, 1);
            messageHistory.add(cleanMessage);

            if (messageHistory.size() > 50) {
                messageHistory.remove(0);
            }

            this.addSingleChatLine(chatComponent, chatLineId, this.mc.ingameGUI.getUpdateCounter());
        }

        Iterator<Map.Entry<String, Integer>> it = repeatCounter.entrySet().iterator();
        while (it.hasNext()) {
            if (!messageHistory.contains(it.next().getKey())) {
                it.remove();
            }
        }
        logger.info("[CHAT] " + chatComponent.getUnformattedText());
    }

    private void addSingleChatLine(IChatComponent component, int lineId, int updateCounter) {
        if (lineId != 0) {
            this.deleteChatLine(lineId);
        }

        int i = MathHelper.floor_float((float) this.getChatWidth() / this.getChatScale());
        List<IChatComponent> list = GuiUtilRenderComponents.func_178908_a(component, i, this.mc.fontRendererObj, false, false);
        boolean flag = this.getChatOpen();
        if (BetterChat.getInstance.animations.is("Height")) {
            newLines = list.size() - 1;
        }

        for (IChatComponent ichatcomponent : list) {
            if (flag && this.scrollPos > 0) {
                this.isScrolled = true;
                this.scroll(1);
            }
            this.field_146253_i.add(0, new ChatLine(updateCounter, ichatcomponent, lineId));
        }

        while (this.field_146253_i.size() > 100) {
            this.field_146253_i.remove(this.field_146253_i.size() - 1);
        }

        chatLines.add(0, new ChatLine(updateCounter, component, lineId));
        while (chatLines.size() > 100) {
            chatLines.remove(chatLines.size() - 1);
        }
    }

    private void setChatLine(IChatComponent p_146237_1_, int p_146237_2_, int p_146237_3_, boolean p_146237_4_) {
        if (p_146237_2_ != 0) {
            this.deleteChatLine(p_146237_2_);
        }

        int i = MathHelper.floor_float((float) this.getChatWidth() / this.getChatScale());
        List<IChatComponent> list = GuiUtilRenderComponents.func_178908_a(p_146237_1_, i, this.mc.fontRendererObj, false, false);
        boolean flag = this.getChatOpen();
        if (BetterChat.getInstance.animations.is("Height")) {
            newLines = list.size() - 1;
        }

        for (IChatComponent ichatcomponent : list) {
            if (flag && this.scrollPos > 0) {
                this.isScrolled = true;
                this.scroll(1);
            }

            this.field_146253_i.add(0, new ChatLine(p_146237_3_, ichatcomponent, p_146237_2_));
        }

        while (this.field_146253_i.size() > 100) {
            this.field_146253_i.remove(this.field_146253_i.size() - 1);
        }

        if (!p_146237_4_) {
            this.chatLines.add(0, new ChatLine(p_146237_3_, p_146237_1_, p_146237_2_));

            while (this.chatLines.size() > 100) {
                this.chatLines.remove(this.chatLines.size() - 1);
            }
        }
    }

    public void refreshChat() {
        this.field_146253_i.clear();
        this.resetScroll();

        for (int i = this.chatLines.size() - 1; i >= 0; --i) {
            ChatLine chatline = (ChatLine) this.chatLines.get(i);
            this.setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
        }
    }

    public List<String> getSentMessages() {
        return this.sentMessages;
    }

    /**
     * Adds this string to the list of sent messages, for recall using the up/down arrow keys
     */
    public void addToSentMessages(String p_146239_1_) {
        if (this.sentMessages.isEmpty() || !((String) this.sentMessages.get(this.sentMessages.size() - 1)).equals(p_146239_1_)) {
            this.sentMessages.add(p_146239_1_);
        }
    }

    /**
     * Resets the chat scroll (executed when the GUI is closed, among others)
     */
    public void resetScroll() {
        this.scrollPos = 0;
        this.isScrolled = false;
    }

    /**
     * Scrolls the chat by the given number of lines.
     */
    public void scroll(int p_146229_1_) {
        this.scrollPos += p_146229_1_;
        int i = this.field_146253_i.size();

        if (this.scrollPos > i - this.getLineCount()) {
            this.scrollPos = i - this.getLineCount();
        }

        if (this.scrollPos <= 0) {
            this.scrollPos = 0;
            this.isScrolled = false;
        }
    }

    /**
     * Gets the chat component under the mouse
     */
    public IChatComponent getChatComponent(int p_146236_1_, int p_146236_2_) {
        if (!this.getChatOpen()) {
            return null;
        } else {
            ScaledResolution scaledresolution = new ScaledResolution(this.mc);
            int i = scaledresolution.getScaleFactor();
            float f = this.getChatScale();
            int j = p_146236_1_ / i - 3;
            int k = p_146236_2_ / i - 27;
            j = MathHelper.floor_float((float) j / f);
            k = MathHelper.floor_float((float) k / f);

            if (j >= 0 && k >= 0) {
                int l = Math.min(this.getLineCount(), this.field_146253_i.size());

                if (j <= MathHelper.floor_float((float) this.getChatWidth() / this.getChatScale()) && k < this.mc.fontRendererObj.FONT_HEIGHT * l + l) {
                    int i1 = k / this.mc.fontRendererObj.FONT_HEIGHT + this.scrollPos;

                    if (i1 >= 0 && i1 < this.field_146253_i.size()) {
                        ChatLine chatline = (ChatLine) this.field_146253_i.get(i1);
                        int j1 = 0;

                        for (IChatComponent ichatcomponent : chatline.getChatComponent()) {
                            if (ichatcomponent instanceof ChatComponentText) {
                                j1 += this.mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText) ichatcomponent).getChatComponentText_TextValue(), false));

                                if (j1 > j) {
                                    return ichatcomponent;
                                }
                            }
                        }
                    }

                    return null;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Returns true if the chat GUI is open
     */
    public boolean getChatOpen() {
        return this.mc.currentScreen instanceof GuiChat;
    }

    /**
     * finds and deletes a Chat line by ID
     */
    public void deleteChatLine(int p_146242_1_) {
        Iterator<ChatLine> iterator = this.field_146253_i.iterator();

        while (iterator.hasNext()) {
            ChatLine chatline = (ChatLine) iterator.next();

            if (chatline.getChatLineID() == p_146242_1_) {
                iterator.remove();
            }
        }

        iterator = this.chatLines.iterator();

        while (iterator.hasNext()) {
            ChatLine chatline1 = (ChatLine) iterator.next();

            if (chatline1.getChatLineID() == p_146242_1_) {
                iterator.remove();
                break;
            }
        }
    }

    public int getChatWidth() {
        return calculateChatboxWidth(this.mc.gameSettings.chatWidth);
    }

    public int getChatHeight() {
        return calculateChatboxHeight(this.getChatOpen() ? this.mc.gameSettings.chatHeightFocused : this.mc.gameSettings.chatHeightUnfocused);
    }

    /**
     * Returns the chatscale from mc.gameSettings.chatScale
     */
    public float getChatScale() {
        return this.mc.gameSettings.chatScale;
    }

    public static int calculateChatboxWidth(float p_146233_0_) {
        int i = 320;
        int j = 40;
        return MathHelper.floor_float(p_146233_0_ * (float) (i - j) + (float) j);
    }

    public static int calculateChatboxHeight(float p_146243_0_) {
        int i = 180;
        int j = 20;
        return MathHelper.floor_float(p_146243_0_ * (float) (i - j) + (float) j);
    }

    public int getLineCount() {
        return this.getChatHeight() / 9;
    }
}