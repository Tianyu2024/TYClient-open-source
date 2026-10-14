package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.Manager.RotationManager;
import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.player.EventMove;
import cn.tianyu.client.event.events.player.EventTimerManipulation;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.event.events.render.EventRender3D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.impl.client.MathUtils;
import cn.tianyu.client.util.impl.player.PlayerUtils;
import cn.tianyu.client.util.impl.player.RotationUtil;
import cn.tianyu.client.util.impl.player.SimulatedPlayer;
import cn.tianyu.client.util.impl.player.TimerUtil;
import cn.tianyu.client.util.impl.render.ColorUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: CN_tianyu
 */
public class TickBase extends Module {
    public TickBase() {
        super("TickBase", "", Category.combat);
    }

    public final ModeValue mode = new ModeValue("Mode","Future","Past","Future");
    public final NumberValue delay = new NumberValue("Delay", 50, 0, 1000,50);
    public final NumberValue minActiveRange = new NumberValue("Min Active Range", 3f, 0.1f, 7f, 0.1f);
    public final NumberValue maxActiveRange = new NumberValue("Max Active Range", 7f, 0.1f, 7f, 0.1f);
    public final NumberValue maxTick = new NumberValue("Max Ticks", 4, 1, 20,1);
    public final BooleanValue displayPredictPos = new BooleanValue("Dislay Predict Pos",false);
    public final BooleanValue check = new BooleanValue("Check",false);
    public final BooleanValue workWithBackTrack = new BooleanValue("Work With Back Track",false);
    public TimerUtil timer = new TimerUtil();
    public int skippedTick = 0;
    private long shifted, previousTime;
    public boolean working;
    private boolean firstAnimation = true;
    private final List<PredictProcess> predictProcesses = new ArrayList<>();
    public EntityPlayer target;

    @Override
    public void onEnable() {
        shifted = 0;
        previousTime = 0;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (workWithBackTrack.get()) {
            //target = (EntityPlayer) getModule(BackTrack.class).target;
        } else {
            target = PlayerUtils.getTarget(maxActiveRange.getValue() * 3);
        }
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        setSuffix(mode.getValue());
        if (mode.is("Future")) {
            if (event.getEventState() == EventMotion.EventState.PRE)
                return;

            if (target == null || predictProcesses.isEmpty() || shouldStop()) {
                return;
            }

            if(timer.hasTimeElapsed(delay.getValue())) {
                if (shouldStart()) {
                    firstAnimation = false;
                    while (skippedTick <= maxTick.getValue() && !shouldStop()) {
                        ++skippedTick;
                        try {
                            mc.runTick();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    timer.reset();
                }
            }
            working = false;
        }
    }


    @EventTarget
    public void onTimerManipulation(EventTimerManipulation event) {
        if (mode.is("Past")) {

            if (target == null || predictProcesses.isEmpty() || shouldStop()) {
                return;
            }

            if (shouldStart() && timer.hasTimeElapsed(delay.getValue())) {
                shifted += event.getTime() - previousTime;
            }

            if (shifted >= maxTick.getValue() * (1000 / 20f)) {
                shifted = 0;
                timer.reset();
            }

            previousTime = event.getTime();
            event.setTime(event.getTime() - shifted);
        }
    }

    @EventTarget
    public void onMove(EventMove event) {

        predictProcesses.clear();

        SimulatedPlayer simulatedPlayer = SimulatedPlayer.fromClientPlayer(mc.thePlayer.movementInput);

        simulatedPlayer.rotationYaw = TYClient.rotationManager.targetRotation != null ? TYClient.rotationManager.targetRotation.getX() : mc.thePlayer.rotationYaw;

        for (int i = 0; i < (skippedTick != 0 ? skippedTick : maxTick.getValue()); i++) {
            simulatedPlayer.tick();
            predictProcesses.add(new PredictProcess(
                    simulatedPlayer.getPos(),
                    simulatedPlayer.fallDistance,
                    simulatedPlayer.onGround,
                    simulatedPlayer.isCollidedHorizontally
            ));
        }
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if(displayPredictPos.get()) {
            Vec3 position = predictProcesses.get(predictProcesses.size() - 1).position;
            double x = position.xCoord - mc.getRenderManager().viewerPosX;
            double y = position.yCoord - mc.getRenderManager().viewerPosY;
            double z = position.zCoord - mc.getRenderManager().viewerPosZ;
            AxisAlignedBB box = mc.thePlayer.getEntityBoundingBox().expand(0.1D, 0.1, 0.1);
            AxisAlignedBB axis = new AxisAlignedBB(box.minX - mc.thePlayer.posX + x, box.minY - mc.thePlayer.posY + y, box.minZ - mc.thePlayer.posZ + z, box.maxX - mc.thePlayer.posX + x, box.maxY - mc.thePlayer.posY + y, box.maxZ - mc.thePlayer.posZ + z);
            RenderUtil.drawAxisAlignedBB(axis,false, true, new Color(50, 255, 255, 150).getRGB());
        }
    }

    public boolean shouldStart(){
        Vec3 targetPos = target.getPositionVector();
//        if(workWithBackTrack.get())
//            targetPos = getModule(BackTrack.class).serverPos;
        return predictProcesses.get((int) (maxTick.getValue() - 1)).position.distanceTo(target.getPositionVector()) <
                mc.thePlayer.getPositionVector().distanceTo(target.getPositionVector()) &&
                MathUtils.inBetween(minActiveRange.getValue(), maxActiveRange.getValue(), predictProcesses.get((int) (maxTick.getValue() - 1)).position.distanceTo(target.getPositionVector())) &&
                mc.thePlayer.canEntityBeSeen(target) &&
                target.canEntityBeSeen(mc.thePlayer) &&
                (RotationUtil.getRotationDifference(mc.thePlayer, target) <= 90 && check.get() || !check.get()) &&
                !predictProcesses.get((int) (maxTick.getValue() - 1)).isCollidedHorizontally;
    }

    public boolean shouldStop(){
        return mc.thePlayer.hurtTime != 0;
    }

    public boolean handleTick() {
        if (mode.is("Future")) {
            if (working || skippedTick < 0) return true;
            if (isEnabled() && skippedTick > 0) {
                --skippedTick;
                return true;
            }
        }
        return false;
    }

    public boolean freezeAnim(){
        if (skippedTick != 0) {
            if (!firstAnimation) {
                firstAnimation = true;
                return false;
            }
            return true;
        }
        return false;
    }

    public static class PredictProcess {
        private final Vec3 position;
        private final float fallDistance;
        private final boolean onGround;
        private final boolean isCollidedHorizontally;

        public PredictProcess(Vec3 position, float fallDistance, boolean onGround, boolean isCollidedHorizontally) {
            this.position = position;
            this.fallDistance = fallDistance;
            this.onGround = onGround;
            this.isCollidedHorizontally = isCollidedHorizontally;
        }
    }
}