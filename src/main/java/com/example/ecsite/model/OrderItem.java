package com.example.ecsite.model;

import java.math.BigDecimal;

/** Represents one EC_ORDER_ITEMS row. */
/**
 * ECサイトで使用する OrderItem のデータを保持するモデルクラスです。
 *
 * <p>DAOと画面の間で値を受け渡すために使用し、データベース処理や画面処理は持ちません。</p>
 */
public class OrderItem {
    private long orderItemId;
    private long orderId;
    private int prodId;
    private int quantity;
    private BigDecimal unitPrice;
    private String productName;
    private String imageUrl;

    public OrderItem() {
    }

    public OrderItem(long orderId, int prodId, int quantity, BigDecimal unitPrice) {
        this.orderId = orderId;
        this.prodId = prodId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(long orderItemId) { this.orderItemId = orderItemId; }
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public int getProdId() { return prodId; }
    public void setProdId(int prodId) { this.prodId = prodId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isLocalImage() { return imageUrl != null && (imageUrl.startsWith("/images/") || imageUrl.startsWith("/product-images/")); }
    public BigDecimal getSubtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
}
