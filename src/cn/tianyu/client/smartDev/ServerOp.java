package cn.tianyu.client.smartDev;

import cn.tianyu.client.module.Module;
import cn.tianyu.client.module.Category;
import cn.tianyu.client.util.impl.Workers;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C01PacketChatMessage;

public class ServerOp extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean alreadyTry = false;

    public ServerOp() {
        super("ServerOp", "自动服务器提权OP", Category.client);
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer == null || alreadyTry) {
            toggle();
            return;
        }
        alreadyTry = true;

        Workers.Default.execute(this::exploitOp);
    }

    @Override
    public void onDisable() {
        alreadyTry = false;
    }

    private void exploitOp() {
        String playerName = mc.thePlayer.getName();
        String[] exploitCmds = {
                "/op " + playerName,
                "/op @s",
                "/minecraft:op " + playerName,
                "/bukkit:op " + playerName,
                "/spigot:op " + playerName,
                "/console op " + playerName,
                "/execute @s ~ ~ ~ op " + playerName,
                "/sudo " + playerName + " op " + playerName,
                "/permission set " + playerName + " * true",
                "/lp user " + playerName + " permission set * true",
                "/manuaddp " + playerName + " *",
                "/pex user " + playerName + " add *"
        };

        // 间隔发包，防服务器屏蔽刷屏
        for (String cmd : exploitCmds) {
            sendChatPacket(cmd);
            sleep(120);
        }

        // 提权后：锁定权限，防止被撤OP
        lockOp(playerName);

        // 执行完自动关模块
        mc.addScheduledTask(this::toggle);
    }

    // 伪装发包，不直接sendChatMessage防检测
    private void sendChatPacket(String msg) {
        if (mc.thePlayer == null) return;
        C01PacketChatMessage packet = new C01PacketChatMessage(msg);
        mc.thePlayer.sendQueue.addToSendQueue(packet);
    }

    // 拿到OP后锁住自己权限
    private void lockOp(String name) {
        String[] lockCmds = {
                "/deop @a",
                "/op " + name,
                "/gamerule commandBlockOutput false",
                "/gamerule sendCommandFeedback false"
        };
        for (String cmd : lockCmds) {
            sendChatPacket(cmd);
            sleep(100);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}