package com.kafei.efmc.common.config;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MultiSlotConfig {
    private static final Map<ResourceLocation, SlotData> ITEM_SIZES = new HashMap<>();

    public record SlotData(int width, int height) {}

    public static void load() {
        Path path = FMLPaths.CONFIGDIR.get().resolve("efmc/multi_slot_items.json");
        try {
            if (!path.toFile().exists()) createDefaultConfig(path);

            JsonObject json = JsonParser.parseReader(new FileReader(path.toFile())).getAsJsonObject();
            json.entrySet().forEach(entry -> {
                JsonObject data = entry.getValue().getAsJsonObject();
                ITEM_SIZES.put(new ResourceLocation(entry.getKey()),
                        new SlotData(data.get("width").getAsInt(), data.get("height").getAsInt()));
            });
        } catch (Exception e) {
            throw new RuntimeException("Config load failed", e);
        }
    }

    private static void createDefaultConfig(Path path) throws IOException {
        JsonObject defaultConfig = new JsonObject();
        JsonObject example = new JsonObject();
        example.addProperty("width", 2);
        example.addProperty("height", 1);
        defaultConfig.add("minecraft:chest", example);

        path.getParent().toFile().mkdirs();
        new GsonBuilder().setPrettyPrinting().create().toJson(defaultConfig, new FileWriter(path.toFile()));
    }

    public static SlotData getSize(ResourceLocation itemId) {
        return ITEM_SIZES.get(itemId);
    }

    public static boolean isMultiSlot(ResourceLocation itemId) {
        return ITEM_SIZES.containsKey(itemId);
    }
}