package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.event.events.render.EventRender3D;
import cn.tianyu.client.event.events.world.EventWorldLoad;
import cn.tianyu.client.font.FontManager;
import cn.tianyu.client.gui.notification.NotificationManager;
import cn.tianyu.client.gui.notification.NotificationType;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import org.lwjglx.util.vector.Vector2f;

import java.awt.*;

/**
 * @Author: CN_tianyu
 */
public class LegitScaffold extends Module {
    public BooleanValue telly = new BooleanValue("Telly", true);
    public BooleanValue antiBlock = new BooleanValue("AntiBlock", false);
    public BooleanValue FakeItem = new BooleanValue("FakeItem", false);
    public BooleanValue esp = new BooleanValue("BlockEsp", true);
    public BooleanValue sprint = new BooleanValue("Sprint", false);
    public BooleanValue BlocksCount = new BooleanValue("BlocksCount", true);
    private int firstItem;

    public LegitScaffold() {
        super("LegitScaffold", "LegitScaffold", Category.movement);
        //addValues(telly, antiBlock, FakeItem, esp, sprint, BlocksCount);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (!BlocksCount.get()) {
            return;
        }
        ScaledResolution sr = new ScaledResolution(mc);
        float x = sr.getScaledWidth() / 2 + 10;
        float y = sr.getScaledHeight() / 2 - 25;
        String blockcount = String.valueOf(getBlocksCount());
        FontManager.arial22.drawString(blockcount + " Blocks left", x, y, new Color(255, 255, 255).getRGB());
    }

    public int getBlocksCount() {
        int result = 0;
        int i = 9;
        while (i < 45) {
            ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (isScaffoldBlock(stack)) {
                result += stack.stackSize;
            }
            ++i;
        }
        return result;
    }

    @EventTarget
    public void onLoadWorld(EventWorldLoad event) {
        toggle();
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (getBlockFromHotbar() == -1) {
            NotificationManager.post(NotificationType.WARNING, "Scaffold Module Close", "You Don't have any Blocks.");
            toggle();
            return;
        }

        if (mc.thePlayer.isSprinting() && !sprint.getValue()) {
            mc.thePlayer.setSprinting(false);
        }

        if (!FakeItem.getValue()) {
            mc.thePlayer.inventory.currentItem = getBlockFromHotbar();
        }

        // 正确 Telly 视角
        if (telly.get()) {
            float yaw = mc.thePlayer.rotationYaw + 180;
            TYClient.rotationManager.setRotation(new Vector2f(yaw, 80f), 180, false);
        } else {
            TYClient.rotationManager.setRotation(new Vector2f((float) (getMoveYaw(mc.thePlayer.rotationYaw) - 180.0), 80.0F), 180, false);
        }
    }

    public static float getMoveYaw(float yaw) {
        Vector2f from = new Vector2f((float) mc.thePlayer.lastTickPosX, (float) mc.thePlayer.lastTickPosZ),
                to = new Vector2f((float) mc.thePlayer.posX, (float) mc.thePlayer.posZ),
                diff = new Vector2f(to.x - from.x, to.y - from.y);

        double x = diff.x, z = diff.y;
        if (x != 0 && z != 0) {
            yaw = (float) Math.toDegrees((Math.atan2(-x, z) + MathHelper.PI2) % MathHelper.PI2);
        }
        return yaw;
    }

    public static Block getBlock(BlockPos pos) {
        return mc.theWorld.getBlockState(pos).getBlock();
    }

    public static Block getBlockUnderPlayer(final EntityPlayer player) {
        return getBlock(new BlockPos(player.posX, player.posY - 1.0, player.posZ));
    }

    @EventTarget
    public void onUpdate(final EventMotion event) {
        if (event.isPre()) {
            if (telly.get()) {
                handleTellyScaffold();
            } else {
                handleLegitScaffold();
            }

            // 防方块
            if (antiBlock.get()) {
                doAntiBlock();
            }
        }
    }

    private void handleLegitScaffold() {
        if (getBlockUnderPlayer(mc.thePlayer) instanceof BlockAir) {
            if (mc.thePlayer.onGround) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
            }
        } else if (mc.thePlayer.onGround) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        }
    }

    private void handleTellyScaffold() {
        if (mc.thePlayer.onGround && getBlockUnderPlayer(mc.thePlayer) instanceof BlockAir) {
            mc.thePlayer.jump();
        }
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
    }

    private void doAntiBlock() {
        BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + 0.5, mc.thePlayer.posZ);
        if (!(getBlock(pos) instanceof BlockAir)) {
            mc.thePlayer.jump();
        }

        BlockPos north = pos.north();
        BlockPos south = pos.south();
        BlockPos east = pos.east();
        BlockPos west = pos.west();

        if (!(getBlock(north) instanceof BlockAir)
                || !(getBlock(south) instanceof BlockAir)
                || !(getBlock(east) instanceof BlockAir)
                || !(getBlock(west) instanceof BlockAir)) {
            mc.thePlayer.motionY = 0.42f;
            mc.thePlayer.motionX *= 1.8;
            mc.thePlayer.motionZ *= 1.8;
        }
    }

    @EventTarget
    public void onRender(EventRender3D event) {
        if (!esp.get()) return;

        BlockPos pos = new BlockPos(mc.thePlayer).down();
        if (mc.thePlayer.getHorizontalFacing() == EnumFacing.NORTH) pos = pos.north();
        else if (mc.thePlayer.getHorizontalFacing() == EnumFacing.SOUTH) pos = pos.south();
        else if (mc.thePlayer.getHorizontalFacing() == EnumFacing.WEST) pos = pos.west();
        else if (mc.thePlayer.getHorizontalFacing() == EnumFacing.EAST) pos = pos.east();

        double x = pos.getX() - mc.getRenderManager().renderPosX;
        double y = pos.getY() - mc.getRenderManager().renderPosY;
        double z = pos.getZ() - mc.getRenderManager().renderPosZ;

        if (mc.theWorld.getBlockState(pos).getBlock().isReplaceable(mc.theWorld, pos)) {
            RenderUtil.drawEntityESP(x, y, z, x + 1, y + 1, z + 1, 1, 1, 1, 0.2F);
        }
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) {
            firstItem = mc.thePlayer.inventory.currentItem;
        }
        if (mc.thePlayer == null) return;

        if (mc.thePlayer.inventory.currentItem != getBlockFromHotbar() && getBlockFromHotbar() != -1) {
            PacketUtils.send(new C09PacketHeldItemChange(getBlockFromHotbar()));
        }
        mc.thePlayer.setSneaking(false);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            mc.thePlayer.inventory.currentItem = firstItem;
        }
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        super.onDisable();
    }

    private int getBlockFromHotbar() {
        int biggest = 0;
        int biggestSlot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack itemStack = this.mc.thePlayer.inventory.mainInventory[i];
            if (!isScaffoldBlock(itemStack)) continue;
            if (biggest < itemStack.stackSize) {
                biggest = itemStack.stackSize;
                biggestSlot = i;
            }
        }
        return biggestSlot;
    }

    private int getBlockFromInventory() {
        int biggest = 0;
        int biggestSlot = -1;
        for (int i = 9; i < 36; ++i) {
            final ItemStack itemStack = this.mc.thePlayer.inventory.mainInventory[i];
            if (!isScaffoldBlock(itemStack)) continue;
            if (biggest < itemStack.stackSize) {
                biggest = itemStack.stackSize;
                biggestSlot = i;
            }
        }
        return biggestSlot;
    }

    public static boolean isScaffoldBlock(ItemStack itemStack) {
        if (itemStack == null || itemStack.stackSize <= 0) return false;
        if (!(itemStack.getItem() instanceof ItemBlock)) return false;

        ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
        if (itemBlock.getBlock() == Blocks.glass) return true;
        return itemBlock.getBlock().isFullBlock();
    }

    private boolean hasBlock() {
        return findBlock(9, 36) != -1 || findBlock(36, 45) != -1;
    }

    private int findBlock(int startSlot, int endSlot) {
        int i = startSlot;
        while (i < endSlot) {
            ItemStack stack = this.mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem() instanceof ItemBlock
                    && ((ItemBlock) stack.getItem()).getBlock().isFullBlock()) {
                return i;
            }
            ++i;
        }
        return -1;
    }
}