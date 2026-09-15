package com.example.ecsite.dao;

import com.example.ecsite.model.OrderTracking;
import com.example.ecsite.util.DBConnection;
import java.sql.*;

/**
 * Oracleデータベースに対する OrderTrackingDAO のデータアクセス処理をまとめたDAOクラスです。
 *
 * <p>SQLはPreparedStatementで実行し、try-with-resourcesによってJDBCリソースを確実に解放します。</p>
 */
public class OrderTrackingDAO {
    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public OrderTracking findByOrderId(long orderId) throws SQLException {
        // 単独検索では接続をここで取得し、try-with-resourcesで確実に閉じます。
        try(Connection c=DBConnection.getConnection()){return findByOrderId(c,orderId);}
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public OrderTracking findByOrderId(Connection c,long orderId)throws SQLException{
        // 注文更新と同じトランザクションから呼べるよう、接続を引数で受け取ります。
        String sql="SELECT order_id,carrier,tracking_number,shipment_status,tracking_information,updated_at FROM ec_order_tracking WHERE order_id=?";
        try(PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,orderId);try(ResultSet r=p.executeQuery()){if(!r.next())return null;OrderTracking t=new OrderTracking();t.setOrderId(r.getLong(1));t.setCarrier(r.getString(2));t.setTrackingNumber(r.getString(3));t.setShipmentStatus(r.getString(4));t.setTrackingInformation(r.getString(5));t.setUpdatedAt(r.getTimestamp(6));return t;}}
    }

    /**
     * 検証済みの値をPreparedStatementへ設定し、新しいデータを安全に保存します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public void save(Connection c,OrderTracking t)throws SQLException{
        // Oracle MERGEにより、既存行は更新し、未登録なら同じSQLで新規作成します。
        String sql="MERGE INTO ec_order_tracking x USING (SELECT ? order_id FROM dual) s ON (x.order_id=s.order_id) WHEN MATCHED THEN UPDATE SET carrier=?,tracking_number=?,shipment_status=?,tracking_information=?,updated_at=SYSTIMESTAMP WHEN NOT MATCHED THEN INSERT(order_id,carrier,tracking_number,shipment_status,tracking_information) VALUES(?,?,?,?,?)";
        try(PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,t.getOrderId());p.setString(2,t.getCarrier());p.setString(3,t.getTrackingNumber());p.setString(4,t.getShipmentStatus());p.setString(5,t.getTrackingInformation());p.setLong(6,t.getOrderId());p.setString(7,t.getCarrier());p.setString(8,t.getTrackingNumber());p.setString(9,t.getShipmentStatus());p.setString(10,t.getTrackingInformation());p.executeUpdate();}
    }
}
