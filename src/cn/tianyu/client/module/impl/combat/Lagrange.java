package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.Manager.FriendManager;
import cn.tianyu.client.Manager.impl.BlinkModules;
import cn.tianyu.client.TYClient;
import cn.tianyu.client.command.commands.FriendCommand;
import cn.tianyu.client.event.EventPriority;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.render.EventRender3D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.client.HUD;
import cn.tianyu.client.module.impl.misc.AntiBot;
import cn.tianyu.client.module.impl.misc.Teams;
import cn.tianyu.client.module.impl.world.BedNuker;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.util.impl.player.ItemUtil;
import cn.tianyu.client.util.impl.player.RotationUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: CN_tianyu
 */
public class Lagrange extends Module {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public final ModeValue mode = new ModeValue("Mode","DelayBlink","DelayBlink","Lag");
    public final NumberValue blinkTick  = new NumberValue("BlinkTick", 3,0,10, 1, () -> Objects.equals(mode.getValue(), "DelayBlink"));
    public final NumberValue delay = new NumberValue("delay", 150, 0, 1000, 5, () -> Objects.equals(mode.getValue(), "Lag"));
    public final NumberValue range = new NumberValue("range", 10.0F, 3.0F, 100.0F, 1.0F);
    public final BooleanValue weaponsOnly = new BooleanValue("weapons-only", true);
    public final BooleanValue allowTools = new BooleanValue("allow-tools", false, this.weaponsOnly::getValue);
    public final BooleanValue botCheck = new BooleanValue("bot-check", true);
    public final BooleanValue teams = new BooleanValue("teams", true);
    public final ModeValue showPosition = new ModeValue("show-position", "NONE", "NONE", "DEFAULT", "HUD");
    private int tickIndex = -1;
    private long delayCounter = 0L;
    private boolean hasTarget = false;
    private Vec3 lastPosition = null;
    private Vec3 currentPosition = null;

    public Lagrange() {
        super("LagRange", "",Category.combat);
    }

    private boolean isValidTarget(EntityPlayer entityPlayer) {
        if (entityPlayer != mc.thePlayer && entityPlayer != mc.thePlayer.ridingEntity) {
            if (entityPlayer == mc.getRenderViewEntity() || entityPlayer == mc.getRenderViewEntity().ridingEntity) {
                return false;
            } else if (entityPlayer.deathTime > 0) {
                return false;
            } else if (TYClient.instance.getFriendManager().isFriend(entityPlayer)) {
                return false;
            } else {
                return (!this.teams.getValue() || !Teams.isSameTeam(entityPlayer)) && (!this.botCheck.getValue() || !AntiBot.INSTANCE.isBot(entityPlayer));
            }
        } else {
            return false;
        }
    }

    private boolean shouldResetOnPacket(Packet<?> packet) {
        if (packet instanceof C02PacketUseEntity) {
            return true;
        } else if (packet instanceof C07PacketPlayerDigging) {
            return ((C07PacketPlayerDigging) packet).getStatus() != C07PacketPlayerDigging.Action.RELEASE_USE_ITEM;
        } else if (packet instanceof C08PacketPlayerBlockPlacement) {
            ItemStack item = ((C08PacketPlayerBlockPlacement) packet).getStack();
            return item == null || !(item.getItem() instanceof ItemSword);
        } else {
            return false;
        }
    }

    @EventPriority(3)
    @EventTarget
    public void onTick(EventTick event) {
        if (this.isEnabled()) {
            switch (event.getType()) {
                case PRE:
                    KillAura killAura = (KillAura) TYClient.instance.moduleManager.getModule(KillAura.class);
                    if ((
                            //killAura.shouldAutoBlock()
                            //|| killAura.isBlocking()
                            //&&
                            killAura.isEnabled() && killAura.getTarget() != null
                    )
                    && Objects.equals(mode.getValue(), "DelayBlink")){
                        TYClient.instance.blinkManager.setBlinkState(false, BlinkModules.BLINK);
                        return;
                    }
                    TYClient.instance.lagManager.setDelay(0);
                    this.hasTarget = false;
                    BedNuker bedNuker = (BedNuker) TYClient.moduleManager.getModule(BedNuker.class);
                    if ((!bedNuker.isEnabled()
                            //|| !bedNuker.isReady())
                            && !(mc.playerController).isHittingBlock()
                            && (!mc.thePlayer.isUsingItem() || mc.thePlayer.isBlocking())
                            && (
                            !(Boolean) this.weaponsOnly.getValue()
                                    || ItemUtil.hasRawUnbreakingEnchant()
                                    || this.allowTools.getValue() && ItemUtil.isHoldingTool()
                    )
                    )) {
                        List<EntityPlayer> players = mc.theWorld
                                .loadedEntityList
                                .stream()
                                .filter(entity -> entity instanceof EntityPlayer)
                                .map(entity -> (EntityPlayer) entity)
                                .filter(this::isValidTarget)
                                .collect(Collectors.toList());
                        if (players.isEmpty()) {
                            TYClient.instance.blinkManager.setBlinkState(false, BlinkModules.BLINK);
                            this.tickIndex = -1;
                        } else {
                            double height = mc.thePlayer.getEyeHeight();
                            Vec3 eyePosition = TYClient.instance.lagManager.getLastPosition().addVector(0.0, height, 0.0);
                            Vec3 targetEyePosition = new Vec3(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY + height, mc.thePlayer.lastTickPosZ);
                            Vec3 playerEyePosition = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + height, mc.thePlayer.posZ);
                            for (EntityPlayer player : players) {
                                double distance = RotationUtil.distanceToBox(player, playerEyePosition);
                                if (!(distance > (double) this.range.getValue())) {
                                    double targetDist = RotationUtil.distanceToBox(player, targetEyePosition);
                                    double eyeDist = RotationUtil.distanceToBox(player, eyePosition);
                                    if (distance < targetDist || distance < eyeDist) {
                                        if (this.tickIndex < 0) {
                                            this.tickIndex = 0;
                                            for (this.delayCounter = this.delayCounter + this.delay.getValue().longValue();
                                                 this.delayCounter > 0L;
                                                 this.delayCounter = this.delayCounter - 50
                                            ) {
                                                this.tickIndex++;
                                            }
                                        }
                                        if (Objects.equals(mode.getValue(), "Lag")) {
                                            TYClient.instance.lagManager.setDelay(this.tickIndex);
                                        }
                                        if (Objects.equals(mode.getValue(), "DelayBlink")){
                                            TYClient.instance.blinkManager.setBlinkState(true, BlinkModules.BLINK);
                                            if (TYClient.instance.blinkManager.countMovement() > blinkTick.getValue().longValue()) {
                                                TYClient.instance.blinkManager.setBlinkState(false, BlinkModules.BLINK);
                                            }
                                        }
                                        this.hasTarget = true;
                                        return;
                                    }
                                }
                            }
                        }
                    } else {
                        this.tickIndex = -1;
                        TYClient.instance.blinkManager.setBlinkState(false, BlinkModules.BLINK);
                    }
                    if (!hasTarget){
                        TYClient.instance.blinkManager.setBlinkState(false, BlinkModules.BLINK);
                    }
                    break;
                case POST:
                    Vec3 savedPosition = TYClient.instance.lagManager.getLastPosition();
                    if (this.currentPosition == null) {
                        this.lastPosition = savedPosition;
                    } else {
                        this.lastPosition = this.currentPosition;
                    }
                    this.currentPosition = savedPosition;
            }
        }
    }

    @EventTarget
    public void onPacket(EventPacket event) {
        if (this.isEnabled()) {
            if (this.shouldResetOnPacket(event.getPacket())) {
                TYClient.instance.lagManager.setDelay(0);
                this.tickIndex = -1;
            }
        }
    }

//    @EventTarget
//    public void onRender3D(EventRender3D event) {
//        if (this.isEnabled()) {
//            if (!Objects.equals(this.showPosition.getValue(), "NONE")
//                    && mc.gameSettings.thirdPersonView != 0
//                    && this.hasTarget
//                    && this.lastPosition != null
//                    && this.currentPosition != null) {
//                Color color = new Color(-1);
//                switch (this.showPosition.getValue()) {
//                    case 1:
//                        color = Teams.getTeamColor(mc.thePlayer, 1.0F);
//                        break;
//                    case 2:
//                        color = ((HUD) TYClient.instance.moduleManager.getModule(HUD.class)).getColor(System.currentTimeMillis());
//                }
//                double x = RenderUtil.lerpDouble(this.currentPosition.xCoord, this.lastPosition.xCoord, event.getPartialTicks());
//                double y = RenderUtil.lerpDouble(this.currentPosition.yCoord, this.lastPosition.yCoord, event.getPartialTicks());
//                double z = RenderUtil.lerpDouble(this.currentPosition.zCoord, this.lastPosition.zCoord, event.getPartialTicks());
//                float size = mc.thePlayer.getCollisionBorderSize();
//                AxisAlignedBB aabb = new AxisAlignedBB(
//                        x - (double) mc.thePlayer.width / 2.0,
//                        y,
//                        z - (double) mc.thePlayer.width / 2.0,
//                        x + (double) mc.thePlayer.width / 2.0,
//                        y + (double) mc.thePlayer.height,
//                        z + (double) mc.thePlayer.width / 2.0
//                )
//                        .expand(size, size, size)
//                        .offset(
//                                -( mc.getRenderManager()).getRenderPosX(),
//                                -( mc.getRenderManager()).getRenderPosY(),
//                                -( mc.getRenderManager()).getRenderPosZ()
//                        );
//                RenderUtil.enableRenderState();
//                RenderUtil.drawFilledBox(aabb, color.getRed(), color.getGreen(), color.getBlue());
//                RenderUtil.disableRenderState();
//            }
//        }
//    }

    @Override
    public void onDisable() {
        TYClient.instance.lagManager.setDelay(0);
        this.tickIndex = -1;
        this.delayCounter = 0L;
        this.hasTarget = false;
        this.lastPosition = null;
        this.currentPosition = null;
    }

    public String getSuffix() {
        if (Objects.equals(this.mode.getValue(), "Lag")) {
            return this.delay.getValue() + "ms";
        }else {
            return String.valueOf(blinkTick.getValue());
        }
    }
}