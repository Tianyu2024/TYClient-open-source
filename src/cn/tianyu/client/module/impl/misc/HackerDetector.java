package cn.tianyu.client.module.impl.misc;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.event.EventTarget;
import cn.tianyu.client.event.events.Packet.EventPacket;
import cn.tianyu.client.event.events.game.EventUpdate;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.impl.misc.hackerdetector.Check;
import cn.tianyu.client.module.impl.misc.hackerdetector.impl.*;
import cn.tianyu.client.value.impl.BooleanValue;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author: CN_tianyu
 */
public class HackerDetector extends Module {

    public HackerDetector(){
        super("HackerDetector","", Category.misc);
        addChecks(
                new AngleCheck(),
                new AutoBlockCheck(),
                new LegitScaffoldCheck(),
                new NoFallCheck(),
                new NoSlowCheck(),
                new ScaffoldCheck(),
                new VelocityCheck(),
                //new MotionCheck();
                new OmniSprintCheck()
        );
    }

    public final BooleanValue angleCheck = new BooleanValue("Angle", false);
    public final BooleanValue autoBlockCheck = new BooleanValue("Auto Block", false);
    //public final BooleanValue motionCheck = new BooleanValue("Invalid motion", false, this);
    public final BooleanValue noFallCheck = new BooleanValue("No Fall", false);
    public final BooleanValue noSlowCheck = new BooleanValue("No Slow", false);
    public final BooleanValue legitScaffoldCheck = new BooleanValue("Legit Scaffold", false);
    public final BooleanValue scaffoldCheck = new BooleanValue("Scaffold", false);
    public final BooleanValue velocityCheck = new BooleanValue("Velocity", false);
    public final BooleanValue omniSprintCheck = new BooleanValue("Omni Sprint", false);
    public final BooleanValue selfCheck = new BooleanValue("Self", false);

    private final Set<EntityPlayer> hackers = new HashSet<>();
    private final ArrayList<Check> checks = new ArrayList<>();

    private boolean isCheckActive(Check check) {
        if (check instanceof AngleCheck) return angleCheck.get();
        if (check instanceof AutoBlockCheck) return autoBlockCheck.get();
        if (check instanceof LegitScaffoldCheck) return legitScaffoldCheck.get();
        if (check instanceof NoFallCheck) return noFallCheck.get();
        if (check instanceof NoSlowCheck) return noSlowCheck.get();
        if (check instanceof ScaffoldCheck) return scaffoldCheck.get();
        if (check instanceof VelocityCheck) return velocityCheck.get();
        if (check instanceof OmniSprintCheck) return omniSprintCheck.get();
        //if (check instanceof MotionCheck) return motionCheck.get();
        return false;
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            for (Check check : checks) {
                if ((selfCheck.get() || player != mc.thePlayer) && !player.isDead && !TYClient.instance.getFriendManager().isFriend(player)) {
                    if(isEnabled(AntiBot.class) && getModule(AntiBot.class).isBot(player))
                        continue;
                    if (isCheckActive(check)) {
                        check.onUpdate(player);
                    }
                }
            }
        }
    }

    @EventTarget
    public final void onPacketReceive(EventPacket event) {
        if (event.getState() != EventPacket.EventState.RECEIVE) return;
        if (event.getPacket() instanceof S14PacketEntity || event.getPacket() instanceof S18PacketEntityTeleport) {
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                for (Check check : checks) {
                    if ((selfCheck.get() || player != mc.thePlayer) && !player.isDead && !TYClient.instance.getFriendManager().isFriend(player)) {
                        if(isEnabled(AntiBot.class) && getModule(AntiBot.class).isBot(player))
                            continue;
                        if (isCheckActive(check)) {
                            check.onPacketReceive(event, player);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        hackers.clear();
    }

    public void addChecks(Check... checks) {
        this.checks.addAll(Arrays.asList(checks));
    }

    public void mark(EntityPlayer ent) {
        hackers.add(ent);
    }

    public boolean isHacker(EntityPlayer ent) {
        for (EntityPlayer hacker : hackers) {
            if (!ent.getName().equals(hacker.getName())) continue;
            return true;
        }
        return false;
    }
}