package com.example.ecsite.controller;

import com.example.ecsite.dao.UserDAO;
import com.example.ecsite.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * HTTPリクエストを受け取り、RegisterServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final UserDAO userDAO = new UserDAO();

    /**
     * GETリクエストを処理し、必要なデータを準備して画面を表示します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp")
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
        String firstName = trim(request.getParameter("firstName"));
        String lastName = trim(request.getParameter("lastName"));
        String email = trim(request.getParameter("email"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String phone = trim(request.getParameter("phone"));
        String address = trim(request.getParameter("address"));

        preserveForm(request, firstName, lastName, email, phone, address);
        String error = validate(firstName, lastName, email, password, confirmPassword);
        if (error != null) {
            showForm(request, response, error);
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                showForm(request, response, "An account with that email already exists.");
                return;
            }

            User user = new User(firstName, lastName, email, phone, address);
            if (!userDAO.register(user, password)) {
                showForm(request, response, "An account with that email already exists.");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/login?registered=true");
        } catch (SQLException e) {
            getServletContext().log("Registration database error", e);
            showForm(request, response,
                    "Registration is temporarily unavailable. Please try again later.");
        }
    }

    private String validate(String firstName, String lastName, String email,
                            String password, String confirmPassword) {
        if (firstName.isEmpty()) return "First name is required.";
        if (lastName.isEmpty()) return "Last name is required.";
        if (email.isEmpty()) return "Email is required.";
        if (!EMAIL_PATTERN.matcher(email).matches()) return "Please enter a valid email address.";
        if (password == null || password.isEmpty()) return "Password is required.";
        if (password.length() < 8) return "Password must be at least 8 characters.";
        if (!password.equals(confirmPassword)) return "Passwords do not match.";
        return null;
    }

    private void preserveForm(HttpServletRequest request, String firstName,
                              String lastName, String email, String phone, String address) {
        request.setAttribute("firstName", firstName);
        request.setAttribute("lastName", lastName);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
        request.setAttribute("address", address);
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          String error) throws ServletException, IOException {
        request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/views/register.jsp")
                .forward(request, response);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
