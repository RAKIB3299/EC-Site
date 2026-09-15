package com.example.ecsite.controller;

import com.example.ecsite.dao.OrderDAO;
import com.example.ecsite.dao.ProductDAO;
import com.example.ecsite.model.CartItem;
import com.example.ecsite.model.Order;
import com.example.ecsite.model.Product;
import com.example.ecsite.model.User;
import com.example.ecsite.util.CartSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

/**
 * HTTPリクエストを受け取り、CheckoutServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    /**
     * GETリクエストを処理し、必要なデータを準備して画面を表示します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = loggedInUser(session);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?returnTo=/checkout");
            return;
        }

        Map<Integer, CartItem> cart = cart(session);
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart?empty=true");
            return;
        }

        try {
            BigDecimal total = refreshPrices(cart);
            request.setAttribute("cartItems", cart.values());
            request.setAttribute("checkoutTotal", total);
            request.setAttribute("shippingAddress", user.getAddress());
            request.getRequestDispatcher("/WEB-INF/views/checkout.jsp")
                    .forward(request, response);
        } catch (SQLException e) {
            getServletContext().log("Could not prepare checkout", e);
            request.setAttribute("error",
                    "Checkout cannot be prepared because a product is unavailable.");
            request.getRequestDispatcher("/WEB-INF/views/checkout.jsp")
                    .forward(request, response);
        }
    }

    /**
     * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        User user = loggedInUser(session);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?returnTo=/checkout");
            return;
        }

        Map<Integer, CartItem> cart = cart(session);
        if (cart == null || cart.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/cart?empty=true");
            return;
        }

        String shippingAddress = request.getParameter("shippingAddress");
        shippingAddress = shippingAddress == null ? "" : shippingAddress.trim();
        if (shippingAddress.isEmpty() || shippingAddress.length() > 500) {
            showCheckoutError(request, response, user, cart, shippingAddress,
                    shippingAddress.isEmpty()
                            ? "Shipping address is required."
                            : "Shipping address must not exceed 500 characters.");
            return;
        }

        try {
            Order order = orderDAO.createOrder(user.getUserId(), shippingAddress, cart);
            cart.clear();
            CartSession.updateCount(session, cart);
            session.setAttribute("completedOrder", order);
            response.sendRedirect(request.getContextPath() + "/order-success");
        } catch (SQLException | RuntimeException e) {
            getServletContext().log("Checkout transaction failed", e);
            showCheckoutError(request, response, user, cart, shippingAddress,
                    "Your order could not be created. Your cart has not been cleared.");
        }
    }

    private void showCheckoutError(HttpServletRequest request, HttpServletResponse response,
                                   User user, Map<Integer, CartItem> cart,
                                   String shippingAddress, String error)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        request.setAttribute("shippingAddress", shippingAddress);
        request.setAttribute("cartItems", cart.values());
        request.setAttribute("checkoutTotal", cart.values().stream()
                .map(CartItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
        request.getRequestDispatcher("/WEB-INF/views/checkout.jsp")
                .forward(request, response);
    }

    private BigDecimal refreshPrices(Map<Integer, CartItem> cart) throws SQLException {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Integer, CartItem> entry : cart.entrySet()) {
            Product liveProduct = productDAO.findById(entry.getKey());
            if (liveProduct == null || liveProduct.getListPrice() == null) {
                throw new SQLException("A cart product is unavailable.");
            }
            entry.getValue().setProduct(liveProduct);
            total = total.add(entry.getValue().getSubtotal());
        }
        return total;
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, CartItem> cart(HttpSession session) {
        if (session == null) return null;
        Object value = session.getAttribute(CartSession.CART_ATTRIBUTE);
        return value instanceof Map<?, ?> ? (Map<Integer, CartItem>) value : null;
    }

    private User loggedInUser(HttpSession session) {
        if (session == null) return null;
        Object value = session.getAttribute("user");
        return value instanceof User ? (User) value : null;
    }
}
