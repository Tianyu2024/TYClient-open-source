package cn.tianyu.client.value.impl;

import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.util.DrawUtil;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.value.Value;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: CN_tianyu
 */
public class ModeValue extends Value<String> {
    private final List<String> modes;
    private boolean open;
    private int hoveredIndex = -1;
    private int x;
    private int y;

    public ModeValue(String name, String defaultValue, String... modes) {
        super(name);
        this.setValue(defaultValue);
        this.modes = Arrays.asList(modes);
    }

    public ModeValue(Dependency dependenc, String name, String defaultValue, String... modes) {
        super(name, dependenc);
        this.setValue(defaultValue);
        this.modes = Arrays.asList(modes);
    }

    @Override
    public void draw(int x, int y, int mouseX, int mouseY) {
        this.x = x;
        this.y = y;
        hoveredIndex = -1;

        DrawUtil.drawRect(x, y, 100, 16, new Color(50, 50, 50).getRGB());
        FontManager.font18.drawString(getName() + ": " + getValue(), x + 2, y + 4, Color.WHITE.getRGB());

        DrawUtil.drawTriangle(x + 90, y + 6, 5, open, new Color(200, 200, 200).getRGB());

        if (open) {
            int menuY = y + 18;
            DrawUtil.drawRect(x, menuY, 100, modes.size() * 16, new Color(30, 30, 30).getRGB());

            for (int i = 0; i < modes.size(); i++) {
                int itemY = menuY + i * 16;
                boolean hovered = mouseX >= x && mouseX <= x + 100 &&
                        mouseY >= itemY && mouseY < itemY + 16;

                if (hovered) {
                    hoveredIndex = i;
                    setValue(modes.get(i));
                    DrawUtil.drawRect(x, itemY, 100, 16, new Color(58, 55, 55).getRGB());
                }

                FontManager.font18.drawString(modes.get(i), x + 2, itemY + 4,
                        getValue().equals(modes.get(i)) ? 0xFF00FF00 : 0xFFFFFFFF);
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return;

        if (mouseX >= x && mouseX <= x + 100 && mouseY >= y && mouseY <= y + 16) {
            open = !open;
            DebugUtil.log(hoveredIndex);
            return;
        }

//        if (open && hoveredIndex != -1) {
//            String selected = modes.get(hoveredIndex);
//            if (!selected.equals(getValue())) {
//                setValue(selected);
//            }
//            open = false;
//        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
    }

    public int getIndex() {
        return modes.indexOf(getValue());
    }

    public boolean is(String sb) {
        return this.getValue().equalsIgnoreCase(sb);
    }

    public String getModeAsString() {
        return getValue();
    }

    public List<String> getModes() {
        return modes;
    }
}