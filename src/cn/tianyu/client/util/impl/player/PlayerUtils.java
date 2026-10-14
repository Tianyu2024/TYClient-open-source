package cn.tianyu.client.util.impl.player;


import cn.tianyu.client.TYClient;
import cn.tianyu.client.module.impl.combat.KillAura;
import cn.tianyu.client.module.impl.misc.AntiBot;
import cn.tianyu.client.module.impl.misc.Teams;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.util.HashMap;
import java.util.Map;

import static cn.tianyu.client.util.impl.player.RotationUtil.mc;

/**
 * @Author: CN_tianyu
 */
public class PlayerUtils {
    private static final Int2IntMap GOOD_POTIONS = new Int2IntOpenHashMap() {{
        put(6, 1); // Instant Health
        put(10, 2); // Regeneration
        put(11, 3); // Resistance
        put(21, 4); // Health Boost
        put(22, 5); // Absorption
        put(23, 6); // Saturation
        put(5, 7); // Strength
        put(1, 8); // Speed
        put(12, 9); // Fire Resistance
        put(14, 10); // Invisibility
        put(3, 11); // Haste
        put(13, 12); // Water Breathing
    }};

    public static boolean goodPotion(final int id) {
        return GOOD_POTIONS.containsKey(id);
    }

    public static Map<BlockPos, Block> searchBlocks(final int radius) {
        final Map<BlockPos, Block> blocks = new HashMap<>();
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

    public static Block getBlock(BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos).getBlock();
    }

    public static EntityPlayer getTarget(double distance) {
        EntityPlayer target = null;
        if (mc.theWorld == null) {
            return null;
        }

        KillAura aura = TYClient.getModuleManager().getModule(KillAura.class);

        if (aura.isEnabled() && aura.target instanceof EntityPlayer) {
            return (EntityPlayer) aura.target;
        }

        for (EntityPlayer entity : mc.theWorld.playerEntities) {
            if (Teams.isSameTeam(entity))
                continue;

            if (TYClient.instance.getModuleManager().getModule(AntiBot.class).isEnabled() && TYClient.instance.getModuleManager().getModule(AntiBot.class).isBot(entity))
                continue;

            float tempDistance = mc.thePlayer.getDistanceToEntity(entity);
            if (entity != mc.thePlayer && tempDistance <= distance) {
                target = entity;
                distance = tempDistance;
            }
        }
        return target;
    }


    public static double distanceToGround(Entity entity, int x, int z) {
        if (entity.onGround) {
            return 0;
        }
        double fallDistance = -1;
        double y = entity.posY;
        if (entity.posY % 1 == 0) {
            y--;
        }
        for (int i = (int) Math.floor(y); i > -1; i--) {
            if (!isPlaceable(new BlockPos(x, i, z))) {
                fallDistance = y - i;
                break;
            }
        }
        return fallDistance - 1;
    }
    public static boolean isPlaceable(BlockPos blockPos) {
        return BlockUtil.replaceable(blockPos) || BlockUtil.isFluid(BlockUtil.getBlock(blockPos));
    }

    public static int findTool(final BlockPos blockPos) {
        float bestSpeed = 1;
        int bestSlot = -1;

        final IBlockState blockState = mc.theWorld.getBlockState(blockPos);

        for (int i = 0; i < 9; i++) {
            final ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

            if (itemStack != null) {

                final float speed = itemStack.getStrVsBlock(blockState.getBlock());

                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }
        }

        return bestSlot;
    }
}
