package ru.peacecraft.core.travel.model;

public final class TravelResult {

    private final boolean success;
    private final TravelErrorCode errorCode;
    private final String errorMessage;

    public enum TravelErrorCode {
        SUCCESS,
        ISLAND_NOT_UNLOCKED,
        ISLAND_NOT_FOUND,
        ON_COOLDOWN,
        INVALID_PORT,
        CANCELLED
    }

    private TravelResult(boolean success, TravelErrorCode errorCode, String errorMessage) {
        this.success = success;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static TravelResult success() {
        return new TravelResult(true, TravelErrorCode.SUCCESS, null);
    }

    public static TravelResult failure(TravelErrorCode errorCode, String errorMessage) {
        return new TravelResult(false, errorCode, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public TravelErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
