package cn.tianyu.client.value;

import cn.tianyu.client.setting.Dependency;
import cn.tianyu.client.util.annotations.EaseBackIn;
import cn.tianyu.client.util.annotations.OutputAnimation;
import cn.tianyu.client.util.impl.render.DecelerateAnimation;
import lombok.Getter;

import java.util.function.Supplier;

/**
 * @Author: CN_tianyu
 */

public abstract class Value<T> {
    public final OutputAnimation numberAnim = new OutputAnimation(0);
    public final EaseBackIn easeBackIn = new EaseBackIn(200, 0.2F, 1.0F);
    public final DecelerateAnimation decelerateAnimation = new DecelerateAnimation(200, 1F);
    public float animation = 0f;

    @Getter
    public float height = 22f;

    @Getter
    public Supplier<Boolean> visible;
    private final String name;
    public T value;
    private final Dependency dependency;
    protected int x, y;  // 添加坐标字段

    public Value(String name) {
        this.name = name;
        this.dependency = () -> Boolean.TRUE;
    }

    public Value(String name, Dependency dependency) {
        this.name = name;
        this.dependency = dependency;
        this.visible = visible;
    }

    // 添加坐标设置方法
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public T get() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isVisible() {
        return dependency == null || dependency.check();
    }

    public abstract void draw(int x, int y, int mouseX, int mouseY);
    public abstract void mouseClicked(int mouseX, int mouseY, int mouseButton);
    public abstract void mouseReleased(int mouseX, int mouseY);

    public Boolean canDisplay() {
        return this.visible.get();
    }
    public boolean isAvailable() {
        //return true;
        return dependency != null && this.dependency.check();
    }

    public boolean isHidden() {
        return !isAvailable();
    }

    @FunctionalInterface
    public interface Dependency {
        boolean check();
    }
}