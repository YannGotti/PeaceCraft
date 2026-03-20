package ru.peacecraft.core.persistence.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.persistence.model.PlayerData;

public final class PlayerDataRepository {

    private static final String DATA_FOLDER_NAME = "player-data";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final PeaceCraftPlugin plugin;
    private final File dataFolder;

    public PlayerDataRepository(PeaceCraftPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), DATA_FOLDER_NAME);
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IllegalStateException("Could not create player-data folder: " + dataFolder.getAbsolutePath());
        }
    }

    public PlayerData load(UUID uuid) {
        File file = getPlayerFile(uuid);
        if (!file.exists()) {
            return new PlayerData(uuid);
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return GSON.fromJson(reader, PlayerData.class);
        } catch (IOException e) {
            plugin.getLogger().severe(() -> "Failed to load player data for " + uuid + ": " + e.getMessage());
            return new PlayerData(uuid);
        }
    }

    public void save(PlayerData playerData) {
        playerData.setLastSaveTime(System.currentTimeMillis());
        File file = getPlayerFile(playerData.getUuid());
        File tempFile = new File(file.getParentFile(), file.getName() + ".tmp");

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
            GSON.toJson(playerData, writer);
        } catch (IOException e) {
            plugin.getLogger().severe(() -> "Failed to save player data for " + playerData.getUuid() + ": " + e.getMessage());
            return;
        }

        if (file.exists() && !file.delete()) {
            plugin.getLogger().warning(() -> "Could not delete old player data file for " + playerData.getUuid());
        }
        if (!tempFile.renameTo(file)) {
            plugin.getLogger().severe(() -> "Could not rename temp file for " + playerData.getUuid());
        }
    }

    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".json");
    }

    public void delete(UUID uuid) {
        File file = getPlayerFile(uuid);
        if (file.exists() && !file.delete()) {
            plugin.getLogger().warning(() -> "Could not delete player data file for " + uuid);
        }
    }
}
