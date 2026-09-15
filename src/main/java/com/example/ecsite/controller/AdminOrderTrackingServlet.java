package com.example.ecsite.controller;

import com.example.ecsite.model.OrderTracking;
import com.example.ecsite.service.OrderNotificationService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * HTTPリクエストを受け取り、AdminOrderTrackingServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/admin/orders/tracking")
public class AdminOrderTrackingServlet extends HttpServlet {
    private final OrderNotificationService service=new OrderNotificationService();
    /**
     * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    protected void doPost(HttpServletRequest r,HttpServletResponse p)throws IOException{
        Long id=positive(r.getParameter("orderId"));if(id==null){p.sendError(400,"Invalid order ID.");return;}
        OrderTracking t=new OrderTracking();t.setOrderId(id);t.setCarrier(clean(r.getParameter("carrier"),100));t.setTrackingNumber(clean(r.getParameter("trackingNumber"),100));t.setShipmentStatus(clean(r.getParameter("shipmentStatus"),50));t.setTrackingInformation(clean(r.getParameter("trackingInformation"),1000));
        try{OrderNotificationService.UpdateResult result=service.updateTracking(t);if(result==OrderNotificationService.UpdateResult.NOT_FOUND){p.sendError(404,"Order not found.");return;}p.sendRedirect(r.getContextPath()+"/admin/orders/detail?id="+id+"&message="+(result==OrderNotificationService.UpdateResult.UNCHANGED?"tracking-unchanged":"tracking"));}catch(SQLException e){getServletContext().log("Tracking and notification update failed",e);p.sendError(500,"Tracking information could not be updated.");}
    }
    private Long positive(String v){try{long n=Long.parseLong(v);return n>0?n:null;}catch(Exception e){return null;}}
    private String clean(String v,int max){if(v==null||v.trim().isEmpty())return null;String x=v.trim();return x.length()<=max?x:x.substring(0,max);}
}
