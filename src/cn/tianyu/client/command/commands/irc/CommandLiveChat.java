//package cn.tianyu.client.command.commands.irc;
//
//import cn.tianyu.client.command.Command;
//import cn.tianyu.client.util.impl.client.DebugUtil;
//import org.apache.commons.lang3.StringUtils;
//
//public class CommandLiveChat extends Command {
//    public CommandLiveChat() {
//        super(new String[]{"i"});
//    }
//
//    @Override
//    public void run(String[] args) {
//        if (!LiveClient.INSTANCE.isActive()) {
//            DebugUtil.Clientlog("无法发送消息: 与Live无连接");
//            return;
//        }
//
//        if (args.length == 0) {
//            DebugUtil.Clientlog("-i <message>");
//            return;
//        }
//
//        LiveClient.INSTANCE.sendPacket(LiveProto.createChat(StringUtils.join(args, " ")));
//    }
//}
