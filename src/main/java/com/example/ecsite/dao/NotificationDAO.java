package com.example.ecsite.dao;

import com.example.ecsite.model.Notification;
import com.example.ecsite.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Oracleデータベースに対する NotificationDAO のデータアクセス処理をまとめたDAOクラスです。
 *
 * <p>SQLはPreparedStatementで実行し、try-with-resourcesによってJDBCリソースを確実に解放します。</p>
 */
public class NotificationDAO {
    /** 通知検索で共通利用する列を、mapメソッドが読む順番で定義しています。 */
    private static final String COLUMNS = "notification_id,user_id,order_id,notification_type,title,message,target_url,is_read,created_at,read_at";

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<Notification> findRecentByUserId(int userId, int limit) throws SQLException {
        // user_idで所有者を限定し、最新通知だけを指定件数取得します。
        String sql = "SELECT " + COLUMNS + " FROM ec_notifications WHERE user_id=? ORDER BY created_at DESC,notification_id DESC FETCH FIRST ? ROWS ONLY";
        return find(sql, userId, Math.max(1, limit), 0);
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<Notification> findByUserId(int userId, int page, int pageSize) throws SQLException {
        // OFFSET/FETCHを使い、通知履歴をOracle互換のページ単位で取得します。
        String sql = "SELECT " + COLUMNS + " FROM ec_notifications WHERE user_id=? ORDER BY created_at DESC,notification_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        return find(sql, userId, Math.max(0, (page - 1) * pageSize), pageSize);
    }

    private List<Notification> find(String sql, int userId, int second, int third) throws SQLException {
        // userIdは常に第1引数へ設定し、他の利用者の通知を返さないようにします。
        List<Notification> result = new ArrayList<>();
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,userId); p.setInt(2,second); if (third > 0) p.setInt(3,third);
            try (ResultSet r=p.executeQuery()) { while(r.next()) result.add(map(r)); }
        }
        return result;
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public int countByUserId(int userId) throws SQLException {
        return count("SELECT COUNT(*) FROM ec_notifications WHERE user_id=?", userId);
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public int countUnreadByUserId(int userId) throws SQLException {
        // ヘッダーの未読バッジ用に、本人のis_read=0だけを数えます。
        return count("SELECT COUNT(*) FROM ec_notifications WHERE user_id=? AND is_read=0", userId);
    }

    private int count(String sql, int userId) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,userId);try(ResultSet r=p.executeQuery()){r.next();return r.getInt(1);}}
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public Notification findOwned(long notificationId, int userId) throws SQLException {
        // 通知IDだけでなく利用者IDも条件に含めることが、横取り閲覧防止の要点です。
        String sql="SELECT "+COLUMNS+" FROM ec_notifications WHERE notification_id=? AND user_id=?";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,notificationId);p.setInt(2,userId);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}}
    }

    /**
     * 対象を限定する条件を付け、他の利用者のデータへ影響しないように更新します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean markAsRead(long notificationId, int userId) throws SQLException {
        // 本人所有の通知だけを既読にし、初回既読日時はCOALESCEで保持します。
        String sql="UPDATE ec_notifications SET is_read=1,read_at=COALESCE(read_at,SYSTIMESTAMP) WHERE notification_id=? AND user_id=?";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setLong(1,notificationId);p.setInt(2,userId);return p.executeUpdate()==1;}
    }

    /**
     * 対象を限定する条件を付け、他の利用者のデータへ影響しないように更新します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public int markAllAsRead(int userId) throws SQLException {
        // ログイン利用者の未読通知だけを一括更新します。
        String sql="UPDATE ec_notifications SET is_read=1,read_at=SYSTIMESTAMP WHERE user_id=? AND is_read=0";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,userId);return p.executeUpdate();}
    }

    /**
     * 検証済みの値をPreparedStatementへ設定し、新しいデータを安全に保存します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public long createNotification(Notification n) throws SQLException {
        try(Connection c=DBConnection.getConnection()){return createNotification(c,n);}
    }

    /**
     * 検証済みの値をPreparedStatementへ設定し、新しいデータを安全に保存します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public long createNotification(Connection c, Notification n) throws SQLException {
        // 呼び出し元のトランザクション接続を再利用し、注文更新と通知作成を一体化できます。
        String sql="INSERT INTO ec_notifications(user_id,order_id,notification_type,title,message,target_url) VALUES(?,?,?,?,?,?)";
        try(PreparedStatement p=c.prepareStatement(sql,new String[]{"NOTIFICATION_ID"})){
            // 注文に紐付かない通知では、ORDER_IDをSQLのNULLとして保存します。
            p.setInt(1,n.getUserId()); if(n.getOrderId()==null)p.setNull(2,Types.NUMERIC);else p.setLong(2,n.getOrderId());
            p.setString(3,n.getNotificationType());p.setString(4,n.getTitle());p.setString(5,n.getMessage());p.setString(6,n.getTargetUrl());p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Oracle did not return the notification ID.");return r.getLong(1);}
        }
    }

    private Notification map(ResultSet r)throws SQLException{
        // getLongの直後にwasNullを確認し、NULLのORDER_IDを0と取り違えないようにします。
        Notification n=new Notification();n.setNotificationId(r.getLong(1));n.setUserId(r.getInt(2));long order=r.getLong(3);n.setOrderId(r.wasNull()?null:order);n.setNotificationType(r.getString(4));n.setTitle(r.getString(5));n.setMessage(r.getString(6));n.setTargetUrl(r.getString(7));n.setRead(r.getInt(8)==1);n.setCreatedAt(r.getTimestamp(9));n.setReadAt(r.getTimestamp(10));return n;
    }
}
