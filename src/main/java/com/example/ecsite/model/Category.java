package com.example.ecsite.model;

/**
 * ECサイトで使用する Category のデータを保持するモデルクラスです。
 *
 * <p>DAOと画面の間で値を受け渡すために使用し、データベース処理や画面処理は持ちません。</p>
 */
public class Category {
    private int categoryId;
    private String categoryName;
    private int displayOrder;
    private boolean active;
    private int productCount;

    public Category() {}
    public Category(int categoryId, String categoryName, int displayOrder, boolean active) {
        this.categoryId = categoryId; this.categoryName = categoryName;
        this.displayOrder = displayOrder; this.active = active;
    }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getProductCount() { return productCount; }
    public void setProductCount(int productCount) { this.productCount = productCount; }
}
