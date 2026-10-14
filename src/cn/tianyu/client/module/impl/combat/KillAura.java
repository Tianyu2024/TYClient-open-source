package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventPriority;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventKey;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.player.EventAttack;
import cn.tianyu.client.event.events.player.UpdateEvent2;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.misc.AntiBot;
import cn.tianyu.client.module.impl.misc.Teams;
import cn.tianyu.client.util.impl.client.DebugUtil;
import cn.tianyu.client.util.impl.client.MathUtil;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.util.impl.player.RotationUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.ModeValue;
import cn.tianyu.client.value.impl.NumberValue;
import cn.tianyu.client.viamcp.utils.AttackOrder;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.*;
import org.lwjglx.input.Keyboard;
import org.lwjglx.util.vector.Vector2f;

import java.util.Objects;
import java.util.Random;

public class KillAura extends Module {
    public static KillAura getInstance;
    public final NumberValue range = new NumberValue("Range", 3.1, 1, 6, 0.01);
    private final BooleanValue VulcanMode = new BooleanValue("Vulcan", false);
    private final BooleanValue randomcps = new BooleanValue("RandomCPS", true);
    private final NumberValue cps = new NumberValue("CPS", 12, 1, 20, 1, ()-> !randomcps.getValue());
    private final NumberValue Maxcps = new NumberValue("MaxCPS", 12, 1, 20, 1, randomcps::getValue);
    private final NumberValue Mincps = new NumberValue("MinCPS", 12, 1, 20, 1, randomcps::getValue);
    private final ModeValue attack = new ModeValue("Attack","pre","post","pre");
    private ModeValue attackmode = new ModeValue("AttackMode","Fakeclick","FakeClick","packet", "fixed");
    //private NumberValue AttackDelay = new NumberValue("Attack", 1,1,1,1);
    private final BooleanValue autoBlock = new BooleanValue("AutoBlock", true);
    private final ModeValue abMode = new ModeValue(autoBlock::getValue, "AbMode","always","always","onHurt", "Hyp");
    private final ModeValue abMode2 = new ModeValue(autoBlock::getValue, "SendAbMode","packet1","packet1","UseItem","Key Bind","packet2", "");
    private final BooleanValue onlyAbCanHurt = new BooleanValue("only ab can hurt",false, autoBlock::get);
    //private final NumberValue abRange = new NumberValue("AutoBlockRange", 5, 1, 20, 1);

    public BooleanValue moveFixValue = new BooleanValue("Movement Fix", true);
    public BooleanValue strictValue = new BooleanValue("Follow Player (Strict)", false, () -> moveFixValue.getValue());
    public BooleanValue strictAltValue = new BooleanValue("Toggle strict when alt key", false, () -> moveFixValue.getValue() && strictValue.isAvailable());

    private static final BooleanValue players = new BooleanValue("Players", true);
    private static final BooleanValue mobs = new BooleanValue("Mobs", false);
    private static final BooleanValue animals = new BooleanValue("Animals", false);
    private static final BooleanValue invisibles = new BooleanValue("Invisibles", true);
    private final BooleanValue swing = new BooleanValue("Swing", true);
    private final BooleanValue rotation = new BooleanValue("Rotation", true);
    private final ModeValue rotationmode= new ModeValue("Rotation Mode", "sync AimAssist", "head");
    private final BooleanValue randomization = new BooleanValue("Randomization", false);
    private final NumberValue rotationspeed = new NumberValue("rotationspeed", 180, 90, 360, 90);
    private final BooleanValue Debug = new BooleanValue("Debug",false);

    private final BooleanValue missEnabled = new BooleanValue("Miss Enabled", true);
    private final NumberValue missChance = new NumberValue("Miss Chance", 8, 0, 30, 1, missEnabled::getValue);

    private final BooleanValue rotationErrorEnabled = new BooleanValue("Rotation Error", true);
    private final NumberValue rotationErrorYaw = new NumberValue("Error Yaw", 1.5, 0, 5, 0.1, rotationErrorEnabled::getValue);
    private final NumberValue rotationErrorPitch = new NumberValue("Error Pitch", 0.8, 0, 3, 0.1, rotationErrorEnabled::getValue);

    private final BooleanValue hitPointRandomize = new BooleanValue("HitPoint Randomize", true);
    private final NumberValue hitPointVerticalRange = new NumberValue("Vertical Range", 0.4, 0, 1.0, 0.05, hitPointRandomize::getValue);
    private final NumberValue hitPointHorizontalRange = new NumberValue("Horizontal Range", 0.2, 0, 0.8, 0.05, hitPointRandomize::getValue);

    private final BooleanValue rangeRandomize = new BooleanValue("Range Randomize", true);
    private final NumberValue rangeRandomOffset = new NumberValue("Range Offset", 0.15, 0, 0.5, 0.01, rangeRandomize::getValue);

    private static final Minecraft mc = Minecraft.getMinecraft();
    @Getter
    public static EntityLivingBase target;
    private long lastAttackTime;
    private final Random random = new Random();
    public static boolean blocking;
//    public float rotationYaw;
//    public float rotationPitch;

    private long lastBlockTime;
    private final Random blockRandom = new Random();
    private boolean isBlocking = false;


    public KillAura() {
        super("KillAura", "杀戮光环", Category.combat);
        getInstance = this;
    }

    public TargetStrafe ts;

    public static float getPlayerRotation(Entity ent) {
        double x2 = ent.posX - KillAura.mc.thePlayer.posX;
        double z = ent.posZ - KillAura.mc.thePlayer.posZ;
        double yaw = Math.atan2(x2, z) * 57.2957795;
        yaw = -yaw;
        return (float)yaw;
    }


    @Override
    public void onDisable() {
        releaseBlock();
        target = null;
    }

    @Override
    public void onEnable(){
        DebugUtil.log("很遗憾，Killaura因为转头写的有问题所以只能绕过低级反作弊");
        DebugUtil.log("请谨慎使用，建议blockhit+aim+ac游玩");

        //releaseBlock();
        ts = getModule(TargetStrafe.class);
    }

    @EventTarget
    @EventPriority(12)
    public void onKey(EventKey e) {
        if (this.getState() && e.getKey() == Keyboard.KEY_LMENU && moveFixValue.getValue() && strictValue.isAvailable() && strictAltValue.getValue()) {
            strictValue.setValue(!strictValue.getValue());
            DebugUtil.log("Set to " + (strictValue.getValue() ? "Strict" : "Silent") + ".");
        }
    }

    @Override
    public void onUpdate() {
        if(Mincps.get() >= Maxcps.get()){
            Mincps.setValue(Maxcps.getValue() - 1);
        }

        if (mc.thePlayer == null || mc.theWorld == null) return;

        updateTarget();


        if (target != null && !target.isDead && target.getHealth() > 0){
            if(attack.getValue().equals("pre")){
                handleAttack();
            }
            if(Objects.equals(abMode.getValue(), "onHurt") && mc.thePlayer.hurtTime == 5){
                handleBlocking();
                return;
            }
            if(Objects.equals(abMode.getValue(), "always")){
                handleBlocking();
            }
            if(Objects.equals(abMode.get(), "Hyp")){
                handleAutoBlock();
            }
            if(attack.getValue().equals("post")){
                handleAttack();
            }
        } else {
            releaseBlock();
        }
    }

    private void updateTarget() {
        EntityLivingBase newTarget = findClosestEntity();
        if (newTarget != target) {
            target = newTarget;
        }
    }

    private EntityLivingBase findClosestEntity() {
        EntityLivingBase closest = null;
        double closestDistance = range.getValue() + 1;

        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (!(entity instanceof EntityLivingBase) || entity == mc.thePlayer) continue;

            EntityLivingBase living = (EntityLivingBase) entity;
            double distance = mc.thePlayer.getDistanceToEntity(living);

            if (distance <= range.getValue() && isValid(living) && (closest == null || distance < closestDistance)) {
                closest = living;
                closestDistance = distance;
            }
        }
        return closest;
    }

    private void handleAttack() {
        if(!autoBlock.getValue() && onlyAbCanHurt.getValue()){
            return;
        }
        long currentTime = System.currentTimeMillis();
        Double cps;
        if(randomcps.get()){
            cps = MathUtil.getRandomInRange(Maxcps.getValue(), Mincps.getValue());
        } else {
            cps = this.cps.getValue();
        }
        long attackDelay = (long) (1000 / cps);
        attackDelay += random.nextInt(50) - 25;

        if (currentTime - lastAttackTime >= attackDelay) {
            performAttack();
            lastAttackTime = currentTime;
        }
    }

    float yaw;
    float pitch;

    @EventTarget
    public void onUpdate(UpdateEvent2 e) {
        if(target != null && !target.isDead && target.getHealth() > 0){
            if (rotation.getValue()) {
                float[] rot;
                //rot = RotationUtil.getAnglesAGC(target);
                rot = RotationUtil.getAngles(target);
                if (randomization.getValue()) {
                    yaw = rot[0] + MathUtil.getRandomInRange(-3.5f, 3.8f);
                    pitch = rot[1] + MathUtil.getRandomInRange(-3.8f, 3.5f);
                } else {
                    yaw = rot[0];
                    pitch = rot[1];
                }

                if (rotationErrorEnabled.getValue()) {
                    if (System.currentTimeMillis() - lastAttackTime > 100) {
                        yaw += (float) (Math.random() - 0.5) * rotationErrorYaw.getValue().floatValue() * 2;
                        pitch += (float) (Math.random() - 0.5) * rotationErrorPitch.getValue().floatValue() * 2;
                    }
                }

                if (hitPointRandomize.getValue()) {
                    pitch += (float) (Math.random() - 0.5) * hitPointVerticalRange.getValue().floatValue() * 2;
                    yaw += (float) (Math.random() - 0.5) * hitPointHorizontalRange.getValue().floatValue() * 2;
                }

                pitch = MathHelper.clamp_float(pitch, -90, 90);

                e.setRotation(yaw, pitch, 4);
//
//                mc.thePlayer.rotationYawHead = yaw;
//                mc.thePlayer.renderYawOffset = yaw;

                //TYClient.rotationManager.setRotation(new Vector2f(yaw, pitch), rotationspeed.getValue().floatValue(), !ts.getState() && moveFixValue.getValue(), strictValue.getValue());
            }
        }
    }

    private void performAttack() {
        if(TYClient.instance.getFriendManager().isFriend(target.getName())){
            return;
        }

        if (missEnabled.getValue()) {
            if (Math.random() < missChance.getValue() / 100.0) {
                if(Debug.getValue()){
                    DebugUtil.log("Miss! (Chance: " + missChance.getValue() + "%)");
                }
                return;
            }
        }

        double actualRange = range.getValue();
        if (rangeRandomize.getValue()) {
            actualRange += (Math.random() - 0.5) * rangeRandomOffset.getValue() * 2;
            if (actualRange < 1.5) actualRange = 1.5;
        }

        if (mc.thePlayer.getDistanceToEntity(target) > actualRange) {
            if(Debug.getValue()){
                DebugUtil.log("Out of range: " + mc.thePlayer.getDistanceToEntity(target) + " > " + actualRange);
            }
            return;
        }

        if(attack.is("pre")){
            TYClient.instance.eventManager.callEvent(new EventAttack(target,true));
        } else{
            TYClient.instance.eventManager.callEvent(new EventAttack(target,false));
        }
        if(blocking && VulcanMode.getValue()){
            mc.clickMouse();
            return;
        }
        if(Debug.getValue()){
            DebugUtil.log("Attack " + target.getDistance(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ)
            );
        }
        switch (attackmode.getValue()){
            case "Fakeclick":
                mc.clickMouse();
                break;
            case "packet":
                mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
                break;
            case "fixed":
                AttackOrder.sendFixedAttackByPacket(mc.thePlayer, target);
        }
        if (swing.getValue() && attackmode.getValue() != "Fakeclick" && attackmode.getValue() != "fixed") {
            mc.thePlayer.swingItem();
        }
    }

    //    private void handleBlocking() {
//        if (autoBlock.getValue() && mc.thePlayer.getHeldItem() != null
//                && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
//            block();
//        } else {
//            releaseBlock();
//        }
//    }
    private void handleBlocking() {
        if (!autoBlock.getValue()) return;
        if (mc.thePlayer.getHeldItem() == null ||
                !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword)) {
            releaseBlock();
            return;
        }

        String mode = abMode.getValue();

        if (mode.equals("Hyp")) {
            handleHypBlock();
            return;
        }

        if (mode.equals("onHurt") && mc.thePlayer.hurtTime == 5) {
            block();
            return;
        }

        if (mode.equals("always")) {
            block();
        }
    }

    private void handleHypBlock() {
        if (target == null || target.isDead || target.getHealth() <= 0) {
            releaseBlock();
            return;
        }

        // 6-7 CPS = 142-166ms
        int cps = 6 + blockRandom.nextInt(2);
        long delay = 1000 / cps;
        delay += blockRandom.nextInt(30) - 15; // 随机抖动

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastBlockTime >= delay) {
            toggleBlock();
            lastBlockTime = currentTime;
        }
    }

    private void toggleBlock() {
        if (blocking) {
            releaseBlock();
        } else {
            block();
        }
    }

    private void handleAutoBlock() {
        if (!autoBlock.getValue()) return;
        handleBlocking();
    }

    public static boolean isValid(Entity entity) {
        if(entity instanceof EntityPlayer){
            if (Teams.isSameTeam(entity))
                return false;
            if (AntiBot.INSTANCE.isBot((EntityLivingBase) entity)) {
                return false;
            }
        }
        if (!mc.thePlayer.canEntityBeSeen(entity)) return false;
        if (entity instanceof EntityPlayer && !players.getValue()) return false;
        if (entity.getClass().getPackage().getName().contains("monster") && !mobs.get()) return false;
        if (entity.getClass().getPackage().getName().contains("passive") && !animals.getValue()) return false;
        return !entity.isInvisible() || invisibles.get();
    }

    private void block() {
        if (!blocking) {
            if(Debug.getValue()){
                DebugUtil.log("Block " + abMode2.get());
            }
            switch (abMode2.getValue()){
                case "packet1":
                    PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(
                            new BlockPos(-1, -1, -1), 255,
                            mc.thePlayer.getHeldItem(), 0, 0, 0
                    ));
                    break;
                case "UseItem":
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getCurrentEquippedItem());
                    break;
                case "Key Bind":
                    mc.gameSettings.keyBindUseItem.pressed = true;
                    break;
                case "packet2":
                    sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
                    sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                    break;
            }
            blocking = true;
        }
    }

    private void releaseBlock() {
        if (blocking) {
            if(Debug.getValue()){
                DebugUtil.log("releaseBlock");
            }
            mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                    BlockPos.ORIGIN, EnumFacing.DOWN
            ));
            blocking = false;
        }
    }
}