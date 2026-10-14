package cn.tianyu.client.util.impl.player;

import cn.tianyu.client.util.impl.client.KeyBindUtil;
import lombok.experimental.UtilityClass;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import org.apache.commons.lang3.StringUtils;

import static cn.tianyu.client.TYClient.mc;
import static cn.tianyu.client.module.Module.isHypixel;

/**
 * @Author: CN_tianyu
 */

@UtilityClass
public class PlayerUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean isInLobby() {
        if (mc.theWorld == null) {
            return false;
        }
        boolean compass = false;
        for (int i = 0; i < 9; i++) {
            final ItemStack stackInSlot = mc.thePlayer.inventory.getStackInSlot(i);

            if (stackInSlot != null && stackInSlot.getUnlocalizedName().toLowerCase().contains("compass")) {
                compass = true;
            }
        }
        return compass;
    }

    public static boolean isHoldingGodAxe(EntityPlayer player) {
        ItemStack holdingItem = player.getEquipmentInSlot(0);
        return isGodAxe(holdingItem);
    }

    public static boolean isGodAxe(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (stack.getItem() != Items.golden_axe) {
            return false;
        }
        int durability = stack.getMaxDamage() - stack.getItemDamage();
        if (durability > 2) {
            return false;
        }
        NBTTagList enchantmentTagList = stack.getEnchantmentTagList();
        if (enchantmentTagList == null) {
            return false;
        }
        for (int i = 0; i < enchantmentTagList.tagCount(); ++i) {
            int level;
            NBTTagCompound nbt = (NBTTagCompound)enchantmentTagList.get(i);
            if (!nbt.hasKey("id") || !nbt.hasKey("lvl") || nbt.getInteger("id") != 16 || (level = nbt.getInteger("lvl")) < 666) continue;
            return true;
        }
        return false;
    }

    public static boolean isKBBall(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (stack.getItem() != Items.slime_ball) {
            return false;
        }
        NBTTagList enchantmentTagList = stack.getEnchantmentTagList();
        if (enchantmentTagList == null) {
            return false;
        }
        for (int i = 0; i < enchantmentTagList.tagCount(); ++i) {
            int level;
            NBTTagCompound nbt = (NBTTagCompound)enchantmentTagList.get(i);
            if (!nbt.hasKey("id") || !nbt.hasKey("lvl") || nbt.getInteger("id") != 19 || (level = nbt.getInteger("lvl")) < 2) continue;
            return true;
        }
        return false;
    }

    public static boolean isFireEnchantBall(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (stack.getItem() != Items.magma_cream) {
            return false;
        }
        NBTTagList enchantmentTagList = stack.getEnchantmentTagList();
        if (enchantmentTagList == null) {
            return false;
        }
        for (int i = 0; i < enchantmentTagList.tagCount(); ++i) {
            int level;
            NBTTagCompound nbt = (NBTTagCompound)enchantmentTagList.get(i);
            if (!nbt.hasKey("id") || !nbt.hasKey("lvl") || nbt.getInteger("id") != 20 || (level = nbt.getInteger("lvl")) < 1) continue;
            return true;
        }
        return false;
    }

    public static boolean isHoldingEnchantedGoldenApple(EntityPlayer player) {
        ItemStack holdingItem = player.getEquipmentInSlot(0);
        if (holdingItem == null) {
            return false;
        }
        if (holdingItem.getItem() != Items.golden_apple) {
            return false;
        }
        return holdingItem.hasEffect();
    }

    public static int hasEatenGoldenApple(EntityPlayer player) {
        PotionEffect regenPotion = player.getActivePotionEffect(Potion.regeneration);
        if (regenPotion == null) {
            return -1;
        }
        if (regenPotion.getAmplifier() < 4) {
            return -1;
        }
        return regenPotion.getDuration();
    }

    public static int isRegen(EntityPlayer player) {
        PotionEffect regenPotion = player.getActivePotionEffect(Potion.regeneration);
        if (regenPotion == null) {
            return -1;
        }
        return regenPotion.getDuration();
    }

    public static int getSpeedPotion() {
        return (mc.thePlayer.isPotionActive(Potion.moveSpeed) ? mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 : 0);
    }
    public Block block(final double x, final double y, final double z) {
        return mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }
    public boolean colorTeam(EntityPlayer sb) {
        String targetName = StringUtils.replace(sb.getDisplayName().getFormattedText(),"§r", "");
        String clientName = mc.thePlayer.getDisplayName().getFormattedText().replace("§r", "");
        return targetName.startsWith("§" + clientName.charAt(1));
    }

    public boolean armorTeam(EntityPlayer entityPlayer) {
        if (mc.thePlayer.inventory.armorInventory[3] != null && entityPlayer.inventory.armorInventory[3] != null) {
            ItemStack myHead = mc.thePlayer.inventory.armorInventory[3];
            ItemArmor myItemArmor = (ItemArmor) myHead.getItem();
            ItemStack entityHead = entityPlayer.inventory.armorInventory[3];
            ItemArmor entityItemArmor = (ItemArmor) entityHead.getItem();
            if (String.valueOf(entityItemArmor.getColor(entityHead)).equals("10511680")) {
                return true;
            }
            return myItemArmor.getColor(myHead) == entityItemArmor.getColor(entityHead);
        }
        return false;
    }

    public static boolean isBlockUnder() {
        return isBlockUnder(mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
    }

    public boolean isBlockUnder(final double height) {
        return isBlockUnder(height, true);
    }

    public boolean isBlockUnder(final double height, final boolean boundingBox) {
        if (boundingBox) {
            for (int offset = 0; offset < height; offset += 2) {
                final AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0, -offset, 0);

                if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                    return true;
                }
            }
        } else {
            for (int offset = 0; offset < height; offset++) {
                if (PlayerUtil.blockRelativeToPlayer(0, -offset, 0).isFullBlock()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int findSoup() {
        for (int i = 36; i < 45; i++) {
            final ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (itemStack != null && itemStack.getItem().equals(Items.mushroom_stew) && itemStack.stackSize > 0 && itemStack.getItem() instanceof ItemFood) {
                return i;
            }
        }

        return -1;
    }

    public static boolean hasSpaceHotBar() {
        for (int i = 36; i < 45; i++) {
            final ItemStack itemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (itemStack == null)
                return true;
        }
        return false;
    }

    public static int findItem(final int startSlot, final int endSlot, final Item item) {
        for (int i = startSlot; i < endSlot; i++) {
            final ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();

            if (stack != null && stack.getItem() == item)
                return i;
        }
        return -1;
    }

    public int findItem(final Item item) {
        for (int i = 0; i < 9; i++) {
            final ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

            if (itemStack == null) {
                if (item == null) {
                    return i;
                }
                continue;
            }

            if (itemStack.getItem() == item) {
                return i;
            }
        }

        return -1;
    }

    public Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).add(offsetX, offsetY, offsetZ)).getBlock();
    }

    public boolean scoreTeam(EntityPlayer entityPlayer) {
        return mc.thePlayer.isOnSameTeam(entityPlayer);
    }

    public static boolean isShop(EntityLivingBase entity) {
        if (entity == mc.thePlayer) {
            return false;
        }
        EntityLivingBase armorStand = mc.theWorld.findNearestEntityWithinAABB(EntityArmorStand.class, entity.getEntityBoundingBox(), entity);
        if (armorStand == null) return false;
        String displayName = armorStand.getName();
        if (displayName.contains("RIGHT CLICK")) return true;
        if (displayName.contains("ITEM SHOP")) return true;
        if (displayName.contains("UPGRADES")) return true;
        if (displayName.contains("BANKER")) return true;
        return displayName.contains("STREAK POWERS");
    }

    public static boolean isFriend(EntityPlayer player) {
        return false;
    }

    public static boolean isTarget(EntityPlayer player) {
        return false;
    }

    public static boolean isBot(EntityPlayer player) {
        if (player == mc.thePlayer) {
            return false;
        }
        NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(player.getName());
        if (playerInfo == null) {
            return true;
        }
        if (!isHypixel()) return false;
        if (player.getName().startsWith("§k")) {
            return player.isInvisible();
        }
        if (playerInfo.getResponseTime() < 1) {
            return true;
        }
        ScorePlayerTeam playerTeam = playerInfo.getPlayerTeam();
        if (playerTeam == null) return false;
        if (!playerTeam.getTeamName().isEmpty()) return false;
        return playerTeam.getColorPrefix().equals("§c");
    }

    public static boolean isAirAbove() {
        AxisAlignedBB axisAlignedBB = PlayerUtil.mc.thePlayer.getEntityBoundingBox().offset(0.0, 1.0, 0.0);
        return !PlayerUtil.mc.theWorld.getCollidingBoundingBoxes(PlayerUtil.mc.thePlayer, axisAlignedBB).isEmpty();
    }

    public static boolean isAirBelow() {
        AxisAlignedBB axisAlignedBB = PlayerUtil.mc.thePlayer.getEntityBoundingBox().offset(0.0, -1.0, 0.0);
        return !PlayerUtil.mc.theWorld.getCollidingBoundingBoxes(PlayerUtil.mc.thePlayer, axisAlignedBB).isEmpty();
    }

    public static boolean isUsingItem() {
        return mc.currentScreen == null && KeyBindUtil.isKeyDown(mc.gameSettings.keyBindUseItem.getKeyCode());
    }

    public static boolean canMove(double x, double z, double y) {
        AxisAlignedBB boundingBox = PlayerUtil.mc.thePlayer.getEntityBoundingBox().offset(x, y, z);
        return PlayerUtil.mc.theWorld.getCollidingBoundingBoxes(PlayerUtil.mc.thePlayer, boundingBox).isEmpty();
    }
}
