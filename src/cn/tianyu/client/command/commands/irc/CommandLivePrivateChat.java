//package cn.tianyu.client.command.commands.irc;
//
//import cn.tianyu.client.command.Command;
//import cn.tianyu.client.util.impl.client.DebugUtil;
//import org.apache.commons.lang3.StringUtils;
//
//import java.util.Arrays;
//
//public class CommandLivePrivateChat extends Command {
//    public CommandLivePrivateChat() {
//        super(new String[]{"ip"});
//    }
//
//    @Override
//    public void run(String[] args) {
//        if (!LiveClient.INSTANCE.isActive()) {
//            DebugUtil.Clientlog("无法发送消息: 与Live无连接");
//            return;
//        }
//
//        if (args.length < 2) {
//            DebugUtil.Clientlog("-ip <username> <message>");
//            return;
//        }
//
//        final String username = args[0];
//        final String[] messages = Arrays.copyOfRange(args, 1, args.length);
//
//        LiveClient.INSTANCE.sendPacket(LiveProto.createPrivateChat(
//                username,
//                StringUtils.join(messages, " ")
//        ));
//    }
//}
