package cn.tianyu.client.module.impl.player;

import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventHigherPacketSend;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.event.events.world.EventWorldLoad;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author: CN_tianyu
 */
public class Blink extends Module {
    public Blink(){
        super("Blink","Blink", Category.player);
    }
    private final LinkedList<List<Packet<?>>> packets = new LinkedList();
    public EntityOtherPlayerMP fakePlayer;
    private int ticks;
    public ModeValue modeValue = new ModeValue("Mode", "Simple", "Simple","Slow Release", "Delay", "Slow Release");
    public BooleanValue autoclose = new BooleanValue("AutoClose", false);
    public BooleanValue auraValue = new BooleanValue("Aura Support", true);

    @Override
    public void onEnable() {
        if (Blink.mc.thePlayer == null) {
            return;
        }
        this.packets.clear();
        this.packets.add(new ArrayList());
        this.ticks = 0;
        this.fakePlayer = new EntityOtherPlayerMP(Blink.mc.theWorld, Blink.mc.thePlayer.getGameProfile());
        this.fakePlayer.clonePlayer(Blink.mc.thePlayer, true);
        this.fakePlayer.copyLocationAndAnglesFrom(Blink.mc.thePlayer);
        this.fakePlayer.rotationYawHead = Blink.mc.thePlayer.rotationYawHead;
        Blink.mc.theWorld.addEntityToWorld(-1337, this.fakePlayer);
    }

    @EventTarget
    public void onWorld(EventWorldLoad event) {
        this.setEnable(false);
    }

    @Override
    public void onDisable() {
        this.packets.forEach(this::sendTick);
        this.packets.clear();
        try {
            if (this.fakePlayer != null) {
                Blink.mc.theWorld.removeEntity(this.fakePlayer);
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D eventRender2D){
        //RenderUtil.drawProgressBar(ticks);
    }

    @EventTarget
    public void onPacket(EventHigherPacketSend event) {
        Packet packet = event.getPacket();
        if (PacketUtils.isCPacket(packet)) {
            mc.addScheduledTask(() -> this.packets.getLast().add(packet));
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onTick(EventTick event) {
        ++this.ticks;
        this.packets.add(new ArrayList());
        switch (this.modeValue.getValue()) {
            case "Delay": {
                if (this.packets.size() <= 100) break;
                this.poll();
                break;
            }
            case "Slow Release": {
                if (this.packets.size() <= 100 && this.ticks % 5 != 0) break;
                this.poll();
            }
        }
    }

    private void poll() {
        if (this.packets.isEmpty()) {
            return;
        }
        this.sendTick(this.packets.getFirst());
        this.packets.removeFirst();
    }

    @Override
    public String getSuffix() {
        return this.modeValue.getValue();
    }

    private void sendTick(List<Packet<?>> tick) {
        tick.forEach(packet -> {
            mc.getNetHandler().getNetworkManager().sendPacketWithoutHigherPacket(packet);
            this.handleFakePlayerPacket(packet);
        });
    }

    private void handleFakePlayerPacket(Packet<?> packet) {
        if (packet instanceof C03PacketPlayer.C04PacketPlayerPosition) {
            C03PacketPlayer.C04PacketPlayerPosition position = (C03PacketPlayer.C04PacketPlayerPosition)packet;
            this.fakePlayer.setPositionAndRotation2(position.x, position.y, position.z, this.fakePlayer.rotationYaw, this.fakePlayer.rotationPitch, 3, true);
            this.fakePlayer.onGround = position.isOnGround();
        } else if (packet instanceof C03PacketPlayer.C05PacketPlayerLook) {
            C03PacketPlayer.C05PacketPlayerLook rotation = (C03PacketPlayer.C05PacketPlayerLook)packet;
            this.fakePlayer.setPositionAndRotation2(this.fakePlayer.posX, this.fakePlayer.posY, this.fakePlayer.posZ, rotation.getYaw(), rotation.getPitch(), 3, true);
            this.fakePlayer.onGround = rotation.isOnGround();
            this.fakePlayer.rotationYawHead = rotation.getYaw();
            this.fakePlayer.rotationYaw = rotation.getYaw();
            this.fakePlayer.rotationPitch = rotation.getPitch();
        } else if (packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
            C03PacketPlayer.C06PacketPlayerPosLook positionRotation = (C03PacketPlayer.C06PacketPlayerPosLook)packet;
            this.fakePlayer.setPositionAndRotation2(positionRotation.x, positionRotation.y, positionRotation.z, positionRotation.getYaw(), positionRotation.getPitch(), 3, true);
            this.fakePlayer.onGround = positionRotation.isOnGround();
            this.fakePlayer.rotationYawHead = positionRotation.getYaw();
            this.fakePlayer.rotationYaw = positionRotation.getYaw();
            this.fakePlayer.rotationPitch = positionRotation.getPitch();
        } else if (packet instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction action = (C0BPacketEntityAction)packet;
            if (action.getAction() == C0BPacketEntityAction.Action.START_SPRINTING) {
                this.fakePlayer.setSprinting(true);
            } else if (action.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                this.fakePlayer.setSprinting(false);
            } else if (action.getAction() == C0BPacketEntityAction.Action.START_SNEAKING) {
                this.fakePlayer.setSneaking(true);
            } else if (action.getAction() == C0BPacketEntityAction.Action.STOP_SNEAKING) {
                this.fakePlayer.setSneaking(false);
            }
        } else if (packet instanceof C0APacketAnimation) {
            C0APacketAnimation animation = (C0APacketAnimation)packet;
            this.fakePlayer.swingItem();
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (Blink.mc.thePlayer.hurtTime > 0 && this.autoclose.getValue().booleanValue()) {
            this.setEnable(false);
            DebugUtil.log("[TYClient] Blink AutoClose!");
        }
    }
}
