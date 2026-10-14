package cn.tianyu.client.module.impl.player;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.player.InvUtil2;
import cn.tianyu.client.util.impl.render.RoundedUtils;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.*;
import net.minecraft.inventory.Container;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.util.BlockPos;
import org.lwjglx.opengl.Display;
import org.lwjglx.util.glu.GLU;

import java.awt.*;
import java.util.List;

/**
 * @author CN_tianyu
 */

public class ChestStealer extends Module {
    public final NumberValue delay = new NumberValue("Delay",100,0,500,10);
    public final BooleanValue silent = new BooleanValue("Silent", true);
    public final BooleanValue silentChestView = new BooleanValue("Silent View", true);
    private BlockPos currentContainerPos;

    public boolean isStealing;

    private final String[] list = new String[]{"mode", "delivery", "menu", "selector", "game", "gui", "server", "inventory", "play", "teleporter", //
            "shop", "melee", "armor", "block", "castle", "mini", "warp", "teleport", "user", "team", "tool", "sure", "trade", "cancel", "accept",  //
            "soul", "book", "recipe", "profile", "tele", "port", "map", "kit", "select", "lobby", "vault", "lock", "anticheat", "travel", "settings", //
            "user", "preference", "compass", "cake", "wars", "buy", "upgrade", "ranged", "potions", "utility"};


    public ChestStealer() {
        super("ChestStealer", "自动拿走箱子里的物品", Category.player);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        if (silentChestView.get()) {
            if (mc.thePlayer.openContainer == null || mc.currentScreen == null || !isStealing) return;
            Container container = mc.thePlayer.openContainer;
            int slots = container.inventorySlots.size();

            int scaleFactor = event.getScaledResolution().getScaleFactor();

            if (slots > 0) {
                float[] projection = calculate(currentContainerPos, scaleFactor);
                if (projection == null) return;

                float roundX = projection[0] - (164 / 2F);
                float roundY = projection[1] / 1.5F;

                GlStateManager.pushMatrix();
                GlStateManager.translate(roundX + 82, roundY + 30, 0);
                GlStateManager.translate(-(roundX + 82), -(roundY + 30), 0);

                RoundedUtils.drawRound(roundX, roundY, 164, 60, 3, new Color(0, 0, 0, 120));

                double startX = roundX + 5;
                double startY = roundY + 5;

                RenderItem itemRender = mc.getRenderItem();

                GlStateManager.pushMatrix();
                RenderHelper.enableGUIStandardItemLighting();
                itemRender.zLevel = 200.0F;

                for (Slot slot : container.inventorySlots) {
                    if (!slot.inventory.equals(mc.thePlayer.inventory)) {
                        int x = (int) (startX + (slot.slotNumber % 9) * 18);
                        int y = (int) (startY + ((double) slot.slotNumber / 9) * 18);

                        itemRender.renderItemAndEffectIntoGUI(slot.getStack(), x, y);
                    }
                }

                GlStateManager.popMatrix();

                itemRender.zLevel = 0.0F;
                GlStateManager.popMatrix();
                GlStateManager.disableLighting();
            }
        }
    }

    public float[] calculate(BlockPos blockPos, int factor) {
        try {
            float renderX = (float) mc.getRenderManager().renderPosX;
            float renderY = (float) mc.getRenderManager().renderPosY;
            float renderZ = (float) mc.getRenderManager().renderPosZ;

            float x = blockPos.getX() + 0.5f - renderX;
            float y = blockPos.getY() + 0.5f - renderY;
            float z = blockPos.getZ() + 0.5f - renderZ;

            float[] projectedCenter = project(x, y, z, factor);
            if (projectedCenter != null && projectedCenter[2] >= 0.0D && projectedCenter[2] < 1.0D) {
                return new float[]{projectedCenter[0],projectedCenter[1],projectedCenter[0],projectedCenter[1]};
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static float[] project(double x, double y, double z, int factor) {
        if (GLU.gluProject((float) x, (float) y, (float) z, ActiveRenderInfo.MODELVIEW, ActiveRenderInfo.PROJECTION, ActiveRenderInfo.VIEWPORT, ActiveRenderInfo.OBJECTCOORDS)) {
            return new float[]{(ActiveRenderInfo.OBJECTCOORDS.get(0) / factor), ((Display.getHeight() - ActiveRenderInfo.OBJECTCOORDS.get(1)) / factor), ActiveRenderInfo.OBJECTCOORDS.get(2)};
        }
        return null;
    }

    @Getter
    @Setter
    private long lastActionTime = 0;

    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        isStealing = false;
        super.onDisable();
    }


    @EventTarget
    public void onPacket(EventPacket event) {
        if (event.getPacket() instanceof S2DPacketOpenWindow) {
            S2DPacketOpenWindow packetOpenWindow = (S2DPacketOpenWindow) event.getPacket();
            String title = packetOpenWindow.getWindowTitle().getUnformattedText().toLowerCase();
            for (String blacklisted : list) {
                if (title.contains(blacklisted)) {
                    isStealing = false;
                    return;
                }
            }
            isStealing = (packetOpenWindow.getGuiId().equals("minecraft:chest") || packetOpenWindow.getGuiId().equals("minecraft:container") );
        }

        if (silentChestView.get()) {
            Packet<?> packet = event.getPacket();
            if (packet instanceof C08PacketPlayerBlockPlacement) {
                C08PacketPlayerBlockPlacement wrapper = (C08PacketPlayerBlockPlacement) packet;
                if (wrapper.getPosition() != null) {
                    Block block = mc.theWorld.getBlockState(wrapper.getPosition()).getBlock();
                    if (block instanceof BlockContainer) {
                        currentContainerPos = wrapper.getPosition();
                    }
                }
            }
        }
    }


    @Override
    public void onUpdate() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen instanceof GuiChest) {
            ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;
            GuiChest guiChest = (GuiChest) mc.currentScreen;

            if (System.currentTimeMillis() - lastActionTime < delay.get()) {
                return;
            }

            boolean hasChestItems = false;
            int chestSlotCount = container.getLowerChestInventory().getSizeInventory();
            for (int i = 0; i < chestSlotCount; i++) {
                ItemStack stack = container.getSlot(i).getStack();
                if (stack != null && stack.stackSize > 0) {
                    hasChestItems = true;
                    break;
                }
            }

            if (!hasChestItems) {
                mc.thePlayer.closeScreen();
                isStealing = false;
                lastActionTime = System.currentTimeMillis();
                return;
            }

            List<Slot> chestSlots = guiChest.inventorySlots.inventorySlots;

            for (int i = 0; i < chestSlots.size(); i++) {
                Slot slot = chestSlots.get(i);
                ItemStack stackInSlot = slot.getStack();

                if (stackInSlot != null && stackInSlot.stackSize > 0 && canTakeItem()) {
                    for (int j = 9; j < 36; j++) {
                        //ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
                        //RenderUtil.drawLoadingCircle((float) sr.getScaledWidth() / 2, (float)sr.getScaledHeight() / 2.0f + 25.0f);

                        Slot playerSlot = mc.thePlayer.inventoryContainer.getSlot(j);
                        if (playerSlot.getStack() == null || playerSlot.getStack().stackSize == 0) {
                            mc.playerController.windowClick(guiChest.inventorySlots.windowId, i, 0, 1, mc.thePlayer);
                            lastActionTime = System.currentTimeMillis();
                            return;
                        }
                    }
                }
            }

            if (InvUtil2.isInventoryFull()) {
                mc.thePlayer.closeScreen();
                isStealing = false;
            }
        }
    }

    private boolean canTakeItem() {
        List<Slot> playerSlots = mc.thePlayer.inventoryContainer.inventorySlots;

        for (Slot slot : playerSlots) {
            if (slot.getStack() == null || slot.getStack().stackSize <= 0) {
                return true;
            }
        }
        return false;
    }
}
