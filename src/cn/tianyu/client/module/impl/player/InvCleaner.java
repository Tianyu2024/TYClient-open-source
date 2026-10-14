package cn.tianyu.client.module.impl.player;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.combat.AutoProjectile;
import cn.tianyu.client.module.impl.combat.AutoRod;
import cn.tianyu.client.module.impl.combat.KillAura;
import cn.tianyu.client.module.impl.movement.Scaffold;
import cn.tianyu.client.util.InventoryUtils;
import cn.tianyu.client.util.impl.player.TimerUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S2DPacketOpenWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static cn.tianyu.client.util.InventoryUtils.windowClick;

/**
 * @Author: CN_tianyu
 */

public class InvCleaner extends Module {
    public InvCleaner() {
        super("InvCleaner", "InvCleaner", Category.player);
    }

    private final BooleanValue postValue = new BooleanValue("Post", false);
    private final BooleanValue smart = new BooleanValue("Smart", true);
    private final BooleanValue putTrashInChest = new BooleanValue("Put the trash in the bin", false);

    private final ModeValue mode = new ModeValue("Mode", "Spoof", "Spoof", "OpenInv");
    private final BooleanValue autodis = new BooleanValue("AutoDisable", true);
    private final NumberValue delay = new NumberValue("Delay", 5, 0, 300, 10);
    private final NumberValue armorDelay = new NumberValue("Armor Delay", 20, 0, 300, 10);

    public final NumberValue slotWeapon = new NumberValue("Weapon Slot", 1, 1, 9, 1);
    public final NumberValue slotPick = new NumberValue("Pickaxe Slot", 2, 1, 9, 1);
    public final NumberValue slotRod = new NumberValue("Rod Slot", 3, 1, 9, 1);
    public final NumberValue slotAxe = new NumberValue("Axe Slot", 9, 1, 9, 1);
    public final NumberValue slotGapple = new NumberValue("AutoGapple Slot", 4, 1, 9, 1);
    public final NumberValue slotShovel = new NumberValue("Shovel Slot", 5, 1, 9, 1);
    public final NumberValue slotBow = new NumberValue("Bow Slot", 6, 1, 9, 1);
    public final NumberValue slotBlock = new NumberValue("Block Slot", 7, 1, 9, 1);
    public final NumberValue slotPearl = new NumberValue("Pearl Slot", 8, 1, 9, 1);

    public final String[] serverItems = {"选择游戏", "加入游戏", "职业选择菜单", "离开对局", "再来一局", "selector", "tracking compass", "(right click)", "tienda ", "perfil", "salir", "shop", "collectibles", "game", "profil", "lobby", "show all", "hub", "friends only", "cofre", "(click", "teleport", "play", "exit", "hide all", "jeux", "gadget", " (activ", "emote", "amis", "bountique", "choisir", "choose "};

    private final int[] bestArmorPieces = new int[4];
    private final List<Integer> trash = new ArrayList<>();
    private final int[] bestToolSlots = new int[3];
    private final List<Integer> gappleStackSlots = new ArrayList<>();
    private int bestSwordSlot;
    private int bestPearlSlot;
    private int bestRodSlot;
    private int bestBowSlot;
    private boolean serverOpen;
    private boolean clientOpen;
    private int ticksSinceLastClick;
    private boolean nextTickCloseInventory;
    private TimerUtil timer = new TimerUtil();
    private boolean waitingForChest = false;

    @EventTarget
    private void onPacket(EventPacket event) {
        final Packet<?> packet = event.getPacket();
        if (packet instanceof S2DPacketOpenWindow) {
            this.clientOpen = false;
            this.serverOpen = false;
        }
    }

    @EventTarget
    private void onPacketSend(EventPacket event) {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C16PacketClientStatus) {
            final C16PacketClientStatus clientStatus = (C16PacketClientStatus) packet;

            if (clientStatus.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                this.clientOpen = true;
                this.serverOpen = true;
            }
        } else if (packet instanceof C0DPacketCloseWindow) {
            final C0DPacketCloseWindow packetCloseWindow = (C0DPacketCloseWindow) packet;

            if (packetCloseWindow.windowId == mc.thePlayer.inventoryContainer.windowId) {
                this.clientOpen = false;
                this.serverOpen = false;
            }
        } else if (packet instanceof C0EPacketClickWindow && !mc.thePlayer.isUsingItem()) {
            this.ticksSinceLastClick = 0;
        }
    }

    private boolean dropItem(final List<Integer> listOfSlots) {
        if (!listOfSlots.isEmpty()) {
            int slot = listOfSlots.remove(0);
            if (putTrashInChest.getValue() && mc.currentScreen instanceof GuiChest) {
                int chestSlot = findEmptyChestSlot();
                if (chestSlot != -1) {
                    windowClick(mc, slot, chestSlot, InventoryUtils.ClickType.SHIFT_CLICK);
                    return true;
                }
            }
            if (!putTrashInChest.getValue()) {
                windowClick(mc, slot, 1, InventoryUtils.ClickType.DROP_ITEM);
                return true;
            }
            return false;
        }
        return false;
    }

    private int findEmptyChestSlot() {
        if (!(mc.currentScreen instanceof GuiChest)) return -1;
        GuiChest chest = (GuiChest) mc.currentScreen;
        int containerSlots = chest.inventorySlots.inventorySlots.size();
        for (int i = 0; i < containerSlots; i++) {
            Slot slot = chest.inventorySlots.getSlot(i);
            if (slot != null && !slot.getHasStack()) {
                return i;
            }
        }
        return -1;
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.thePlayer.ticksExisted < 10 && autodis.getValue()) {
            this.setEnable(false);
        }
    }

    @EventTarget
    private void onMotion(EventMotion event) {
        if ((postValue.getValue() && event.isPost()) || (!postValue.getValue() && event.isPre())) {
            if (!mc.thePlayer.isOnLadder() && !(getModule(Blink.class).getState()) && !mc.thePlayer.isUsingItem() && (mc.currentScreen == null || mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof GuiInventory || !mc.thePlayer.isSpectator() || mc.currentScreen instanceof GuiIngameMenu) && KillAura.target == null && (!getModule(Scaffold.class).getState())) {

                this.ticksSinceLastClick++;

                if (this.ticksSinceLastClick < Math.floor(this.delay.getValue().doubleValue() / 50)) return;

                if (this.clientOpen || (mc.currentScreen == null && !this.mode.getValue().equals("OpenInv"))) {
                    this.clear();

                    for (int slot = InventoryUtils.INCLUDE_ARMOR_BEGIN; slot < InventoryUtils.END; slot++) {
                        final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();

                        if (stack != null) {
                            if (stack.getItem() instanceof ItemSword && InventoryUtils.isBestSword(mc.thePlayer, stack)) {
                                this.bestSwordSlot = slot;
                            } else if (stack.getItem() instanceof ItemTool && InventoryUtils.isBestTool(mc.thePlayer, stack)) {
                                final int toolType = InventoryUtils.getToolType(stack);
                                if (toolType != -1 && slot != this.bestToolSlots[toolType])
                                    this.bestToolSlots[toolType] = slot;
                            } else if (stack.getItem() instanceof ItemArmor && InventoryUtils.isBestArmor(mc.thePlayer, stack)) {
                                final ItemArmor armor = (ItemArmor) stack.getItem();
                                final int pieceSlot = this.bestArmorPieces[armor.armorType];
                                if (pieceSlot == -1 || slot != pieceSlot)
                                    this.bestArmorPieces[armor.armorType] = slot;
                            } else if (stack.getItem() instanceof ItemBow && InventoryUtils.isBestBow(mc.thePlayer, stack)) {
                                if (slot != this.bestBowSlot)
                                    this.bestBowSlot = slot;
                            } else if (stack.getItem() instanceof ItemAppleGold) {
                                this.gappleStackSlots.add(slot);
                            } else if (stack.getItem() instanceof ItemEnderPearl) {
                                this.bestPearlSlot = slot;
                            } else if (stack.getItem() instanceof ItemFishingRod && getModule(AutoRod.class).getState() && InventoryUtils.isBestRod(mc.thePlayer, stack)) {
                                if (bestRodSlot == -1 || slot != bestRodSlot)
                                    bestRodSlot = slot;
                            } else if (!this.trash.contains(slot) && !isValidStack(stack)) {
                                boolean allowThrow = true;
                                if (getModule(AutoProjectile.class).getState() && (stack.getItem() instanceof ItemSnowball || stack.getItem() instanceof ItemEgg)) {
                                    allowThrow = false;
                                    if (smart.get() && (mc.thePlayer.inventory.getFirstEmptyStack() == -1) && (!gappleStackSlots.isEmpty() || bestPearlSlot != -1)) {
                                        allowThrow = true;
                                    }
                                }
                                if (getModule(AutoRod.class).getState() && stack.getItem() instanceof ItemFishingRod) {
                                    allowThrow = false;
                                }
                                if (Arrays.stream(serverItems).anyMatch(stack.getDisplayName()::contains)) continue;
                                if (stack.getItem() instanceof ItemSkull) continue;
                                if (allowThrow)
                                    this.trash.add(slot);
                            }
                        }
                    }

                    final boolean busy = (!this.trash.isEmpty()) || this.equipArmor(false) || this.sortItems(false);

                    if (!busy) {
                        if (this.nextTickCloseInventory) {
                            if (mode.is("Spoof")) this.close();
                            this.nextTickCloseInventory = false;
                        } else {
                            this.nextTickCloseInventory = true;
                        }
                        return;
                    } else {
                        boolean waitUntilNextTick = !this.serverOpen;

                        if (mode.is("Spoof")) this.open();

                        if (this.nextTickCloseInventory)
                            this.nextTickCloseInventory = false;

                        if (waitUntilNextTick) return;
                    }

                    if (timer.hasTimeElapsed(this.armorDelay.getValue().floatValue()) && this.equipArmor(true))
                        return;
                    if (this.dropItem(this.trash)) return;
                    this.sortItems(true);
                }
            }
        }
    }

    private boolean sortItems(final boolean moveItems) {
        int goodSwordSlot = this.slotWeapon.getValue().intValue() + 35;
        if (this.bestSwordSlot != -1) {
            if (this.bestSwordSlot != goodSwordSlot) {
                if (moveItems) {
                    this.putItemInSlot(goodSwordSlot, this.bestSwordSlot);
                    this.bestSwordSlot = goodSwordSlot;
                }
                return true;
            }
        }

        int goodBowSlot = this.slotBow.getValue().intValue() + 35;
        if (this.bestBowSlot != -1) {
            if (this.bestBowSlot != goodBowSlot) {
                if (moveItems) {
                    this.putItemInSlot(goodBowSlot, this.bestBowSlot);
                    this.bestBowSlot = goodBowSlot;
                }
                return true;
            }
        }

        int goodGappleSlot = this.slotGapple.getValue().intValue() + 35;
        if (!this.gappleStackSlots.isEmpty()) {
            this.gappleStackSlots.sort(Comparator.comparingInt(slot -> mc.thePlayer.inventoryContainer.getSlot(slot).getStack().stackSize));
            final int bestGappleSlot = this.gappleStackSlots.get(0);
            if (bestGappleSlot != goodGappleSlot) {
                if (moveItems) {
                    this.putItemInSlot(goodGappleSlot, bestGappleSlot);
                    this.gappleStackSlots.set(0, goodGappleSlot);
                }
                return true;
            }
        }

        final int[] toolSlots = {
                slotPick.getValue().intValue() + 35,
                slotAxe.getValue().intValue() + 35,
                slotShovel.getValue().intValue() + 35
        };

        for (final int toolSlot : this.bestToolSlots) {
            if (toolSlot != -1) {
                final int type = InventoryUtils.getToolType(mc.thePlayer.inventoryContainer.getSlot(toolSlot).getStack());
                if (type != -1) {
                    if (toolSlot != toolSlots[type]) {
                        if (moveItems) {
                            this.putToolsInSlot(type, toolSlots);
                        }
                        return true;
                    }
                }
            }
        }

        int goodBlockSlot = this.slotBlock.getValue().intValue() + 35;
        int mostBlocksSlot = getMostBlocks();
        if (mostBlocksSlot != -1 && mostBlocksSlot != goodBlockSlot) {
            Slot dss = mc.thePlayer.inventoryContainer.getSlot(goodBlockSlot);
            ItemStack dsis = dss.getStack();
            if (!(dsis != null && dsis.getItem() instanceof ItemBlock && dsis.stackSize >= mc.thePlayer.inventoryContainer.getSlot(mostBlocksSlot).getStack().stackSize && Arrays.stream(serverItems).noneMatch(dsis.getDisplayName().toLowerCase()::contains))) {
                this.putItemInSlot(goodBlockSlot, mostBlocksSlot);
            }
        }

        int goodPearlSlot = this.slotPearl.getValue().intValue() + 35;
        if (this.bestPearlSlot != -1) {
            if (this.bestPearlSlot != goodPearlSlot) {
                if (moveItems) {
                    this.putItemInSlot(goodPearlSlot, this.bestPearlSlot);
                    this.bestPearlSlot = goodPearlSlot;
                }
                return true;
            }
        }
        return false;
    }

    public int getMostBlocks() {
        int stack = 0;
        int biggestSlot = -1;
        for (int i = 9; i < 45; i++) {
            Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
            ItemStack is = slot.getStack();
            if (is != null && is.getItem() instanceof ItemBlock && is.stackSize > stack && Arrays.stream(serverItems).noneMatch(is.getDisplayName().toLowerCase()::contains)) {
                stack = is.stackSize;
                biggestSlot = i;
            }
        }
        return biggestSlot;
    }

    private boolean equipArmor(boolean moveItems) {
        for (int i = 0; i < this.bestArmorPieces.length; i++) {
            final int piece = this.bestArmorPieces[i];
            if (piece != -1) {
                int armorPieceSlot = i + 5;
                final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(armorPieceSlot).getStack();
                if (stack != null) continue;
                if (moveItems) {
                    windowClick(mc, piece, 0, InventoryUtils.ClickType.SHIFT_CLICK);
                }
                timer.reset();
                return true;
            }
        }
        return false;
    }

    private void putItemInSlot(final int slot, final int slotIn) {
        windowClick(mc, slotIn, slot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
    }

    private void putToolsInSlot(final int tool, final int[] toolSlots) {
        final int toolSlot = toolSlots[tool];
        windowClick(mc, this.bestToolSlots[tool], toolSlot - 36, InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
        this.bestToolSlots[tool] = toolSlot;
    }

    private static boolean isValidStack(final ItemStack stack) {
        if (stack.getItem() instanceof ItemBlock && InventoryUtils.isStackValidToPlace(stack)) {
            return true;
        } else if (stack.getItem() instanceof ItemPotion && InventoryUtils.isBuffPotion(stack)) {
            return true;
        } else if (stack.getItem() instanceof ItemFood && InventoryUtils.isGoodFood(stack)) {
            return true;
        } else {
            return InventoryUtils.isGoodItem(stack);
        }
    }

    @Override
    public void onEnable() {
        this.ticksSinceLastClick = 0;
        this.clientOpen = mc.currentScreen instanceof GuiInventory;
        this.serverOpen = this.clientOpen;
    }

    @Override
    public void onDisable() {
        this.close();
        this.clear();
    }

    private void open() {
        if (!this.clientOpen && !this.serverOpen) {
            mc.thePlayer.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            this.serverOpen = true;
        }
    }

    private void close() {
        if (!this.clientOpen && this.serverOpen) {
            mc.thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
            this.serverOpen = false;
        }
    }

    private void clear() {
        this.trash.clear();
        this.bestBowSlot = -1;
        this.bestSwordSlot = -1;
        this.gappleStackSlots.clear();
        Arrays.fill(this.bestArmorPieces, -1);
        Arrays.fill(this.bestToolSlots, -1);
    }
}