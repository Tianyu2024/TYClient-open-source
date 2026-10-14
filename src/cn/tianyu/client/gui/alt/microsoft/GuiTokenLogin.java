package cn.tianyu.client.gui.alt.microsoft;

import cn.tianyu.client.gui.alt.GuiAltManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Session;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class GuiTokenLogin extends GuiScreen {


    private final GuiScreen parent;


    private GuiTextField tokenField;


    private String status =
            EnumChatFormatting.YELLOW
                    +
                    "Waiting...";


    private Thread loginThread;



    public GuiTokenLogin(GuiScreen parent) {
        this.parent = parent;
    }



    @Override
    public void initGui() {


        tokenField =
                new GuiTextField(
                        0,
                        fontRendererObj,
                        width / 2 - 100,
                        80,
                        200,
                        20
                );


        buttonList.add(
                new GuiButton(
                        1,
                        width / 2 - 100,
                        120,
                        200,
                        20,
                        "Login"
                )
        );


        buttonList.add(
                new GuiButton(
                        2,
                        width / 2 - 100,
                        150,
                        200,
                        20,
                        "Back"
                )
        );

    }




    @Override
    protected void actionPerformed(GuiButton button) {


        if(button.id == 1)
        {

            if(loginThread != null &&
                    loginThread.isAlive())
                return;


            String token =
                    tokenField.getText();


            if(token.isEmpty())
            {
                status =
                        EnumChatFormatting.RED
                                +
                                "Token empty";

                return;
            }



            loginThread =
                    new Thread(() -> {


                        try {

                            status =
                                    EnumChatFormatting.YELLOW
                                            +
                                            "Logging in...";


                            Result result =
                                    login(token);



                            Minecraft.getMinecraft()
                                    .session =
                                    new Session(
                                            result.username,
                                            result.uuid,
                                            token,
                                            "mojang"
                                    );



                            status =
                                    EnumChatFormatting.GREEN
                                            +
                                            "Login successful! "
                                            +
                                            result.username;


                        }
                        catch(Exception e)
                        {

                            e.printStackTrace();


                            status =
                                    EnumChatFormatting.RED
                                            +
                                            "Login failed: "
                                            +
                                            e.getMessage();

                        }


                    });


            loginThread.setDaemon(true);
            loginThread.start();

        }



        if(button.id == 2)
        {
            mc.displayGuiScreen(
                    new GuiAltManager(this)
            );
        }

    }




    private Result login(String accessToken)
            throws Exception {


        URL url =
                new URL(
                        "https://api.minecraftservices.com/minecraft/profile"
                );


        HttpURLConnection connection =
                (HttpURLConnection)
                        url.openConnection();



        connection.setRequestMethod("GET");


        connection.setRequestProperty(
                "Authorization",
                "Bearer " + accessToken
        );


        int code =
                connection.getResponseCode();



        if(code != 200)
        {
            throw new Exception(
                    "Invalid token (" + code + ")"
            );
        }



        BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()
                        )
                );


        StringBuilder builder =
                new StringBuilder();


        String line;


        while((line = reader.readLine()) != null)
        {
            builder.append(line);
        }


        reader.close();



        JsonObject json =
                new JsonParser()
                        .parse(
                                builder.toString()
                        )
                        .getAsJsonObject();



        return new Result(
                json.get("name")
                        .getAsString(),

                json.get("id")
                        .getAsString()
        );

    }




    @Override
    protected void keyTyped(char typedChar,int keyCode)
    {

        tokenField.textboxKeyTyped(
                typedChar,
                keyCode
        );

    }



    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton)
    {

        tokenField.mouseClicked(
                mouseX,
                mouseY,
                mouseButton
        );

    }




    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks)
    {


        drawDefaultBackground();


        drawCenteredString(
                fontRendererObj,
                "Token Login",
                width / 2,
                40,
                0xffffff
        );


        tokenField.drawTextBox();



        drawCenteredString(
                fontRendererObj,
                status,
                width / 2,
                190,
                0xffffff
        );


        super.drawScreen(
                mouseX,
                mouseY,
                partialTicks
        );

    }




    private static class Result {


        final String username;

        final String uuid;



        Result(
                String username,
                String uuid)
        {

            this.username=username;

            this.uuid=uuid;

        }

    }

}