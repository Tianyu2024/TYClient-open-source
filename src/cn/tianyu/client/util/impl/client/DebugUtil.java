package cn.tianyu.client.util.impl.client;

import cn.tianyu.client.TYClient;
import com.yumegod.obfuscation.Native;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.Arrays;
import java.util.Objects;

import static net.minecraft.util.EnumChatFormatting.*;

/**
 * @Author: CN_tianyu
 */
@Native
public class DebugUtil {
    @Getter
    public static String Username = null;
    @Getter
    public static String Password = null;

    private static Minecraft mc = Minecraft.getMinecraft();

    public static void print(Object... debug) {
        if (isDev()) {
            String message = Arrays.toString(debug);
            mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(message));
        }
    }

    public static void log(Object message) {
        String text = String.valueOf(message);
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(formatColor(text)));
    }

    public static void Clientlog(Object message) {
        String text = EnumChatFormatting.BOLD + "[" + TYClient.NAME + "] " + RESET + " " + message;
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(text));
    }

    public static void log(String prefix, Object message) {
        String text = EnumChatFormatting.BOLD + "[" + TYClient.NAME + "-" + prefix + "] " + RESET + " " + message;
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(text));
    }

    public static void logIRC(String user, String message) {
        String text = EnumChatFormatting.GREEN + "[IRC] " + WHITE + user + ":" + RESET + message;
        byte[] utf8Bytes;
        String utf8Str = "";
        try {
            utf8Bytes = text.getBytes("UTF-8");
            utf8Str = new String(utf8Bytes, "UTF-8");
        }catch (java.io.UnsupportedEncodingException e){
            mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("IRC can't know what things receive"));
        }
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(utf8Str));
    }

    public static void logcheater(String prefix, Object message) {
        String text = EnumChatFormatting.BOLD + "[" + TYClient.NAME + "-" + prefix + "] " + RESET + " " + message;
        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(text));
    }

    public static boolean isDev(){
        return Objects.equals(Username, "CN_tianyu");
    }

    public static String getPlayerName(){
        return mc.thePlayer.getName();
    }

    public static String formatColor(String txt) {
        return txt.replaceAll("&", "§");
    }
}
