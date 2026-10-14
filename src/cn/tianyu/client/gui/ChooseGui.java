package cn.tianyu.client.gui;

import cn.tianyu.client.Network.IRC.IRC;
import cn.tianyu.client.Network.NetWorkManager;
import cn.tianyu.client.Network.api.packet.Packets.I02Usermsg;
import cn.tianyu.client.TYClient;
import cn.tianyu.client.gui.MainMenuGui.MainMenuGui;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.util.impl.render.RoundedUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static cn.tianyu.client.Network.IRC.IRC.send;

/**
 * @Author: CN_tianyu
 */
public class ChooseGui extends GuiScreen {

    //"🏳️‍⚧"
    private static final int TITLE_Y = 30;
    private static final int BOX_WIDTH = 130;
    private static final int BOX_HEIGHT = 160;
    private static final int BOX_GAP_X = 20;
    private static final int BOX_GAP_Y = 25;
    private static final int ICON_SIZE = 90;
    private static final float RADIUS = 10f;
    private static final float OUTLINE_THICK = 1.2f;
    private static final Color BOX_BG = new Color(0, 0, 0, 160);
    private static final Color BOX_STROKE = new Color(45, 45, 45, 220);
    private static final Color SELECT_BG = new Color(25, 90, 160, 180);
    private static final Color SELECT_STROKE = new Color(90, 170, 255, 255);
    private static final int BACK_BTN_W = 85;
    private static final int BACK_BTN_H = 28;
    // 本地身份缓存文件
    private static final File LOCAL_IDENTITY_FILE = new File("client_identity.txt");
    // 和服务端完全一致加密密钥
    private static final byte[] CRYPT_KEY = "TYClient2026IRCSecretKey11451".getBytes();

    private final List<IdentityData> identityList = new ArrayList<>();
    private IdentityData selectedIdentity;

    public ChooseGui() {
        identityList.add(new IdentityData("普通用户", new ResourceLocation("client/identity/normal.png")));
        identityList.add(new IdentityData("男娘", new ResourceLocation("client/identity/boygirl.png")));
        identityList.add(new IdentityData("药娘", new ResourceLocation("client/identity/yoniang.png")));
        identityList.add(new IdentityData("日本用户", new ResourceLocation("client/identity/jp.png")));
        identityList.add(new IdentityData("台湾用户", new ResourceLocation("client/identity/tw.png")));
        identityList.add(new IdentityData("猫娘", new ResourceLocation("client/identity/catgirl.png")));
        identityList.add(new IdentityData("福瑞", new ResourceLocation("client/identity/furry.png")));
        // 加载本地上次选择的身份
        loadLocalIdentity();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        TYClient.instance.wallpaperEngine.render(width, height);
        super.drawScreen(mouseX, mouseY, partialTicks);

        String titleText = "选择身份";
        int titleWidth = fontRendererObj.getStringWidth(titleText);
        fontRendererObj.drawStringWithShadow(titleText, width / 2f - titleWidth / 2f, TITLE_Y, 0xFFFFFF);

        float backX = 12;
        float backY = 12;
        RoundedUtils.drawRoundOutline(backX, backY, BACK_BTN_W, BACK_BTN_H, 6f, 1f, new Color(20,20,20,180), new Color(70,70,70,220));
        String backText = "返回";
        int backTextW = fontRendererObj.getStringWidth(backText);
        fontRendererObj.drawStringWithShadow(backText, backX + BACK_BTN_W / 2f - backTextW / 2f, backY + (BACK_BTN_H - fontRendererObj.FONT_HEIGHT) / 2f, 0xffffff);

        int maxPerRow = (width - 60) / (BOX_WIDTH + BOX_GAP_X);
        if(maxPerRow <= 0) maxPerRow = 1;
        int totalPanelW = maxPerRow * BOX_WIDTH + (maxPerRow - 1) * BOX_GAP_X;
        float startX = width / 2f - totalPanelW / 2f;
        float startY = TITLE_Y + 40f;

        for (int i = 0; i < identityList.size(); i++) {
            IdentityData data = identityList.get(i);
            int col = i % maxPerRow;
            int row = i / maxPerRow;

            float boxX = startX + col * (BOX_WIDTH + BOX_GAP_X);
            float boxY = startY + row * (BOX_HEIGHT + BOX_GAP_Y);

            if(selectedIdentity != null && selectedIdentity == data){
                RoundedUtils.drawRoundOutline(boxX, boxY, BOX_WIDTH, BOX_HEIGHT, RADIUS, OUTLINE_THICK, SELECT_BG, SELECT_STROKE);
            }else{
                RoundedUtils.drawRoundOutline(boxX, boxY, BOX_WIDTH, BOX_HEIGHT, RADIUS, OUTLINE_THICK, BOX_BG, BOX_STROKE);
            }

            float iconX = boxX + (BOX_WIDTH - ICON_SIZE) / 2f;
            float iconY = boxY + 12f;
            RenderUtil.drawImage(data.texture, iconX, iconY, ICON_SIZE, ICON_SIZE);

            int textW = fontRendererObj.getStringWidth(data.name);
            float textX = boxX + BOX_WIDTH / 2f - textW / 2f;
            float textY = boxY + BOX_HEIGHT - 22f;
            fontRendererObj.drawStringWithShadow(data.name, textX, textY, 0xFFFFFF);

            if(selectedIdentity != null && selectedIdentity == data){
                String tag = "已选择";
                int tagW = fontRendererObj.getStringWidth(tag);
                fontRendererObj.drawStringWithShadow(tag, boxX + BOX_WIDTH / 2f - tagW / 2f, textY - 14, 0xFF88DDFF);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float backX = 12;
        float backY = 12;
        if(mouseX >= backX && mouseX <= backX + BACK_BTN_W && mouseY >= backY && mouseY <= backY + BACK_BTN_H){
            mc.displayGuiScreen(new MainMenuGui());
            return;
        }

        int maxPerRow = (width - 60) / (BOX_WIDTH + BOX_GAP_X);
        if(maxPerRow <= 0) maxPerRow = 1;
        int totalPanelW = maxPerRow * BOX_WIDTH + (maxPerRow - 1) * BOX_GAP_X;
        float startX = width / 2f - totalPanelW / 2f;
        float startY = TITLE_Y + 40f;

        for (int i = 0; i < identityList.size(); i++) {
            int col = i % maxPerRow;
            int row = i / maxPerRow;
            float boxX = startX + col * (BOX_WIDTH + BOX_GAP_X);
            float boxY = startY + row * (BOX_HEIGHT + BOX_GAP_Y);

            if (mouseX >= boxX && mouseX <= boxX + BOX_WIDTH && mouseY >= boxY && mouseY <= boxY + BOX_HEIGHT) {
                selectedIdentity = identityList.get(i);
                executeSelectLogic(selectedIdentity);
                return;
            }
        }
    }

    private void executeSelectLogic(IdentityData data) {
        // 1. 本地保存当前选择身份
        saveLocalIdentity(data.name);
        // 2. 组装IRC身份设置指令
        String rawCmd = "SETID|" + data.name;
        // 3. 加密发送给IRC服务器
        String encCmd = encrypt(rawCmd);
        //if(TYClient.instance.ircClient != null){
            IRC.sendRawMessage(encCmd);
        //}
        // 4. 选择完成关闭界面返回主菜单
        mc.displayGuiScreen(new MainMenuGui());
    }

    // 读取本地保存的身份
    private void loadLocalIdentity(){
        if(!LOCAL_IDENTITY_FILE.exists()) return;
        try(FileReader fr = new FileReader(LOCAL_IDENTITY_FILE)){
            char[] buf = new char[128];
            int len = fr.read(buf);
            String saveName = new String(buf,0,len).trim();
            for(IdentityData id : identityList){
                if(id.name.equals(saveName)){
                    selectedIdentity = id;
                    break;
                }
            }
        }catch (Exception e){}
    }

    // 保存身份到本地txt
    private void saveLocalIdentity(String name){
        try(FileWriter fw = new FileWriter(LOCAL_IDENTITY_FILE, false)){
            fw.write(name);
        }catch (Exception e){}
    }

    // 和服务端一致 XOR 加密
    private String encrypt(String raw) {
        byte[] data = raw.getBytes();
        byte[] key = CRYPT_KEY;
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return bytesToHex(data);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    static class IdentityData {
        String name;
        ResourceLocation texture;
        IdentityData(String name, ResourceLocation texture) {
            this.name = name;
            this.texture = texture;
        }
    }
}