package ru.peacecraft.core.persistence.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.persistence.model.PlayerData;

public final class PlayerDataRepository {

    private static final String DATA_FOLDER_NAME = "player-data";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final PeaceCraftPlugin plugin;
    private final Path dataFolder;

    public PlayerDataRepository(PeaceCraftPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder().toPath().resolve(DATA_FOLDER_NAME);

        try {
            Files.createDirectories(this.dataFolder);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create player-data folder: " + this.dataFolder, e);
        }
    }

    public PlayerData load(UUID uuid) {
        Path file = getPlayerFile(uuid);

        if (!Files.exists(file)) {
            return new PlayerData(uuid);
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            PlayerData data = GSON.fromJson(reader, PlayerData.class);

            if (data == null) {
                data = new PlayerData(uuid);
            }

            data.normalizeAfterLoad(uuid);
            return data;
        } catch (Exception e) {
            plugin.getLogger().severe(() -> "Failed to load player data for " + uuid + ": " + e.getMessage());
            return new PlayerData(uuid);
        }
    }

    public boolean save(PlayerData playerData) {
        playerData.setLastSaveTime(System.currentTimeMillis());

        Path file = getPlayerFile(playerData.getUuid());
        Path tempFile = file.resolveSibling(file.getFileName() + ".tmp");

        try (Writer writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
            GSON.toJson(playerData, writer);
        } catch (IOException e) {
            plugin.getLogger().severe(() -> "Failed to write temp player data for " + playerData.getUuid() + ": " + e.getMessage());
            return false;
        }

        try {
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (AtomicMoveNotSupportedException e) {
            try {
                Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException moveFallbackException) {
                plugin
                    .getLogger()
                    .severe(
                        () -> "Failed to replace player data file for " + playerData.getUuid() + ": " + moveFallbackException.getMessage()
                    );
                return false;
            }
        } catch (IOException e) {
            plugin.getLogger().severe(() -> "Failed to replace player data file for " + playerData.getUuid() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean exists(UUID uuid) {
        return Files.exists(getPlayerFile(uuid));
    }

    public boolean delete(UUID uuid) {
        Path file = getPlayerFile(uuid);

        if (!Files.exists(file)) {
            return true;
        }

        try {
            Files.delete(file);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning(() -> "Could not delete player data file for " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    private Path getPlayerFile(UUID uuid) {
        return dataFolder.resolve(uuid.toString() + ".json");
    }
}
