package com.example.ecsite.controller;

import com.example.ecsite.dao.UserDAO;
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
 * HTTPリクエストを受け取り、LoginServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

    /**
     * GETリクエストを処理し、必要なデータを準備して画面を表示します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/login.jsp")
                .forward(request, response);
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
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        request.setAttribute("email", email == null ? "" : email.trim());

        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            showError(request, response, "Email and password are required.");
            return;
        }

        try {
            User user = userDAO.authenticate(email, password);
            if (user == null) {
                showError(request, response, "Invalid email or password.");
                return;
            }

            HttpSession session = request.getSession(true);
            request.changeSessionId();
            session.setAttribute("user", user);
            String returnTo = safeReturnTo(request.getParameter("returnTo"));
            response.sendRedirect(request.getContextPath() + returnTo);
        } catch (SQLException e) {
            getServletContext().log("Login database error", e);
            showError(request, response,
                    "Login is temporarily unavailable. Please try again later.");
        }
    }

    private String safeReturnTo(String value) {
        return "/checkout".equals(value) ? "/checkout" : "/products";
    }

    private void showError(HttpServletRequest request, HttpServletResponse response,
                           String error) throws ServletException, IOException {
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/views/login.jsp")
                .forward(request, response);
    }
}
