package com.pkillboredom.flarpbotplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class BotPluginMain extends JavaPlugin {
    public final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public Map<String, Object> ConfigMap = new HashMap<>();
    private File configFile;

    @Override
    public void onEnable() {
        // Load config.
        configFile = new File(getDataFolder(), "config.json");
        try {
            ConfigMap = gson.fromJson(new FileReader(configFile), new HashMap<String, Object>().getClass());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerCountListener(ConfigMap, getLogger()), this);

        getLogger().info("Flap-Bot-Plugin Enabled.");
    }

    @Override
    public void onDisable() {
        final String json = gson.toJson(ConfigMap);
        configFile.delete();
        try {
            Files.write(configFile.toPath(), json.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
