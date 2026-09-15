package com.example.ecsite.controller;

import com.example.ecsite.dao.OrderDAO;
import com.example.ecsite.dao.OrderTrackingDAO;
import com.example.ecsite.model.Order;
import com.example.ecsite.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 * HTTPリクエストを受け取り、OrderDetailServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/orders/detail")
public class OrderDetailServlet extends HttpServlet {
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderTrackingDAO trackingDAO = new OrderTrackingDAO();

    /**
     * GETリクエストを処理し、必要なデータを準備して画面を表示します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = loggedInUser(request.getSession(false));
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Long orderId = positiveLong(request.getParameter("id"));
        if (orderId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Please provide a valid order ID.");
            return;
        }
        try {
            Order order = orderDAO.findByIdForUser(orderId, user.getUserId());
            if (order == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "The requested order was not found.");
                return;
            }
            request.setAttribute("order", order);
            request.setAttribute("orderItems", orderDAO.findItemsByOrderId(orderId));
            request.setAttribute("tracking", trackingDAO.findByOrderId(orderId));
            request.getRequestDispatcher("/WEB-INF/views/order-detail.jsp")
                    .forward(request, response);
        } catch (SQLException e) {
            getServletContext().log("Could not load order details", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The order cannot be loaded right now. Please try again later.");
        }
    }

    private Long positiveLong(String value) {
        try {
            long number = Long.parseLong(value);
            return number > 0 ? number : null;
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    private User loggedInUser(HttpSession session) {
        if (session == null) return null;
        Object value = session.getAttribute("user");
        return value instanceof User ? (User) value : null;
    }
}
