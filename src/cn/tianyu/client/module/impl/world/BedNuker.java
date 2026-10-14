package cn.tianyu.client.module.impl.world;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.player.EventTeleport;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.event.events.render.EventRender3D;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.client.ClickGUI;
import cn.tianyu.client.module.impl.movement.Scaffold;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.annotations.ContinualAnimation;
import cn.tianyu.client.util.impl.player.PlayerUtils;
import cn.tianyu.client.util.impl.player.RotationUtil;
import cn.tianyu.client.util.impl.render.RoundedUtils;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjglx.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BedNuker extends Module {
    public final NumberValue breakRange = new NumberValue("Break Range", 4, 1, 5, 1);
    public final BooleanValue breakSurroundings = new BooleanValue("Break Top", true);
    public final BooleanValue autoTool = new BooleanValue("Auto Tool", true);
    public final BooleanValue progressText = new BooleanValue("Progress Text", true);
    public final BooleanValue progressBar = new BooleanValue("Progress Bar", true);
    public final BooleanValue whitelistOwnBed = new BooleanValue("Whitelist Own Bed", true);
    public final BooleanValue swap = new BooleanValue("Swap", false);
    public final BooleanValue ignoreSlow = new BooleanValue("Ignore Slow", false, swap::get);
    public final BooleanValue groundSpoof = new BooleanValue("Hypixel Ground Spoof", false, swap::get);
    public final ModeValue bedMode = new ModeValue("Bed Mode", "Normal", "Normal", "Legit");

    public BlockPos bedPos;
    public boolean rotate = false;
    private float breakProgress;
    private int delayTicks;
    private Vec3 home;
    private boolean spoofed;
    public ContinualAnimation barAnim = new ContinualAnimation();

    // Legit 模式
    private BlockPos targetBedPos;
    private BlockPos currentMinePos;
    private List<BlockPos> blockPath;
    private int pathIndex;

    public BedNuker() {
        super("BedNucker", "版本", Category.world);
    }

    @Override
    public void onEnable() {
        rotate = false;
        bedPos = null;
        targetBedPos = null;
        currentMinePos = null;
        blockPath = null;
        pathIndex = 0;
        breakProgress = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        reset(true);
        super.onDisable();
    }

    @EventTarget
    public void onTeleport(EventTeleport event) {
        if (whitelistOwnBed.get()) {
            final double distance = mc.thePlayer.getDistance(event.getPosX(), event.getPosY(), event.getPosZ());
            if (distance > 40) {
                home = new Vec3(event.getPosX(), event.getPosY(), event.getPosZ());
            }
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        setSuffix(bedMode.getValue());

        if (TYClient.instance.getModuleManager().getModule(Scaffold.class).isEnabled() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock) {
            reset(true);
            return;
        }

        if (bedMode.getValue().equals("Legit")) {
            updateLegit();
        } else {
            updateNormal();
        }
    }

    private void updateNormal() {
        getBedPos();
        if (bedPos != null) {
            if (rotate) {
                float[] rot = RotationUtil.getRotations(bedPos);
                TYClient.rotationManager.setRotation(new Vector2f(rot[0], rot[1]), 180, true, false);
                rotate = false;
            }
            mine(bedPos);
        } else {
            reset(true);
        }
    }

    private void updateLegit() {
        if (targetBedPos == null || mc.theWorld.getBlockState(targetBedPos).getBlock() instanceof BlockAir) {
            targetBedPos = findBestBed();
            if (targetBedPos == null) {
                reset(true);
                return;
            }
            // 计算路径
            blockPath = calculateBlockPath(targetBedPos);
            pathIndex = 0;
        }

        if (blockPath != null && pathIndex < blockPath.size()) {
            // 先挖路径上的方块
            currentMinePos = blockPath.get(pathIndex);
            if (!(mc.theWorld.getBlockState(currentMinePos).getBlock() instanceof BlockAir)) {
                if (rotate) {
                    float[] rot = RotationUtil.getRotations(currentMinePos);
                    TYClient.rotationManager.setRotation(new Vector2f(rot[0], rot[1]), 180, true, false);
                    rotate = false;
                }
                mine(currentMinePos);
                return;
            } else {
                pathIndex++;
                return;
            }
        }

        // 路径挖完，挖床
        if (targetBedPos != null && !(mc.theWorld.getBlockState(targetBedPos).getBlock() instanceof BlockAir)) {
            if (rotate) {
                float[] rot = RotationUtil.getRotations(targetBedPos);
                TYClient.rotationManager.setRotation(new Vector2f(rot[0], rot[1]), 180, true, false);
                rotate = false;
            }
            mine(targetBedPos);
        } else {
            reset(true);
        }
    }

    private List<BlockPos> calculateBlockPath(BlockPos bed) {
        List<BlockPos> path = new ArrayList<>();
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);

        // 检查床周围是否有遮挡
        BlockPos[] checkPositions = {
                bed.add(0, 1, 0),
                bed.add(1, 0, 0),
                bed.add(-1, 0, 0),
                bed.add(0, 0, 1),
                bed.add(0, 0, -1)
        };

        for (BlockPos pos : checkPositions) {
            Block block = mc.theWorld.getBlockState(pos).getBlock();
            if (!(block instanceof BlockAir) && !(block instanceof BlockBed) && block.getMaterial() != MaterialLiquid.water) {
                // 检查这个方块是否可以被看到（没有遮挡）
                if (!canSeeBlock(pos)) {
                    // 需要挖开
                    path.add(pos);
                } else {
                    // 可以直接看到，挖这个
                    path.add(pos);
                }
            }
        }

        // 按距离排序，先挖近的
        path.sort(Comparator.comparingDouble(
                pos -> mc.thePlayer.getDistance(pos.getX(), pos.getY(), pos.getZ())
        ));

        return path;
    }

    private boolean canSeeBlock(BlockPos pos) {
        // 检查是否可以直接看到这个方块
        Vec3 eyePos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        Vec3 targetPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        return mc.theWorld.rayTraceBlocks(eyePos, targetPos, false) == null;
    }

    private BlockPos findBestBed() {
        List<BlockPos> bedList = new ArrayList<>();
        double range = breakRange.getValue();

        for (double x = mc.thePlayer.posX - range; x <= mc.thePlayer.posX + range; x++) {
            for (double y = mc.thePlayer.posY + mc.thePlayer.getEyeHeight() - range; y <= mc.thePlayer.posY + mc.thePlayer.getEyeHeight() + range; y++) {
                for (double z = mc.thePlayer.posZ - range; z <= mc.thePlayer.posZ + range; z++) {
                    BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();

                    if (block instanceof BlockBed && mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD) {
                        if (home != null && mc.thePlayer.getDistanceSq(home.xCoord, home.yCoord, home.zCoord) < 35 * 35 && whitelistOwnBed.get()) {
                            continue;
                        }
                        bedList.add(pos);
                    }
                }
            }
        }

        if (bedList.isEmpty()) {
            return null;
        }

        bedList.sort(Comparator.comparingDouble(
                pos -> mc.thePlayer.getDistance(pos.getX(), pos.getY(), pos.getZ())
        ));

        return bedList.get(0);
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (event.isPost()) return;

        if (bedPos != null && groundSpoof.canDisplay() && groundSpoof.get() && !mc.thePlayer.onGround) {
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                mc.timer.timerSpeed = 0.5f;
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                spoofed = true;
            } else {
                mc.timer.timerSpeed = 1f;
                spoofed = false;
            }
        } else if (spoofed) {
            mc.timer.timerSpeed = 1f;
            spoofed = false;
        }
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (progressText.get()) {
            BlockPos renderPos = bedPos != null ? bedPos : (currentMinePos != null ? currentMinePos : targetBedPos);
            if (renderPos != null && breakProgress > 0) {
                renderProgressText(renderPos);
            }
        }
    }

    private void renderProgressText(BlockPos pos) {
        RenderUtil.renderBlock(pos, ClickGUI.maincolor.getColorInt(), true, true);

        final double n = pos.getX() + 0.5 - mc.getRenderManager().viewerPosX;
        final double n2 = pos.getY() + 0.5 - mc.getRenderManager().viewerPosY;
        final double n3 = pos.getZ() + 0.5 - mc.getRenderManager().viewerPosZ;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) n, (float) n2, (float) n3);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-0.02266667f, -0.02266667f, -0.02266667f);
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        String progressStr = (int) (100.0 * (this.breakProgress / 1.0)) + "%";
        mc.fontRendererObj.drawString(progressStr, (float) (-mc.fontRendererObj.getStringWidth(progressStr) / 2), -3.0f, -1, true);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (progressBar.get() && breakProgress > 0) {
            renderProgressBar(event);
        }
    }

    private void renderProgressBar(EventRender2D event) {
        final ScaledResolution resolution = event.getScaledResolution();
        final int x = resolution.getScaledWidth() / 2;
        final int y = resolution.getScaledHeight() - 70;
        final float thickness = 6;
        final int width = resolution.getScaledWidth() / 4;
        final int half = width / 2;
        barAnim.animate(width * (breakProgress), 40);

        RoundedUtils.drawRound(x - half, y, width, thickness, thickness / 2, new Color(new Color(1, 1, 1).getRGB(), true));
        RoundedUtils.drawGradientHorizontal(x - half, y, barAnim.getOutput(), thickness, thickness / 2, ClickGUI.maincolor.getValue(), TYClient.instance.getcolor2(90, 100));

        String progressStr = (int) (100.0 * (this.breakProgress / 1.0)) + "%";
        FontManager.arial12.drawCenteredStringWithShadow(progressStr, x, y + 1, -1);
    }

    private void getBedPos() {
        if (home != null && mc.thePlayer.getDistanceSq(home.xCoord, home.yCoord, home.zCoord) < 35 * 35 && whitelistOwnBed.get()) {
            return;
        }
        bedPos = null;
        double range = breakRange.getValue();
        for (double x = mc.thePlayer.posX - range; x <= mc.thePlayer.posX + range; x++) {
            for (double y = mc.thePlayer.posY + mc.thePlayer.getEyeHeight() - range; y <= mc.thePlayer.posY + mc.thePlayer.getEyeHeight() + range; y++) {
                for (double z = mc.thePlayer.posZ - range; z <= mc.thePlayer.posZ + range; z++) {
                    BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                    if (mc.theWorld.getBlockState(pos).getBlock() instanceof BlockBed && mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD) {
                        if (breakSurroundings.get() && isBedCovered(pos)) {
                            bedPos = pos.add(0, 1, 0);
                        } else {
                            bedPos = pos;
                        }
                        break;
                    }
                }
            }
        }
    }

    private void mine(BlockPos blockPos) {
        if (delayTicks > 0) {
            delayTicks--;
            return;
        }

        IBlockState blockState = mc.theWorld.getBlockState(blockPos);

        if (blockState.getBlock() instanceof BlockAir) {
            if (bedMode.getValue().equals("Legit") && blockPos.equals(currentMinePos)) {
                pathIndex++;
            }
            return;
        }

        if (breakProgress == 0) {
            rotate = true;
            if (autoTool.get() && !swap.get()) {
                doAutoTool(blockPos);
            }
            mc.thePlayer.swingItem();
            sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.UP));
        } else if (breakProgress >= 1) {
            rotate = true;
            if (autoTool.get() && swap.get()) {
                doAutoTool(blockPos);
            }
            mc.thePlayer.swingItem();
            sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.UP));

            // 如果是路径方块，移到下一个
            if (bedMode.getValue().equals("Legit") && blockPos.equals(currentMinePos)) {
                pathIndex++;
                breakProgress = 0;
            } else {
                reset(false);
            }
            return;
        } else {
            if (!swap.get()) {
                rotate = true;
            }
            if (autoTool.get()) {
                if (!swap.get()) {
                    doAutoTool(blockPos);
                }
            }
            mc.thePlayer.swingItem();
        }

        if (swap.get()) {
            breakProgress += (getBlockHardness(blockPos, PlayerUtils.findTool(blockPos) != -1 ? mc.thePlayer.inventory.getStackInSlot(PlayerUtils.findTool(blockPos)) : mc.thePlayer.getHeldItem(), ignoreSlow.get(), groundSpoof.get()));
        } else {
            breakProgress += mc.theWorld.getBlockState(blockPos).getBlock().getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, blockPos);
        }

        mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), blockPos, (int) (breakProgress * 10));
    }

    private void reset(boolean resetRotate) {
        if (bedPos != null) {
            mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), bedPos, -1);
        }
        if (targetBedPos != null) {
            mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), targetBedPos, -1);
        }
        if (currentMinePos != null) {
            mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), currentMinePos, -1);
        }

        breakProgress = 0;
        delayTicks = 5;
        bedPos = null;
        targetBedPos = null;
        currentMinePos = null;
        blockPath = null;
        pathIndex = 0;
        rotate = !resetRotate;
    }

    private void doAutoTool(BlockPos pos) {
        if (PlayerUtils.findTool(pos) != -1) {
            mc.thePlayer.inventory.currentItem = PlayerUtils.findTool(pos);
        }
    }

    private boolean isBedCovered(BlockPos headBlockBedPos) {
        BlockPos headBlockBedPosOffSet1 = headBlockBedPos.add(1, 0, 0);
        BlockPos headBlockBedPosOffSet2 = headBlockBedPos.add(-1, 0, 0);
        BlockPos headBlockBedPosOffSet3 = headBlockBedPos.add(0, 0, 1);
        BlockPos headBlockBedPosOffSet4 = headBlockBedPos.add(0, 0, -1);

        if (!isBlockCovered(headBlockBedPos)) {
            return false;
        } else if (mc.theWorld.getBlockState(headBlockBedPosOffSet1).getBlock() instanceof BlockBed && mc.theWorld.getBlockState(headBlockBedPosOffSet1).getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
            return isBlockCovered(headBlockBedPosOffSet1);
        } else if (mc.theWorld.getBlockState(headBlockBedPosOffSet2).getBlock() instanceof BlockBed && mc.theWorld.getBlockState(headBlockBedPosOffSet2).getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
            return isBlockCovered(headBlockBedPosOffSet2);
        } else if (mc.theWorld.getBlockState(headBlockBedPosOffSet3).getBlock() instanceof BlockBed && mc.theWorld.getBlockState(headBlockBedPosOffSet3).getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
            return isBlockCovered(headBlockBedPosOffSet3);
        } else if (mc.theWorld.getBlockState(headBlockBedPosOffSet4).getBlock() instanceof BlockBed && mc.theWorld.getBlockState(headBlockBedPosOffSet4).getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
            return isBlockCovered(headBlockBedPosOffSet4);
        }
        return false;
    }

    private boolean isBlockCovered(BlockPos blockPos) {
        BlockPos[] directions = {
                blockPos.add(0, 1, 0),
                blockPos.add(1, 0, 0),
                blockPos.add(-1, 0, 0),
                blockPos.add(0, 0, 1),
                blockPos.add(0, 0, -1)
        };

        for (BlockPos pos : directions) {
            Block block = mc.theWorld.getBlockState(pos).getBlock();
            if (block instanceof BlockAir || block.getMaterial() instanceof MaterialLiquid) {
                return false;
            }
        }
        return true;
    }

    public static float getBlockHardness(final BlockPos blockPos, final ItemStack itemStack, boolean ignoreSlow, boolean ignoreGround) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        final float getBlockHardness = block.getBlockHardness(mc.theWorld, null);
        if (getBlockHardness < 0.0f) {
            return 0.0f;
        }
        return (block.getMaterial().isToolNotRequired() || (itemStack != null && itemStack.canHarvestBlock(block))) ? (getToolDigEfficiency(itemStack, block, ignoreSlow, ignoreGround) / getBlockHardness / 30.0f) : (getToolDigEfficiency(itemStack, block, ignoreSlow, ignoreGround) / getBlockHardness / 100.0f);
    }

    public static float getToolDigEfficiency(ItemStack itemStack, Block block, boolean ignoreSlow, boolean ignoreGround) {
        float n = (itemStack == null) ? 1.0f : itemStack.getItem().getStrVsBlock(itemStack, block);
        if (n > 1.0f) {
            final int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
            if (getEnchantmentLevel > 0 && itemStack != null) {
                n += getEnchantmentLevel * getEnchantmentLevel + 1;
            }
        }
        if (mc.thePlayer.isPotionActive(Potion.digSpeed)) {
            n *= 1.0f + (mc.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2f;
        }
        if (!ignoreSlow) {
            if (mc.thePlayer.isPotionActive(Potion.digSlowdown)) {
                float n2;
                switch (mc.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
                    case 0: {
                        n2 = 0.3f;
                        break;
                    }
                    case 1: {
                        n2 = 0.09f;
                        break;
                    }
                    case 2: {
                        n2 = 0.0027f;
                        break;
                    }
                    default: {
                        n2 = 8.1E-4f;
                        break;
                    }
                }
                n *= n2;
            }
            if (mc.thePlayer.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(mc.thePlayer)) {
                n /= 5.0f;
            }
            if (!mc.thePlayer.onGround && !ignoreGround) {
                n /= 5.0f;
            }
        }
        return n;
    }
}