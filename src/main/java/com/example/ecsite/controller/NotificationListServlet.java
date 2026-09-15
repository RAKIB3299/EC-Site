package com.example.ecsite.controller;
import com.example.ecsite.dao.NotificationDAO;import com.example.ecsite.model.User;import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.SQLException;
@WebServlet("/notifications") /**
 * HTTPリクエストを受け取り、NotificationListServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
public class NotificationListServlet extends HttpServlet{private static final int SIZE=12;private final NotificationDAO dao=new NotificationDAO();/**
 * GETリクエストを処理し、必要なデータを準備して画面を表示します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doGet(HttpServletRequest r,HttpServletResponse p)throws ServletException,IOException{User u=user(r);if(u==null){p.sendRedirect(r.getContextPath()+"/login");return;}int page=page(r.getParameter("page"));try{int total=dao.countByUserId(u.getUserId()),pages=Math.max(1,(total+SIZE-1)/SIZE);if(page>pages)page=pages;r.setAttribute("notifications",dao.findByUserId(u.getUserId(),page,SIZE));r.setAttribute("currentPage",page);r.setAttribute("totalPages",pages);r.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(r,p);}catch(SQLException e){getServletContext().log("Notifications could not be loaded",e);p.sendError(500,"Notifications cannot be loaded right now.");}}private User user(HttpServletRequest r){Object x=r.getSession(false)==null?null:r.getSession(false).getAttribute("user");return x instanceof User?(User)x:null;}private int page(String v){try{return Math.max(1,Integer.parseInt(v));}catch(Exception e){return 1;}}}
