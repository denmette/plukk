package dev.casteels.plukk.shared.notification;

import java.util.List;

public record Notification(List<NotificationIssue> issues) {

    public Notification {
        issues = List.copyOf(issues);
    }

    public static Notification success() {
        return new Notification(List.of());
    }

    public static Notification issue(String code, String message) {
        return new Notification(List.of(new NotificationIssue(code, message)));
    }

    public boolean isSuccess() {
        return issues.isEmpty();
    }
}
