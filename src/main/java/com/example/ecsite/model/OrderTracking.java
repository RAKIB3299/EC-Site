package com.example.ecsite.model;

import java.sql.Timestamp;

/** Optional shipping information for one EC order. */
/**
 * ECサイトで使用する OrderTracking のデータを保持するモデルクラスです。
 *
 * <p>DAOと画面の間で値を受け渡すために使用し、データベース処理や画面処理は持ちません。</p>
 */
public class OrderTracking {
    private long orderId;
    private String carrier;
    private String trackingNumber;
    private String shipmentStatus;
    private String trackingInformation;
    private Timestamp updatedAt;

    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getShipmentStatus() { return shipmentStatus; }
    public void setShipmentStatus(String shipmentStatus) { this.shipmentStatus = shipmentStatus; }
    public String getTrackingInformation() { return trackingInformation; }
    public void setTrackingInformation(String trackingInformation) { this.trackingInformation = trackingInformation; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
