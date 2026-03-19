package ru.peacecraft.core.islands.model;

public final class IslandData {

    private final String id;
    private final String displayName;
    private final String worldName;
    private final String regionId;
    private final String currencyId;
    private final String unlockRequirement;

    private final IslandCoordinate center;
    private final IslandCoordinate spawn;
    private final IslandCoordinate port;
    private final IslandCoordinate compassTarget;

    public IslandData(
        String id,
        String displayName,
        String worldName,
        String regionId,
        String currencyId,
        String unlockRequirement,
        IslandCoordinate center,
        IslandCoordinate spawn,
        IslandCoordinate port,
        IslandCoordinate compassTarget
    ) {
        this.id = id;
        this.displayName = displayName;
        this.worldName = worldName;
        this.regionId = regionId;
        this.currencyId = currencyId;
        this.unlockRequirement = unlockRequirement;
        this.center = center;
        this.spawn = spawn;
        this.port = port;
        this.compassTarget = compassTarget;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getRegionId() {
        return regionId;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public String getUnlockRequirement() {
        return unlockRequirement;
    }

    public IslandCoordinate getCenter() {
        return center;
    }

    public IslandCoordinate getSpawn() {
        return spawn;
    }

    public IslandCoordinate getPort() {
        return port;
    }

    public IslandCoordinate getCompassTarget() {
        return compassTarget;
    }

    @Override
    public String toString() {
        return (
            "IslandData{" +
            "id='" +
            id +
            '\'' +
            ", displayName='" +
            displayName +
            '\'' +
            ", worldName='" +
            worldName +
            '\'' +
            ", regionId='" +
            regionId +
            '\'' +
            ", currencyId='" +
            currencyId +
            '\'' +
            ", unlockRequirement='" +
            unlockRequirement +
            '\'' +
            ", center=" +
            center +
            ", spawn=" +
            spawn +
            ", port=" +
            port +
            ", compassTarget=" +
            compassTarget +
            '}'
        );
    }
}
