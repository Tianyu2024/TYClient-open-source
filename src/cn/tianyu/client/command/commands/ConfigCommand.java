package cn.tianyu.client.command.commands;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.command.Command;
import cn.tianyu.client.util.impl.client.DebugUtil;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: CN_tianyu
 */
public class ConfigCommand extends Command {
    public ConfigCommand() {
        super(new String[]{"config", "cfg"});
    }

    @Override
    public void run(String[] args) {
        if (args.length == 2) {
            switch (args[0]) {
                case "save": {
                    String name = args[1];
                    if (!name.isEmpty()) {
                        TYClient.instance.configManager.saveUserConfig(name + ".json");
                        DebugUtil.log(EnumChatFormatting.GREEN + "Config " + name + " Saved!");
                    } else {
                        DebugUtil.log(EnumChatFormatting.RED + "?");
                    }
                    break;
                }

                case "load": {
                    String name = args[1];
                    if (!name.isEmpty()) {
                        TYClient.instance.configManager.loadUserConfig(name + ".json");
                        DebugUtil.log(EnumChatFormatting.GREEN + "Config " + name + " Loaded!");
                    } else {
                        DebugUtil.log(EnumChatFormatting.RED + "?");
                    }
                    break;
                }
            }

        } else {
            DebugUtil.log(EnumChatFormatting.RED + "Usage: config save/load <name>");
        }
    }
}
