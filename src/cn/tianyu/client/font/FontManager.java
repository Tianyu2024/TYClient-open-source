package cn.tianyu.client.font;

import cn.tianyu.client.TYClient;

import java.awt.*;
import java.io.InputStream;

public class FontManager {
    public static RapeMasterFontManager interSemiBold15;
    public static RapeMasterFontManager interSemiBold13;
    public static RapeMasterFontManager GuiICONS28;
    public static RapeMasterFontManager tenacityBoldFont25;
    public static RapeMasterFontManager tenacityBoldFont24;
    public static RapeMasterFontManager tenacityBoldFont14;
    public static RapeMasterFontManager tenacityBoldFont16;
    public static RapeMasterFontManager tenacityBoldFont18;
    public static RapeMasterFontManager font20;
    public static RapeMasterFontManager lettuceBoldFont26;
    public static RapeMasterFontManager F22;
    public static RapeMasterFontManager F48;
    public static RapeMasterFontManager font40;

    public static RapeMasterFontManager arial16;
    public static RapeMasterFontManager arial12;
    public static RapeMasterFontManager arial14;
    public static RapeMasterFontManager arial17;
    public static RapeMasterFontManager arial18;
    public static RapeMasterFontManager arial20;

    public static RapeMasterFontManager font18;
    public static RapeMasterFontManager material18;
    public static RapeMasterFontManager museo18;
    public static RapeMasterFontManager arial22;
    public static RapeMasterFontManager tenacityBoldFont20;
    public static RapeMasterFontManager axBold20;
    public static RapeMasterFontManager font15;
    public static RapeMasterFontManager font14;
    public static RapeMasterFontManager icontestFont40;

    public static RapeMasterFontManager xyfont18;
    public static RapeMasterFontManager xyfont14;
    public static RapeMasterFontManager font16;

    public static RapeMasterFontManager bold20;
    public static RapeMasterFontManager bold26;
    public static RapeMasterFontManager bold32;
    public static RapeMasterFontManager bold12;
    public static RapeMasterFontManager bold24;
    public static RapeMasterFontManager bold38;
    public static RapeMasterFontManager bold34;

    public static void init(){
        interSemiBold15 = getFont("Inter_SemiBold.ttf",15);
        interSemiBold13 = getFont("Inter_SemiBold.ttf",13);
        GuiICONS28 = getFont("GuiICONS.ttf",28);
        xyfont18 = getFont("xy.ttf",18);
        xyfont14 = getFont("xy.ttf",14);
        font16 = getFont("xy.ttf",16);
        font20 = getFont("xy.ttf",20);
        lettuceBoldFont26 = getFont("geologica-bold.ttf", 26);
        F22 = getFont("1.ttf", 22);
        F48 = getFont("2.ttf", 48);
        font40 = getFont("font.ttf", 40);
        arial16 = getFont("sb.ttf", 16);
        arial12 = getFont("sb.ttf", 12);
        font18 = getFont("sb.ttf", 18);
        museo18 = getFont("museo500.ttf",18);

        arial14 = getFont("sb.ttf", 14);
        arial17 = getFont("sb.ttf", 17);
        arial18 = getFont("sb.ttf", 18);
        arial20 = getFont("sb.ttf", 20);

        material18 = getFont("material.ttf",18);
        arial22 = getFont("sb.ttf", 22);
        tenacityBoldFont25 = getFont("tenacity-bold.ttf",25);
        tenacityBoldFont24 = getFont("tenacity-bold.ttf",24);
        tenacityBoldFont20 = getFont("tenacity-bold.ttf",20);
        tenacityBoldFont14 = getFont("tenacity-bold.ttf", 14);
        tenacityBoldFont16 = getFont("tenacity-bold.ttf", 16);
        tenacityBoldFont18 = getFont("tenacity-bold.ttf", 18);
        axBold20 = getFont("ax-bold.ttf", 20);
        font15 = getFont("xy.ttf", 15);
        font14 = getFont("xy.ttf", 14);
        icontestFont40 = getFont("geologica-bold.ttf", 40);

        bold26 = getFont("bold.ttf", 26);
        bold32 = getFont("bold.ttf", 32);
        bold12 = getFont("bold.ttf", 12);
        bold20 = getFont("bold.ttf", 20);
        bold24 = getFont("bold.ttf", 24);
        bold38 = getFont("bold.ttf", 38);
        bold34 = getFont("bold.ttf", 34);

    }

    private static RapeMasterFontManager getFont(String fontName, float fontSize) {
        try {
            InputStream inputStream = TYClient.class.getResourceAsStream("/assets/minecraft/client/font/" + fontName);
            if (inputStream == null) {
                System.err.println("Font file not found: " + fontName);
                return getFallbackFont(fontSize); // 提供回退字体
            }

            Font font = Font.createFont(Font.PLAIN, inputStream).deriveFont(fontSize);
            inputStream.close();
            return new RapeMasterFontManager(font);
        } catch (Exception e) {
            System.err.println("Failed to load font " + fontName + ": " + e.getMessage());
            return getFallbackFont(fontSize); // 提供回退字体
        }
    }

    private static RapeMasterFontManager getFallbackFont(float fontSize) {
        return new RapeMasterFontManager(new Font("Arial", Font.PLAIN, (int)fontSize));
    }
}
