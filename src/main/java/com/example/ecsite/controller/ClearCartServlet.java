package com.example.ecsite.controller;

import com.example.ecsite.model.CartItem;
import com.example.ecsite.util.CartSession;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

/**
 * HTTPリクエストを受け取り、ClearCartServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/cart/clear")
public class ClearCartServlet extends HttpServlet {
    /**
     * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Map<Integer, CartItem> cart = CartSession.getOrCreate(session);
            cart.clear();
            CartSession.updateCount(session, cart);
        }
        response.sendRedirect(request.getContextPath() + "/cart");
    }
}
