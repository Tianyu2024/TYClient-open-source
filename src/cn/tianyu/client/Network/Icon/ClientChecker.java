package cn.tianyu.client.Network.Icon;

import cn.tianyu.client.TYClient;
import com.yumegod.obfuscation.Native;
import com.yumegod.obfuscation.StringObfuscate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

@Native
@StringObfuscate
//你想用就用，反正我写的很烂很老
public class ClientChecker {

    private static final String SERVER_IP = TYClient.instance.netWorkManager.getServerip(); // 服务器IP
    private static final int SERVER_PORT = 25412; // 服务器端口

    private static final Set<String> onlinePlayers = new HashSet<>(); // 在线玩家列表


    public static boolean isUsingMyClient(String playerName) {
        return onlinePlayers.contains(playerName);
    }

    /**
     * 启动客户端，发送玩家名并接收在线玩家列表
     */
    public static void startClient(String playerName) {
//        new Thread(() -> {
//            while (true) {
//                try (final Socket socket = new Socket(SERVER_IP, SERVER_PORT);
//                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
//                    // 发送玩家名到服务器
//                    if(playerName != null){
//                        out.println(playerName);
//                    }
//
//                    String response = in.readLine();
//                    if (response != null) {
//                        updateOnlinePlayers(response);
//                    }
//
//                    Thread.sleep(5000);
//                } catch (IOException | InterruptedException e) {
//                    e.printStackTrace();
//                    break;
//                }
//            }
//        }).start();
    }

//    public static void exit() {
//        try {
//            socket.close();
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private static void updateOnlinePlayers(String response) {
        onlinePlayers.clear();
        for (String name : response.split(",")) {
            onlinePlayers.add(name.trim()); // 去除空格
        }
    }

    public static Set<String> getOnlinePlayers() {
        return new HashSet<>(onlinePlayers); // 返回副本以避免直接修改
    }

//    public static void sb() {
//        while (true){
//            if(mc.thePlayer == null){
//                ClientChecker.exit();
//                return;
//            }
//        }
//    }
}