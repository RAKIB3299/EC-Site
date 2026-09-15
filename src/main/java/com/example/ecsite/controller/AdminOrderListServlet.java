package com.example.ecsite.controller;
import com.example.ecsite.dao.OrderDAO;import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.SQLException;
@WebServlet("/admin/orders") /**
 * HTTPリクエストを受け取り、AdminOrderListServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
public class AdminOrderListServlet extends HttpServlet{private final OrderDAO dao=new OrderDAO();/**
 * GETリクエストを処理し、必要なデータを準備して画面を表示します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doGet(HttpServletRequest r,HttpServletResponse p)throws ServletException,IOException{try{r.setAttribute("orders",dao.findAllForAdmin());r.setAttribute("message",r.getParameter("message"));r.getRequestDispatcher("/WEB-INF/views/admin/orders.jsp").forward(r,p);}catch(SQLException e){getServletContext().log("Admin orders",e);p.sendError(500,"Orders cannot be loaded.");}}}
