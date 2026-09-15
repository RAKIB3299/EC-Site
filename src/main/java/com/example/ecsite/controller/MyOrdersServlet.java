package com.example.ecsite.controller;

import com.example.ecsite.dao.OrderDAO;
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
 * HTTPリクエストを受け取り、MyOrdersServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/orders")
public class MyOrdersServlet extends HttpServlet {
    private final OrderDAO orderDAO = new OrderDAO();

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
        try {
            request.setAttribute("orders", orderDAO.findByUserId(user.getUserId()));
            request.getRequestDispatcher("/WEB-INF/views/orders.jsp")
                    .forward(request, response);
        } catch (SQLException e) {
            getServletContext().log("Could not load order history", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Your orders cannot be loaded right now. Please try again later.");
        }
    }

    private User loggedInUser(HttpSession session) {
        if (session == null) return null;
        Object value = session.getAttribute("user");
        return value instanceof User ? (User) value : null;
    }
}
