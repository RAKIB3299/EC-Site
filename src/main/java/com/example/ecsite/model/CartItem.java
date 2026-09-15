package com.example.ecsite.model;

import java.math.BigDecimal;

/** One product and its quantity in the session shopping cart. */
/**
 * ECサイトで使用する CartItem のデータを保持するモデルクラスです。
 *
 * <p>DAOと画面の間で値を受け渡すために使用し、データベース処理や画面処理は持ちません。</p>
 */
public class CartItem {
    private Product product;
    private int quantity;

    public CartItem() {
    }

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getSubtotal() {
        if (product == null || product.getListPrice() == null) {
            return BigDecimal.ZERO;
        }
        return product.getListPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
