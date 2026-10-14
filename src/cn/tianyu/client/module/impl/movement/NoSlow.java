package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.Manager.PingSpoofManager;
import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventPriority;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.player.EventSlowDown;
import cn.tianyu.client.event.events.player.EventSprint;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.combat.KillAura;
import cn.tianyu.client.setting.BlinkComponent;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.Protocol1_19To1_18_2;
import com.viaversion.viaversion.protocols.protocol1_19to1_18_2.ServerboundPackets1_19;
import com.yumegod.obfuscation.Native;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

/**
 * @Author: CN_tianyu
 */

@Native
public class NoSlow extends Module {
    public NoSlow() {
        super("NoSlow","无减速", Category.movement);
    }
    public final ModeValue mode = new ModeValue("Mode","WatchdogPrediction",
            "WatchdogPrediction"
            //,"Blink"
    );
    public final BooleanValue sprint = new BooleanValue("Sprint", false);
    public final BooleanValue foodValue = new BooleanValue("Food", false);
    public final BooleanValue potionValue = new BooleanValue("Potion", false);
    public final BooleanValue swordValue = new BooleanValue("Sword", false);
    public final BooleanValue bowValue = new BooleanValue("Bow", false);
    boolean usingItem;

    @EventTarget
    public void onMotion(EventMotion event) {
        setSuffix(mode.getValue());

//        if (event.isPre()) {
//            if (mc.thePlayer.getCurrentEquippedItem() == null) return;
//
//            final Item item = mc.thePlayer.getCurrentEquippedItem().getItem();
//
//            if (mc.thePlayer.isUsingItem()) {
//                if (item instanceof ItemSword && swordValue.get()) {
//                    event.setY(event.getY() + 0.00000000646);
//                    BlinkComponent.blinking = true;
//                    if (mc.thePlayer.ticksExisted % 5 == 0) {
//                        PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
//                        BlinkComponent.dispatch();
//                        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
//                    }
//                } else if (item instanceof ItemFood && foodValue.get() || item instanceof ItemBow && bowValue.get()) {
//                    BlinkComponent.blinking = true;
//                }
//
//                usingItem = true;
//            } else if (usingItem) {
//                usingItem = false;
//
//                BlinkComponent.blinking = false;
//            }
//        }else if (event.isPost()){
//            if (mc.thePlayer.isUsingItem()) {
//                mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
//            }
//
//        }
    }

//    @EventTarget
//    public void onSlowDown(EventSlowDown event) {
//        event.setSprinting(sprint.get());
//
//        if (foodValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
//            event.setForward(1);
//            event.setStrafe(1);
//        }
//        if (potionValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) {
//            event.setForward(1);
//            event.setStrafe(1);
//        }
//        if (swordValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
//            event.setForward(1);
//            event.setStrafe(1);
//        }
//        if (bowValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
//            event.setForward(1);
//            event.setStrafe(1);
//        }
//    }

    boolean wasUsingItem;
    int usingTicks;
    public NumberValue maxPingSpoof = new NumberValue("Max Ping Spoof", 8, 0, 30, 1);
    public NumberValue whenToFinishEating = new NumberValue("When to finish eating", 30, 20, 36, 1);
    public final BooleanValue nonBlinkSpeedBypass = new BooleanValue("Non-Blink Speed Bypass", true);

    @EventTarget
    public void onPreEventMotion(EventMotion e) {
        if(!e.isPre()){
            return;
        }
        if (mc.thePlayer.getCurrentEquippedItem() != null) {
            Item item = mc.thePlayer.getCurrentEquippedItem().getItem();
            if (mc.thePlayer.isUsingItem()) {
                if ((!(item instanceof ItemSword) || !this.swordValue.get())
                        && (
                        item instanceof ItemFood && this.foodValue.getValue() && mc.thePlayer.isEating()
                                || item instanceof ItemBow && this.bowValue.getValue()
                                || mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion
                                && !ItemPotion.isSplash(mc.thePlayer.getHeldItem().getMetadata())
                                && this.potionValue.getValue()
                                && mc.thePlayer.isEating()
                )) {
                    this.usingTicks++;
                    if (this.usingTicks > this.maxPingSpoof.getValue().intValue()) {
                        //BlinkComponent.a(30000, true, false, false, false, true);
                    }
                }

                this.wasUsingItem = true;
            } else if (this.wasUsingItem) {
                this.usingTicks = 0;
                this.wasUsingItem = false;
                BlinkComponent.dispatch();
            }

            if (this.usingTicks > this.whenToFinishEating.getValue().intValue()) {
                mc.gameSettings.keyBindUseItem.setPressed(false);
            }
        }
    };
    @EventTarget
    public void onSprint(EventSprint e){
        if (mc.thePlayer.isUsingItem() && mc.thePlayer.moveForward > 0.0F && this.nonBlinkSpeedBypass.getValue() && this.usingTicks <= this.maxPingSpoof.getValue().intValue()) {
            mc.thePlayer.setSprinting(true);
        }
    };

//    @EventPriority(1)
//    @EventTarget
//    public void onpreUpdate(EventUpdate e) {
//        if ((!KillAura.attacking || !TYClient.getModuleManager().getModule(KillAura.class).isEnabled())
//                && (!mc.thePlayer.onGround || mc.thePlayer.cqL > 2)
//                && !mc.gameSettings.keyBindRight.isKeyDown()
//                && !mc.gameSettings.keyBindLeft.isKeyDown()
//                && mc.thePlayer.Zl > 5
//                && mc.thePlayer.isUsingItem()
//                && (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword))
//                && (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemBow))) {
//            RotationComponent.setRotations(new Vector2f(mc.thePlayer.pl + 45.0F, mc.thePlayer.rotationPitch), 10.0, MovementFix.NORMAL);
//        }
//    };
    @EventTarget
    public void onSlowDown(EventSlowDown e) {
        if (this.foodValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && this.usingTicks > this.maxPingSpoof.getValue().intValue()) {
            e.setCancelled(true);
        }

        if (this.potionValue.getValue()
                && mc.thePlayer.isUsingItem()
                && mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion
                && this.usingTicks > this.maxPingSpoof.getValue().intValue()) {
            e.setCancelled(true);
        }

        if (this.swordValue.get() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            PacketUtils.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            PingSpoofManager.dispatch();
//            if (ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_19)) {
//                UserConnection userconnection = Via.getManager().getConnectionManager().getConnections().iterator().next();
//                PacketWrapper packetwrapper = PacketWrapper.create(ServerboundPackets1_19.USE_ITEM, userconnection);
//                packetwrapper.write(Types.VAR_INT, 0);
//                packetwrapper.write(Types.VAR_INT, mc.playerController.GZ());
//                packetwrapper.sendToServer(Protocol1_19To1_18_2.class);
//            } else {
                PacketUtils.send(new C08PacketPlayerBlockPlacement(getItemStack()));
//            }

            e.setCancelled(true);
        }

        if (this.bowValue.getValue() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && this.usingTicks > this.maxPingSpoof.getValue().intValue()) {
            e.setCancelled(true);
        }
    };

    public static ItemStack getItemStack() {
        return mc.thePlayer != null && mc.thePlayer.inventoryContainer != null
                ? mc.thePlayer.inventoryContainer.getSlot(bQ() + 36).getStack() : null;
    }

    public static int bQ() {
        InventoryPlayer inventoryplayer = mc.thePlayer.inventory;
        return inventoryplayer.cIU ? inventoryplayer.cIT : inventoryplayer.currentItem;
    }

    @Override
    public void onEnable() {
    }
}
