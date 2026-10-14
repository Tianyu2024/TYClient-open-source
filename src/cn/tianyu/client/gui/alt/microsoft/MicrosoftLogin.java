package cn.tianyu.client.gui.alt.microsoft;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.EnumChatFormatting;
import org.lwjglx.Sys;

import java.awt.Desktop;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public final class MicrosoftLogin implements Closeable {
    private static final String CLIENT_ID = "67b74668-ef33-49c3-a75c-18cbb2481e0c";
    private static final String REDIRECT_URI = "http://localhost:3434/sad";
    private static final String SCOPE = "XboxLive.signin%20offline_access";
    private static final String RAW_OAUTH_URL = "https://login.live.com/oauth20_authorize.srf?client_id=%s&redirect_uri=%s&response_type=code&display=touch&scope=%s";
    private static final String URL = String.format(RAW_OAUTH_URL, CLIENT_ID, REDIRECT_URI, SCOPE);
    public volatile String uuid = null;
    public volatile String userName = null;
    public volatile String accessToken = null;
    public volatile String refreshToken = null;
    public volatile boolean logged = false;
    public volatile String status = EnumChatFormatting.YELLOW + "Login...";
    private final HttpServer httpServer;
    private final MicrosoftHttpHandler handler;
    private final Gson gson = new Gson();

    public MicrosoftLogin() throws IOException {
        this.handler = new MicrosoftHttpHandler();
        this.httpServer = HttpServer.create(new InetSocketAddress("localhost", 3434), 0);
        this.httpServer.createContext("/sad", this.handler);
        this.httpServer.start();
        Sys.openURL(URL);
    }

    public MicrosoftLogin(String refreshToken) throws IOException {
        this.refreshToken = refreshToken;
        this.httpServer = null;
        this.handler = null;
        String msToken = getMicrosoftTokenFromRefreshToken(refreshToken);
        String xblToken = getXBoxLiveToken(msToken);
        String[] xstsPair = getXSTSTokenAndUserHash(xblToken);
        String mcAccess = getAccessToken(xstsPair[0], xstsPair[1]);
        URL profileUrl = new URL("https://api.minecraftservices.com/minecraft/profile");
        HttpURLConnection connection = (HttpURLConnection) profileUrl.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + mcAccess);
        String read = read(connection.getInputStream());
        connection.disconnect();
        JsonObject jsonObject = JsonParser.parseString(read).getAsJsonObject();
        String uuid = jsonObject.get("id").getAsString();
        String userName = jsonObject.get("name").getAsString();
        this.uuid = uuid;
        this.userName = userName;
        this.accessToken = mcAccess;
        this.logged = true;
    }

    @Override
    public void close() {
        if (this.httpServer != null) {
            this.httpServer.stop(0);
        }
    }

    public void show() throws Exception {
        Desktop.getDesktop().browse(new URI(URL));
    }

    private String getAccessToken(String xstsToken, String uhs) throws IOException {
        this.status = EnumChatFormatting.YELLOW + "Getting access token";
        URL url = new URL("https://api.minecraftservices.com/authentication/login_with_xbox");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        JsonObject input = new JsonObject();
        input.addProperty("identityToken", "XBL3.0 x=" + uhs + ";" + xstsToken);
        String jsonStr = gson.toJson(input);
        write(connection.getOutputStream(), jsonStr);
        String resp = read(connection.getInputStream());
        connection.disconnect();
        JsonObject jsonObject = JsonParser.parseString(resp).getAsJsonObject();
        return jsonObject.get("access_token").getAsString();
    }

    public String getMicrosoftTokenFromRefreshToken(String refreshToken) throws IOException {
        this.status = EnumChatFormatting.YELLOW + "Getting microsoft token from refresh token";
        URL url = new URL("https://login.live.com/oauth20_token.srf");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String param = "client_id=" + CLIENT_ID + "&refresh_token=" + refreshToken + "&grant_type=refresh_token&redirect_uri=" + REDIRECT_URI;
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        write(connection.getOutputStream(), param);
        String resp = read(connection.getInputStream());
        connection.disconnect();
        JsonObject responseObj = JsonParser.parseString(resp).getAsJsonObject();
        return responseObj.get("access_token").getAsString();
    }

    public String[] getMicrosoftTokenAndRefreshToken(String code) throws IOException {
        this.status = EnumChatFormatting.YELLOW + "Getting microsoft token";
        URL url = new URL("https://login.live.com/oauth20_token.srf");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String param = "client_id=" + CLIENT_ID + "&code=" + code + "&grant_type=authorization_code&redirect_uri=" + REDIRECT_URI + "&scope=" + SCOPE;
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        write(connection.getOutputStream(), param);
        String resp = read(connection.getInputStream());
        connection.disconnect();
        JsonObject responseObj = JsonParser.parseString(resp).getAsJsonObject();
        String access = responseObj.get("access_token").getAsString();
        String rt = responseObj.get("refresh_token").getAsString();
        return new String[]{access, rt};
    }

    public String getXBoxLiveToken(String microsoftToken) throws IOException {
        this.status = EnumChatFormatting.YELLOW + "Getting xbox live token";
        URL connectUrl = new URL("https://user.auth.xboxlive.com/user/authenticate");
        JsonObject xblParam = new JsonObject();
        JsonObject xblProperties = new JsonObject();
        xblProperties.addProperty("AuthMethod", "RPS");
        xblProperties.addProperty("SiteName", "user.auth.xboxlive.com");
        xblProperties.addProperty("RpsTicket", "d=" + microsoftToken);
        xblParam.add("Properties", xblProperties);
        xblParam.addProperty("RelyingParty", "http://auth.xboxlive.com");
        xblParam.addProperty("TokenType", "JWT");
        String param = gson.toJson(xblParam);
        HttpURLConnection connection = (HttpURLConnection) connectUrl.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        write(connection.getOutputStream(), param);
        String resp = read(connection.getInputStream());
        connection.disconnect();
        JsonObject responseObj = JsonParser.parseString(resp).getAsJsonObject();
        return responseObj.get("Token").getAsString();
    }

    public String[] getXSTSTokenAndUserHash(String xboxLiveToken) throws IOException {
        this.status = EnumChatFormatting.YELLOW + "Getting xsts token and user hash";
        URL connectUrl = new URL("https://xsts.auth.xboxlive.com/xsts/authorize");
        ArrayList<String> tokens = new ArrayList<>();
        tokens.add(xboxLiveToken);
        JsonObject xblParam = new JsonObject();
        JsonObject xblProperties = new JsonObject();
        xblProperties.addProperty("SandboxId", "RETAIL");
        JsonArray tokenArr = JsonParser.parseString(gson.toJson(tokens)).getAsJsonArray();
        xblProperties.add("UserTokens", tokenArr);
        xblParam.add("Properties", xblProperties);
        xblParam.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        xblParam.addProperty("TokenType", "JWT");
        String param = gson.toJson(xblParam);
        HttpURLConnection connection = (HttpURLConnection) connectUrl.openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoInput(true);
        connection.setDoOutput(true);
        write(connection.getOutputStream(), param);
        String resp = read(connection.getInputStream());
        connection.disconnect();
        JsonObject responseObj = JsonParser.parseString(resp).getAsJsonObject();
        String token = responseObj.get("Token").getAsString();
        String uhs = responseObj.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0).getAsJsonObject().get("uhs").getAsString();
        return new String[]{token, uhs};
    }

    private void write(OutputStream out, String content) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write(content);
        writer.flush();
        writer.close();
        out.close();
    }

    private String read(InputStream stream) throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        stream.close();
        reader.close();
        return stringBuilder.toString();
    }

    public boolean isLogged() {
        return this.logged;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }

    private class MicrosoftHttpHandler implements HttpHandler {
        private boolean got = false;

        private MicrosoftHttpHandler() {}

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String query;
            if (!this.got && (query = httpExchange.getRequestURI().getQuery()).contains("code")) {
                this.got = true;
                String code = query.split("code=")[1];
                String[] msPair = MicrosoftLogin.this.getMicrosoftTokenAndRefreshToken(code);
                String xblToken = MicrosoftLogin.this.getXBoxLiveToken(msPair[0]);
                String[] xstsPair = MicrosoftLogin.this.getXSTSTokenAndUserHash(xblToken);
                String mcAccess = MicrosoftLogin.this.getAccessToken(xstsPair[0], xstsPair[1]);
                URL url = new URL("https://api.minecraftservices.com/minecraft/profile");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoInput(true);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + mcAccess);
                String read = MicrosoftLogin.this.read(connection.getInputStream());
                connection.disconnect();
                JsonObject jsonObject = JsonParser.parseString(read).getAsJsonObject();
                String uuid = jsonObject.get("id").getAsString();
                String userName = jsonObject.get("name").getAsString();
                MicrosoftLogin.this.uuid = uuid;
                MicrosoftLogin.this.userName = userName;
                MicrosoftLogin.this.accessToken = mcAccess;
                MicrosoftLogin.this.refreshToken = msPair[1];
                MicrosoftLogin.this.logged = true;
            }
        }
    }
}