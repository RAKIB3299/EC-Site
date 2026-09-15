package com.example.ecsite.controller;

import com.example.ecsite.dao.UserDAO;
import com.example.ecsite.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;

/**
 * HTTPリクエストを受け取り、AccountPasswordServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/account/password")
public class AccountPasswordServlet extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();

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
        User user = sessionUser(session);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String current = request.getParameter("currentPassword");
        String next = request.getParameter("newPassword");
        String confirm = request.getParameter("confirmPassword");
        String error = validate(current, next, confirm);
        if (error != null) {
            showError(request, response, error);
            return;
        }

        try {
            if (!userDAO.verifyPassword(user.getUserId(), current)) {
                showError(request, response, "Current password is incorrect.");
                return;
            }
            String hash = BCrypt.hashpw(next, BCrypt.gensalt(12));
            if (!userDAO.updatePassword(user.getUserId(), hash)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Account not found.");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/account?passwordUpdated=true");
        } catch (SQLException e) {
            getServletContext().log("Password change failed", e);
            showError(request, response, "Your password could not be changed. Please try again.");
        }
    }

    private String validate(String current, String next, String confirm) {
        if (current == null || current.isEmpty()) return "Current password is required.";
        if (next == null || next.isEmpty()) return "New password is required.";
        if (next.length() < 8) return "New password must be at least 8 characters.";
        if (!next.equals(confirm)) return "New passwords do not match.";
        return null;
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("passwordError", error);
        request.getRequestDispatcher("/WEB-INF/views/account.jsp").forward(request, response);
    }

    private User sessionUser(HttpSession session) {
        Object value = session == null ? null : session.getAttribute("user");
        return value instanceof User ? (User) value : null;
    }
}
