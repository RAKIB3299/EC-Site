package com.example.ecsite.controller;
import com.example.ecsite.dao.NotificationDAO;import com.example.ecsite.model.User;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.SQLException;
@WebServlet("/notifications/read-all") /**
 * HTTPリクエストを受け取り、NotificationMarkAllServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
public class NotificationMarkAllServlet extends HttpServlet{private final NotificationDAO dao=new NotificationDAO();/**
 * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doPost(HttpServletRequest r,HttpServletResponse p)throws IOException{HttpSession s=r.getSession(false);Object x=s==null?null:s.getAttribute("user");if(!(x instanceof User u)){p.sendRedirect(r.getContextPath()+"/login");return;}try{dao.markAllAsRead(u.getUserId());String back=r.getParameter("returnTo");p.sendRedirect(r.getContextPath()+("/notifications".equals(back)?back:"/notifications"));}catch(SQLException e){getServletContext().log("Notifications could not be marked read",e);p.sendError(500,"Notifications could not be updated.");}}}
