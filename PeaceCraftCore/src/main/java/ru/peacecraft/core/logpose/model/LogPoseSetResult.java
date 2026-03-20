package ru.peacecraft.core.logpose.model;

public record LogPoseSetResult(boolean success, String message) {
    public static LogPoseSetResult ok(String message) {
        return new LogPoseSetResult(true, message);
    }

    public static LogPoseSetResult fail(String message) {
        return new LogPoseSetResult(false, message);
    }
}
