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
 * HTTPリクエストを受け取り、AccountUpdateServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/account/update")
public class AccountUpdateServlet extends HttpServlet {
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
        User sessionUser = sessionUser(session);
        if (sessionUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String firstName = trim(request.getParameter("firstName"));
        String lastName = trim(request.getParameter("lastName"));
        String phone = trim(request.getParameter("phone"));
        String address = trim(request.getParameter("address"));
        String error = validate(firstName, lastName, phone, address);
        if (error != null) {
            request.setAttribute("profileError", error);
            preserve(request, firstName, lastName, phone, address);
            forward(request, response);
            return;
        }

        try {
            if (!userDAO.updateProfile(sessionUser.getUserId(), firstName, lastName, phone, address)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Account not found.");
                return;
            }
            User updatedUser = userDAO.findById(sessionUser.getUserId());
            if (updatedUser == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Account not found.");
                return;
            }
            updatedUser.setPasswordHash(null);
            session.setAttribute("user", updatedUser);
            response.sendRedirect(request.getContextPath() + "/account?profileUpdated=true");
        } catch (SQLException e) {
            getServletContext().log("Account profile update failed", e);
            request.setAttribute("profileError", "Your profile could not be updated. Please try again.");
            preserve(request, firstName, lastName, phone, address);
            forward(request, response);
        }
    }

    private String validate(String first, String last, String phone, String address) {
        if (first.isEmpty()) return "First name is required.";
        if (last.isEmpty()) return "Last name is required.";
        if (first.length() > 100) return "First name must not exceed 100 characters.";
        if (last.length() > 100) return "Last name must not exceed 100 characters.";
        if (phone.length() > 30) return "Phone must not exceed 30 characters.";
        if (address.length() > 500) return "Address must not exceed 500 characters.";
        return null;
    }

    private void preserve(HttpServletRequest request, String first, String last,
                          String phone, String address) {
        request.setAttribute("formFirstName", first);
        request.setAttribute("formLastName", last);
        request.setAttribute("formPhone", phone);
        request.setAttribute("formAddress", address);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/account.jsp").forward(request, response);
    }

    private User sessionUser(HttpSession session) {
        Object value = session == null ? null : session.getAttribute("user");
        return value instanceof User ? (User) value : null;
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }
}
