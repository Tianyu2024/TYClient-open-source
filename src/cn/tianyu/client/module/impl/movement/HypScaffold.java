package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.Manager.RotationState;
import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventPriority;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventLeftClickMouse;
import cn.tianyu.client.event.events.game.EventRightClickMouse;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.player.*;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.event.events.world.EventBlockHit;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.client.HUD;
import cn.tianyu.client.module.impl.render.PostProcessing;
import cn.tianyu.client.module.impl.world.BedNuker;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.util.impl.client.RandomUtils;
import cn.tianyu.client.util.impl.player.*;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.WorldSettings.GameType;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class HypScaffold extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final double[] placeOffsets = new double[]{
            0.03125,
            0.09375,
            0.15625,
            0.21875,
            0.28125,
            0.34375,
            0.40625,
            0.46875,
            0.53125,
            0.59375,
            0.65625,
            0.71875,
            0.78125,
            0.84375,
            0.90625,
            0.96875
    };
    private final float[] lastErrors = new float[20];
    private int errorIndex = 0;
    public final ModeValue rotationMode = new ModeValue("rotations", "Prediction", "None", "Vanilla", "BackWards", "Strafe", "Prediction");
    public final ModeValue moveFix = new ModeValue("move-fix", "SILENT", "NONE", "SILENT");
    public final ModeValue sprintMode = new ModeValue("sprint", "NONE", "NONE", "VANILLA");
    public final NumberValue groundMotion = new NumberValue("ground-motion", 100, 0,100,1);
    public final NumberValue airMotion = new NumberValue("air-motion", 100, 0, 100, 1);
    public final NumberValue speedMotion = new NumberValue("speed-motion", 100, 0, 100, 1);
    public final ModeValue tower = new ModeValue("tower", "NONE", "NONE", "VANILLA", "EXTRA", "TELLY");
    public final ModeValue keepY = new ModeValue("keep-y", "NONE", "NONE", "VANILLA", "EXTRA", "TELLY");
    public final NumberValue tellystartrotationminspeed = new NumberValue("telly-start-rotation-min-speed", 90.0F, 1.0F, 180.0F, 1.0F, () -> Objects.equals(this.keepY.getValue(), "TELLY"));
    public final NumberValue tellystartrotationmaxspeed = new NumberValue("telly-start-rotation-max-speed", 95.0F, 1.0F, 180.0F, 1.0F, () -> Objects.equals(this.keepY.getValue(), "TELLY"));
    public final NumberValue tellynormalrotationminspeed = new NumberValue("telly-normal-rotation-min-speed", 30.0F, 1.0F, 180.0F, 1.0F, () -> Objects.equals(this.keepY.getValue(), "TELLY"));
    public final NumberValue tellynormalrotationmaxspeed = new NumberValue("telly-normal-rotation-max-speed", 35.0F, 1.0F, 180.0F, 1.0F, () -> Objects.equals(this.keepY.getValue(), "TELLY"));
    public final BooleanValue keepYonPress = new BooleanValue("keep-y-on-press", false, () -> !Objects.equals(this.keepY.getValue(), "NONE"));
    public final BooleanValue multiplace = new BooleanValue("multi-place", false);
    public final BooleanValue safeWalk = new BooleanValue("safe-walk", true);
    public final BooleanValue swing = new BooleanValue("swing", true);
    //public final BooleanValue itemSpoof = new BooleanValue("item-spoof", false);
    public final BooleanValue blockCounter = new BooleanValue("block-counter", true);
    private int rotationTick = 0;
    private int lastSlot = -1;
    private int blockCount = -1;
    private float yaw = -180.0F;
    private float pitch = 0.0F;
    private boolean canRotate = false;
    private int towerTick = 0;
    private int towerDelay = 0;
    private int stage = 0;
    private int startY = 256;
    private boolean shouldKeepY = false;
    private boolean towering = false;
    private EnumFacing targetFacing = null;
    private float lastYaw = 0;
    private float lastYawChange = 0;
    private float lastPitchChange = 0;
    public static int count = 0;

    public HypScaffold() {
        super("HypScaffold", "", Category.movement);
    }

    private boolean shouldStopSprint() {
        if (this.isTowering()) {
            return false;
        } else {
            boolean stage = Objects.equals(this.keepY.getValue(), "VANILLA") || Objects.equals(this.keepY.getValue(), "EXTRA");
            return (!stage || this.stage <= 0) && Objects.equals(this.sprintMode.getValue(), "NONE");
        }
    }

    private boolean canPlace() {
        BedNuker bedNuker = (BedNuker) TYClient.getModuleManager().getModule(BedNuker.class);
        if (bedNuker.isEnabled()
                //&& bedNuker.isReady()
        ) {
            return false;
        }
        else {
            LongJump longJump = (LongJump) TYClient.getModuleManager().getModule(LongJump.class);
            return !longJump.isEnabled()
                    //|| !longJump.isAutoMode()
                    //|| longJump.isJumping()
                    ;
        }
    }

    private EnumFacing getBestFacing(BlockPos blockPos1, BlockPos blockPos3) {
        double offset = 0.0;
        EnumFacing enumFacing = null;
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing != EnumFacing.DOWN) {
                BlockPos pos = blockPos1.offset(facing);
                if (pos.getY() <= blockPos3.getY()) {
                    double distance = pos.distanceSqToCenter((double) blockPos3.getX() + 0.5, (double) blockPos3.getY() + 0.5, (double) blockPos3.getZ() + 0.5);
                    if (enumFacing == null || distance < offset || distance == offset && facing == EnumFacing.UP) {
                        offset = distance;
                        enumFacing = facing;
                    }
                }
            }
        }
        return enumFacing;
    }

    private BlockData getBlockData() {
        int startY = MathHelper.floor_double(mc.thePlayer.posY);
        BlockPos targetPos = new BlockPos(
                MathHelper.floor_double(mc.thePlayer.posX),
                (this.stage != 0 && !this.shouldKeepY ? Math.min(startY, this.startY) : startY) - 1,
                MathHelper.floor_double(mc.thePlayer.posZ)
        );
        if (!BlockUtil.isReplaceable(targetPos)) {
            return null;
        } else {
            ArrayList<BlockPos> positions = new ArrayList<>();
            for (int x = -4; x <= 4; x++) {
                for (int y = -4; y <= 0; y++) {
                    for (int z = -4; z <= 4; z++) {
                        BlockPos pos = targetPos.add(x, y, z);
                        if (!BlockUtil.isReplaceable(pos)
                                && !BlockUtil.isInteractable(pos)
                                && !(
                                mc.thePlayer.getDistance((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5)
                                        > (double) mc.playerController.getBlockReachDistance()
                        )
                                && (this.stage == 0 || this.shouldKeepY || pos.getY() < this.startY)) {
                            for (EnumFacing facing : EnumFacing.VALUES) {
                                if (facing != EnumFacing.DOWN) {
                                    BlockPos blockPos = pos.offset(facing);
                                    if (BlockUtil.isReplaceable(blockPos)) {
                                        positions.add(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (positions.isEmpty()) {
                return null;
            } else {
                positions.sort(
                        Comparator.comparingDouble(
                                o -> o.distanceSqToCenter((double) targetPos.getX() + 0.5, (double) targetPos.getY() + 0.5, (double) targetPos.getZ() + 0.5)
                        )
                );
                BlockPos blockPos = positions.get(0);
                EnumFacing facing = this.getBestFacing(blockPos, targetPos);
                return facing == null ? null : new BlockData(blockPos, facing);
            }
        }
    }

    private void place(BlockPos blockPos, EnumFacing enumFacing, Vec3 vec3) {
        if (ItemUtil.isHoldingBlock() && this.blockCount > 0) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), blockPos, enumFacing, vec3)) {
                if (mc.playerController.getCurrentGameType() != GameType.CREATIVE) {
                    this.blockCount--;
                }
                if (this.swing.getValue()) {
                    mc.thePlayer.swingItem();
                } else {
                    PacketUtils.sendPacket(new C0APacketAnimation());
                }
            }
        }
    }

    private EnumFacing yawToFacing(float yaw) {
        if (yaw < -135.0F || yaw > 135.0F) {
            return EnumFacing.NORTH;
        } else if (yaw < -45.0F) {
            return EnumFacing.EAST;
        } else {
            return yaw < 45.0F ? EnumFacing.SOUTH : EnumFacing.WEST;
        }
    }

    private double distanceToEdge(EnumFacing enumFacing) {
        switch (enumFacing) {
            case NORTH:
                return mc.thePlayer.posZ - Math.floor(mc.thePlayer.posZ);
            case EAST:
                return Math.ceil(mc.thePlayer.posX) - mc.thePlayer.posX;
            case SOUTH:
                return Math.ceil(mc.thePlayer.posZ) - mc.thePlayer.posZ;
            case WEST:
            default:
                return mc.thePlayer.posX - Math.floor(mc.thePlayer.posX);
        }
    }

    private float getSpeed() {
        if (!mc.thePlayer.onGround) {
            return this.airMotion.getValue().floatValue() / 100.0F;
        } else {
            return MoveUtil.getSpeedLevel() > 0
                    ? this.speedMotion.getValue().floatValue()/ 100.0F
                    : this.groundMotion.getValue().floatValue() / 100.0F;
        }
    }

    private double getRandomOffset() {
        return 0.2155 - RandomUtils.nextDouble(1.0E-4, 9.0E-4);
    }

    private float getCurrentYaw() {
        return MoveUtil.adjustYaw(
                mc.thePlayer.rotationYaw, (float) MoveUtil.getForwardValue(), (float) MoveUtil.getLeftValue()
        );
    }

    private boolean isDiagonal(float yaw) {
        float absYaw = Math.abs(yaw % 90.0F);
        return absYaw > 20.0F && absYaw < 70.0F;
    }

    private boolean isTowering() {
        if (mc.thePlayer.onGround && MoveUtil.isForwardPressed() && !PlayerUtil.isAirAbove()) {
            boolean keepY = Objects.equals(this.keepY.getValue(), "TELLY");
            boolean tower = Objects.equals(this.tower.getValue(), "TELLY");
            return keepY && this.stage > 0 || tower && mc.gameSettings.keyBindJump.isKeyDown();
        } else {
            return false;
        }
    }

    public int getSlot() {
        return this.lastSlot;
    }

    @EventPriority(1)
    @EventTarget
    public void onUpdate(UpdateEvent2 event) {
        if (this.isEnabled() && event.getType() == UpdateEvent2.EventType.PRE) {
            if (this.rotationTick > 0) {
                this.rotationTick--;
            }
            if (mc.thePlayer.onGround) {
                if (this.stage > 0) {
                    this.stage--;
                }
                if (this.stage < 0) {
                    this.stage++;
                }
                if (this.stage == 0
                        && !Objects.equals(this.keepY.getValue(), "NONE")
                        && (!(Boolean) this.keepYonPress.getValue() || PlayerUtil.isUsingItem())
                        && !mc.gameSettings.keyBindJump.isKeyDown()) {
                    this.stage = 1;
                }
                this.startY = this.shouldKeepY ? this.startY : MathHelper.floor_double(mc.thePlayer.posY);
                this.shouldKeepY = false;
                this.towering = false;
            }
            if (this.canPlace()) {
                ItemStack stack = mc.thePlayer.getHeldItem();
                int count = ItemUtil.isBlock(stack) ? stack.stackSize : 0;
                this.blockCount = Math.min(this.blockCount, count);
                if (this.blockCount <= 0) {
                    int slot = mc.thePlayer.inventory.currentItem;
                    if (this.blockCount == 0) {
                        slot--;
                    }
                    for (int i = slot; i > slot - 9; i--) {
                        int hotbarSlot = (i % 9 + 9) % 9;
                        ItemStack candidate = mc.thePlayer.inventory.getStackInSlot(hotbarSlot);
                        if (ItemUtil.isBlock(candidate)) {
                            mc.thePlayer.inventory.currentItem = hotbarSlot;
                            this.blockCount = candidate.stackSize;
                            break;
                        }
                    }
                }
                float currentYaw = this.getCurrentYaw();
                float yawDiffTo180 = RotationUtil.wrapAngleDiff(currentYaw - 180.0F, event.getYaw());
                float diagonalYaw = this.isDiagonal(currentYaw)
                        ? yawDiffTo180
                        : RotationUtil.wrapAngleDiff(currentYaw - 135.0F * ((currentYaw + 180.0F) % 90.0F < 45.0F ? 1.0F : -1.0F), event.getYaw());
                if (!this.canRotate) {
                    switch (this.rotationMode.getValue()) {
                        case "Vanilla":
                            if (this.yaw == -180.0F && this.pitch == 0.0F) {
                                this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                                this.pitch = RotationUtil.quantizeAngle(85.0F);
                            } else {
                                this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                            }
                            break;
                        case "BackWards":
                            if (this.yaw == -180.0F && this.pitch == 0.0F) {
                                this.yaw = RotationUtil.quantizeAngle(yawDiffTo180);
                                this.pitch = RotationUtil.quantizeAngle(85.0F);
                            } else {
                                this.yaw = RotationUtil.quantizeAngle(yawDiffTo180);
                            }
                            break;
                        case "Strafe":
                            if (this.yaw == -180.0F && this.pitch == 0.0F) {
                                this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                                this.pitch = RotationUtil.quantizeAngle(85.0F);
                            } else {
                                this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                            }
                            break;
                        case "滚木":
                            if (this.yaw == -180.0F && this.pitch == 0.0F) {
                                this.yaw = (float) (RotationUtil.quantizeAngle(diagonalYaw) + RandomUtils.nextDouble(0.7d, 1.5d));
                                this.pitch = RotationUtil.quantizeAngle(85.0F);
                            }
                            break;
                        case "Prediction":
                            BlockData currentBlockData = this.getBlockData();

                            if (currentBlockData != null) {
                                float[] targetRots = RotationUtil.getRotations(getVec3(currentBlockData));
                                float targetYaw = targetRots[0];
                                float targetPitch = targetRots[1];
                                float predictedYaw = getPredictedYaw();
                                float currentYaw2 = this.yaw;
                                float currentPitch = this.pitch;

                                float yawToTarget = MathHelper.wrapAngleTo180_float(targetYaw - currentYaw2);
                                float pitchToTarget = targetPitch - currentPitch;
                                float absYawDiff = Math.abs(yawToTarget);
                                float distance = (float) mc.thePlayer.getDistance(
                                        currentBlockData.blockPos().getX() + 0.5,
                                        currentBlockData.blockPos().getY() + 0.5,
                                        currentBlockData.blockPos().getZ() + 0.5
                                );
                                float currentSpeed = getCurrentSpeed(distance);
                                float actualYawDiff = MathHelper.wrapAngleTo180_float(currentYaw - lastYaw);
                                float error = Math.abs(actualYawDiff - lastYawChange);
                                lastErrors[errorIndex] = error;
                                errorIndex = (errorIndex + 1) % 20;

                                float avgError = 0;
                                for (float e : lastErrors) avgError += e;
                                avgError /= 20;

                                if (avgError > 5) currentSpeed *= 0.8F;
                                else if (avgError < 1) currentSpeed *= 1.1F;
                                float yawChange;
                                if (absYawDiff > 90) {
                                    yawChange = Math.signum(yawToTarget) * currentSpeed * 1.2F;
                                } else if (absYawDiff > 30) {
                                    yawChange = Math.signum(yawToTarget) * currentSpeed * 0.8F;
                                } else {
                                    float fineSpeed = currentSpeed * 0.3F;
                                    yawChange = yawToTarget * 0.2F;
                                    yawChange = MathHelper.clamp_float(yawChange, -fineSpeed, fineSpeed);
                                }
                                float inertia = 0.3F;
                                yawChange = lastYawChange * inertia + yawChange * (1 - inertia);
                                lastYawChange = yawChange;
                                float pitchChange = Math.signum(pitchToTarget) * currentSpeed * 0.3F;
                                pitchChange = lastPitchChange * inertia + pitchChange * (1 - inertia);
                                lastPitchChange = pitchChange;
                                double ticks = 1.0;
                                double futureX = mc.thePlayer.posX + mc.thePlayer.motionX * ticks;
                                double futureY = mc.thePlayer.posY + mc.thePlayer.motionY * ticks;
                                double futureZ = mc.thePlayer.posZ + mc.thePlayer.motionZ * ticks;
                                BlockPos futureBlockPos = new BlockPos(
                                        MathHelper.floor_double(futureX),
                                        MathHelper.floor_double(futureY) - 1,
                                        MathHelper.floor_double(futureZ)
                                );

                                if (BlockUtil.isReplaceable(futureBlockPos)) {
                                    float[] futureRots = RotationUtil.getRotations(
                                            futureX, futureY + mc.thePlayer.getEyeHeight(), futureZ,
                                            currentBlockData.blockPos().getX() + 0.5,
                                            currentBlockData.blockPos().getY() + 0.5,
                                            currentBlockData.blockPos().getZ() + 0.5
                                    );
                                    yawChange = yawChange * 0.7F + (futureRots[0] - currentYaw) * 0.3F;
                                }

                                float jitterAmount;
                                if (absYawDiff < 5) {
                                    jitterAmount = RandomUtils.nextFloat(-2.0F, 2.0F);
                                    if (RandomUtils.nextDouble(0, 1) < 0.05) jitterAmount *= 3;
                                } else if (this.towering) {
                                    jitterAmount = RandomUtils.nextFloat(-0.5F, 0.5F);
                                } else {
                                    jitterAmount = RandomUtils.nextFloat(-1.0F, 1.0F);
                                }
                                yawChange += jitterAmount;
                                yawChange = MathHelper.clamp_float(yawChange, -currentSpeed, currentSpeed);
                                pitchChange = MathHelper.clamp_float(pitchChange, -currentSpeed * 0.4F, currentSpeed * 0.4F);
                                yawChange += RandomUtils.nextFloat(-0.5F, 0.5F);
                                pitchChange += RandomUtils.nextFloat(-0.3F, 0.3F);
                                float newYaw = currentYaw + yawChange;
                                float newPitch = currentPitch + pitchChange;
                                newPitch = MathHelper.clamp_float(newPitch, -90F, 90F);
                                if (this.yaw == -180.0F && this.pitch == 0.0F) {
                                    newYaw = RotationUtil.quantizeAngle(predictedYaw);
                                    newPitch = 85.0F;
                                }
                                this.yaw = RotationUtil.quantizeAngle(newYaw);
                                this.pitch = RotationUtil.quantizeAngle(newPitch);
                                lastYaw = this.yaw;

                            } else {
                                if (this.yaw != -180.0F) {
                                    float targetYaw = event.getYaw();
                                    float yawDiff = MathHelper.wrapAngleTo180_float(targetYaw - this.yaw);
                                    float returnSpeed = 3.0F;
                                    yawDiff = MathHelper.clamp_float(yawDiff, -returnSpeed, returnSpeed);

                                    this.yaw = RotationUtil.quantizeAngle(this.yaw + yawDiff);

                                    if (this.pitch > 10) {
                                        this.pitch -= 1.0F;
                                    } else if (this.pitch < -10) {
                                        this.pitch += 1.0F;
                                    }
                                }
                            }
                            break;
                    }
                }
                BlockData blockData = this.getBlockData();
                Vec3 hitVec = null;
                if (blockData != null) {
                    double[] x = placeOffsets;
                    double[] y = placeOffsets;
                    double[] z = placeOffsets;
                    switch (blockData.facing()) {
                        case NORTH:
                            z = new double[]{0.0};
                            break;
                        case EAST:
                            x = new double[]{1.0};
                            break;
                        case SOUTH:
                            z = new double[]{1.0};
                            break;
                        case WEST:
                            x = new double[]{0.0};
                            break;
                        case DOWN:
                            y = new double[]{0.0};
                            break;
                        case UP:
                            y = new double[]{1.0};
                    }
                    float bestYaw = -180.0F;
                    float bestPitch = 0.0F;
                    float bestDiff = 0.0F;
                    for (double dx : x) {
                        for (double dy : y) {
                            for (double dz : z) {
                                double relX = (double) blockData.blockPos().getX() + dx - mc.thePlayer.posX;
                                double relY = (double) blockData.blockPos().getY() + dy - mc.thePlayer.posY - (double) mc.thePlayer.getEyeHeight();
                                double relZ = (double) blockData.blockPos().getZ() + dz - mc.thePlayer.posZ;
                                float baseYaw = RotationUtil.wrapAngleDiff(this.yaw, event.getYaw());
                                float[] rotations = RotationUtil.getRotationsTo(relX, relY, relZ, baseYaw, this.pitch);
                                MovingObjectPosition mop = RotationUtil.rayTrace(rotations[0], rotations[1], mc.playerController.getBlockReachDistance(), 1.0F);
                                if (mop != null
                                        && mop.typeOfHit == MovingObjectType.BLOCK
                                        && mop.getBlockPos().equals(blockData.blockPos())
                                        && mop.sideHit == blockData.facing()) {
                                    float totalDiff = Math.abs(rotations[0] - baseYaw) + Math.abs(rotations[1] - this.pitch);
                                    if (bestYaw == -180.0F && bestPitch == 0.0F || totalDiff < bestDiff) {
                                        bestYaw = rotations[0];
                                        bestPitch = rotations[1];
                                        bestDiff = totalDiff;
                                        hitVec = mop.hitVec;
                                    }
                                }
                            }
                        }
                    }
                    if (bestYaw != -180.0F || bestPitch != 0.0F) {
                        this.yaw = bestYaw;
                        this.pitch = bestPitch;
                        this.canRotate = true;
                    }
                }
                if (this.canRotate && MoveUtil.isForwardPressed() && Math.abs(MathHelper.wrapAngleTo180_float(yawDiffTo180 - this.yaw)) < 90.0F) {
                    switch (this.rotationMode.getValue()) {
                        case "BackWards":
                            this.yaw = RotationUtil.quantizeAngle(yawDiffTo180);
                            break;
                        case "Strafe":
                            this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                    }
                }
                if (!Objects.equals(this.rotationMode.getValue(), "None")) {
                    float targetYaw = this.yaw;
                    float targetPitch = this.pitch;
                    if (this.towering && (mc.thePlayer.motionY > 0.0 || mc.thePlayer.posY > (double) (this.startY + 1))) {
                        float yawDiff = MathHelper.wrapAngleTo180_float(this.yaw - event.getYaw());
                        float tolerance = this.rotationTick >= 2 ? RandomUtils.nextFloat(tellystartrotationminspeed.getValue().floatValue(), tellystartrotationmaxspeed.getValue().floatValue()) : RandomUtils.nextFloat(tellynormalrotationminspeed.getValue().floatValue(), tellynormalrotationmaxspeed.getValue().floatValue());
                        if (Math.abs(yawDiff) > tolerance) {
                            float clampedYaw = RotationUtil.clampAngle(yawDiff, tolerance);
                            targetYaw = RotationUtil.quantizeAngle(event.getYaw() + clampedYaw);
                            this.rotationTick = Math.max(this.rotationTick, 1);
                        }
                    }
                    if (this.isTowering()) {
                        float yawDelta = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - event.getYaw());
                        targetYaw = RotationUtil.quantizeAngle(event.getYaw() + yawDelta * RandomUtils.nextFloat(0.98F, 0.99F));
                        targetPitch = RotationUtil.quantizeAngle(RandomUtils.nextFloat(30.0F, 80.0F));
                        this.rotationTick = 3;
                        this.towering = true;
                    }
                    event.setRotation(targetYaw, targetPitch, 3);
                    if (Objects.equals(this.moveFix.getValue(), "SILENT")) {
                        event.setPervRotation(targetYaw, 3);
                    }
                }
                if (blockData != null && hitVec != null && this.rotationTick <= 0) {
                    this.place(blockData.blockPos(), blockData.facing(), hitVec);
                    if (this.multiplace.getValue()) {
                        for (int i = 0; i < 3; i++) {
                            blockData = this.getBlockData();
                            if (blockData == null) {
                                break;
                            }
                            MovingObjectPosition mop = RotationUtil.rayTrace(this.yaw, this.pitch, mc.playerController.getBlockReachDistance(), 1.0F);
                            if (mop != null
                                    && mop.typeOfHit == MovingObjectType.BLOCK
                                    && mop.getBlockPos().equals(blockData.blockPos())
                                    && mop.sideHit == blockData.facing()) {
                                this.place(blockData.blockPos(), blockData.facing(), mop.hitVec);
                            } else {
                                hitVec = BlockUtil.getClickVec(blockData.blockPos(), blockData.facing());
                                double dx = hitVec.xCoord - mc.thePlayer.posX;
                                double dy = hitVec.yCoord - mc.thePlayer.posY - (double) mc.thePlayer.getEyeHeight();
                                double dz = hitVec.zCoord - mc.thePlayer.posZ;
                                float[] rotations = RotationUtil.getRotationsTo(dx, dy, dz, event.getYaw(), event.getPitch());
                                if (!(Math.abs(rotations[0] - this.yaw) < 120.0F) || !(Math.abs(rotations[1] - this.pitch) < 60.0F)) {
                                    break;
                                }
                                mop = RotationUtil.rayTrace(rotations[0], rotations[1], mc.playerController.getBlockReachDistance(), 1.0F);
                                if (mop == null
                                        || mop.typeOfHit != MovingObjectType.BLOCK
                                        || !mop.getBlockPos().equals(blockData.blockPos())
                                        || mop.sideHit != blockData.facing()) {
                                    break;
                                }
                                this.place(blockData.blockPos(), blockData.facing(), mop.hitVec);
                            }
                        }
                    }
                }
                if (this.targetFacing != null) {
                    if (this.rotationTick <= 0) {
                        int playerBlockX = MathHelper.floor_double(mc.thePlayer.posX);
                        int playerBlockY = MathHelper.floor_double(mc.thePlayer.posY);
                        int playerBlockZ = MathHelper.floor_double(mc.thePlayer.posZ);
                        BlockPos belowPlayer = new BlockPos(playerBlockX, playerBlockY - 1, playerBlockZ);
                        hitVec = BlockUtil.getHitVec(belowPlayer, this.targetFacing, this.yaw, this.pitch);
                        this.place(belowPlayer, this.targetFacing, hitVec);
                    }
                    this.targetFacing = null;
                } else if (Objects.equals(this.keepY.getValue(), "EXTRA") && this.stage > 0 && !mc.thePlayer.onGround) {
                    int nextBlockY = MathHelper.floor_double(mc.thePlayer.posY + mc.thePlayer.motionY);
                    if (nextBlockY <= this.startY && mc.thePlayer.posY > (double) (this.startY + 1)) {
                        this.shouldKeepY = true;
                        blockData = this.getBlockData();
                        if (blockData != null && this.rotationTick <= 0) {
                            hitVec = BlockUtil.getHitVec(blockData.blockPos(), blockData.facing(), this.yaw, this.pitch);
                            this.place(blockData.blockPos(), blockData.facing(), hitVec);
                        }
                    }
                }
            }
        }
    }
    private float getCurrentSpeed(float distance) {
        float baseSpeed;
        if (this.towering) {
            baseSpeed = 40.0F;
        } else if (MoveUtil.getSpeedLevel() > 0) {
            baseSpeed = 35.0F;
        } else {
            baseSpeed = 25.0F;
        }
        float speedMultiplier = Math.min(1.2F, distance);
        float currentSpeed = baseSpeed * speedMultiplier;
        currentSpeed = Math.min(45.0F, Math.max(10.0F, currentSpeed));
        return currentSpeed;
    }

    private float getPredictedYaw() {
        float currentMoveYaw = this.getCurrentYaw();
        float predictedYaw;
        if (this.isDiagonal(currentMoveYaw)) {
            predictedYaw = currentMoveYaw - 180.0F;
        } else {
            float sideMultiplier = (currentMoveYaw + 180.0F) % 90.0F < 45.0F ? 1.0F : -1.0F;
            predictedYaw = currentMoveYaw - 135.0F * sideMultiplier;
        }
        return predictedYaw;
    }

    private Vec3 getVec3(BlockData data) {
        if (data == null) return null;

        BlockPos pos = data.blockPos();
        EnumFacing face = data.facing();
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        x += (double) face.getFrontOffsetX() * 0.5D;
        y += (double) face.getFrontOffsetY() * 0.5D;
        z += (double) face.getFrontOffsetZ() * 0.5D;

        return new Vec3(x, y, z);
    }
    @EventTarget
    public void onStrafe(EventStrafe event) {
        if (this.isEnabled()) {
            if (!mc.thePlayer.isCollidedHorizontally
                    && mc.thePlayer.hurtTime <= 5
                    && !mc.thePlayer.isPotionActive(Potion.jump)
                    && mc.gameSettings.keyBindJump.isKeyDown()
                    && ItemUtil.isHoldingBlock()) {
                int yState = (int) (mc.thePlayer.posY % 1.0 * 100.0);
                switch (this.tower.getValue()) {
                    case "VANILLA":
                        switch (this.towerTick) {
                            case 0:
                                if (mc.thePlayer.onGround) {
                                    this.towerTick = 1;
                                    mc.thePlayer.motionY = -0.0784000015258789;
                                }
                                return;
                            case 1:
                                if (yState == 0 && PlayerUtil.isAirBelow()) {
                                    this.startY = MathHelper.floor_double(mc.thePlayer.posY);
                                    this.towerTick = 2;
                                    mc.thePlayer.motionY = 0.42F;
                                    if (MoveUtil.isForwardPressed()) {
                                        MoveUtil.setSpeed(MoveUtil.getSpeed(), MoveUtil.getMoveYaw());
                                    } else {
                                        MoveUtil.setSpeed(0.0);
                                        event.setForward(0.0F);
                                        event.setStrafe(0.0F);
                                    }
                                    return;
                                } else {
                                    this.towerTick = 0;
                                    return;
                                }
                            case 2:
                                this.towerTick = 3;
                                mc.thePlayer.motionY = 0.75 - mc.thePlayer.posY % 1.0;
                                return;
                            case 3:
                                this.towerTick = 1;
                                mc.thePlayer.motionY = 1.0 - mc.thePlayer.posY % 1.0;
                                return;
                            default:
                                this.towerTick = 0;
                                return;
                        }
                    case "EXTRA":
                        switch (this.towerTick) {
                            case 0:
                                if (mc.thePlayer.onGround) {
                                    this.towerTick = 1;
                                    mc.thePlayer.motionY = -0.0784000015258789;
                                }
                                return;
                            case 1:
                                if (yState == 0 && PlayerUtil.isAirBelow()) {
                                    this.startY = MathHelper.floor_double(mc.thePlayer.posY);
                                    if (!MoveUtil.isForwardPressed()) {
                                        this.towerDelay = 2;
                                        MoveUtil.setSpeed(0.0);
                                        event.setForward(0.0F);
                                        event.setStrafe(0.0F);
                                        EnumFacing facing = this.yawToFacing(MathHelper.wrapAngleTo180_float(this.yaw - 180.0F));
                                        double distance = this.distanceToEdge(facing);
                                        if (distance > 0.1) {
                                            if (mc.thePlayer.onGround) {
                                                Vec3i directionVec = facing.getDirectionVec();
                                                double offset = Math.min(this.getRandomOffset(), distance - 0.05);
                                                double jitter = RandomUtils.nextDouble(0.02, 0.03);
                                                AxisAlignedBB nextBox = mc.thePlayer
                                                        .getEntityBoundingBox()
                                                        .offset((double) directionVec.getX() * (offset - jitter), 0.0, (double) directionVec.getZ() * (offset - jitter));
                                                if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, nextBox).isEmpty()) {
                                                    mc.thePlayer.motionY = -0.0784000015258789;
                                                    mc.thePlayer
                                                            .setPosition(nextBox.minX + (nextBox.maxX - nextBox.minX) / 2.0, nextBox.minY, nextBox.minZ + (nextBox.maxZ - nextBox.minZ) / 2.0);
                                                }
                                                return;
                                            }
                                        } else {
                                            this.towerTick = 2;
                                            this.targetFacing = facing;
                                            mc.thePlayer.motionY = 0.42F;
                                        }
                                        return;
                                    } else {
                                        this.towerTick = 2;
                                        this.towerDelay++;
                                        mc.thePlayer.motionY = 0.42F;
                                        MoveUtil.setSpeed(MoveUtil.getSpeed(), MoveUtil.getMoveYaw());
                                        return;
                                    }
                                } else {
                                    this.towerTick = 0;
                                    this.towerDelay = 0;
                                    return;
                                }
                            case 2:
                                this.towerTick = 3;
                                mc.thePlayer.motionY = mc.thePlayer.motionY - RandomUtils.nextDouble(0.00101, 0.00109);
                                return;
                            case 3:
                                if (this.towerDelay >= 4) {
                                    this.towerTick = 4;
                                    this.towerDelay = 0;
                                } else {
                                    this.towerTick = 1;
                                    mc.thePlayer.motionY = 1.0 - mc.thePlayer.posY % 1.0;
                                }
                                return;
                            case 4:
                                this.towerTick = 5;
                                return;
                            case 5:
                                if (!PlayerUtil.isAirBelow()) {
                                    this.towerTick = 0;
                                } else {
                                    this.towerTick = 1;
                                    mc.thePlayer.motionY -= 0.08;
                                    mc.thePlayer.motionY *= 0.98F;
                                    mc.thePlayer.motionY -= 0.08;
                                    mc.thePlayer.motionY *= 0.98F;
                                }
                                return;
                            default:
                                this.towerTick = 0;
                                this.towerDelay = 0;
                                return;
                        }
                    default:
                        this.towerTick = 0;
                        this.towerDelay = 0;
                }
            } else {
                this.towerTick = 0;
                this.towerDelay = 0;
            }
        }
    }

    @EventTarget
    public void onMoveInput(EventMoveInput event) {
        if (this.isEnabled()) {
            if (Objects.equals(this.moveFix.getValue(), "SILENT")
                    && RotationState.isActived()
                    && RotationState.getPriority() == 3.0F
                    && MoveUtil.isForwardPressed()) {
                MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
            }
            if (mc.thePlayer.onGround && this.stage > 0 && MoveUtil.isForwardPressed()) {
                mc.thePlayer.movementInput.jump = true;
            }
        }
    }

    @EventTarget
    public void onLivingUpdate(EventUpdate event) {
        if (this.isEnabled()) {
            float speed = this.getSpeed();
            if (speed != 1.0F) {
                if (mc.thePlayer.movementInput.moveForward != 0.0F && mc.thePlayer.movementInput.moveStrafe != 0.0F) {
                    mc.thePlayer.movementInput.moveForward = mc.thePlayer.movementInput.moveForward * (1.0F / (float) Math.sqrt(2.0));
                    mc.thePlayer.movementInput.moveStrafe = mc.thePlayer.movementInput.moveStrafe * (1.0F / (float) Math.sqrt(2.0));
                }
                mc.thePlayer.movementInput.moveForward *= speed;
                mc.thePlayer.movementInput.moveStrafe *= speed;
            }
            if (this.shouldStopSprint()) {
                mc.thePlayer.setSprinting(false);
            }
        }
    }

    @EventTarget
    public void onSafeWalk(EventSafeWalk event) {
        if (this.isEnabled() && this.safeWalk.getValue()) {
            if (mc.thePlayer.onGround && mc.thePlayer.motionY <= 0.0 && PlayerUtil.canMove(mc.thePlayer.motionX, mc.thePlayer.motionZ, -1.0)) {
                event.setSafeWalk(true);
            }
        }
    }

    @EventTarget
    public void onRender(EventRender2D event) {
        if (this.isEnabled()) {
            int count = 0;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (stack != null && stack.stackSize > 0) {
                    Item item = stack.getItem();
                    if (item instanceof ItemBlock) {
                        Block block = ((ItemBlock) item).getBlock();
                        if (!BlockUtil.isInteractable(block) && BlockUtil.isSolid(block)) {
                            count += stack.stackSize;
                        }
                    }
                }
            }
            HypScaffold.count = count;
            HUD hud = (HUD) TYClient.getModuleManager().getModule(HUD.class);
            float scale = 1.0F;
            if (this.blockCounter.getValue()) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 0.0F);
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                mc.fontRendererObj
                        .drawString(
                                String.format("%d block%s left", count, count != 1 ? "s" : ""),
                                ((float) new ScaledResolution(mc).getScaledWidth() / 2.0F + (float) mc.fontRendererObj.FONT_HEIGHT * 1.5F) / scale,
                                (float) new ScaledResolution(mc).getScaledHeight() / 2.0F / scale - (float) mc.fontRendererObj.FONT_HEIGHT / 2.0F + 1.0F,
                                (count > 0 ? Color.WHITE.getRGB() : new Color(255, 85, 85).getRGB()) | -1090519040,
                                TYClient.getModuleManager().getModule(PostProcessing.class).isEnable()
                        );
                GlStateManager.disableBlend();
                GlStateManager.enableDepth();
                GlStateManager.popMatrix();
            }
        }
    }

    @EventTarget
    public void onLeftClick(EventLeftClickMouse event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onRightClick(EventRightClickMouse event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onHitBlock(EventBlockHit event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onSwap(EventSwapItem event) {
        if (this.isEnabled()) {
            this.lastSlot = event.setSlot(this.lastSlot);
            event.setCancelled(true);
        }
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) {
            this.lastSlot = mc.thePlayer.inventory.currentItem;
        } else {
            this.lastSlot = -1;
        }
        this.blockCount = -1;
        this.rotationTick = 3;
        this.yaw = -180.0F;
        this.pitch = 0.0F;
        this.canRotate = false;
        this.towerTick = 0;
        this.towerDelay = 0;
        this.towering = false;
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null && this.lastSlot != -1) {
            mc.thePlayer.inventory.currentItem = this.lastSlot;
        }
    }

    public static class BlockData {
        private final BlockPos blockPos;
        private final EnumFacing facing;

        public BlockData(BlockPos blockPos, EnumFacing enumFacing) {
            this.blockPos = blockPos;
            this.facing = enumFacing;
        }

        public BlockPos blockPos() {
            return this.blockPos;
        }

        public EnumFacing facing() {
            return this.facing;
        }
    }
}
