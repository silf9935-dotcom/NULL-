package dev.nullremnant.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.nullremnant.RemnantMod;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public final class RemnantConfig {
    public static boolean enabled = true;
    public static boolean nightmares = true;
    public static boolean subtleAnomalies = true;
    public static int eventFrequency = 100;
    public static int stalkerFrequency = 100;

    private RemnantConfig() {
    }

    public static void load() {
        resetDefaults();
        Path file = FabricLoader.getInstance().getConfigDir().resolve("null_remnant.json");

        try {
            if (!Files.exists(file)) {
                Files.createDirectories(file.getParent());
                Files.writeString(file, defaultJson().toString());
            }

            JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            enabled = getBoolean(root, "enabled", true);
            nightmares = getBoolean(root, "nightmares", true);
            subtleAnomalies = getBoolean(root, "subtleAnomalies", true);
            eventFrequency = clamp(getInt(root, "eventFrequency", 100));
            stalkerFrequency = clamp(getInt(root, "stalkerFrequency", 100));
        } catch (Exception exception) {
            // Preserve a malformed user file for manual repair; keep safe in-memory defaults.
            resetDefaults();
            RemnantMod.LOGGER.warn(
                "Could not load NULL Remnant config from {}. Using defaults without overwriting the file.",
                file,
                exception
            );
        }
    }

    private static JsonObject defaultJson() {
        JsonObject defaults = new JsonObject();
        defaults.addProperty("enabled", true);
        defaults.addProperty("nightmares", true);
        defaults.addProperty("subtleAnomalies", true);
        defaults.addProperty("eventFrequency", 100);
        defaults.addProperty("stalkerFrequency", 100);
        return defaults;
    }

    private static void resetDefaults() {
        enabled = true;
        nightmares = true;
        subtleAnomalies = true;
        eventFrequency = 100;
        stalkerFrequency = 100;
    }

    private static boolean getBoolean(JsonObject object, String key, boolean fallback) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()
                || !object.getAsJsonPrimitive(key).isBoolean()) {
            return fallback;
        }
        return object.get(key).getAsBoolean();
    }

    private static int getInt(JsonObject object, String key, int fallback) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()
                || !object.getAsJsonPrimitive(key).isNumber()) {
            return fallback;
        }
        return object.get(key).getAsInt();
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(200, value));
    }
}
