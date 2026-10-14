package cn.tianyu.client.module.impl.world;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.player.EventMoveInput;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.InventoryUtils;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.util.impl.client.MathUtil;
import cn.tianyu.client.util.impl.player.PlayerUtil;
import cn.tianyu.client.util.impl.player.TimerUtil;
import cn.tianyu.client.util.impl.render.ProjectileUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import org.lwjglx.util.vector.Vector2f;

import java.awt.*;

/**
 * @Author: CN_tianyu
 */
public class AutoPearl extends Module {
    private final BooleanValue debugValue = new BooleanValue("Debug", false);
    private static final double T = 10.0;
    private static final double T_MIN = 1.0E-4;
    private static final double ALPHA = 0.997;
    private CalculateThread calculateThread;
    private final TimerUtil timer = new TimerUtil();
    private boolean attempted;
    private boolean calculating;
    private int bestPearlSlot;

    public AutoPearl() {
        super("AutoPearl","自动扔珍珠",Category.world);
    }
    @EventTarget
    public void onMoveInput(EventMoveInput event) {
        if (this.calculating) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onRender(EventRender2D event) {
        if (!this.debugValue.getValue().booleanValue()) {
            return;
        }
        FontManager.arial12.drawString("assessment: " + new ProjectileUtil.EnderPearlPredictor(AutoPearl.mc.thePlayer.posX, AutoPearl.mc.thePlayer.posY, AutoPearl.mc.thePlayer.posZ, AutoPearl.mc.thePlayer.motionY - 0.01, AutoPearl.mc.thePlayer.motionY + 0.02).assessRotation(new Vector2f(AutoPearl.mc.thePlayer.rotationYaw, AutoPearl.mc.thePlayer.rotationPitch)), 20.0f, 20.0f, Color.WHITE.getRGB());
        FontManager.arial12.drawString("(" + AutoPearl.mc.thePlayer.rotationYaw + ", " + AutoPearl.mc.thePlayer.rotationPitch + ")", 20.0f, 30.0f, Color.WHITE.getRGB());
    }

    @EventTarget
    public void onMotion(EventMotion event) throws InterruptedException {
        boolean overVoid;
        if (AutoPearl.mc.thePlayer.onGround) {
            this.attempted = false;
            this.calculating = false;
        }
        if (event.isPost() && this.calculating && (this.calculateThread == null || this.calculateThread.completed)) {
            this.calculating = false;
            Stuck.throwPearl(this.calculateThread.solution);
        }
        boolean bl = overVoid = !AutoPearl.mc.thePlayer.onGround && !PlayerUtil.isBlockUnder(30.0, true);
        if (!this.attempted && !AutoPearl.mc.thePlayer.onGround && overVoid && TYClient.instance.fallDistanceManager.distance > 2.0f) {
            TYClient.instance.fallDistanceManager.distance = 0.0f;
            this.attempted = true;
            for (int slot = 5; slot < 45; ++slot) {
                ItemStack stack = AutoPearl.mc.thePlayer.inventoryContainer.getSlot(slot).getStack();
                if (stack == null || !(stack.getItem() instanceof ItemEnderPearl) || slot < 36) continue;
                this.bestPearlSlot = slot;
                if (this.debugValue.getValue().booleanValue()) {
                    DebugUtil.log("Found Pearl:" + (this.bestPearlSlot - 36));
                }
                if (this.bestPearlSlot - 36 == -37) continue;
                AutoPearl.mc.thePlayer.inventory.currentItem = this.bestPearlSlot - 36;
            }
            if (this.bestPearlSlot == 0) {
                return;
            }
            if (!(AutoPearl.mc.thePlayer.inventoryContainer.getSlot(this.bestPearlSlot).getStack().getItem() instanceof ItemEnderPearl)) {
                return;
            }
            this.calculating = true;
            this.calculateThread = new CalculateThread(AutoPearl.mc.thePlayer.posX, AutoPearl.mc.thePlayer.posY, AutoPearl.mc.thePlayer.posZ, 0.0, 0.0);
            this.calculateThread.start();
            this.getModule(Stuck.class).setEnable(true);
        }
    }

    private void putItemInSlot(int slot, int slotIn) {
        InventoryUtils.windowClick(mc, slotIn, slot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
    }

    private class CalculateThread
            extends Thread {
        private int iteration;
        private boolean completed;
        private double temperature;
        private double energy;
        private double solutionE;
        private Vector2f solution;
        public boolean stop;
        private final ProjectileUtil.EnderPearlPredictor predictor;

        private CalculateThread(double predictX, double predictY, double predictZ, double minMotionY, double maxMotionY) {
            this.predictor = new ProjectileUtil.EnderPearlPredictor(predictX, predictY, predictZ, minMotionY, maxMotionY);
            this.iteration = 0;
            this.temperature = 10.0;
            this.energy = 0.0;
            this.stop = false;
            this.completed = false;
        }

        @Override
        public void run() {
            TimerUtil timer = new TimerUtil();
            timer.reset();
            Vector2f current = this.solution = new Vector2f((float) MathUtil.getRandomInRange(-180, 180), (float)MathUtil.getRandomInRange(-90, 90));
            this.solutionE = this.energy = this.predictor.assessRotation(this.solution);
            while (this.temperature >= 1.0E-4 && !this.stop) {
                double assessment;
                double deltaE;
                Vector2f rotation = new Vector2f((float)((double)current.x + MathUtil.getRandomInRange(-this.temperature * 18.0, this.temperature * 18.0)), (float)((double)current.y + MathUtil.getRandomInRange(-this.temperature * 9.0, this.temperature * 9.0)));
                if (rotation.y > 90.0f) {
                    rotation.y = 90.0f;
                }
                if (rotation.y < -90.0f) {
                    rotation.y = -90.0f;
                }
                if ((deltaE = (assessment = this.predictor.assessRotation(rotation)) - this.energy) >= 0.0 || (double)MathUtil.getRandomInRange(0, 1) < Math.exp(-deltaE / this.temperature * 100.0)) {
                    this.energy = assessment;
                    current = rotation;
                    if (assessment > this.solutionE) {
                        this.solutionE = assessment;
                        this.solution = new Vector2f(rotation.x, rotation.y);
                        DebugUtil.log("Find a better solution: (" + this.solution.x + ", " + this.solution.y + "), value: " + this.solutionE);
                    }
                }
                this.temperature *= 0.997;
                ++this.iteration;
            }
            DebugUtil.log("Simulated annealing completed within " + this.iteration + " iterations");
            DebugUtil.log("Time used: " + timer.getDifference() + " solution energy: " + this.solutionE);
            this.completed = true;
        }
    }
}
