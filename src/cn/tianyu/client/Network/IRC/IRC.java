package cn.tianyu.client.Network.IRC;

import cn.tianyu.client.Network.NetWorkManager;
import cn.tianyu.client.TYClient;
import cn.tianyu.client.gui.ChooseGui;
import cn.tianyu.client.util.impl.client.DebugUtil;
import com.yumegod.obfuscation.*;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static cn.tianyu.client.TYClient.mc;
import static java.lang.Thread.sleep;

/**
 * @Author: CN_tianyu
 */

@CallEncryption
@VMProtectMutation
public class IRC {
    public static Socket socket;
    public static PrintWriter writer;
    public static BufferedReader reader;
    public static Boolean isstart = false;
    public static Boolean sb = false;
    @Setter
    @Getter
    public static int port = 11451;
    @Getter
    public static String serverip = TYClient.instance.netWorkManager.getDebugServerip();
    public static final byte[] CRYPT_KEY = "TYClient2026IRCSecretKey11451".getBytes();
    private static final ConcurrentHashMap<String, String> identityCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, CountDownLatch> queryLatchMap = new ConcurrentHashMap<>();

    public static void start(){
        new Thread(() -> {
            try{
                socket = new Socket(serverip,port);
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                startIRC();
            } catch (IOException e) {
                System.out.println("can not login to IRC Server");
                TYClient.stop();
                mc.shutdown();
            }
        }).start();
    }

    public static void startIRC() {
        isstart = true;
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (mc.thePlayer != null) {
                    String message;
                    try {
                        message = reader.readLine();
                        if (message == null) {
                            break;
                        }
                        String rawMsg = decrypt(message);
                        try {
                            logIRC(rawMsg);
                        } catch (Exception ignored) {}
                    } catch (IOException e) {
                        break;
                    }
                }
                sleep(50);
            }
        } catch (InterruptedException e) {
            System.out.println("IRC 炸了!");
            stopIRC();
        }
    }

    public static void logIRC(String m) {
        if (m == null || m.isEmpty()) {
            return;
        }
        if(m.startsWith("IDRESP|")){
            String respBody = m.substring("IDRESP|".length());
            String[] respSplit = respBody.split("||");
            if(respSplit.length == 2){
                String queryPlayer = respSplit[0];
                String identityName = respSplit[1];
                identityCache.put(queryPlayer, identityName);
                CountDownLatch latch = queryLatchMap.get(queryPlayer);
                if(latch != null){
                    latch.countDown();
                }
            }
            return;
        }
        int dotIndex = m.indexOf('.');
        if (dotIndex <= 0 || dotIndex == m.length() - 1) {
            DebugUtil.log(m);
            return;
        }
        String currentPlayer = m.substring(0, dotIndex);
        String currentMessage = m.substring(dotIndex + 1);
        if (currentMessage != null && currentPlayer != null && mc.thePlayer != null) {
            String tempId = identityCache.getOrDefault(currentPlayer,"普通用户");
            DebugUtil.logIRC(" " + tempId + " " + currentPlayer, currentMessage);
            new Thread(()->{
                try{
                    queryTargetIdentity(currentPlayer);
                }catch (Exception e){}
            }).start();
        }
    }

    public static void send(String m){
        String encryptStr = encrypt(m);
        writer.println(encryptStr);
    }

    public static void sendRawMessage(String hexStr){
        writer.println(hexStr);
    }

    public static String encrypt(String raw) {
        byte[] data = raw.getBytes();
        byte[] key = CRYPT_KEY;
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return bytesToHex(data);
    }

    public static String decrypt(String hexStr) {
        if (hexStr == null || hexStr.length() % 2 != 0) return "";
        byte[] data = hexToBytes(hexStr);
        byte[] key = CRYPT_KEY;
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return new String(data);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        byte[] res = new byte[hex.length() / 2];
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return res;
    }

    public static String querySelfIdentity() throws InterruptedException {
        String selfName = DebugUtil.getUsername();
        return queryTargetIdentity(selfName);
    }

    public static String queryTargetIdentity(String targetName) throws InterruptedException {
        identityCache.remove(targetName);
        CountDownLatch newLatch = new CountDownLatch(1);
        CountDownLatch existLatch = queryLatchMap.putIfAbsent(targetName, newLatch);
        if(existLatch != null){
            existLatch.await();
            return identityCache.getOrDefault(targetName, "普通用户");
        }
        String cmd = "GETID|" + targetName;
        send(cmd);
        newLatch.await();
        queryLatchMap.remove(targetName, newLatch);
        return identityCache.getOrDefault(targetName, "普通用户");
    }

    public static void stopIRC(){
        try{
            socket.close();
            reader.close();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}