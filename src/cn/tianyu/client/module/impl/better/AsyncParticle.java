package cn.tianyu.client.module.impl.better;

import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: CN_tianyu
 */
public class AsyncParticle extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final List<EntityFX> asyncFx = new CopyOnWriteArrayList<>();
    private final List<EntityFX> deadFx = new ArrayList<>();
    private final Object lock = new Object();

    public AsyncParticle() {
        super("AsyncParticle", "异步粒子", Category.better);
    }

    @Override
    public void onDisable() {
        synchronized (lock) {
            asyncFx.clear();
            deadFx.clear();
        }
    }

    @Override
    public void onUpdate() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            synchronized (lock) {
                asyncFx.clear();
                deadFx.clear();
            }
            return;
        }

        tickParticle();
    }

    public void putParticle(EntityFX fx) {
        if (fx == null || !isEnabled() || mc.theWorld == null) {
            return;
        }
        synchronized (lock) {
            asyncFx.add(fx);
        }
    }

    private void tickParticle() {
        deadFx.clear();
        for (EntityFX fx : asyncFx) {
            if (fx == null || fx.isDead) {
                deadFx.add(fx);
                continue;
            }
            fx.onUpdate();
        }
        asyncFx.removeAll(deadFx);
    }

    public List<EntityFX> getAsyncParticleList() {
        synchronized (lock) {
            return new ArrayList<>(asyncFx);
        }
    }
}