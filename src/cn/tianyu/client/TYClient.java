package cn.tianyu.client;

import cn.tianyu.client.Manager.*;
import cn.tianyu.client.Network.NetWorkManager;
import cn.tianyu.client.TianyuOptiFine.betterfps.impl.render.FastTessellator;
import cn.tianyu.client.TianyuOptiFine.betterfps.impl.sky.CustomSkyManager;
import cn.tianyu.client.command.CommandManager;
import cn.tianyu.client.config.ConfigManager;
import cn.tianyu.client.event.EventManager;
import cn.tianyu.client.gui.ClientSplashScreen;
import cn.tianyu.client.gui.WallpaperEngine;
import cn.tianyu.client.gui.clickgui.oldui.ClickGui2;
import cn.tianyu.client.gui.clickgui.powerx.PXClickGui;
import cn.tianyu.client.gui.clickgui.tianyu.NeverLoseClickGui;
import cn.tianyu.client.gui.ui.UiManager;
import cn.tianyu.client.module.ModuleManager;
import cn.tianyu.client.setting.BlinkComponent;
import cn.tianyu.client.util.impl.player.MoveUtil;
import cn.tianyu.client.util.impl.render.ColorUtil;
import com.yumegod.obfuscation.Native;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.bilibili.BilibiliConfig;
import net.minecraft.client.renderer.culling.Frustum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjglx.opengl.Display;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author CN_tianyu
 */

@Native
public class TYClient {
    public static Frustum currentFrustum;
    public static boolean isstart = false;
    public static TYClient instance;
    public static final String NAME = "TYClient";
    public static final String VERSION = "3.3-end";
    //Release
    public Boolean HackerMode = false;
    @Getter
    public static ModuleManager moduleManager;
    @Getter
    private FriendManager friendManager;
    public static final Logger LOGGER = LogManager.getLogger(NAME);
    public FastTessellator fastTessellator;
    public static ConfigManager configManager;
    public static CommandManager commandManager;
    public EventManager eventManager;
    public WallpaperEngine wallpaperEngine;
    public static RotationManager rotationManager;
    public static SlotSpoofManager slotSpoofManager;
    public NetWorkManager netWorkManager;
    public BanManager banManager;
    public LagManager lagManager;
    public BlinkManager blinkManager;
    public PlayerStateManager playerStateManager;
    public FallDistanceManager fallDistanceManager;
    public MoveUtil moveUtil;
    public UiManager uiManager;

    public NeverLoseClickGui NeverLoseClickGui;
    public static ClickGui2 oldclickGui;
    public PXClickGui PXClickGui;

    public static Minecraft mc = Minecraft.getMinecraft();

    public int getcolor(int counter, int alpha) {
        int colors = getMainColor().getRGB();
        ColorUtil.swapAlpha(ColorUtil.getRainbow(counter), alpha);
        return new Color(colors,true).getRGB();
    }
    public Color getcolor2(int counter, int alpha) {
        int colors = getMainColor().getRGB();
        ColorUtil.swapAlpha(ColorUtil.getRainbow(counter), alpha);
        return new Color(colors,true);
    }

    public Color getMainColor() {
        return new Color(128, 128, 255);
    }

    @Native
    public void start() {
        instance = this;
//        {
//            System.setProperty("sun.net.inetaddr.disableReverseDnsLookup", "true");
//            System.setProperty("sun.net.inetaddr.ttl", "0");
//            System.setProperty("sun.net.inetaddr.negative.ttl", "0");
//        }
        //SplashScreen.setProgress(9, "Manager");
        //fastTessellator = new FastTessellator();
        rotationManager = new RotationManager();
        fallDistanceManager = new FallDistanceManager();
        slotSpoofManager = new SlotSpoofManager();
        moduleManager = new ModuleManager();
        friendManager = new FriendManager();
        uiManager = new UiManager();
        configManager = new ConfigManager();
        commandManager = new CommandManager();
        eventManager = new EventManager();
        netWorkManager = new NetWorkManager();
        banManager = new BanManager();
        lagManager = new LagManager();
        blinkManager = new BlinkManager();
        playerStateManager = new PlayerStateManager();
        moveUtil = new MoveUtil();
        //live
        this.wallpaperEngine = new WallpaperEngine();
        String gameDirPath = TYClient.mc.mcDataDir.getAbsolutePath();
        String videoFilePath = gameDirPath + File.separator + "background.mp4";
        try {
            InputStream inputStream = TYClient.class.getResourceAsStream("/assets/minecraft/client/background.mp4");
            Files.copy(inputStream, Paths.get(videoFilePath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //SplashScreen.setProgress(11, "ViaMCP");
        moduleManager.load();
        configManager.loadAllConfig();
        commandManager.load();
        netWorkManager.packet.init();
        NeverLoseClickGui = new NeverLoseClickGui();
        oldclickGui = new ClickGui2();
        PXClickGui = new PXClickGui();
        eventManager.register(this);
        eventManager.register(fallDistanceManager);
        eventManager.register(rotationManager);
        eventManager.register(moveUtil);
        //Event Manager
        eventManager.register(lagManager);
        eventManager.register(blinkManager);
        eventManager.register(new PingSpoofManager());
        eventManager.register(new BlinkComponent());
        BilibiliConfig.load();
        TYClient.instance.uiManager.init();
        //CustomSkyManager.loadCustomSky();
        ClientSplashScreen.updateProgress(7, "Done!");
        this.wallpaperEngine.setup(new File(videoFilePath), 30);
        Display.setTitle("TYClient-Reborn | " + VERSION + " by CN_tianyu" + " 人民熟了千万次，麦子万岁第一次！");
        isstart = true;
    }

    public static void stop() {
        configManager.saveAllConfig();
    }
}
