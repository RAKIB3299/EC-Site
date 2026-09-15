package com.example.ecsite.controller;
import com.example.ecsite.service.OrderNotificationService;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.SQLException;import java.util.Set;
@WebServlet("/admin/orders/status") /**
 * HTTPリクエストを受け取り、AdminOrderStatusServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
public class AdminOrderStatusServlet extends HttpServlet{private static final Set<String> VALID=Set.of("PENDING","CONFIRMED","PROCESSING","SHIPPED","DELIVERED","COMPLETED","CANCELLED");private final OrderNotificationService service=new OrderNotificationService();/**
 * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doPost(HttpServletRequest r,HttpServletResponse p)throws IOException{String status=r.getParameter("status");try{long id=Long.parseLong(r.getParameter("orderId"));if(id<=0||!VALID.contains(status)){p.sendError(400,"Invalid order status.");return;}OrderNotificationService.UpdateResult result=service.updateStatus(id,status);if(result==OrderNotificationService.UpdateResult.NOT_FOUND){p.sendError(404,"Order not found.");return;}p.sendRedirect(r.getContextPath()+"/admin/orders?message="+(result==OrderNotificationService.UpdateResult.UNCHANGED?"unchanged":"status"));}catch(NumberFormatException e){p.sendError(400,"Invalid order ID.");}catch(SQLException e){getServletContext().log("Admin status and notification update failed",e);p.sendError(500,"Status could not be updated.");}}}
