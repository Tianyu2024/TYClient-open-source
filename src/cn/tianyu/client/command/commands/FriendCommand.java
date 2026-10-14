package cn.tianyu.client.command.commands;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.command.Command;
import cn.tianyu.client.util.impl.client.DebugUtil;

/**
 * @Author: CN_tianyu
 */
public class FriendCommand extends Command {
    
    public FriendCommand() {
        super(new String[]{"friend", "f", "fr"});
    }

    @Override
    public void run(final String[] arguments) {
        if (arguments.length == 1) {
            DebugUtil.Clientlog("Usage: " + getUsage());
            return;
        }
        final String lowerCase = arguments[1].toLowerCase();
        if (arguments.length == 2) {
            switch (lowerCase) {
                case "clear": {
                    DebugUtil.Clientlog("Cleared all friended players");
                    TYClient.instance.getFriendManager().getFriends().clear();
                    break;
                }
                case "list": {
                    if (!TYClient.instance.getFriendManager().getFriends().isEmpty()) {
                        DebugUtil.Clientlog("Friend§7[§f" + TYClient.instance.getFriendManager().getFriends().size() + "§7]§f : §a" + TYClient.instance.getFriendManager().getFriendsName());
                        break;
                    }
                    DebugUtil.Clientlog("The friend list is empty");
                    break;
                }
            }
        } else {
            if (arguments.length != 3) {
                DebugUtil.log("friend add <name> | remove <name> | list | clear");
            }
            if (arguments[2].contains(TYClient.mc.thePlayer.getName())) {
                DebugUtil.Clientlog("§c§lNO");
                return;
            }
            final String lowerCase2 = arguments[1].toLowerCase();
            switch (lowerCase2) {
                case "add": {
                    DebugUtil.Clientlog("§b" + arguments[2] + " §7has been §2friended");
                    TYClient.instance.getFriendManager().add(arguments[2]);
                    break;
                }
                case "remove": {
                    DebugUtil.Clientlog("§b" + arguments[2] + " §7has been §2unfriended");
                    TYClient.instance.getFriendManager().remove(arguments[2]);
                    break;
                }
            }
        }
    }

    public String getUsage() {
        return "friend add <name> | remove <name> | list | clear";
    }
}
