package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.player.EventMoveInput;
import cn.tianyu.client.event.events.player.MoveMathEvent;
import cn.tianyu.client.gui.notification.NotificationManager;
import cn.tianyu.client.gui.notification.NotificationType;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.InventoryUtils;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import com.yumegod.obfuscation.Native;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;

/**
 * @Author: CN_tianyu
 */

@Native
public class AutoGapple extends Module {
    private final NumberValue health = new NumberValue("Health", 15, 1, 20, 0.5f);
    private final BooleanValue LogAir = new BooleanValue("Lag in Air",true);
    private final BooleanValue EatMove = new BooleanValue("movement in eat",false);
    public static boolean eat = false;
    private boolean needSkip = false;
    public AutoGapple() {
        super("AutoGapple","AutoGapple", Category.combat);
    }

    int slot;
    private int oldSlot = -1;
    private int eatingTicks = 0;
    private boolean doGapple = false;

    @Override
    public void draw(){
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        if(eat){
            for(int i=1;i<= eatingTicks * 2;i++){
                RenderUtil.drawLoadingRect("SendPacket",sr.getScaledWidth()/2,sr.getScaledHeight()/2 ,i);
            }
        }
    }

    @EventTarget
    public void onEventMoveInput(EventMoveInput event) {
        if(doGapple && !EatMove.getValue()){
            event.setForward(0);
            event.setStrafe(0);
        }
    }


    @EventTarget
    public void onMoveMathEvent(MoveMathEvent event) {
        if (eat && LogAir.get() && mc.thePlayer.positionUpdateTicks < 20 && !needSkip) event.setCancelled(true);
        else if (needSkip) needSkip = false;
    }

    @Override
    public void onDisable(){
        eat = false;
    }

    @Override
    public void onEnable(){
        if(mc.thePlayer == null) return;
        doGapple = false;
        slot = -1;
        needSkip = false;
        eat = false;
    }

    @Override
    public void onUpdate(){

        if (eatingTicks == 0 && mc.thePlayer.getHealth() <= health.getValue()){
            doGapple = true;
            slot = getAppleFromInventory();
            if (slot != -1){
                slot -= 36;
                oldSlot = mc.thePlayer.inventory.currentItem;
                eat = true;
                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(slot));
                NotificationManager.post(NotificationType.INFO,"AutoGapple","找一找苹果");
                mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(slot)));
                NotificationManager.post(NotificationType.INFO,"AutoGapple","吃苹果");
                eatingTicks = 33;
            }else{
                NotificationManager.post(NotificationType.INFO,"AutoGapple","没有苹果!");
                toggle();
            }
        } else if (eatingTicks > 0) {
            eatingTicks--;
            if (eatingTicks == 0 && oldSlot != -1) {
                mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(oldSlot));
                eat = false;
                doGapple = false;
                NotificationManager.post(NotificationType.INFO,"AutoGapple","吃了一个苹果~");
            }
        }
    }

    private int getAppleFromInventory() {
        for (int i = 36; i < 45; i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack == null) continue;

            Item item = stack.getItem();
            if (item == null || InventoryUtils.isItemEmpty(item)) continue;

            if (item != Items.golden_apple) continue;

            return i;
        }

        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack == null) continue;

            Item item = stack.getItem();
            if (item == null || InventoryUtils.isItemEmpty(item)) continue;

            if (item != Items.golden_apple) continue;

            mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, i, slot ,2, mc.thePlayer);
        }

        return -1;
    }

    @EventTarget
    public void onPacket(EventPacket event){
        if (mc.thePlayer == null || !mc.playerController.getCurrentGameType().isSurvivalOrAdventure()) return;

        final Packet<?> packet = event.getPacket();
        if(event.getState() == EventPacket.EventState.SEND) {

            if (packet instanceof C00Handshake || packet instanceof C00PacketLoginStart ||
                    packet instanceof C00PacketServerQuery || packet instanceof C01PacketPing ||
                    packet instanceof C01PacketEncryptionResponse || packet instanceof C01PacketChatMessage) {
                return;
            }

            if (!(packet instanceof C09PacketHeldItemChange) &&
                    !(packet instanceof C0EPacketClickWindow) &&
                    !(packet instanceof C16PacketClientStatus) &&
                    !(packet instanceof C0DPacketCloseWindow)
            ) {
                if (eat) {
                    event.setCancelled(true);
                }
            }
        } else {
            if (packet instanceof S12PacketEntityVelocity) {

                S12PacketEntityVelocity wrapped = (S12PacketEntityVelocity) packet;

                if (wrapped.getEntityID() == mc.thePlayer.getEntityId())
                    needSkip = true;
            }
        }
    }
}
