package cn.tianyu.client.config;

import cn.tianyu.client.TYClient;
import cn.tianyu.client.config.configs.HudConfig;
import cn.tianyu.client.config.configs.ModConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.ibm.icu.text.UnicodeSet;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectArrayList;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.walk;

/**
 * @author CN_tianyu
 */

public class ConfigManager {
    private static final List<Config> configs = new ArrayList<Config>() {
        {
            add(new ModConfig());
            add(new HudConfig());
        }
    };
    public static final File dir = new File(TYClient.mc.mcDataDir, "TYClient");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigManager() {
        if (!ConfigManager.dir.exists()) {
            ConfigManager.dir.mkdir();
        }
        ConfigManager.configs.add((Config)new ModConfig());
        ConfigManager.configs.add((Config)new HudConfig());
    }

    public void loadConfig(final String name) {
        JsonParser jsonParser = new JsonParser();
        final File file = new File(ConfigManager.dir, name);
        if (file.exists()) {
            System.out.println("Loading config: " + name);
            for (final Config config : ConfigManager.configs) {
                if (config.getName().equals(name)) {
                    try {
                        config.loadConfig(jsonParser.parse((Reader)new FileReader(file)).getAsJsonObject());
                    }
                    catch (FileNotFoundException e) {
                        System.out.println("Failed to load config: " + name);
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        else {
            System.out.println("Config " + name + " doesn't exist, creating a new one...");
            this.saveConfig(name);
        }
    }

    public void loadUserConfig(final String name) {
        JsonParser jsonParser = new JsonParser();
        final File file = new File(ConfigManager.dir, name);
        if (file.exists()) {
            System.out.println("Loading config: " + name);
            for (final Config config : ConfigManager.configs) {
                if (config.getName().equals("modules.json")) {
                    try {
                        config.loadConfig(jsonParser.parse((Reader)new FileReader(file)).getAsJsonObject());
                    }
                    catch (FileNotFoundException e) {
                        System.out.println("Failed to load config: " + name);
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        else {
            System.out.println("Config " + name + " doesn't exist, creating a new one...");
            this.saveUserConfig(name);
        }
    }

    public void saveConfig(final String name) {
        final File file = new File(ConfigManager.dir, name);
        try {
            System.out.println("Saving config: " + name);
            file.createNewFile();
            for (final Config config : ConfigManager.configs) {
                if (config.getName().equals(name)) {
                    FileUtils.writeByteArrayToFile(file, ConfigManager.gson.toJson((JsonElement)config.saveConfig()).getBytes(StandardCharsets.UTF_8));
                    break;
                }
            }
        }
        catch (IOException e) {
            System.out.println("Failed to save config: " + name);
        }
    }

    public void saveUserConfig(final String name) {
        final File file = new File(ConfigManager.dir, name);
        try {
            System.out.println("Saving config: " + name);
            file.createNewFile();
            for (final Config config : ConfigManager.configs) {
                if (config.getName().equals("modules.json")) {
                    FileUtils.writeByteArrayToFile(file, ConfigManager.gson.toJson((JsonElement)config.saveConfig()).getBytes(StandardCharsets.UTF_8));
                    break;
                }
            }
        }
        catch (IOException e) {
            System.out.println("Failed to save config: " + name);
        }
    }

    public void loadAllConfig() {
        System.out.println("Loading all configs...");
        ConfigManager.configs.forEach(it -> this.loadConfig(it.getName()));
    }

    public void saveAllConfig() {
        System.out.println("Saving all configs...");
        ConfigManager.configs.forEach(it -> this.saveConfig(it.getName()));
    }

    private static final String EXTENSION = ".json";

    public List<String> getConfigs() {
        Stream<Path> filesStream;

        try {
            filesStream = walk(dir.toPath());
        } catch (IOException e) {
            return Collections.emptyList();
        }

        return filesStream // @off
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(s -> s.endsWith(EXTENSION))
                .map(s -> s.substring(0, s.length() - EXTENSION.length()))
                .collect(Collectors.toCollection(ObjectArrayList::new)); // @on
    }
}
