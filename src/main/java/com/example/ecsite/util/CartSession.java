package com.example.ecsite.util;

import com.example.ecsite.model.CartItem;
import jakarta.servlet.http.HttpSession;

import java.util.LinkedHashMap;
import java.util.Map;

/** Small helper for reading and updating the cart stored in HttpSession. */
/**
 * ECサイト内で共通利用する CartSession のユーティリティクラスです。
 *
 * <p>重複しやすい検証や設定読込、セッション・ファイル操作を一か所にまとめます。</p>
 */
public final class CartSession {
    public static final String CART_ATTRIBUTE = "cart";
    public static final String COUNT_ATTRIBUTE = "cartItemCount";

    private CartSession() {
    }

    @SuppressWarnings("unchecked")
    public static Map<Integer, CartItem> getOrCreate(HttpSession session) {
        Object existingCart = session.getAttribute(CART_ATTRIBUTE);
        if (existingCart instanceof Map<?, ?>) {
            return (Map<Integer, CartItem>) existingCart;
        }

        Map<Integer, CartItem> cart = new LinkedHashMap<>();
        session.setAttribute(CART_ATTRIBUTE, cart);
        updateCount(session, cart);
        return cart;
    }

    /**
     * 対象を限定する条件を付け、他の利用者のデータへ影響しないように更新します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static void updateCount(HttpSession session, Map<Integer, CartItem> cart) {
        int itemCount = cart.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        session.setAttribute(COUNT_ATTRIBUTE, itemCount);
    }
}
