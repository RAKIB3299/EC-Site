package com.example.ecsite.controller;
import com.example.ecsite.dao.OrderDAO;import com.example.ecsite.dao.OrderTrackingDAO;import com.example.ecsite.model.Order;import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.SQLException;
@WebServlet("/admin/orders/detail") /**
 * HTTPリクエストを受け取り、AdminOrderDetailServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
public class AdminOrderDetailServlet extends HttpServlet{private final OrderDAO dao=new OrderDAO();private final OrderTrackingDAO trackingDAO=new OrderTrackingDAO();/**
 * GETリクエストを処理し、必要なデータを準備して画面を表示します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doGet(HttpServletRequest r,HttpServletResponse p)throws ServletException,IOException{try{long id=Long.parseLong(r.getParameter("id"));if(id<=0)throw new NumberFormatException();Order o=dao.findByIdForAdmin(id);if(o==null){p.sendError(404,"Order not found.");return;}r.setAttribute("order",o);r.setAttribute("orderItems",dao.findItemsByOrderId(id));r.setAttribute("tracking",trackingDAO.findByOrderId(id));r.getRequestDispatcher("/WEB-INF/views/admin/order-detail.jsp").forward(r,p);}catch(NumberFormatException e){p.sendError(400,"Invalid order ID.");}catch(SQLException e){getServletContext().log("Admin order detail",e);p.sendError(500,"Order cannot be loaded.");}}}
