package ru.peacecraft.core.persistence.task;

import org.bukkit.scheduler.BukkitTask;
import ru.peacecraft.core.PeaceCraftPlugin;
import ru.peacecraft.core.persistence.service.PlayerDataService;

public final class PlayerDataAutosaveTask {

    private final PeaceCraftPlugin plugin;
    private final PlayerDataService playerDataService;
    private final int autosaveIntervalSeconds;

    private BukkitTask task;

    public PlayerDataAutosaveTask(PeaceCraftPlugin plugin, PlayerDataService playerDataService, int autosaveIntervalSeconds) {
        this.plugin = plugin;
        this.playerDataService = playerDataService;
        this.autosaveIntervalSeconds = autosaveIntervalSeconds;
    }

    public void start() {
        if (autosaveIntervalSeconds <= 0) {
            plugin.getLogger().info("Autosave is disabled.");
            return;
        }

        stop();

        long intervalTicks = autosaveIntervalSeconds * 20L;
        this.task = plugin
            .getServer()
            .getScheduler()
            .runTaskTimer(
                plugin,
                () -> {
                    int saved = playerDataService.saveAllDirty();
                    if (saved > 0) {
                        plugin.getLogger().info(() -> "Autosaved " + saved + " dirty player(s).");
                    }
                },
                intervalTicks,
                intervalTicks
            );
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
