package cn.tianyu.client.util.impl.client;

import cn.tianyu.client.font.RapeMasterFontManager;
import net.minecraft.client.gui.FontRenderer;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: CN_tianyu
 */
public class FontUtils {
    public static List<String> listFormattedStringToWidth(RapeMasterFontManager fontDrawer, String str, int wrapWidth) {
        return Arrays.asList(wrapFormattedStringToWidth(fontDrawer, str, wrapWidth).split("\n"));
    }

    public static String wrapFormattedStringToWidth(RapeMasterFontManager fontDrawer, String str, int wrapWidth) {
        if (str.length() <= 1)
        {
            return str;
        }
        else
        {
            int i = sizeStringToWidth(fontDrawer, str, wrapWidth);

            if (str.length() <= i)
            {
                return str;
            }
            else
            {
                String s = str.substring(0, i);
                char c0 = str.charAt(i);
                boolean flag = c0 == 32 || c0 == 10;
                String s1 = FontRenderer.getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
                return s + "\n" + wrapFormattedStringToWidth(fontDrawer, s1, wrapWidth);
            }
        }
    }

    public static int sizeStringToWidth(RapeMasterFontManager fontDrawer, String str, int wrapWidth) {
        int i = str.length();
        float f = 0.0F;
        int j = 0;
        int k = -1;

        for (boolean flag = false; j < i; ++j)
        {
            char c0 = str.charAt(j);

            switch (c0)
            {
                case '\n':
                    --j;
                    break;

                case ' ':
                    k = j;

                default:
                    f += fontDrawer.getCharWidth(c0);

                    if (flag)
                    {
                        ++f;
                    }

                    break;

                case '\u00a7':
                    if (j < i - 1)
                    {
                        ++j;
                        char c1 = str.charAt(j);

                        if (c1 != 108 && c1 != 76)
                        {
                            if (c1 == 114 || c1 == 82 || FontRenderer.isFormatColor(c1))
                            {
                                flag = false;
                            }
                        }
                        else
                        {
                            flag = true;
                        }
                    }
            }

            if (c0 == 10)
            {
                ++j;
                k = j;
                break;
            }

            if (Math.round(f) > wrapWidth)
            {
                break;
            }
        }

        return j != i && k != -1 && k < j ? k : j;
    }
}
