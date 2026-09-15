package com.example.ecsite.controller;

import com.example.ecsite.model.CartItem;
import com.example.ecsite.util.CartSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * HTTPリクエストを受け取り、CartServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    /**
     * GETリクエストを処理し、必要なデータを準備して画面を表示します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        Map<Integer, CartItem> cart = CartSession.getOrCreate(session);
        BigDecimal cartTotal = cart.values().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        request.setAttribute("cartItems", cart.values());
        request.setAttribute("cartTotal", cartTotal);
        request.getRequestDispatcher("/WEB-INF/views/cart.jsp")
                .forward(request, response);
    }
}
