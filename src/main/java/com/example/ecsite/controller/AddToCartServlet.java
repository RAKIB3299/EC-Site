package com.example.ecsite.controller;

import com.example.ecsite.dao.ProductDAO;
import com.example.ecsite.model.CartItem;
import com.example.ecsite.model.Product;
import com.example.ecsite.util.CartSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * HTTPリクエストを受け取り、AddToCartServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/cart/add")
public class AddToCartServlet extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO();

    /**
     * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Integer productId = positiveInteger(request.getParameter("productId"));
        Integer quantity = positiveInteger(request.getParameter("quantity"));
        if (productId == null || quantity == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Please provide a valid product and quantity.");
            return;
        }

        try {
            Product product = productDAO.findById(productId);
            if (product == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "The requested product was not found.");
                return;
            }

            HttpSession session = request.getSession(true);
            Map<Integer, CartItem> cart = CartSession.getOrCreate(session);
            CartItem existingItem = cart.get(productId);
            if (existingItem == null) {
                cart.put(productId, new CartItem(product, quantity));
            } else {
                try {
                    existingItem.setQuantity(Math.addExact(existingItem.getQuantity(), quantity));
                } catch (ArithmeticException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "The requested quantity is too large.");
                    return;
                }
            }
            CartSession.updateCount(session, cart);
            response.sendRedirect(request.getContextPath() + "/cart");
        } catch (SQLException e) {
            getServletContext().log("Could not add product to cart", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The cart could not be updated. Please try again later.");
        }
    }

    private Integer positiveInteger(String value) {
        try {
            int number = Integer.parseInt(value);
            return number > 0 ? number : null;
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }
}
