package com.darkvenice.particleopacity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/** JSON-backed configuration. Missing IDs are enabled, so newly added registry entries default ON. */
public final class ParticleOpacityConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("particleopacity.json");
    private static Data data = new Data();

    private ParticleOpacityConfig() {}

    public static synchronized void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }
        try (Reader reader = Files.newBufferedReader(FILE)) {
            Data loaded = GSON.fromJson(reader, Data.class);
            data = loaded == null ? new Data() : loaded;
            data.normalize();
        } catch (Exception exception) {
            ParticleOpacity.LOGGER.error("Could not read particleopacity.json; using defaults.", exception);
            data = new Data();
            save();
        }
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Path temporary = FILE.resolveSibling(FILE.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary)) {
                GSON.toJson(data, writer);
            }
            try {
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException atomicMoveUnsupported) {
                Files.move(temporary, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            ParticleOpacity.LOGGER.error("Could not save particleopacity.json.", exception);
        }
    }

    public static synchronized boolean blocksEnabled() {
        return data.blocksEnabled;
    }

    public static synchronized void setBlocksEnabled(boolean enabled) {
        data.blocksEnabled = enabled;
        save();
    }

    public static synchronized int blockOpacity() {
        return data.blockOpacity;
    }

    public static synchronized void setBlockOpacity(int percent) {
        data.blockOpacity = clamp(percent);
        save();
    }

    public static synchronized boolean foodsEnabled() {
        return data.foodsEnabled;
    }

    public static synchronized void setFoodsEnabled(boolean enabled) {
        data.foodsEnabled = enabled;
        save();
    }

    public static synchronized int foodOpacity() {
        return data.foodOpacity;
    }

    public static synchronized void setFoodOpacity(int percent) {
        data.foodOpacity = clamp(percent);
        save();
    }

    public static synchronized boolean isBlockEnabled(Identifier id) {
        return !data.disabledBlockIds.contains(id.toString());
    }

    public static synchronized void setBlockEnabled(Identifier id, boolean enabled) {
        updateDisabled(data.disabledBlockIds, id, enabled);
        save();
    }

    public static synchronized boolean isFoodEnabled(Identifier id) {
        return !data.disabledFoodIds.contains(id.toString());
    }

    public static synchronized void setFoodEnabled(Identifier id, boolean enabled) {
        updateDisabled(data.disabledFoodIds, id, enabled);
        save();
    }

    public static synchronized void setAllBlocksEnabled(boolean enabled, Collection<Identifier> allIds) {
        applyBulk(data.disabledBlockIds, enabled, allIds);
        save();
    }

    public static synchronized void setAllFoodsEnabled(boolean enabled, Collection<Identifier> allIds) {
        applyBulk(data.disabledFoodIds, enabled, allIds);
        save();
    }

    public static synchronized float blockMultiplier(Identifier id) {
        if (!data.blocksEnabled || data.blockOpacity >= 100 || id == null || data.disabledBlockIds.contains(id.toString())) {
            return 1.0f;
        }
        return data.blockOpacity / 100.0f;
    }

    public static synchronized float foodMultiplier(Identifier id) {
        if (!data.foodsEnabled || data.foodOpacity >= 100 || id == null || data.disabledFoodIds.contains(id.toString())) {
            return 1.0f;
        }
        return data.foodOpacity / 100.0f;
    }

    public static synchronized boolean shouldTranslucentlyRenderBlock(Identifier id) {
        return data.blocksEnabled && data.blockOpacity < 100 && id != null
                && !data.disabledBlockIds.contains(id.toString());
    }

    public static synchronized boolean shouldTranslucentlyRenderFood(Identifier id) {
        return data.foodsEnabled && data.foodOpacity < 100 && id != null
                && !data.disabledFoodIds.contains(id.toString());
    }

    public static Set<Identifier> getAllBlockIds() {
        Set<Identifier> result = new HashSet<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            result.add(BuiltInRegistries.BLOCK.getKey(block));
        }
        return result;
    }

    public static Set<Identifier> getAllItemIds() {
        Set<Identifier> result = new HashSet<>();
        for (Item item : BuiltInRegistries.ITEM) {
            result.add(BuiltInRegistries.ITEM.getKey(item));
        }
        return result;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static void updateDisabled(Set<String> disabled, Identifier id, boolean enabled) {
        if (enabled) {
            disabled.remove(id.toString());
        } else {
            disabled.add(id.toString());
        }
    }

    private static void applyBulk(Set<String> disabled, boolean enabled, Collection<Identifier> allIds) {
        if (enabled) {
            disabled.clear();
        } else {
            disabled.clear();
            for (Identifier id : allIds) {
                disabled.add(id.toString());
            }
        }
    }

    private static final class Data {
        boolean blocksEnabled = true;
        int blockOpacity = 50;
        boolean foodsEnabled = true;
        int foodOpacity = 50;
        Set<String> disabledBlockIds = new HashSet<>();
        Set<String> disabledFoodIds = new HashSet<>();

        void normalize() {
            blockOpacity = clamp(blockOpacity);
            foodOpacity = clamp(foodOpacity);
            if (disabledBlockIds == null) disabledBlockIds = new HashSet<>();
            if (disabledFoodIds == null) disabledFoodIds = new HashSet<>();
        }
    }
}
