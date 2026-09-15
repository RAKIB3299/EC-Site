package com.example.ecsite.model;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

/** One customer notification from EC_NOTIFICATIONS. */
/**
 * ECサイトで使用する Notification のデータを保持するモデルクラスです。
 *
 * <p>DAOと画面の間で値を受け渡すために使用し、データベース処理や画面処理は持ちません。</p>
 */
public class Notification {
    private long notificationId;
    private int userId;
    private Long orderId;
    private String notificationType;
    private String title;
    private String message;
    private String targetUrl;
    private boolean read;
    private Timestamp createdAt;
    private Timestamp readAt;

    public long getNotificationId() { return notificationId; }
    public void setNotificationId(long notificationId) { this.notificationId = notificationId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getNotificationType() { return notificationType; }
    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTargetUrl() { return targetUrl; }
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getReadAt() { return readAt; }
    public void setReadAt(Timestamp readAt) { this.readAt = readAt; }

    public String getRelativeTime() {
        if (createdAt == null) return "";
        long seconds = Math.max(0, Duration.between(createdAt.toInstant(), Instant.now()).getSeconds());
        if (seconds < 60) return "Just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        long hours = minutes / 60;
        if (hours < 24) return hours + (hours == 1 ? " hour ago" : " hours ago");
        long days = hours / 24;
        if (days < 7) return days + (days == 1 ? " day ago" : " days ago");
        return createdAt.toLocalDateTime().toLocalDate().toString();
    }
}
