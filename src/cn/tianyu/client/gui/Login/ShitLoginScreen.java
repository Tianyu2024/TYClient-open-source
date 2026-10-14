package cn.tianyu.client.gui.Login;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.gui.MainMenuGui.MainMenuGui;
import cn.tianyu.client.module.impl.better.CustomSky2;
import cn.tianyu.client.util.annotations.LayeringAnimation;
import cn.tianyu.client.util.impl.client.DebugUtil;
import com.yumegod.obfuscation.Native;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.*;
import java.net.Socket;

@Native
public class ShitLoginScreen extends GuiScreen {
    public static Boolean ClientLogin = false;
    private static GuiTextField usernameField;
    private static GuiTextField passwordField;
    private String statusMessage;
    private ResourceLocation backgroundTexture;
    public static String User;

    public static String username = null;
    public static String password = null;

    // 新增一个静态变量来存储用户名
    @Getter
    public static String loggedInUsername = null;

    private static final String CREDENTIALS_FILE = "login_credentials.txt"; // 存储账号密码的文件路径

    @Override
    public void initGui() {
        buttonList.clear();
        usernameField = new GuiTextField(0, fontRendererObj, width / 2 - 100, height / 4 - 20, 200, 20);
        usernameField.setMaxStringLength(30);
        usernameField.setFocused(true);
        passwordField = new GuiTextField(1, fontRendererObj, width / 2 - 100, height / 4 + 20, 200, 20);
        passwordField.setMaxStringLength(30);
        passwordField.setEnableBackgroundDrawing(true);
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 60, "Login"));
        buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 100, "Exit"));
        buttonList.add(new GuiButton(2, width / 2 - 100, height / 4 + 140, "Reg"));
        buttonList.add(new GuiButton(3, width / 2 - 100, height / 4 + 180, "FreeUser"));
        backgroundTexture = new ResourceLocation("client/Q.png");
        Minecraft.getMinecraft().getTextureManager().bindTexture(backgroundTexture);

        // 尝试读取之前保存的用户名和密码
        loadCredentials();
    }

    @Native
    private void login(){
        String username = "sbCN_tianyu";
        String password = "LLLDev";
        new Thread(() -> {
            String response = checkAccountOnServer(username, password);
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if ("Login successful".equals(response)) {
                    // 保存用户名
                    loggedInUsername = username;
                    //saveCredentials(username, password);
                    //IRC.start();
                    LayeringAnimation.play(new MainMenuGui());
                    ClientLogin = true;
                    User = username;
                    DebugUtil.Username = username;
                    DebugUtil.Password = password;
                    CustomSky2.isCustomSky = true;
                } else {
                    statusMessage = "Login failed: Account does not exist or password is incorrect.";
                }
            });
        }).start();
    }

    @Override
    public void actionPerformed(GuiButton button) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (button.id == 0) {
            new Thread(() -> {
                String response = checkAccountOnServer(username, password);
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    if ("Login successful".equals(response)) {
                        // 保存用户名
                        loggedInUsername = username;
                        saveCredentials(username, password);
//                        SystemTray tray = SystemTray.getSystemTray();
//                        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
//                        TrayIcon trayIcon = new TrayIcon(image, "Tooltip");
//                        trayIcon.setImageAutoSize(true);
//                        try {
//                            tray.add(trayIcon);
//                        } catch (AWTException e) {
//                            throw new RuntimeException(e);
//                        }
//                        trayIcon.displayMessage(TYClient.NAME + " " + TYClient.VERSION, "Hello～(∠・ω< )⌒☆",TrayIcon.MessageType.INFO);
                        //IRC.start();
                        LayeringAnimation.play(new MainMenuGui());
                        ClientLogin = true;
                        User = username;
                        DebugUtil.Username = username;
                        DebugUtil.Password = password;
                        CustomSky2.isCustomSky = true;
                    } else {
                        statusMessage = "Login failed: Account does not exist or password is incorrect.";
                    }
                });
            }).start();
        } else if (button.id == 1) {
            Minecraft.getMinecraft().shutdown();
        } else if (button.id == 2) {
            statusMessage = "Plz use Public Actor\nUserName:sbCN_tianyu pass:LLLDev";
            //LayeringAnimation.play(new RegScreen());
        } else if (button.id == 3) {
            login();
        }
    }

    private String checkAccountOnServer(String username, String password) {
        try (Socket socket = new Socket(TYClient.instance.netWorkManager.getServerip(), 29043);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String credentials = username + " " + password;
            out.println(credentials);
            return in.readLine();
        } catch (IOException e) {
            //e.printStackTrace();
            return "Connection failed";
        }
    }

    private void saveCredentials(String username, String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CREDENTIALS_FILE))) {
            writer.write(username + "\n" + password);
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    private void loadCredentials() {
        File file = new File(CREDENTIALS_FILE);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String username = reader.readLine();
                String password = reader.readLine();
                if (username != null && password != null) {
                    usernameField.setText(username);
                    passwordField.setText(password);
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground();
        drawDynamicBackground(partialTicks);

        FontManager.font40.drawCenteredString("Welcome", width / 2 ,height / 4 - 60, Color.WHITE.getRGB());
        usernameField.drawTextBox114514();
        passwordField.drawTextBox114514();
        FontManager.F22.drawCenteredString("UserName:", width / 2 - 100, height / 4 - 40, Color.WHITE.getRGB());
        FontManager.F22.drawCenteredString("Password:", width / 2 - 100, height / 4 + 0, Color.WHITE.getRGB());

        if (statusMessage != null) {
            FontManager.F22.drawCenteredString(statusMessage, width / 2 - 100, height / 4 + 120, new Color(255, 255, 255).getRGB());
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        super.drawScreen1(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    public void drawBackground() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(backgroundTexture);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex2f(0, height);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex2f(width, height);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex2f(width, 0);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex2f(0, 0);
        GL11.glEnd();
    }

    private void drawDynamicBackground(float partialTicks) {
        int width = this.width;
        int height = this.height;

        GL11.glPushMatrix();
        GL11.glBegin(GL11.GL_QUADS);

        // 动态颜色计算
        float time = (Minecraft.getSystemTime() % 10000L) / 10000.0F;
        float red = 0.5f + 0.5f * (float) Math.sin(time * 2 * Math.PI);
        float green = 0.5f + 0.5f * (float) Math.sin(time * 2 * Math.PI + Math.PI / 3);
        float blue = 0.5f + 0.5f * (float) Math.sin(time * 2 * Math.PI + 2 * Math.PI / 3);

        // 左上角
        GL11.glColor3f(red, green, blue);
        GL11.glVertex2f(0, 0);

        // 右上角
        GL11.glColor3f(green, blue, red);
        GL11.glVertex2f(width, 0);

        // 右下角
        GL11.glColor3f(blue, red, green);
        GL11.glVertex2f(width, height);

        // 左下角
        GL11.glColor3f(red, green, blue);
        GL11.glVertex2f(0, height);

        GL11.glEnd();
        GL11.glPopMatrix();
    }

    @Override
    public void updateScreen() {
        usernameField.updateCursorCounter();
        passwordField.updateCursorCounter();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        super.mouseClicked(mouseX, mouseY, button);
        usernameField.mouseClicked(mouseX, mouseY, button);
        passwordField.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        usernameField.textboxKeyTyped(typedChar, keyCode);
        passwordField.textboxKeyTyped(typedChar, keyCode);
    }

    private boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("windows");
    }
}
