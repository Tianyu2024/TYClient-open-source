package cn.tianyu.client.command.commands;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.command.Command;
import cn.tianyu.client.module.Module;
import cn.tianyu.client.util.impl.client.DebugUtil;
import net.minecraft.util.EnumChatFormatting;

import org.lwjglx.input.Keyboard;

public class bindCommand extends Command {
    public bindCommand() {
        super(new String[]{"bind","b"}); // 命令名称
    }

    @Override
    public void run(String[] args) {
        if (args.length == 2) {
            Module m = TYClient.moduleManager.getModule(args[0]);
            if (m != null) {
                int key = Keyboard.getKeyIndex(args[1].toUpperCase());
                m.setKey(key);
                DebugUtil.log(EnumChatFormatting.GREEN + "Success bound " + m.getName() + " to " + Keyboard.getKeyName(m.getKey()) + "!");
            } else {
                DebugUtil.log(EnumChatFormatting.RED + "Module not found!");
            }
        } else {
            DebugUtil.log(EnumChatFormatting.RED + "Usage: bind <Module> <Key>");
        }
    }
}