package cn.tianyu.client.module.impl.movement;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventKey;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventTick;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.player.EventJump;
import cn.tianyu.client.event.events.player.EventMoveInput;
import cn.tianyu.client.event.events.player.EventStrafe;
import cn.tianyu.client.event.events.render.EventRender2D;
import cn.tianyu.client.event.events.render.EventRender3D;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.RenderUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.*;
import org.lwjglx.input.Keyboard;

import java.awt.*;

/**
 * @Author: CN_tianyu
 */
public class TellyBridge extends Module {
    private final BooleanValue autoSwap = new BooleanValue("Auto Swap", true);
    private final BooleanValue showActivation = new BooleanValue("Show Activation", true);
    private final NumberValue activationWait = new NumberValue("Activation Wait", 1000, 500, 2000, 50);

    private final float[] yawCurve = {
            91.68f, 98.88f, 78.94f, 37.45f, 1.61f, -21.69f, -33.98f,
            -35.80f, -34.64f, -33.85f, -33.06f, -31.55f, -29.26f, -26.65f,
            -24.19f, -21.07f, -18.84f, -17.06f, -8.87f, 2.61f, 41.94f
    };

    private final float[] pitchCurve = {
            64.31f, 59.95f, 60.57f, 61.46f, 60.64f, 58.89f, 56.91f,
            56.63f, 58.65f, 61.63f, 64.20f, 66.74f, 68.69f, 70.64f,
            73.01f, 75.37f, 77.46f, 78.56f, 78.90f, 77.22f, 72.25f
    };

    private final float[] forwardCurve = {
            1.0f, 1.0f, 0.0f, 0.0f, -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f
    };

    private final float[] strafeCurve = {
            -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, -1.0f, -1.0f, -1.0f, -1.0f
    };

    private static final double SENSITIVITY_QUANTUM = 0.03404715;
    private final double[] FACE_HIT_OFFSETS = {0.5, 0.25, 0.75, 0.15, 0.85};
    private final int[] ALLOWED_PLACE_FACES = {2, 3, 4, 5, 1};
    private final String[] REPLACEABLE_BLOCKS = {"air", "water", "flowing_water", "lava", "flowing_lava", "fire", "tallgrass", "deadbush", "snow_layer", "vine"};

    private boolean armed = false;
    private boolean running = false;
    private int setupTick = 0;
    private int cyclePhase = 0;
    private float baseYaw = 0.0f;

    private long activatePromptAt = 0L;
    private long promptBrokeAt = 0L;
    private float promptAlpha = 0.0f;
    private long promptFadeLastAt = 0L;
    private int promptFadeRgb = 0xFF5555;
    private int[] hitboxLastPos = null;
    private int hitboxLastFace = -1;

    private long rotationStartedAt = 0L;
    private long rotationDuration = 50L;
    private float rotationStartYaw = 0.0f;
    private float rotationStartPitch = 0.0f;
    private float rotationTargetYaw = 0.0f;
    private float rotationTargetPitch = 0.0f;
    private float scriptedRotationYaw = 0.0f;
    private float scriptedRotationPitch = 0.0f;
    private boolean rotationActive = false;

    private int rotationStepCounter = 0;
    private int currentClientTick = 0;
    private int lastSuccessfulPlaceTick = 0;
    private long totalC08Counter = 0L;
    private boolean silentPitchActive = false;
    private float silentPitch = 0f;
    private Object[] cachedCandidate = null;
    private int[] lastPlacedPos = null;
    private int[] lastSupportPos = null;
    private int lastSupportFace = -1;

    public TellyBridge() {
        super("TellyBridge", "神桥", Category.movement);
    }

    @Override
    public void onEnable() {
        armed = true;
        running = false;
        activatePromptAt = 0L;
        promptBrokeAt = 0L;
        setupTick = 0;
        cyclePhase = 0;
        rotationActive = false;
        promptAlpha = 0.0f;
        hitboxLastPos = null;
        lastPlacedPos = null;
        lastSupportPos = null;
        lastSupportFace = -1;
        releaseAllKeys();
    }

    @Override
    public void onDisable() {
        stopAutomation();
        armed = false;
        running = false;
        releaseAllKeys();
        TYClient.rotationManager.resetRotation();
    }

    @EventTarget
    public void onTick(EventTick event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        updateActivationPrompt();

        if (!running) return;

        if (detectManualTakeover()) {
            stopAutomation();
            return;
        }

        EntityPlayer player = mc.thePlayer;
        if (player == null || player.isDead || player.fallDistance > 7.0f) {
            stopAutomation();
            return;
        }

        if (autoSwap.getValue()) {
            handleAutoSwap();
        }

        if (!isHoldingBlock()) {
            stopAutomation();
            return;
        }

        if (running && mc.thePlayer != null && isHoldingBlock()) {
            processAutoPlace();
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (!running || mc.thePlayer == null) return;

        if (setupTick >= 0) {
            if (setupTick < 12) {
                boolean setupJump = setupTick >= 6;
                applyMovement(forwardCurve[19], strafeCurve[19], setupJump, false);
                applyUse(true);

                if (setupTick == 11) {
                    setRotationTarget(baseYaw + yawCurve[19], pitchCurve[19], 50L);
                } else {
                    setRotationTarget(baseYaw, 74.52f, 50L);
                }
                setupTick++;
                return;
            }

            setupTick = -1;
            cyclePhase = 0;
        }

        int phase = cyclePhase;
        int nextPhase = (phase + 1) % yawCurve.length;

        applyMovement(forwardCurve[phase], strafeCurve[phase],
                phase >= 1 && phase <= 19,
                phase == 0 || phase == 1);

        applyUse(phase >= 7);

        setRotationTarget(baseYaw + yawCurve[nextPhase], pitchCurve[nextPhase], 50L);

        cyclePhase = nextPhase;
    }

    @EventTarget
    public void onStrafe(EventStrafe event) {
        if (!running || mc.thePlayer == null) return;
        event.setYaw(scriptedRotationYaw);
    }

    @EventTarget
    public void onJump(EventJump event) {
        if (!running || mc.thePlayer == null) return;
        event.setYaw(scriptedRotationYaw);
    }

    @EventTarget
    public void onMoveInput(EventMoveInput event) {
    }

    @EventTarget
    public void onMotion(EventMotion event) {
        if (!running || mc.thePlayer == null) return;

        if (event.isPre()) {
            event.setYaw(scriptedRotationYaw);
            event.setPitch(MathHelper.clamp_float(scriptedRotationPitch, -90, 90));

            mc.thePlayer.rotationYawHead = scriptedRotationYaw;
            mc.thePlayer.renderYawOffset = scriptedRotationYaw;
        }
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        drawActivationPrompt();
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {
        if (!showActivation.getValue()) return;
        if (!armed || running) return;
        if (promptAlpha < 0.05f) return;

        if (activatePromptAt != 0L) {
            Object[] hit = raycastBlock(4.5);
            if (hit != null && hit.length >= 3 && hit[0] != null) {
                int face = faceFromName((String) hit[2]);
                if (face >= 2) {
                    hitboxLastPos = posFromVec((Vec3) hit[0]);
                    hitboxLastFace = face;
                }
            }
        }

        if (hitboxLastPos == null || hitboxLastFace < 2) return;
        drawActivationBox(hitboxLastPos, hitboxLastFace);
    }

    @EventTarget
    public void onPacketSend(EventPacket event) {
        if (event.getEventType() != EventPacket.EventState.SEND) return;
        if (!running) return;

        if (event.getPacket() instanceof C03PacketPlayer) {
            C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();
            if (packet.rotating) {
                packet.yaw = scriptedRotationYaw;
                packet.pitch = scriptedRotationPitch;
            }
        }

        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            C08PacketPlayerBlockPlacement place = (C08PacketPlayerBlockPlacement) event.getPacket();
            if (place.getPlacedBlockDirection() != 255) {
                BlockPos pos = place.getPosition();
                lastPlacedPos = new int[]{pos.getX(), pos.getY(), pos.getZ()};
            }
        }
    }

    private void updateActivationPrompt() {
        if (mc.thePlayer == null) return;

        boolean lookingDown = mc.thePlayer.rotationPitch >= 75.0f;
        boolean atEdge = lookingDown && isLookingAtEdge();

        if (mc.gameSettings.keyBindSneak.pressed && atEdge) {
            if (activatePromptAt == 0L) {
                activatePromptAt = System.currentTimeMillis();
            }

            long elapsed = System.currentTimeMillis() - activatePromptAt;
            promptFadeRgb = elapsed >= activationWait.getValue().longValue() ? 0x55FF55 : 0xFF5555;

            if (elapsed >= activationWait.getValue().longValue() &&
                    mc.gameSettings.keyBindUseItem.pressed) {
                beginAutomation();
                activatePromptAt = 0L;
                promptBrokeAt = 0L;
                return;
            }
            return;
        }

        if (activatePromptAt != 0L) {
            if (promptBrokeAt == 0L) {
                promptBrokeAt = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - promptBrokeAt > 300L) {
                activatePromptAt = 0L;
                promptBrokeAt = 0L;
            }
        }

        boolean show = armed && !running && activatePromptAt != 0L;
        long now = System.currentTimeMillis();
        long elapsedFade = promptFadeLastAt == 0L ? 0L : Math.min(100L, now - promptFadeLastAt);
        promptFadeLastAt = now;
        float step = elapsedFade / 200.0f;
        promptAlpha += show ? step : -step;
        promptAlpha = Math.max(0, Math.min(1, promptAlpha));
    }

    private boolean isLookingAtEdge() {
        if (mc.thePlayer == null) return false;

        float yaw = mc.thePlayer.rotationYaw;
        float nearestDiagonal = Math.round((yaw - 45.0f) / 90.0f) * 90.0f + 45.0f;
        if (Math.abs(wrapAngle(yaw - nearestDiagonal)) > 2.0f) return false;

        Object[] hit = raycastBlock(4.5);
        if (hit == null || hit.length < 3) return false;

        int face = faceFromName((String) hit[2]);
        if (face < 2) return false;

        Vec3 localHit = (Vec3) hit[1];
        if (localHit == null) return false;

        double acrossFace = (face == 4 || face == 5) ? localHit.zCoord : localHit.xCoord;
        if (face == 3 || face == 4) acrossFace = 1.0 - acrossFace;

        if (acrossFace < 0.38 || acrossFace > 0.65) return false;
        if (localHit.yCoord < 0.25 || localHit.yCoord > 0.75) return false;

        int[] pos = posFromVec((Vec3) hit[0]);
        int[] travel = travelDirection(yaw);

        int travelFace = travel[0] > 0 ? 5 : travel[0] < 0 ? 4 : travel[1] > 0 ? 3 : 2;
        if (face != travelFace) return false;

        int aheadX = pos[0] + travel[0];
        int aheadZ = pos[2] + travel[1];
        if (!isReplaceable(aheadX, pos[1] + 1, aheadZ)) return false;

        return true;
    }

    private int[] travelDirection(float yaw) {
        double radians = Math.toRadians(yaw);
        double rawX = Math.sin(radians) - Math.cos(radians);
        double rawZ = -Math.cos(radians) - Math.sin(radians);
        if (Math.abs(rawX) >= Math.abs(rawZ)) {
            return new int[]{rawX >= 0.0 ? 1 : -1, 0};
        }
        return new int[]{0, rawZ >= 0.0 ? 1 : -1};
    }

    private void beginAutomation() {
        if (mc.thePlayer == null) return;

        if (!isHoldingBlock()) {
            return;
        }

        armed = false;
        running = true;
        setupTick = 0;
        cyclePhase = 0;
        baseYaw = mc.thePlayer.rotationYaw;

        scriptedRotationYaw = mc.thePlayer.rotationYaw;
        scriptedRotationPitch = mc.thePlayer.rotationPitch;

        releaseAllKeys();
        mc.gameSettings.keyBindUseItem.pressed = true;

        setRotationTarget(baseYaw, 74.52f, 50L);
    }

    private void stopAutomation() {
        running = false;
        setupTick = 0;
        cyclePhase = 0;
        rotationActive = false;

        releaseAllKeys();
        TYClient.rotationManager.resetRotation();

        armed = true;
        activatePromptAt = 0L;
        promptBrokeAt = 0L;
    }

    private void setRotationTarget(float targetYaw, float targetPitch, long duration) {
        if (mc.thePlayer == null) return;

        rotationStartYaw = scriptedRotationYaw;
        rotationStartPitch = scriptedRotationPitch;

        rotationStepCounter++;
        targetYaw += (float) (SENSITIVITY_QUANTUM * new int[]{0, 1, -1, 2, -2}[rotationStepCounter % 5]);

        rotationTargetYaw = rotationStartYaw + wrapAngle(targetYaw - rotationStartYaw);
        rotationTargetPitch = MathHelper.clamp_float(targetPitch, -90, 90);
        rotationStartedAt = System.currentTimeMillis();
        rotationDuration = Math.max(1L, duration);
        rotationActive = true;
    }

    private void applyMovement(float forward, float strafe, boolean jump, boolean sprint) {
        mc.gameSettings.keyBindForward.pressed = forward > 0.03f;
        mc.gameSettings.keyBindBack.pressed = forward < -0.03f;
        mc.gameSettings.keyBindLeft.pressed = strafe > 0.5f;
        mc.gameSettings.keyBindRight.pressed = strafe < -0.5f;

        mc.thePlayer.movementInput.moveForward = forward;
        mc.thePlayer.movementInput.moveStrafe = strafe;

        if (jump) {
            mc.thePlayer.movementInput.jump = true;
        }

        if (sprint) {
            mc.thePlayer.setSprinting(true);
        }
    }

    private void applyUse(boolean pressed) {
        mc.gameSettings.keyBindUseItem.pressed = pressed;
    }

    private void releaseAllKeys() {
        mc.gameSettings.keyBindForward.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        mc.gameSettings.keyBindBack.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode());
        mc.gameSettings.keyBindLeft.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode());
        mc.gameSettings.keyBindRight.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
        mc.gameSettings.keyBindJump.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
        mc.gameSettings.keyBindSneak.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());
        mc.gameSettings.keyBindUseItem.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindUseItem.getKeyCode());
    }

    private boolean isHoldingBlock() {
        ItemStack stack = mc.thePlayer.getHeldItem();
        return stack != null && stack.getItem() instanceof ItemBlock;
    }

    private void handleAutoSwap() {
        if (mc.thePlayer == null) return;

        ItemStack held = mc.thePlayer.getHeldItem();
        int heldCount = held != null && isHoldingBlock() ? held.stackSize : 0;
        if (heldCount > 5) return;

        int bestSlot = -1;
        int bestSize = heldCount;
        for (int slot = 0; slot <= 8; slot++) {
            if (slot == mc.thePlayer.inventory.currentItem) continue;
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
            if (stack == null || !(stack.getItem() instanceof ItemBlock)) continue;
            if (stack.stackSize > bestSize) {
                bestSize = stack.stackSize;
                bestSlot = slot;
            }
        }

        if (bestSlot != -1) {
            mc.thePlayer.inventory.currentItem = bestSlot;
        }
    }

    private boolean detectManualTakeover() {
        if (mc.thePlayer == null || setupTick < 0) return false;
        float yawDiff = Math.abs(wrapAngle(mc.thePlayer.rotationYaw - scriptedRotationYaw));
        return yawDiff > 5.0f;
    }

    private boolean isReplaceable(int x, int y, int z) {
        Block block = mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
        for (String name : REPLACEABLE_BLOCKS) {
            if (block.getLocalizedName().toLowerCase().contains(name)) return true;
        }
        return block == Blocks.air || block == Blocks.water || block == Blocks.flowing_water ||
                block == Blocks.lava || block == Blocks.flowing_lava;
    }

    private Object[] raycastBlock(double range) {
        if (mc.thePlayer == null) return null;

        Vec3 eyes = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 look = mc.thePlayer.getLookVec();
        Vec3 end = eyes.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range);

        MovingObjectPosition mop = mc.theWorld.rayTraceBlocks(eyes, end, false, true, false);

        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return null;

        BlockPos pos = mop.getBlockPos();
        Vec3 hitVec = mop.hitVec;
        EnumFacing face = mop.sideHit;

        return new Object[]{
                new Vec3(pos.getX(), pos.getY(), pos.getZ()),
                new Vec3(hitVec.xCoord - pos.getX(), hitVec.yCoord - pos.getY(), hitVec.zCoord - pos.getZ()),
                face.getName()
        };
    }

    private int faceFromName(String name) {
        if (name == null) return -1;
        String upper = name.toUpperCase();
        if (upper.equals("DOWN")) return 0;
        if (upper.equals("UP")) return 1;
        if (upper.equals("NORTH")) return 2;
        if (upper.equals("SOUTH")) return 3;
        if (upper.equals("WEST")) return 4;
        if (upper.equals("EAST")) return 5;
        return -1;
    }

    private int[] posFromVec(Vec3 vec) {
        return new int[]{floor(vec.xCoord), floor(vec.yCoord), floor(vec.zCoord)};
    }

    private int floor(double value) {
        int i = (int) value;
        return value < i ? i - 1 : i;
    }

    private float wrapAngle(float angle) {
        angle = angle % 360f;
        if (angle >= 180f) angle -= 360f;
        if (angle < -180f) angle += 360f;
        return angle;
    }

    private void drawActivationPrompt() {
        if (promptAlpha < 0.05f) return;

        int displayWidth = mc.displayWidth;
        int displayHeight = mc.displayHeight;

        String text = "Activate?";
        int alpha = (int) (promptAlpha * 255.0f);
        if (alpha < 16) alpha = 16;
        int color = (alpha << 24) | promptFadeRgb;

        float x = displayWidth / 2.0f - mc.fontRendererObj.getStringWidth(text) / 2.0f;
        float y = displayHeight / 2.0f + 10.0f;

        mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
    }

    private void drawActivationBox(int[] pos, int face) {
        if (mc.getRenderManager() == null) return;

        double x = pos[0];
        double y = pos[1];
        double z = pos[2];

        double yMin = y + 0.25;
        double yMax = y + 0.75;
        double x1, z1, x2, z2;

        if (face == 5) {
            x1 = x + 1.005;
            x2 = x1;
            z1 = z + 0.38;
            z2 = z + 0.65;
        } else if (face == 4) {
            x1 = x - 0.005;
            x2 = x1;
            z1 = z + (1.0 - 0.65);
            z2 = z + (1.0 - 0.38);
        } else if (face == 3) {
            z1 = z + 1.005;
            z2 = z1;
            x1 = x + (1.0 - 0.65);
            x2 = x + (1.0 - 0.38);
        } else {
            z1 = z - 0.005;
            z2 = z1;
            x1 = x + 0.38;
            x2 = x + 0.65;
        }

        int r = (promptFadeRgb >> 16) & 0xFF;
        int g = (promptFadeRgb >> 8) & 0xFF;
        int b = promptFadeRgb & 0xFF;
        int fillAlpha = (int) (60.0f * promptAlpha);
        int lineAlpha = (int) (220.0f * promptAlpha);

        if (fillAlpha < 4) fillAlpha = 4;
        if (lineAlpha < 16) lineAlpha = 16;

        double renderX = x - mc.getRenderManager().viewerPosX;
        double renderY = y - mc.getRenderManager().viewerPosY;
        double renderZ = z - mc.getRenderManager().viewerPosZ;

        RenderUtil.drawFilledBox(
                renderX + x1, renderY + yMin, renderZ + z1,
                renderX + x2, renderY + yMax, renderZ + z2,
                new Color(r, g, b, fillAlpha)
        );

        RenderUtil.drawBoundingBox(
                renderX + x1, renderY + yMin, renderZ + z1,
                renderX + x2, renderY + yMax, renderZ + z2,
                2.0f,
                new Color(r, g, b, lineAlpha)
        );
    }

    private void processAutoPlace() {
        if (mc.thePlayer == null) return;
        if (!isHoldingBlock()) return;

        currentClientTick = mc.thePlayer.ticksExisted;

        float yaw = scriptedRotationYaw;
        float pitch = scriptedRotationPitch;

        Object[] candidate = findPlacementCandidate(yaw, pitch);

        if (candidate == null) {
            return;
        }

        int[] supportPos = (int[]) candidate[1];
        int face = (Integer) candidate[2];
        Vec3 hitVec = (Vec3) candidate[3];

        if (!isSupportAvailable(supportPos[0], supportPos[1], supportPos[2])) return;

        long counterBefore = totalC08Counter;
        boolean placed = mc.playerController.onPlayerRightClick(
                mc.thePlayer,
                mc.theWorld,
                mc.thePlayer.getHeldItem(),
                new BlockPos(supportPos[0], supportPos[1], supportPos[2]),
                EnumFacing.getFront(face),
                hitVec
        );

        if (placed && totalC08Counter > counterBefore) {
            lastSuccessfulPlaceTick = currentClientTick;
            int[] placedPos = offsetPos(supportPos, face);
            lastPlacedPos = placedPos;
            lastSupportPos = supportPos;
            lastSupportFace = face;
            mc.thePlayer.swingItem();
        }
    }

    private Object[] findPlacementCandidate(float yaw, float pitch) {
        Object[] traced = rayCast(yaw, pitch);
        if (traced != null) {
            int[] supportPos = (int[]) traced[0];
            int face = (Integer) traced[1];
            int[] targetPos = offsetPos(supportPos, face);
            if (isReplaceable(targetPos[0], targetPos[1], targetPos[2])) {
                return new Object[]{pitch, supportPos, face, (Vec3) traced[2], targetPos};
            }
        }

        Vec3 pos = mc.thePlayer.getPositionVector();
        int feetX = floor(pos.xCoord);
        int feetY = floor(pos.yCoord) - 1;
        int feetZ = floor(pos.zCoord);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                int[] targetPos = new int[]{feetX + dx, feetY, feetZ + dz};
                if (!isReplaceable(targetPos[0], targetPos[1], targetPos[2])) continue;

                for (int placeFace : ALLOWED_PLACE_FACES) {
                    int[] supportPos = offsetPos(targetPos, opposite(placeFace));
                    if (!isSupportAvailable(supportPos[0], supportPos[1], supportPos[2])) continue;

                    double[] offsets = FACE_HIT_OFFSETS;
                    for (double ox : offsets) {
                        for (double oy : offsets) {
                            Vec3 hitVec = getSupportFaceHitVec(supportPos, placeFace, ox, oy);
                            Float computedPitch = computePitchToHitVec(hitVec);
                            if (computedPitch == null) continue;

                            Object[] verified = rayCast(yaw, computedPitch);
                            if (verified == null) continue;
                            int[] vSupport = (int[]) verified[0];
                            int vFace = (Integer) verified[1];
                            if (!posEquals(vSupport, supportPos) || vFace != placeFace) continue;
                            int[] vTarget = offsetPos(vSupport, vFace);
                            if (!posEquals(vTarget, targetPos)) continue;

                            return new Object[]{computedPitch, supportPos, placeFace, hitVec, targetPos};
                        }
                    }
                }
            }
        }
        return null;
    }

    private Float computePitchToHitVec(Vec3 hitVec) {
        if (mc.thePlayer == null) return null;
        Vec3 eyes = mc.thePlayer.getPositionEyes(1.0f);
        double dx = hitVec.xCoord - eyes.xCoord;
        double dy = hitVec.yCoord - eyes.yCoord;
        double dz = hitVec.zCoord - eyes.zCoord;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontal)));
        return Math.max(-89.0f, Math.min(89.0f, pitch));
    }

    private Vec3 getSupportFaceHitVec(int[] supportPos, int face, double primary, double secondary) {
        double p = Math.max(0.001, Math.min(0.999, primary));
        double s = Math.max(0.001, Math.min(0.999, secondary));
        if (face == 2) return new Vec3(supportPos[0] + p, supportPos[1] + s, supportPos[2] + 0.001);
        if (face == 3) return new Vec3(supportPos[0] + p, supportPos[1] + s, supportPos[2] + 0.999);
        if (face == 5) return new Vec3(supportPos[0] + 0.999, supportPos[1] + p, supportPos[2] + s);
        if (face == 4) return new Vec3(supportPos[0] + 0.001, supportPos[1] + p, supportPos[2] + s);
        if (face == 0) return new Vec3(supportPos[0] + p, supportPos[1] + 0.001, supportPos[2] + s);
        return new Vec3(supportPos[0] + p, supportPos[1] + 0.999, supportPos[2] + s);
    }

    private boolean isSupportAvailable(int x, int y, int z) {
        Block block = mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
        return block != Blocks.air && !isReplaceable(x, y, z);
    }

    private int[] offsetPos(int[] pos, int face) {
        if (face == 0) return new int[]{pos[0], pos[1] - 1, pos[2]};
        if (face == 1) return new int[]{pos[0], pos[1] + 1, pos[2]};
        if (face == 2) return new int[]{pos[0], pos[1], pos[2] - 1};
        if (face == 3) return new int[]{pos[0], pos[1], pos[2] + 1};
        if (face == 4) return new int[]{pos[0] - 1, pos[1], pos[2]};
        if (face == 5) return new int[]{pos[0] + 1, pos[1], pos[2]};
        return pos;
    }

    private int opposite(int face) {
        if (face == 0) return 1;
        if (face == 1) return 0;
        if (face == 2) return 3;
        if (face == 3) return 2;
        if (face == 4) return 5;
        return 4;
    }

    private boolean posEquals(int[] a, int[] b) {
        if (a == null || b == null) return false;
        return a[0] == b[0] && a[1] == b[1] && a[2] == b[2];
    }

    private Object[] rayCast(float yaw, float pitch) {
        if (mc.thePlayer == null || mc.theWorld == null) return null;

        double range = 4.5;
        Vec3 eyes = mc.thePlayer.getPositionEyes(1.0f);

        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        float cosPitch = MathHelper.cos(pitchRad);
        Vec3 lookVec = new Vec3(
                -MathHelper.sin(yawRad) * cosPitch,
                -MathHelper.sin(pitchRad),
                MathHelper.cos(yawRad) * cosPitch
        );

        Vec3 end = eyes.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);
        MovingObjectPosition mop = mc.theWorld.rayTraceBlocks(eyes, end, false, true, false);

        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return null;

        BlockPos pos = mop.getBlockPos();
        Vec3 hitVec = mop.hitVec;
        EnumFacing face = mop.sideHit;

        return new Object[]{
                new int[]{pos.getX(), pos.getY(), pos.getZ()},
                faceFromName(face.getName()),
                new Vec3(hitVec.xCoord - pos.getX(), hitVec.yCoord - pos.getY(), hitVec.zCoord - pos.getZ())
        };
    }
}
