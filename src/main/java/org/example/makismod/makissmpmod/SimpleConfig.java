package org.example.makismod.makissmpmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SimpleConfig {
    public static String ha_token = "";
    public static Boolean isDimitriEpstein = true;
    public static Boolean shouldDimitriPlayHK = true;
    private static final File CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(Makissmpmod.MOD_ID + "_config.json")
            .toFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save();
            return;
        }
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                ha_token = data.ha_token;
                isDimitriEpstein = data.isDimitriEpstein;
                shouldDimitriPlayHK = data.shouldDimitriPlayHK;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void save(){
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            ConfigData data = new ConfigData();
            data.ha_token = ha_token;
            data.isDimitriEpstein = isDimitriEpstein;
            data.shouldDimitriPlayHK = shouldDimitriPlayHK;
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static class ConfigData {
        String ha_token = "default_token_here";
        Boolean isDimitriEpstein = true;
        Boolean shouldDimitriPlayHK = true;
    }
}
