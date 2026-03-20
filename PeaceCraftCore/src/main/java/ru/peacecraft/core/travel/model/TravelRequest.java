package ru.peacecraft.core.travel.model;

import java.util.UUID;

public final class TravelRequest {

    private final UUID playerId;
    private final String fromIslandId;
    private final String toIslandId;
    private final long requestTime;
    private final TravelType type;

    public enum TravelType {
        PORT_TRAVEL,
        FAST_TRAVEL,
        COMMAND_TRAVEL
    }

    public TravelRequest(UUID playerId, String fromIslandId, String toIslandId, TravelType type) {
        this.playerId = playerId;
        this.fromIslandId = fromIslandId;
        this.toIslandId = toIslandId;
        this.requestTime = System.currentTimeMillis();
        this.type = type;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getFromIslandId() {
        return fromIslandId;
    }

    public String getToIslandId() {
        return toIslandId;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public TravelType getType() {
        return type;
    }

    public boolean isFastTravel() {
        return type == TravelType.FAST_TRAVEL;
    }
}
