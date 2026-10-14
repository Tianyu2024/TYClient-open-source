//package cn.tianyu.client.command.commands;
//
//import cn.tianyu.client.Network.api.packet.Packets.I01Devmsg;
//import cn.tianyu.client.Network.api.packet.Packets.I02Usermsg;
//import cn.tianyu.client.TYClient;
//import cn.tianyu.client.command.Command;
//import cn.tianyu.client.module.api.DefaultEnabled;
//import cn.tianyu.client.util.impl.client.DebugUtil;
//import com.yumegod.obfuscation.Native;
//import net.minecraft.util.EnumChatFormatting;
//
//@Native
//public class IRCCommand extends Command {
//
//    public IRCCommand() {
//        super(new String[]{"i","irc"});
//    }
//
//    @Override
//    public void run(String[] args) {
//        if(true){
//            DebugUtil.log("IRC Closed!");
//            return;
//        }
//        if (args.length == 1) {
//            if(DebugUtil.isDev()){
//                I01Devmsg I01Devmsg = new I01Devmsg();
//                TYClient.instance.netWorkManager.sendPacket(I01Devmsg,args[0]);
//            }else {
//                I02Usermsg I02Usermsg = new I02Usermsg();
//                TYClient.instance.netWorkManager.sendPacket(I02Usermsg,args[0]);
//            }
//        } else {
//            DebugUtil.log(EnumChatFormatting.RED + "Usage: .i <movement you say>");
//        }
//    }
//}
