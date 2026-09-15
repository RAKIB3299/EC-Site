package com.example.ecsite.controller;
import com.example.ecsite.dao.NotificationDAO;import com.example.ecsite.model.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.SQLException;
@WebServlet("/notifications/open") /**
 * HTTPリクエストを受け取り、NotificationOpenServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
public class NotificationOpenServlet extends HttpServlet{private final NotificationDAO dao=new NotificationDAO();/**
 * GETリクエストを処理し、必要なデータを準備して画面を表示します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doGet(HttpServletRequest r,HttpServletResponse p)throws IOException{User u=user(r);if(u==null){p.sendRedirect(r.getContextPath()+"/login");return;}try{long id=Long.parseLong(r.getParameter("id"));if(id<=0)throw new NumberFormatException();Notification n=dao.findOwned(id,u.getUserId());if(n==null){p.sendError(404,"Notification not found.");return;}dao.markAsRead(id,u.getUserId());String target=n.getTargetUrl();if(target==null||!target.startsWith("/orders/detail?id="))target="/notifications";p.sendRedirect(r.getContextPath()+target);}catch(NumberFormatException e){p.sendError(400,"Invalid notification ID.");}catch(SQLException e){getServletContext().log("Notification could not be opened",e);p.sendError(500,"Notification cannot be opened right now.");}}private User user(HttpServletRequest r){HttpSession s=r.getSession(false);Object x=s==null?null:s.getAttribute("user");return x instanceof User?(User)x:null;}}
