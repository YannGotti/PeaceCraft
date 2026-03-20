package ru.peacecraft.core.islands.model;

public final class IslandEnterResult {

    private static final IslandEnterResult NONE = new IslandEnterResult(false, false, null);

    private final boolean discoveredNow;
    private final boolean currentIslandChanged;
    private final String islandId;

    public IslandEnterResult(boolean discoveredNow, boolean currentIslandChanged, String islandId) {
        this.discoveredNow = discoveredNow;
        this.currentIslandChanged = currentIslandChanged;
        this.islandId = islandId;
    }

    public static IslandEnterResult none() {
        return NONE;
    }

    public boolean isDiscoveredNow() {
        return discoveredNow;
    }

    public boolean isCurrentIslandChanged() {
        return currentIslandChanged;
    }

    public String getIslandId() {
        return islandId;
    }

    public boolean hasChanges() {
        return discoveredNow || currentIslandChanged;
    }
}
