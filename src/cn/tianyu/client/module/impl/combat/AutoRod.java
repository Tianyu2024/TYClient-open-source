package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @Author: CN_tianyu
 */
public class AutoRod extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Random random = new Random();

    public final NumberValue rodRange = new NumberValue("RodRange", 4.8, 2, 6.5, 0.1);
    public final NumberValue tickCool = new NumberValue("TickCool", 10, 3, 25, 1);
    public final BooleanValue autoSword = new BooleanValue("AutoSword", true);
    public final NumberValue swordRange = new NumberValue("SwordRange", 3.0, 2, 4.5, 0.1);

    private int coolTick;
    private int lastHotbarSlot = -1;
    private boolean hasHook = false;
    private EntityPlayer currentTarget = null;

    public AutoRod() {
        super("AutoRod", "Auto Rod", Category.combat);
    }

    @Override
    public void onEnable() {
        coolTick = 0;
        lastHotbarSlot = -1;
        hasHook = false;
        currentTarget = null;
    }

    @Override
    public void onDisable() {
        restoreOriginalItem();
        coolTick = 0;
        currentTarget = null;
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        if (coolTick > 0) {
            coolTick--;
            return;
        }

        double maxRange = rodRange.getValue();
        EntityPlayer target = findTarget(maxRange);
        currentTarget = target;

        if (target == null) {
            restoreOriginalItem();
            handleSwordMode();
            return;
        }

        boolean holdRod = mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFishingRod;

        if (!holdRod) {
            lastHotbarSlot = mc.thePlayer.inventory.currentItem;
            switchToRod();
            coolTick = (int) (tickCool.getValue() + random.nextInt(3));
            return;
        }

        hasHook = checkHasFishHook();

        if (!hasHook) {
            mc.rightClickMouse();
            coolTick = (int) (tickCool.getValue() + random.nextInt(5));
        } else {
            mc.rightClickMouse();
            coolTick = (int) (tickCool.getValue() + random.nextInt(3));
        }

        handleSwordMode();
    }

    private void handleSwordMode() {
        if (!autoSword.getValue()) return;

        if (currentTarget == null) {
            return;
        }

        double dist = mc.thePlayer.getDistanceToEntity(currentTarget);
        double swordRangeVal = swordRange.getValue();

        if (dist <= swordRangeVal) {
            boolean hasSword = false;
            int swordSlot = -1;

            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (stack != null && stack.getItem() instanceof net.minecraft.item.ItemSword) {
                    hasSword = true;
                    swordSlot = i;
                    break;
                }
            }

            if (hasSword && swordSlot != -1) {
                int currentSlot = mc.thePlayer.inventory.currentItem;
                if (currentSlot != swordSlot) {
                    mc.thePlayer.inventory.currentItem = swordSlot;
                }

                if (mc.thePlayer.getDistanceToEntity(currentTarget) <= swordRangeVal) {
                    mc.thePlayer.swingItem();
                    mc.playerController.attackEntity(mc.thePlayer, currentTarget);
                }

                if (lastHotbarSlot != -1 && mc.thePlayer.inventory.currentItem != lastHotbarSlot) {
                    int rodSlot = findRodSlot();
                    if (rodSlot != -1) {
                        mc.thePlayer.inventory.currentItem = rodSlot;
                    }
                }
            }
        } else {
            int rodSlot = findRodSlot();
            if (rodSlot != -1) {
                mc.thePlayer.inventory.currentItem = rodSlot;
            }
        }
    }

    private int findRodSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemFishingRod) {
                return i;
            }
        }
        return -1;
    }

    private EntityPlayer findTarget(double maxDist) {
        List<Entity> entityList = new ArrayList<>(mc.theWorld.loadedEntityList);
        EntityPlayer bestTarget = null;
        double minDist = Double.MAX_VALUE;

        double pX = mc.thePlayer.posX;
        double pY = mc.thePlayer.posY;
        double pZ = mc.thePlayer.posZ;

        for (Entity entity : entityList) {
            if (!(entity instanceof EntityPlayer)) continue;
            EntityPlayer enemy = (EntityPlayer) entity;
            if (enemy == mc.thePlayer || !enemy.isEntityAlive()) continue;
            if (enemy.getHealth() <= 0) continue;

            double dist = getDistance(pX, pY, pZ, enemy.posX, enemy.posY, enemy.posZ);
            if (dist < maxDist && dist < minDist) {
                minDist = dist;
                bestTarget = enemy;
            }
        }
        return bestTarget;
    }

    private double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private boolean checkHasFishHook() {
        List<Entity> snapshot = new ArrayList<>(mc.theWorld.loadedEntityList);
        for (Entity e : snapshot) {
            if (e instanceof EntityFishHook) {
                EntityFishHook hook = (EntityFishHook) e;
                if (hook.angler == mc.thePlayer) {
                    return true;
                }
            }
        }
        return false;
    }

    private void switchToRod() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemFishingRod) {
                mc.thePlayer.inventory.currentItem = i;
                break;
            }
        }
    }

    private void restoreOriginalItem() {
        if (lastHotbarSlot != -1 && mc.thePlayer.inventory.currentItem != lastHotbarSlot) {
            mc.thePlayer.inventory.currentItem = lastHotbarSlot;
            lastHotbarSlot = -1;
        }
    }
}