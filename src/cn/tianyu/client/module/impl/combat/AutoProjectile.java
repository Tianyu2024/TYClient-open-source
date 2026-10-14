package cn.tianyu.client.module.impl.combat;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.game.EventMotion;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.event.events.player.EventMoveInput;
import cn.tianyu.client.event.events.player.UpdateEvent2;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.combat.KillAura;
import cn.tianyu.client.module.impl.misc.Teams;
import cn.tianyu.client.util.impl.client.PacketUtils;
import cn.tianyu.client.util.impl.player.ItemUtil;
import cn.tianyu.client.util.impl.player.MoveUtil;
import cn.tianyu.client.util.impl.player.RotationUtil;
import cn.tianyu.client.util.impl.player.TimerUtil;
import cn.tianyu.client.value.impl.BooleanValue;
import cn.tianyu.client.value.impl.NumberValue;
import de.gerrygames.viarewind.utils.PacketUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * @Author: CN_tianyu
 */
public class AutoProjectile extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final NumberValue cooldown = new NumberValue("Delay", 500, 0, 2000, 100);
    public final NumberValue maxRange = new NumberValue("max-range", 20.0F, 3.0F, 64.0F, 1.0F);
    private final TimerUtil timer = new TimerUtil();

    public AutoProjectile() {
        super("AutoProjectile", "", Category.combat);
    }

    @EventTarget
    public void onUpdate(UpdateEvent2 event) {
        if (!this.isEnabled() || mc.thePlayer == null) return;
        if (event.getType() != UpdateEvent2.EventType.PRE) return;

        KillAura killAura = (KillAura) TYClient.getModuleManager().getModule(KillAura.class);
        if (killAura == null || !killAura.isEnabled()) return;

        EntityLivingBase target = killAura.getTarget();
        if (target == null) return;

        double distance = mc.thePlayer.getDistanceToEntity(target);
        if (distance > killAura.range.getValue() && distance <= maxRange.getValue()) {
            int projectileCount = ItemUtil.findInventorySlot(ItemUtil.ItemType.Projectile);
            if (projectileCount > 0 && timer.hasTimeElapsed(cooldown.getValue().longValue())) {
                int projectileSlot = findProjectileHotbarSlot();
                if (projectileSlot != -1) {
                    float[] rotations = RotationUtil.getRotationsToBox(
                            target.getEntityBoundingBox(),
                            event.getYaw(),
                            event.getPitch(),
                            180.0F,
                            0.0F
                    );
                    event.setRotation(rotations[0], rotations[1], 1);
                    int originalSlot = mc.thePlayer.inventory.currentItem;
                    if (projectileSlot != originalSlot) {
                        PacketUtils.sendPacket(new C09PacketHeldItemChange(projectileSlot));
                    }
                    PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(projectileSlot)));
                    if (projectileSlot != originalSlot) {
                        PacketUtils.sendPacket(new C09PacketHeldItemChange(originalSlot));
                    }

                    timer.reset();
                }
            }
        }
    }

    private int findProjectileHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (ItemUtil.isProjectile(stack)) {
                return i;
            }
        }
        return -1;
    }

    public String getSuffix() {
        int count = ItemUtil.findInventorySlot(ItemUtil.ItemType.Projectile);
        return String.valueOf(count);
    }
}