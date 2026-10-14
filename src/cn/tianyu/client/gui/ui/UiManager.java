package cn.tianyu.client.gui.ui;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.gui.ui.modules.ModuleList;
import cn.tianyu.client.gui.ui.modules.TargetHud;
import cn.tianyu.client.util.RenderUtil;
import net.minecraft.client.Minecraft;
import org.lwjglx.input.Mouse;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class UiManager {
    public final List<UiModule> UiModules = new ArrayList<UiModule>();
    private final Minecraft mc = Minecraft.getMinecraft();
    public double mouseX;
    public double mouseY;

    public void init() {
        TYClient.instance.eventManager.register(this);
        this.addModule(new ModuleList());
        this.addModule(new TargetHud());
    }

    @EventTarget
    public void moveUi(EventRender2D event) {
        if (!this.mc.ingameGUI.getChatGUI().getChatOpen()) {
            return;
        }
        for (UiModule m : this.UiModules) {
            if (!m.getState()) continue;
            double xpos = m.getPosX();
            double ypos = m.getPosY();
            double mwidth = m.getWidth();
            double mheight = m.getHeight();
            double mousex = this.mouseX;
            double mousey = this.mouseY;
            if (mousex > xpos && mousey > ypos && mousex < xpos + mwidth && mousey < ypos + mheight && Mouse.isButtonDown(0)) {
                RenderUtil.drawBorderedRect((float)xpos, (float)ypos, (float)(xpos + mwidth), (float)(ypos + mheight), 2.0f, new Color(225, 225, 225).getRGB(), 0);
                if (m.moveX == 0.0 && m.moveY == 0.0) {
                    m.moveX = (double)((float)this.mouseX) - xpos;
                    m.moveY = (double)((float)this.mouseY) - ypos;
                    continue;
                }
                double setX = this.mouseX - m.moveX;
                double setY = this.mouseY - m.moveY;
                setX = Math.min(Math.max(0.0, setX), (double)RenderUtil.width() - m.width);
                setY = Math.min(Math.max(0.0, setY), (double)RenderUtil.height() - m.height);
                m.setPosX(setX);
                m.setPosY(setY);
                continue;
            }
            if (m.moveX == 0.0 && m.moveY == 0.0) continue;
            m.moveX = 0.0;
            m.moveY = 0.0;
        }
    }

    public void addModule(UiModule module) {
        for (Field field : module.getClass().getDeclaredFields()) {
            field.setAccessible(true);
        }
        this.UiModules.add(module);
    }

    public UiModule getModule(String name) {
        for (UiModule m : this.UiModules) {
            if (!m.getName().equalsIgnoreCase(name)) continue;
            return m;
        }
        return null;
    }

    public UiModule getModule(Class<?> cls) {
        for (UiModule m : this.UiModules) {
            if (m.getClass() != cls) continue;
            return m;
        }
        return null;
    }

    public List<UiModule> getModules() {
        return this.UiModules;
    }
}

