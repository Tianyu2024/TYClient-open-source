package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.player.EventPlace;
import cn.tianyu.client.event.events.player.EventStrafe;
import cn.tianyu.client.event.events.player.UpdateEvent2;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.event.events.render.EventRender3D;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.font.RapeMasterFontManager;
import cn.tianyu.client.gui.notification.NotificationManager;
import cn.tianyu.client.gui.notification.NotificationType;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.InventoryUtils;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.annotations.Animation;
import cn.tianyu.client.util.annotations.Direction;
import cn.tianyu.client.util.impl.client.MathUtil;
import cn.tianyu.client.util.impl.player.*;
import cn.tianyu.client.util.impl.render.ColorUtil;
import cn.tianyu.client.util.impl.render.DecelerateAnimation;
import cn.tianyu.client.util.impl.render.RoundedUtils;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;
import org.lwjglx.util.vector.Vector2f;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.*;
import java.util.List;

/**
 * @Author: CN_tianyu
 */
public class Scaffold extends Module {

    private final ModeValue countMode = new ModeValue("Block Counter", "Tenacity", "None", "Tenacity", "Basic", "Polar");

    public static final List<Block> invalidBlocks = Arrays.asList(Blocks.enchanting_table, Blocks.furnace, Blocks.carpet, Blocks.crafting_table, Blocks.trapped_chest, Blocks.chest, Blocks.dispenser, Blocks.air, Blocks.water, Blocks.lava, Blocks.flowing_water, Blocks.flowing_lava, Blocks.snow_layer, Blocks.torch, Blocks.anvil, Blocks.jukebox, Blocks.stone_button, Blocks.wooden_button, Blocks.lever, Blocks.noteblock, Blocks.stone_pressure_plate, Blocks.light_weighted_pressure_plate, Blocks.wooden_pressure_plate, Blocks.heavy_weighted_pressure_plate, Blocks.stone_slab, Blocks.wooden_slab, Blocks.stone_slab2, Blocks.red_mushroom, Blocks.brown_mushroom, Blocks.yellow_flower, Blocks.red_flower, Blocks.anvil, Blocks.glass_pane, Blocks.stained_glass_pane, Blocks.iron_bars, Blocks.cactus, Blocks.ladder, Blocks.web, Blocks.tnt);
    private static final BooleanValue keepYValue = new BooleanValue("Keep Y", false);
    public static Scaffold INSTANCE;
    public static double keepYCoord;
    public final BooleanValue swing = new BooleanValue("Swing", true);
    public final BooleanValue sprintValue = new BooleanValue("Sprint", false);
    public final BooleanValue eagle = new BooleanValue("Eagle", false);
    public final BooleanValue telly = new BooleanValue("Telly", true);
    public final BooleanValue upValue = new BooleanValue("Up", false, () -> this.telly.getValue() && !keepYValue.getValue());
    public final BooleanValue esp = new BooleanValue("ESP", true);
    public final BooleanValue lockAngle = new BooleanValue("Lock Angle", true);
    public final ModeValue angleMode = new ModeValue("Angle Mode", "45", "45", "90", "135", "180");

    public boolean tip = false;
    public int vl = 0;
    protected Random rand = new Random();
    int idkTick = 0;
    int towerTick = 0;
    private int direction;
    private Robot robot;

    @Getter
    private int slot;
    private BlockPos data;
    private boolean canTellyPlace;
    private int prevItem = 0;
    private EnumFacing enumFacing;

    private float lockedYaw;
    private float lockedPitch;
    private boolean isLocked;
    private int lockTicks;

    public Scaffold() {
        super("Scaffold", "自动搭路", Category.movement);
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public static double getYLevel() {
        if (!keepYValue.getValue()) {
            return Scaffold.mc.thePlayer.posY - 1.0;
        }
        return !MoveUtil.isMoving() ? Scaffold.mc.thePlayer.posY - 1.0 : keepYCoord;
    }

    public static Vec3 getVec3(BlockPos pos, EnumFacing face) {
        double x2 = (double)pos.getX() + 0.5;
        double y2 = (double)pos.getY() + 0.5;
        double z = (double)pos.getZ() + 0.5;
        if (face == EnumFacing.UP || face == EnumFacing.DOWN) {
            x2 += MathUtil.getRandomInRange(0.3, -0.3);
            z += MathUtil.getRandomInRange(0.3, -0.3);
        } else {
            y2 += 0.08;
        }
        if (face == EnumFacing.WEST || face == EnumFacing.EAST) {
            z += MathUtil.getRandomInRange(0.3, -0.3);
        }
        if (face == EnumFacing.SOUTH || face == EnumFacing.NORTH) {
            x2 += MathUtil.getRandomInRange(0.3, -0.3);
        }
        return new Vec3(x2, y2, z);
    }

    @Override
    public void onEnable() {
        this.idkTick = 5;
        if (Scaffold.mc.thePlayer == null) {
            return;
        }
//        if(mc.thePlayer.rotationPitch != 90.0) {
//            NotificationManager.post(NotificationType.WARNING,"Scaffold Warning",
//                    "请竖直看地面身子朝后然后点击一下地面");
//            this.toggle();
//        } else {
//            mc.thePlayer.rotationYaw = 0.21775578f;
//            mc.clickMouse();
//        }
        this.prevItem = Scaffold.mc.thePlayer.inventory.currentItem;
        Scaffold.mc.thePlayer.setSprinting(this.sprintValue.getValue() || !this.canTellyPlace);
        Scaffold.mc.gameSettings.keyBindSprint.pressed = this.sprintValue.getValue() || !this.canTellyPlace;
        this.canTellyPlace = false;
        this.tip = false;
        this.data = null;
        this.slot = -1;

        this.isLocked = false;
        this.lockTicks = 0;
        if (this.lockAngle.getValue() && !this.telly.getValue()) {
            this.lockedYaw = getLockedYaw();
            this.lockedPitch = 78.0f;
            this.isLocked = true;
        }
    }

    @Override
    public void onDisable() {
        if (Scaffold.mc.thePlayer == null) {
            return;
        }
        KeyBinding.setKeyBindState(Scaffold.mc.gameSettings.keyBindSneak.getKeyCode(), false);
        Scaffold.mc.thePlayer.inventory.currentItem = this.prevItem;
        TYClient.instance.slotSpoofManager.stopSpoofing();
        this.isLocked = false;
        TYClient.rotationManager.resetRotation();
    }

    private float getLockedYaw() {
        float angle;
        switch (angleMode.get()) {
            case "45": angle = 45; break;
            case "90": angle = 90; break;
            case "135": angle = 135; break;
            case "180": angle = 180; break;
            default: angle = 45;
        }
        float currentYaw = mc.thePlayer.rotationYaw;
        float normalized = currentYaw % 360;
        if (normalized < 0) normalized += 360;

        float closest = angle;
        float minDiff = 360;
        for (int i = -3; i <= 3; i++) {
            float candidate = angle + i * 90;
            float diff = Math.abs(normalized - candidate);
            if (diff < minDiff) {
                minDiff = diff;
                closest = candidate;
            }
        }
        return closest;
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (this.data == null) {
            return;
        }
        for (int i = 0; i < 2; ++i) {
            BlockPos blockPos = this.data;
            PlaceInfo placeInfo = PlaceInfo.get(blockPos);
            if (!BlockUtil.isValidBock(blockPos) || placeInfo == null || !this.esp.getValue()) continue;
            RenderUtil.drawBlockBox(blockPos, new Color(255, 255, 255, 255), false);
            break;
        }
    }

    @EventTarget
    public void onUpdate(EventMotion event) {
        if (this.idkTick > 0) {
            --this.idkTick;
        }
        if (event.isPre() && this.eagle.getValue()) {
            if (Eagle.getBlockUnderPlayer(Scaffold.mc.thePlayer) instanceof BlockAir) {
                if (Scaffold.mc.thePlayer.onGround) {
                    KeyBinding.setKeyBindState(Scaffold.mc.gameSettings.keyBindSneak.getKeyCode(), true);
                }
            } else if (Scaffold.mc.thePlayer.onGround) {
                KeyBinding.setKeyBindState(Scaffold.mc.gameSettings.keyBindSneak.getKeyCode(), false);
            }
        }
    }

    @EventTarget
    public void onStrafe(EventStrafe event) {
        if ((this.upValue.getValue() || keepYValue.getValue()) && Scaffold.mc.thePlayer.onGround && MoveUtil.isMoving() && !Scaffold.mc.gameSettings.keyBindJump.isKeyDown()) {
            Scaffold.mc.thePlayer.jump();
        }
    }

    @EventTarget
    private void onTick(EventTick event) {
        if (Scaffold.mc.thePlayer == null) {
            return;
        }
        if (this.slot < 0) {
            return;
        }
        if (!this.telly.getValue()) {
            this.canTellyPlace = true;
        }
    }

    @EventTarget
    private void onPlace(EventPlace event) {
        this.slot = this.getBlockSlot();
        if (this.slot < 0) {
            return;
        }
        if (!this.telly.getValue()) {
            Scaffold.mc.thePlayer.setSprinting(this.sprintValue.getValue());
            Scaffold.mc.gameSettings.keyBindSprint.pressed = false;
        }
        event.setCancelled(true);
        if (Scaffold.mc.thePlayer == null) {
            return;
        }
        this.place();
        robotclick();
    }

    private void robotclick(){
        robot.mousePress(InputEvent.BUTTON3_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_MASK);
    }

    private final float[] normalPos = {78f, -315, -225, -135, -45, 0, 45, 135, 225, 315};
    public float[] getYawPitch(float[] positions, int index) {
        float playerYaw = mc.thePlayer.rotationYaw;
        float pitch = positions[0];
        if (index == 0) {
            return new float[]{playerYaw, pitch};
        } else {
            return new float[]{playerYaw + positions[index], pitch};
        }
    }

    @EventTarget
    private void onUpdate(UpdateEvent2 event) {
        if (this.telly.getValue()) {
            if (Scaffold.mc.gameSettings.keyBindJump.pressed) {
                this.upValue.setValue(true);
                keepYValue.setValue(false);
            } else {
                this.upValue.setValue(false);
                keepYValue.setValue(true);
            }
        }
        if (Scaffold.mc.thePlayer.onGround) {
            keepYCoord = Math.floor(Scaffold.mc.thePlayer.posY - 1.0);
        }

        if (this.lockAngle.getValue() && !this.telly.getValue() && this.isLocked) {
            if (this.lockTicks % 5 == 0) {
                this.lockedYaw = getLockedYaw();
            }
            this.lockTicks++;
        }

        this.slot = this.getBlockSlot();
        if (this.slot < 0) {
            return;
        }
        Scaffold.mc.thePlayer.inventory.currentItem = this.slot;
        TYClient.instance.slotSpoofManager.startSpoofing(this.prevItem);
        this.findBlock();
        if (this.telly.getValue()) {
            if (this.canTellyPlace && !Scaffold.mc.thePlayer.onGround && MoveUtil.isMoving()) {
                Scaffold.mc.thePlayer.setSprinting(false);
            }
            boolean bl = this.canTellyPlace = Scaffold.mc.thePlayer.offGroundTicks >= (this.upValue.getValue() ? (Scaffold.mc.thePlayer.ticksExisted % 16 == 0 ? 2 : 1) : 3);
        }
        if (!this.canTellyPlace) {
            return;
        }
        if (this.data != null) {
            if (!this.telly.getValue() && this.lockAngle.getValue() && this.isLocked) {
                float pitch = 78.0f + (float)(Math.random() - 0.5) * 2.0f;
                event.setRotation(this.lockedYaw, pitch, 3);

                //TYClient.rotationManager.setRotation(new Vector2f(this.lockedYaw, pitch), 180.0f, true);
            } else {
                float yaw = RotationUtil.getRotationForPlace(this.data)[0];
                float pitch = RotationUtil.getRotationForPlace(this.data)[1];
                event.setRotation(yaw, pitch, 3);
                //TYClient.rotationManager.setRotation(new Vector2f(yaw, pitch), 180.0f, true);
            }
        }
    }

    @EventTarget
    private void onMotion(EventMotion event) {
        if (event.isPre()) {
            if (this.slot < 0) {
                return;
            }
            if (this.getBlockCount() <= 0) {
                int spoofSlot = this.getBestSpoofSlot();
                this.getBlock(spoofSlot);
            }
            if (this.slot < 0) {
                return;
            }
            Scaffold.mc.thePlayer.inventoryContainer.getSlot(this.slot + 36).getStack();
        }
    }

    private void place() {
        if (!this.canTellyPlace) {
            return;
        }
        this.slot = this.getBlockSlot();
        if (this.slot < 0) {
            return;
        }
        if (this.data != null) {
            EnumFacing enumFacing;
            EnumFacing enumFacing2 = enumFacing = keepYValue.getValue() ? this.enumFacing : this.getPlaceSide(this.data);
            if (enumFacing == null) {
                return;
            }
            if (Scaffold.mc.playerController.onPlayerRightClick(Scaffold.mc.thePlayer, Scaffold.mc.theWorld, Scaffold.mc.thePlayer.getCurrentEquippedItem(), this.data, enumFacing, Scaffold.getVec3(this.data, enumFacing))) {
                if (this.swing.getValue()) {
                    Scaffold.mc.thePlayer.swingItem();
                } else {
                    Scaffold.mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                }
            }
        }
    }

    private void findBlock() {
        if (MoveUtil.isMoving() && keepYValue.getValue()) {
            boolean shouldGoDown = false;
            BlockPos blockPosition = new BlockPos(Scaffold.mc.thePlayer.posX, Scaffold.getYLevel(), Scaffold.mc.thePlayer.posZ);
            if (BlockUtil.isValidBock(blockPosition) || this.search(blockPosition, !shouldGoDown)) {
                return;
            }
            for (int x2 = -1; x2 <= 1; ++x2) {
                for (int z = -1; z <= 1; ++z) {
                    if (!this.search(blockPosition.add(x2, 0, z), !shouldGoDown)) continue;
                    return;
                }
            }
        } else {
            this.data = this.getBlockPos();
        }
    }

    private double calcStepSize(double range) {
        double accuracy = 6.0;
        accuracy += accuracy % 2.0;
        return Math.max(range / accuracy, 0.01);
    }

    private boolean search(BlockPos blockPosition, boolean checks) {
        Vec3 eyesPos = new Vec3(Scaffold.mc.thePlayer.posX, Scaffold.mc.thePlayer.getEntityBoundingBox().minY + (double)Scaffold.mc.thePlayer.getEyeHeight(), Scaffold.mc.thePlayer.posZ);
        PlaceRotation placeRotation = null;
        double xzRV = 0.5;
        double yRV = 0.5;
        double xzSSV = this.calcStepSize(xzRV);
        double ySSV = this.calcStepSize(xzRV);

        float targetYaw = this.isLocked ? this.lockedYaw : mc.thePlayer.rotationYaw;
        Vec3 targetDir = new Vec3(
                -Math.sin(Math.toRadians(targetYaw)),
                0,
                Math.cos(Math.toRadians(targetYaw))
        );

        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbor = blockPosition.offset(side);
            if (!BlockUtil.isValidBock(neighbor)) continue;
            Vec3 dirVec = new Vec3(side.getDirectionVec());
            for (double xSearch = 0.5 - xzRV / 2.0; xSearch <= 0.5 + xzRV / 2.0; xSearch += xzSSV) {
                for (double ySearch = 0.5 - yRV / 2.0; ySearch <= 0.5 + yRV / 2.0; ySearch += ySSV) {
                    for (double zSearch = 0.5 - xzRV / 2.0; zSearch <= 0.5 + xzRV / 2.0; zSearch += xzSSV) {
                        Vec3 posVec = new Vec3(blockPosition).addVector(xSearch, ySearch, zSearch);
                        double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
                        Vec3 hitVec = posVec.add(new Vec3(dirVec.xCoord * 0.5, dirVec.yCoord * 0.5, dirVec.zCoord * 0.5));
                        if (checks && (eyesPos.squareDistanceTo(hitVec) > 18.0 || distanceSqPosVec > eyesPos.squareDistanceTo(posVec.add(dirVec)) || Scaffold.mc.theWorld.rayTraceBlocks(eyesPos, hitVec, false, true, false) != null)) continue;
                        double diffX = hitVec.xCoord - eyesPos.xCoord;
                        double diffY = hitVec.yCoord - eyesPos.yCoord;
                        double diffZ = hitVec.zCoord - eyesPos.zCoord;
                        double diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
                        Rotation rotation = new Rotation(MathHelper.wrapAngleTo180_float((float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f), MathHelper.wrapAngleTo180_float((float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)))));

                        Vec3 rotVec = new Vec3(RotationUtil.getVectorForRotation(rotation.toVec2f()).xCoord, RotationUtil.getVectorForRotation(rotation.toVec2f()).yCoord, RotationUtil.getVectorForRotation(rotation.toVec2f()).zCoord);
                        double angleDiff = 0;
                        if (this.isLocked) {
                            angleDiff = Math.acos(MathHelper.clamp_double(targetDir.dotProduct(new Vec3(rotVec.xCoord, 0, rotVec.zCoord).normalize()), -1, 1));
                        }

                        Vec3 vector = eyesPos.addVector(rotVec.xCoord * 4.0, rotVec.yCoord * 4.0, rotVec.zCoord * 4.0);
                        MovingObjectPosition obj = Scaffold.mc.theWorld.rayTraceBlocks(eyesPos, vector, false, false, true);
                        if (obj.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !obj.getBlockPos().equals(neighbor)) continue;

                        double score = placeRotation != null ? TYClient.rotationManager.getRotationDifference(rotation) : 0;
                        double newScore = angleDiff * 0.5 + TYClient.rotationManager.getRotationDifference(rotation) * 0.5;
                        if (placeRotation == null || newScore < score) {
                            placeRotation = new PlaceRotation(new PlaceInfo(neighbor, side.getOpposite(), hitVec), rotation);
                        }
                    }
                }
            }
        }
        if (placeRotation == null) {
            return false;
        }
        this.data = placeRotation.getPlaceInfo().getBlockPos();
        this.enumFacing = placeRotation.getPlaceInfo().getEnumFacing();
        return true;
    }

    private EnumFacing getPlaceSide(BlockPos blockPos) {
        Vec3 vec3;
        BlockPos bp;
        ArrayList<Vec3> positions = new ArrayList<>();
        HashMap<Vec3, EnumFacing> hashMap = new HashMap<>();
        BlockPos playerPos = new BlockPos(Scaffold.mc.thePlayer.posX, Scaffold.mc.thePlayer.posY, Scaffold.mc.thePlayer.posZ);
        if (BlockUtil.isAirBlock(blockPos.add(0, 1, 0)) && !blockPos.add(0, 1, 0).equals(playerPos) && !Scaffold.mc.thePlayer.onGround) {
            bp = blockPos.add(0, 1, 0);
            vec3 = this.getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.UP);
        }
        if (BlockUtil.isAirBlock(blockPos.add(1, 0, 0)) && !blockPos.add(1, 0, 0).equals(playerPos)) {
            bp = blockPos.add(1, 0, 0);
            vec3 = this.getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.EAST);
        }
        if (BlockUtil.isAirBlock(blockPos.add(-1, 0, 0)) && !blockPos.add(-1, 0, 0).equals(playerPos)) {
            bp = blockPos.add(-1, 0, 0);
            vec3 = this.getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.WEST);
        }
        if (BlockUtil.isAirBlock(blockPos.add(0, 0, 1)) && !blockPos.add(0, 0, 1).equals(playerPos)) {
            bp = blockPos.add(0, 0, 1);
            vec3 = this.getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.SOUTH);
        }
        if (BlockUtil.isAirBlock(blockPos.add(0, 0, -1)) && !blockPos.add(0, 0, -1).equals(playerPos)) {
            bp = blockPos.add(0, 0, -1);
            vec3 = this.getBestHitFeet(bp);
            positions.add(vec3);
            hashMap.put(vec3, EnumFacing.NORTH);
        }
        positions.sort(Comparator.comparingDouble(vec3x -> Scaffold.mc.thePlayer.getDistance(vec3x.xCoord, vec3x.yCoord, vec3x.zCoord)));
        if (!positions.isEmpty()) {
            vec3 = this.getBestHitFeet(this.data);
            if (Scaffold.mc.thePlayer.getDistance(vec3.xCoord, vec3.yCoord, vec3.zCoord) >= Scaffold.mc.thePlayer.getDistance(positions.get(0).xCoord, positions.get(0).yCoord, positions.get(0).zCoord)) {
                return hashMap.get(positions.get(0));
            }
        }
        return null;
    }

    private Vec3 getBestHitFeet(BlockPos blockPos) {
        Block block = Scaffold.mc.theWorld.getBlockState(blockPos).getBlock();
        double ex = MathHelper.clamp_double(Scaffold.mc.thePlayer.posX, blockPos.getX(), (double)blockPos.getX() + block.getBlockBoundsMaxX());
        double ey = MathHelper.clamp_double(keepYValue.getValue() ? Scaffold.getYLevel() : Scaffold.mc.thePlayer.posY, blockPos.getY(), (double)blockPos.getY() + block.getBlockBoundsMaxY());
        double ez = MathHelper.clamp_double(Scaffold.mc.thePlayer.posZ, blockPos.getZ(), (double)blockPos.getZ() + block.getBlockBoundsMaxZ());
        return new Vec3(ex, ey, ez);
    }

    private BlockPos getBlockPos() {
        BlockPos playerPos = new BlockPos(Scaffold.mc.thePlayer.posX, Scaffold.getYLevel(), Scaffold.mc.thePlayer.posZ);
        ArrayList<Vec3> positions = new ArrayList<>();
        HashMap<Vec3, BlockPos> hashMap = new HashMap<>();
        for (int x2 = playerPos.getX() - 5; x2 <= playerPos.getX() + 5; ++x2) {
            for (int y2 = playerPos.getY() - 1; y2 <= playerPos.getY(); ++y2) {
                for (int z = playerPos.getZ() - 5; z <= playerPos.getZ() + 5; ++z) {
                    if (!BlockUtil.isValidBock(new BlockPos(x2, y2, z))) continue;
                    BlockPos blockPos = new BlockPos(x2, y2, z);
                    Block block = Scaffold.mc.theWorld.getBlockState(blockPos).getBlock();
                    double ex = MathHelper.clamp_double(Scaffold.mc.thePlayer.posX, blockPos.getX(), (double)blockPos.getX() + block.getBlockBoundsMaxX());
                    double ey = MathHelper.clamp_double(keepYValue.getValue() ? Scaffold.getYLevel() : Scaffold.mc.thePlayer.posY, blockPos.getY(), (double)blockPos.getY() + block.getBlockBoundsMaxY());
                    double ez = MathHelper.clamp_double(Scaffold.mc.thePlayer.posZ, blockPos.getZ(), (double)blockPos.getZ() + block.getBlockBoundsMaxZ());
                    Vec3 vec3 = new Vec3(ex, ey, ez);
                    positions.add(vec3);
                    hashMap.put(vec3, blockPos);
                }
            }
        }
        if (!positions.isEmpty()) {
            positions.sort(Comparator.comparingDouble(this::getBestBlock));
            return hashMap.get(positions.get(0));
        }
        return null;
    }

    private double getBestBlock(Vec3 vec3) {
        return Scaffold.mc.thePlayer.getDistanceSq(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    public int getBlockSlot() {
        for (int i = 0; i < 9; ++i) {
            if (!Scaffold.mc.thePlayer.inventoryContainer.getSlot(i + 36).getHasStack() || !(Scaffold.mc.thePlayer.inventoryContainer.getSlot(i + 36).getStack().getItem() instanceof ItemBlock)) continue;
            return i;
        }
        return -1;
    }

    public int getBlockCount() {
        int n = 0;
        for (int i = 36; i < 45; ++i) {
            if (!Scaffold.mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) continue;
            ItemStack stack = Scaffold.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            Item item = stack.getItem();
            if (!(stack.getItem() instanceof ItemBlock) || !this.isValid(item)) continue;
            n += stack.stackSize;
        }
        return n;
    }

    public boolean isValid(Item item) {
        return item instanceof ItemBlock && !invalidBlocks.contains(((ItemBlock)item).getBlock());
    }

    private float getYaw() {
        if (Scaffold.mc.gameSettings.keyBindBack.isKeyDown()) {
            return Scaffold.mc.thePlayer.rotationYaw;
        }
        if (Scaffold.mc.gameSettings.keyBindLeft.isKeyDown()) {
            return Scaffold.mc.thePlayer.rotationYaw + 90.0f;
        }
        if (Scaffold.mc.gameSettings.keyBindRight.isKeyDown()) {
            return Scaffold.mc.thePlayer.rotationYaw - 90.0f;
        }
        return Scaffold.mc.thePlayer.rotationYaw - 180.0f;
    }

    private void getBlock(int switchSlot) {
        for (int i = 9; i < 45; ++i) {
            ItemBlock block;
            ItemStack is;
            if (!Scaffold.mc.thePlayer.inventoryContainer.getSlot(i).getHasStack() || Scaffold.mc.currentScreen != null && !(Scaffold.mc.currentScreen instanceof GuiInventory) || !((is = Scaffold.mc.thePlayer.inventoryContainer.getSlot(i).getStack()).getItem() instanceof ItemBlock) || !this.isValid(is.getItem())) continue;
            if (36 + switchSlot == i) break;
            InventoryUtils.swap(i, switchSlot);
            break;
        }
    }

    int getBestSpoofSlot() {
        int spoofSlot = 5;
        for (int i = 36; i < 45; ++i) {
            if (Scaffold.mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) continue;
            spoofSlot = i - 36;
            break;
        }
        return spoofSlot;
    }

    @EventTarget
    public void onRender2DEvent(EventRender2D event) {
        if (mc.thePlayer != null) {
            renderCounter();
        }
    }

    private final Animation anim = new DecelerateAnimation(250, 1);

    public void renderCounter() {
        anim.setDirection(isEnable() ? Direction.FORWARDS : Direction.BACKWARDS);
        if (!isEnable() && anim.isDone()) return;
        int slot = getBlockSlot();
        ItemStack heldItem = slot == -1 ? null : mc.thePlayer.inventory.mainInventory[slot];
        int count = slot == -1 ? 0 :getBlockCount();
        String countStr = String.valueOf(count);
        ScaledResolution sr = new ScaledResolution(mc);
        RapeMasterFontManager tenacityFont18 = FontManager.tenacityBoldFont18;
        int color;
        float x, y;
        String str = countStr + " block" + (count != 1 ? "s" : "");
        float output = (float) anim.getOutput();
        switch (countMode.get()) {
            case "Tenacity":
                float blockWH = heldItem != null ? 15 : -2;
                int spacing = 3;
                String text = "§l" + countStr + "§r block" + (count != 1 ? "s" : "");
                float textWidth = tenacityFont18.getStringWidth(text);

                float totalWidth = ((textWidth + blockWH + spacing) + 6) * output;
                x = sr.getScaledWidth() / 2f - (totalWidth / 2f);
                y = sr.getScaledHeight() - (sr.getScaledHeight() / 2f - 20);
                float height = 20;
                RenderUtil.scissorStart(x - 1.5, y - 1.5, totalWidth + 3, height + 3);

                RoundedUtils.drawRound(x, y, totalWidth, height, 5, ColorUtil.tripleColor(20, .45f));

                tenacityFont18.drawString(text, x + 3 + blockWH + spacing, y + tenacityFont18.getMiddleOfBox(height) + .5f, -1);

                if (heldItem != null) {
                    RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, (int) x + 3, (int) (y + 10 - (blockWH / 2)));
                    RenderHelper.disableStandardItemLighting();
                }
                RenderUtil.scissorEnd();
                break;
            case "Basic":
                x = sr.getScaledWidth() / 2F - tenacityFont18.getStringWidth(str) / 2F + 1;
                y = sr.getScaledHeight() / 2F + 10;
                RenderUtil.scaleStart(sr.getScaledWidth() / 2.0F, y + tenacityFont18.FONT_HEIGHT / 2.0F, output);
                tenacityFont18.drawStringWithShadow(str, x, y, -1);
                RenderUtil.scaleEnd();
                break;
            case "Polar":
                color = count < 24 ? 0xFFFF5555 : count < 128 ? 0xFFFFFF55 : 0xFF55FF55;
                x = sr.getScaledWidth() / 2F - tenacityFont18.getStringWidth(countStr) / 2F + (heldItem != null ? 6 : 1);
                y = sr.getScaledHeight() / 2F + 10;

                GlStateManager.pushMatrix();
                RenderUtil.fixBlendIssues();
                GL11.glTranslatef(x + (heldItem == null ? 1 : 0), y, 1);
                GL11.glScaled((float)anim.getOutput(), (float)anim.getOutput(), 1);
                GL11.glTranslatef(-x - (heldItem == null ? 1 : 0), -y, 1);

                tenacityFont18.drawOutlinedString(countStr, x, y, ColorUtil.applyOpacity(color, output), true);

                if (heldItem != null) {
                    double scale = 0.7;
                    GlStateManager.color(1, 1, 1, 1);
                    GlStateManager.scale(scale, scale, scale);
                    RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(
                            heldItem,
                            (int) ((sr.getScaledWidth() / 2F - tenacityFont18.getStringWidth(countStr) / 2F - 7) / scale),
                            (int) ((sr.getScaledHeight() / 2F + 8.5F) / scale)
                    );
                    RenderHelper.disableStandardItemLighting();
                }
                GlStateManager.popMatrix();
                break;
        }
    }
}