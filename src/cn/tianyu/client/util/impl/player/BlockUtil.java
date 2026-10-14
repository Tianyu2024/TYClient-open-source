package cn.tianyu.client.util.impl.player;

import cn.tianyu.client.util.impl.client.RandomUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.util.*;

import java.util.HashMap;
import java.util.Map;

import static cn.tianyu.client.TYClient.mc;

/**
 * @Author: CN_tianyu
 */
public class BlockUtil {

    public static boolean isReplaceable(BlockPos blockPos) {
        return BlockUtil.isReplaceable(mc.theWorld.getBlockState(blockPos).getBlock());
    }
    public static boolean isReplaceable(Block block) {
        if (!block.getMaterial().isReplaceable()) return false;
        if (!(block instanceof BlockSnow)) return true;
        return !(block.getBlockBoundsMaxY() > 0.125);
    }

    public static boolean isAirBlock(final BlockPos blockPos) {
        final Block block = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();
        return block instanceof BlockAir;
    }

    public static boolean isValidBock(final BlockPos blockPos) {
        final Block block = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();
        return !(block instanceof BlockLiquid) && !(block instanceof BlockAir) && !(block instanceof BlockChest) && !(block instanceof BlockFurnace) && !(block instanceof BlockLadder) && !(block instanceof BlockTNT);
    }
    public static BlockPos getBlockCorner(BlockPos start, BlockPos end) {
        for (int x = 0; x <= 1; ++x) {
            for (int y = 0; y <= 1; ++y) {
                for (int z = 0; z <= 1; ++z) {
                    BlockPos pos = new BlockPos(end.getX() + x, end.getY() + y, end.getZ() + z);
                    if (!isBlockBetween(start, pos)) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }

    public static boolean isBlockBetween(BlockPos start, BlockPos end) {
        int startX = start.getX();
        int startY = start.getY();
        int startZ = start.getZ();
        int endX = end.getX();
        int endY = end.getY();
        int endZ = end.getZ();
        double diffX = (double) (endX - startX);
        double diffY = (double) (endY - startY);
        double diffZ = (double) (endZ - startZ);
        double x = (double) startX;
        double y = (double) startY;
        double z = (double) startZ;
        double STEP = 0.1D;
        int STEPS = (int) Math.max(Math.abs(diffX), Math.max(Math.abs(diffY), Math.abs(diffZ))) * 4;

        for (int i = 0; i < STEPS - 1; ++i) {
            x += diffX / (double) STEPS;
            y += diffY / (double) STEPS;
            z += diffZ / (double) STEPS;
            if (x != (double) endX || y != (double) endY || z != (double) endZ) {
                BlockPos pos = new BlockPos(x, y, z);
                Block block = mc.theWorld.getBlockState(pos).getBlock();
                if (block.getMaterial() != Material.air && block.getMaterial() != Material.water && !(block instanceof BlockVine) && !(block instanceof BlockLadder)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Block getBlock(final BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos).getBlock();
    }

    public static boolean isInteractable(BlockPos blockPos) {
        return BlockUtil.isInteractable(mc.theWorld.getBlockState(blockPos).getBlock());
    }

    public static boolean isInteractable(Block block) {
        if (block instanceof BlockContainer) return true;
        if (block instanceof BlockWorkbench) return true;
        if (block instanceof BlockAnvil) return true;
        if (block instanceof BlockBed) return true;
        if (block instanceof BlockDoor) {
            if (block.getMaterial() != Material.iron) return true;
        }
        if (block instanceof BlockTrapDoor) return true;
        if (block instanceof BlockFenceGate) return true;
        if (block instanceof BlockFence) return true;
        if (block instanceof BlockButton) return true;
        if (block instanceof BlockLever) return true;
        return block instanceof BlockJukebox;
    }

    public static Map<BlockPos, Block> searchBlocks(final int radius) {
        final Map<BlockPos, Block> blocks = new HashMap<BlockPos, Block>();
        for (int x = radius; x > -radius; --x) {
            for (int y = radius; y > -radius; --y) {
                for (int z = radius; z > -radius; --z) {
                    final BlockPos blockPos = new BlockPos(mc.thePlayer.lastTickPosX + x, mc.thePlayer.lastTickPosY + y, mc.thePlayer.lastTickPosZ + z);
                    final Block block = getBlock(blockPos);
                    blocks.put(blockPos, block);
                }
            }
        }
        return blocks;
    }

    public static boolean replaceable(BlockPos blockPos) {
        if (!nullCheck()) {
            return true;
        }
        return getBlock(blockPos).isReplaceable(mc.theWorld, blockPos);
    }
    public static boolean nullCheck() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    public static boolean isFluid(Block block) {
        return block.getMaterial() == Material.lava || block.getMaterial() == Material.water;
    }

    public static AxisAlignedBB getOffsetBoundingBox(AxisAlignedBB aabb, Block.EnumOffsetType offsetType, BlockPos pos) {
        int i = pos.getX();
        int j = pos.getZ();
        long k = (i * 3129871L) ^ (long) j * 116129781L;
        k = k * k * 42317861L + k * 11L;
        double d0 = ((double) ((float) (k >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D;
        double d1 = ((double) ((float) (k >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D;
        double d2 = 0.0D;

        if (offsetType == Block.EnumOffsetType.XYZ) {
            d2 = ((double) ((float) (k >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D;
        }

        return aabb.offset(d0, d2, d1);
    }

    public static boolean isSolid(Block block) {
        if (block instanceof BlockStairs) return false;
        if (block instanceof BlockSlab) return false;
        if (block instanceof BlockEndPortalFrame) return false;
        if (block instanceof BlockEndPortal) return false;
        if (block instanceof BlockVine) return false;
        if (block instanceof BlockPumpkin) return false;
        if (block instanceof BlockCactus) return false;
        if (block instanceof BlockBush) return false;
        if (block instanceof BlockFalling) return false;
        if (block instanceof BlockWeb) return false;
        if (block instanceof BlockPane) return false;
        if (block instanceof BlockCarpet) return false;
        if (block instanceof BlockSnow) return false;
        if (block instanceof BlockFence) return false;
        if (block instanceof BlockFenceGate) return false;
        if (block instanceof BlockWall) return false;
        if (block instanceof BlockLadder) return false;
        if (block instanceof BlockTorch) return false;
        if (block instanceof BlockRedstoneWire) return false;
        if (block instanceof BlockRedstoneDiode) return false;
        if (block instanceof BlockBasePressurePlate) return false;
        if (block instanceof BlockTripWire) return false;
        if (block instanceof BlockTripWireHook) return false;
        if (block instanceof BlockRailBase) return false;
        if (block instanceof BlockSlime) return false;
        return !(block instanceof BlockTNT);
    }

    public static Vec3 getHitVec(BlockPos blockPos, EnumFacing enumFacing, float yaw, float pitch) {
        MovingObjectPosition movingObjectPosition = RotationUtil.rayTrace(yaw, pitch, mc.playerController.getBlockReachDistance(), 1.0f);
        if (movingObjectPosition != null) {
            if (movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                if (movingObjectPosition.getBlockPos().equals(blockPos)) {
                    if (movingObjectPosition.sideHit == enumFacing) {
                        return movingObjectPosition.hitVec;
                    }
                }
            }
        }
        return BlockUtil.getClickVec(blockPos, enumFacing);
    }

    public static Vec3 getClickVec(BlockPos blockPos, EnumFacing enumFacing) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        Vec3 vec3 = new Vec3((double) blockPos.getX() + Math.min(Math.max(RandomUtils.nextDouble(0.0, 1.0), block.getBlockBoundsMinX()), block.getBlockBoundsMaxX()), (double) blockPos.getY() + Math.min(Math.max(RandomUtils.nextDouble(0.0, 1.0), block.getBlockBoundsMinY()), block.getBlockBoundsMaxY()), (double) blockPos.getZ() + Math.min(Math.max(RandomUtils.nextDouble(0.0, 1.0), block.getBlockBoundsMinZ()), block.getBlockBoundsMaxZ()));
        switch (enumFacing) {
            default: {
                return new Vec3(vec3.xCoord, (double) blockPos.getY() + block.getBlockBoundsMinY(), vec3.zCoord);
            }
            case UP: {
                return new Vec3(vec3.xCoord, (double) blockPos.getY() + block.getBlockBoundsMaxY(), vec3.zCoord);
            }
            case NORTH: {
                return new Vec3(vec3.xCoord, vec3.yCoord, (double) blockPos.getZ() + block.getBlockBoundsMinZ());
            }
            case EAST: {
                return new Vec3((double) blockPos.getX() + block.getBlockBoundsMaxX(), vec3.yCoord, vec3.zCoord);
            }
            case SOUTH: {
                return new Vec3(vec3.xCoord, vec3.yCoord, (double) blockPos.getZ() + block.getBlockBoundsMaxZ());
            }
            case WEST:
        }
        return new Vec3((double) blockPos.getX() + block.getBlockBoundsMinX(), vec3.yCoord, vec3.zCoord);
    }
}
