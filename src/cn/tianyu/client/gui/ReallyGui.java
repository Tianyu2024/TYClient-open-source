package cn.tianyu.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjglx.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

/**
 * @Author CN_tianyu
 */
public class ReallyGui extends GuiScreen {

    private final ResourceLocation background =
            new ResourceLocation(
                    "tianyu/story/background.png"
            );


    private int scroll;

    private float animation;


    private final String title =
            "共产党的真面目";


    /*
     * 在这里编辑你的故事
     */
    private final String story =
            "我的故事\n\n" +
                    "共产党\n\n" +
                    "这是一个属于自己的记录空间。\n\n" +
                    "你可以写文字，记录想法。\n\n" +
                    "支持多行滚动显示。\n\n" +
                    "未来这里可以加入更多资料。";


    /*
     * 八个按钮网址
     */
    private final String[] urls =
            {
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    ""
            };


    private final String[] names =
            {
                    "01 ",
                    "02 ",
                    "03 ",
                    "04 ",
                    "05 ",
                    "06 ",
                    "07 ",
                    "08 "
            };


    @Override
    public void initGui() {

        buttonList.clear();


        for (int i = 0; i < 8; i++) {

            buttonList.add(
                    new GuiButton(
                            i,
                            45,
                            65 + i * 38,
                            150,
                            28,
                            names[i]
                    )
            );
        }
    }



    @Override
    protected void actionPerformed(
            GuiButton button
    ) {


        String url = urls[button.id];


        if (!url.isEmpty()) {

            try {

                Desktop.getDesktop()
                        .browse(
                                new URI(url)
                        );

            } catch (Exception ignored) {

            }
        }
    }



    @Override
    public void handleMouseInput() throws IOException {

        super.handleMouseInput();


        int wheel =
                Mouse.getEventDWheel();


        if (wheel != 0) {

            scroll += wheel > 0 ? 20 : -20;


            if (scroll > 0)
                scroll = 0;


            if (scroll < -300)
                scroll = -300;
        }
    }



    @Override
    public void updateScreen() {

        if(animation < 1)
            animation += 0.04f;
    }



    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {


        drawDefaultBackground();



        /*
         * 总背景
         */

        drawRect(
                15,
                15,
                width - 15,
                height - 15,
                new Color(
                        10,
                        10,
                        18,
                        245
                ).getRGB()
        );



        /*
         * 左侧菜单
         */

        drawRect(
                30,
                45,
                215,
                height - 35,
                new Color(
                        20,
                        20,
                        32,
                        230
                ).getRGB()
        );



        /*
         * 右侧内容
         */

        drawRect(
                230,
                45,
                width - 35,
                height - 35,
                new Color(
                        18,
                        18,
                        28,
                        240
                ).getRGB()
        );



        /*
         * 标题渐变效果
         */

        drawGradientText(
                title,
                width / 2,
                25
        );



        /*
         * 故事内容
         */

        int y = 70 + scroll;


        for(String line : story.split("\n")) {


            drawString(
                    fontRendererObj,
                    line,
                    250,
                    y,
                    0xDDDDDD
            );


            y += 18;
        }



        super.drawScreen(
                mouseX,
                mouseY,
                partialTicks
        );
    }



    private void drawGradientText(
            String text,
            int x,
            int y
    ) {


        int start =
                x -
                        fontRendererObj.getStringWidth(text)
                                /2;


        for(int i = 0;i < text.length();i++) {


            float percent =
                    (float)i /
                            text.length();



            int color =
                    interpolate(
                            0x55AAFF,
                            0xCC66FF,
                            percent
                    );



            fontRendererObj.drawString(
                    String.valueOf(
                            text.charAt(i)
                    ),
                    start,
                    y,
                    color
            );


            start +=
                    fontRendererObj
                            .getCharWidth(
                                    text.charAt(i)
                            );
        }
    }



    private int interpolate(
            int a,
            int b,
            float t
    ){

        int ar =
                (a >> 16) & 255;

        int ag =
                (a >> 8) & 255;

        int ab =
                a & 255;



        int br =
                (b >> 16) & 255;

        int bg =
                (b >> 8) & 255;

        int bb =
                b & 255;



        int r =
                (int)
                        (ar+(br-ar)*t);


        int g =
                (int)
                        (ag+(bg-ag)*t);


        int bl =
                (int)
                        (ab+(bb-ab)*t);



        return
                (r<<16)
                        |
                        (g<<8)
                        |
                        bl;
    }
}