package com.example.ecsite.service;

import com.example.ecsite.dao.NotificationDAO;
import com.example.ecsite.dao.OrderTrackingDAO;
import com.example.ecsite.model.Notification;
import com.example.ecsite.model.Order;
import com.example.ecsite.model.OrderTracking;
import com.example.ecsite.util.DBConnection;
import java.sql.*;
import java.util.Map;
import java.util.Objects;

/** Keeps an order/tracking update and its in-site notification in one transaction. */
/**
 * 複数の処理を安全に連携させる OrderNotificationService のサービスクラスです。
 *
 * <p>データの整合性を守りながらDAOを組み合わせ、画面から独立した業務処理を担当します。</p>
 */
public class OrderNotificationService {
    private static final Map<String,String[]> EVENTS=Map.of(
        "CONFIRMED",new String[]{"ORDER_CONFIRMED","Order Confirmed","Your order #%d has been confirmed."},
        "PROCESSING",new String[]{"ORDER_PROCESSING","Order Processing","Your order #%d is now being processed."},
        "SHIPPED",new String[]{"ORDER_SHIPPED","Order Shipped","Your order #%d has been shipped."},
        "DELIVERED",new String[]{"ORDER_DELIVERED","Order Delivered","Your order #%d has been delivered."},
        "CANCELLED",new String[]{"ORDER_CANCELLED","Order Cancelled","Your order #%d has been cancelled."}
    );
    private final NotificationDAO notifications=new NotificationDAO();
    private final OrderTrackingDAO trackingDAO=new OrderTrackingDAO();
    private final EmailService email=new EmailService();

    /**
     * 対象を限定する条件を付け、他の利用者のデータへ影響しないように更新します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public UpdateResult updateStatus(long orderId,String newStatus)throws SQLException{
        Order emailOrder=null;String emailType=null;boolean changed=false;
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                Order order=lockOrder(c,orderId);if(order==null){c.rollback();return UpdateResult.NOT_FOUND;}
                if(Objects.equals(order.getStatus(),newStatus)){c.rollback();return UpdateResult.UNCHANGED;}
                try(PreparedStatement p=c.prepareStatement("UPDATE ec_orders SET status=? WHERE order_id=?")){p.setString(1,newStatus);p.setLong(2,orderId);p.executeUpdate();}
                String[] event=EVENTS.get(newStatus);
                if(event!=null){notifications.createNotification(c,notification(order,event));emailOrder=order;emailOrder.setStatus(newStatus);emailType=event[0];}
                c.commit();changed=true;
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
        if(changed&&emailOrder!=null)email.sendOrderEvent(emailOrder,emailType,null);
        return UpdateResult.UPDATED;
    }

    /**
     * 対象を限定する条件を付け、他の利用者のデータへ影響しないように更新します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public UpdateResult updateTracking(OrderTracking tracking)throws SQLException{
        Order emailOrder=null;boolean changed=false;
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                Order order=lockOrder(c,tracking.getOrderId());if(order==null){c.rollback();return UpdateResult.NOT_FOUND;}
                OrderTracking old=trackingDAO.findByOrderId(c,tracking.getOrderId());
                if(same(old,tracking)){c.rollback();return UpdateResult.UNCHANGED;}
                trackingDAO.save(c,tracking);
                String[] event={"TRACKING_UPDATED","Tracking Updated","Tracking information for order #%d has been updated."};
                notifications.createNotification(c,notification(order,event));
                c.commit();emailOrder=order;changed=true;
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
        if(changed)email.sendOrderEvent(emailOrder,"TRACKING_UPDATED",tracking);
        return UpdateResult.UPDATED;
    }

    private Notification notification(Order o,String[] event){Notification n=new Notification();n.setUserId(o.getUserId());n.setOrderId(o.getOrderId());n.setNotificationType(event[0]);n.setTitle(event[1]);n.setMessage(event[2].formatted(o.getOrderId()));n.setTargetUrl("/orders/detail?id="+o.getOrderId());return n;}
    private Order lockOrder(Connection c,long id)throws SQLException{
        String sql="SELECT o.order_id,o.user_id,o.total_price,o.status,o.shipping_address,o.order_date,u.first_name||' '||u.last_name,u.email FROM ec_orders o JOIN app_users u ON u.user_id=o.user_id WHERE o.order_id=? FOR UPDATE OF o.status";
        try(PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())return null;Order o=new Order();o.setOrderId(r.getLong(1));o.setUserId(r.getInt(2));o.setTotalPrice(r.getBigDecimal(3));o.setStatus(r.getString(4));o.setShippingAddress(r.getString(5));o.setOrderDate(r.getTimestamp(6));o.setCustomerName(r.getString(7));o.setCustomerEmail(r.getString(8));return o;}}
    }
    private boolean same(OrderTracking a,OrderTracking b){return a!=null&&Objects.equals(a.getCarrier(),b.getCarrier())&&Objects.equals(a.getTrackingNumber(),b.getTrackingNumber())&&Objects.equals(a.getShipmentStatus(),b.getShipmentStatus())&&Objects.equals(a.getTrackingInformation(),b.getTrackingInformation());}
    public enum UpdateResult{UPDATED,UNCHANGED,NOT_FOUND}
}
