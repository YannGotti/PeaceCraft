package ru.peacecraft.core.logpose.model;

import org.bukkit.Location;
import ru.peacecraft.core.islands.model.IslandData;

public final class LogPoseTarget {

    private final String targetId;
    private final TargetType type;
    private final String islandId;
    private final Location location;
    private final String displayName;
    private final boolean unlocked;

    public enum TargetType {
        ISLAND,
        PORT,
        BOSS,
        QUEST
    }

    public LogPoseTarget(String targetId, TargetType type, String islandId, Location location, String displayName, boolean unlocked) {
        this.targetId = targetId;
        this.type = type;
        this.islandId = islandId;
        this.location = location;
        this.displayName = displayName;
        this.unlocked = unlocked;
    }

    public static LogPoseTarget fromIsland(IslandData island, boolean unlocked) {
        return new LogPoseTarget(
            island.getId(),
            TargetType.ISLAND,
            island.getId(),
            new Location(
                org.bukkit.Bukkit.getWorld(island.getWorldName()),
                island.getCompassTarget().getX(),
                island.getCompassTarget().getY(),
                island.getCompassTarget().getZ()
            ),
            island.getDisplayName(),
            unlocked
        );
    }

    // Getters
    public String getTargetId() {
        return targetId;
    }

    public TargetType getType() {
        return type;
    }

    public String getIslandId() {
        return islandId;
    }

    public Location getLocation() {
        return location;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isUnlocked() {
        return unlocked;
    }
}
