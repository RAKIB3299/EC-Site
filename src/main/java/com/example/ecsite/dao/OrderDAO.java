package com.example.ecsite.dao;

import com.example.ecsite.model.CartItem;
import com.example.ecsite.model.Order;
import com.example.ecsite.model.OrderItem;
import com.example.ecsite.model.Product;
import com.example.ecsite.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Oracleデータベースに対する OrderDAO のデータアクセス処理をまとめたDAOクラスです。
 *
 * <p>SQLはPreparedStatementで実行し、try-with-resourcesによってJDBCリソースを確実に解放します。</p>
 */
public class OrderDAO {
    /** 注文確定直前に、商品名と現在価格をPRODUCTSから再取得するSQLです。 */
    private static final String FIND_PRODUCT_SQL = """
            SELECT prod_id, prod_name, list_price FROM products WHERE prod_id = ?
            """;
    /** 注文ヘッダーをEC_ORDERSへ保存するSQLです。 */
    private static final String INSERT_ORDER_SQL = """
            INSERT INTO ec_orders (user_id, total_price, shipping_address)
            VALUES (?, ?, ?)
            """;
    /** 購入時点の単価と数量をEC_ORDER_ITEMSへ保存するSQLです。 */
    private static final String INSERT_ITEM_SQL = """
            INSERT INTO ec_order_items (order_id, prod_id, quantity, unit_price)
            VALUES (?, ?, ?, ?)
            """;
    /** 作成後の注文ヘッダーをIDで再取得するSQLです。 */
    private static final String FIND_ORDER_SQL = """
            SELECT order_id, user_id, total_price, status, shipping_address, order_date
            FROM ec_orders WHERE order_id = ?
            """;
    /** ログイン利用者自身の注文だけを新しい順で取得するSQLです。 */
    private static final String FIND_BY_USER_SQL = """
            SELECT o.order_id, o.user_id, o.total_price, o.status,
                   o.shipping_address, o.order_date,
                   MIN(img.image_url) KEEP (DENSE_RANK FIRST ORDER BY i.order_item_id),
                   COUNT(i.order_item_id)
            FROM ec_orders o
            LEFT JOIN ec_order_items i ON i.order_id = o.order_id
            LEFT JOIN ec_product_images img
              ON img.prod_id = i.prod_id AND img.is_primary = 'Y'
            WHERE o.user_id = ?
            GROUP BY o.order_id, o.user_id, o.total_price, o.status,
                     o.shipping_address, o.order_date
            ORDER BY o.order_date DESC, o.order_id DESC
            """;
    /** 注文IDと利用者IDの両方を条件にし、他人の注文を取得させないSQLです。 */
    private static final String FIND_BY_ID_FOR_USER_SQL = """
            SELECT order_id, user_id, total_price, status, shipping_address, order_date
            FROM ec_orders
            WHERE order_id = ? AND user_id = ?
            """;
    /** 注文明細・商品名・現在の主画像をLEFT JOINでまとめて取得するSQLです。 */
    private static final String FIND_ITEMS_SQL = """
            SELECT i.order_item_id, i.order_id, i.prod_id, i.quantity,
                   i.unit_price, p.prod_name, img.image_url
            FROM ec_order_items i
            JOIN products p ON p.prod_id = i.prod_id
            LEFT JOIN ec_product_images img
              ON img.prod_id = i.prod_id AND img.is_primary = 'Y'
            WHERE i.order_id = ?
            ORDER BY i.order_item_id
            """;
    /** 管理者向けに全注文と購入者情報を新しい順で取得するSQLです。 */
    private static final String FIND_ALL_ADMIN_SQL = """
            SELECT o.order_id,o.user_id,o.total_price,o.status,o.shipping_address,o.order_date,
                   u.first_name||' '||u.last_name,u.email
            FROM ec_orders o JOIN app_users u ON u.user_id=o.user_id
            ORDER BY o.order_date DESC,o.order_id DESC
            """;
    /** 管理者が指定した注文を購入者情報付きで1件取得するSQLです。 */
    private static final String FIND_ADMIN_ORDER_SQL = """
            SELECT o.order_id,o.user_id,o.total_price,o.status,o.shipping_address,o.order_date,
                   u.first_name||' '||u.last_name,u.email
            FROM ec_orders o JOIN app_users u ON u.user_id=o.user_id WHERE o.order_id=?
            """;

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<Order> findAllForAdmin() throws SQLException {
        List<Order> result=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(FIND_ALL_ADMIN_SQL);ResultSet r=s.executeQuery()){
            while(r.next())result.add(mapAdminOrder(r));
        } return result;
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public Order findByIdForAdmin(long id)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(FIND_ADMIN_ORDER_SQL)){
            s.setLong(1,id);try(ResultSet r=s.executeQuery()){return r.next()?mapAdminOrder(r):null;}
        }
    }

    /**
     * 対象を限定する条件を付け、他の利用者のデータへ影響しないように更新します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean updateStatus(long id,String status)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("UPDATE ec_orders SET status=? WHERE order_id=?")){
            s.setString(1,status);s.setLong(2,id);return s.executeUpdate()==1;
        }
    }

    private Order mapAdminOrder(ResultSet r)throws SQLException{
        Order o=mapOrder(r);o.setCustomerName(r.getString(7));o.setCustomerEmail(r.getString(8));return o;
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<Order> findByUserId(long userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USER_SQL)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Order order=mapOrder(resultSet);
                    order.setPreviewImageUrl(resultSet.getString(7));
                    order.setItemCount(resultSet.getInt(8));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public Order findByIdForUser(long orderId, long userId) throws SQLException {
        // user_idもWHERE条件に含め、URLのorderIdを書き換えても他人の注文を返しません。
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_FOR_USER_SQL)) {
            statement.setLong(1, orderId);
            statement.setLong(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapOrder(resultSet) : null;
            }
        }
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<OrderItem> findItemsByOrderId(long orderId) throws SQLException {
        // LEFT JOINにより画像未登録の商品を落とさず、プレースホルダー表示を可能にします。
        List<OrderItem> items = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ITEMS_SQL)) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(resultSet.getLong(1));
                    item.setOrderId(resultSet.getLong(2));
                    item.setProdId(resultSet.getInt(3));
                    item.setQuantity(resultSet.getInt(4));
                    item.setUnitPrice(resultSet.getBigDecimal(5));
                    item.setProductName(resultSet.getString(6));
                    item.setImageUrl(resultSet.getString(7));
                    items.add(item);
                }
            }
        }
        return items;
    }

    /**
     * 検証済みの値をPreparedStatementへ設定し、新しいデータを安全に保存します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public Order createOrder(int userId, String shippingAddress,
                             Map<Integer, CartItem> cart) throws SQLException {
        // 注文ヘッダーと全明細は一体なので、どれか一つでも失敗したら全体を取り消します。
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("The cart is empty.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            // 注文と注文明細を一つの処理として保存するため、トランザクションを開始します。
            connection.setAutoCommit(false);
            try {
                List<OrderItem> items = reloadItems(connection, cart);
                BigDecimal total = items.stream()
                        .map(OrderItem::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                long orderId = insertOrder(connection, userId, total, shippingAddress);
                insertItems(connection, orderId, items);
                Order order = findOrder(connection, orderId);
                // すべての登録に成功した場合だけ変更を確定します。
                connection.commit();
                return order;
            } catch (SQLException | RuntimeException e) {
                try {
                    // 途中で失敗した場合は、不完全な注文を残さないように取り消します。
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // The connection is about to close.
                }
            }
        }
    }

    private List<OrderItem> reloadItems(Connection connection,
                                        Map<Integer, CartItem> cart) throws SQLException {
        // カート側の価格を信用せず、確定時点のPRODUCTS.LIST_PRICEをDBから読み直します。
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(FIND_PRODUCT_SQL)) {
            for (Map.Entry<Integer, CartItem> entry : cart.entrySet()) {
                int quantity = entry.getValue().getQuantity();
                if (quantity <= 0) {
                    throw new SQLException("A cart item has an invalid quantity.");
                }
                statement.setInt(1, entry.getKey());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("A product in the cart no longer exists.");
                    }
                    BigDecimal livePrice = resultSet.getBigDecimal(3);
                    if (livePrice == null || livePrice.signum() < 0) {
                        throw new SQLException("A product in the cart has no valid price.");
                    }
                    Product liveProduct = new Product();
                    liveProduct.setProductId(resultSet.getInt(1));
                    liveProduct.setProductName(resultSet.getString(2));
                    liveProduct.setListPrice(livePrice);
                    entry.getValue().setProduct(liveProduct);
                    items.add(new OrderItem(0, liveProduct.getProductId(), quantity, livePrice));
                }
            }
        }
        return items;
    }

    private long insertOrder(Connection connection, int userId, BigDecimal total,
                             String shippingAddress) throws SQLException {
        // Oracle identityが生成したORDER_IDをGeneratedKeysから受け取ります。
        try (PreparedStatement statement = connection.prepareStatement(
                INSERT_ORDER_SQL, new String[]{"ORDER_ID"})) {
            statement.setInt(1, userId);
            statement.setBigDecimal(2, total);
            statement.setString(3, shippingAddress);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Oracle did not return the generated order ID.");
                }
                return keys.getLong(1);
            }
        }
    }

    private void insertItems(Connection connection, long orderId,
                             List<OrderItem> items) throws SQLException {
        // 同じPreparedStatementを再利用して、各商品の購入時単価を順番に保存します。
        try (PreparedStatement statement = connection.prepareStatement(INSERT_ITEM_SQL)) {
            for (OrderItem item : items) {
                statement.setLong(1, orderId);
                statement.setInt(2, item.getProdId());
                statement.setInt(3, item.getQuantity());
                statement.setBigDecimal(4, item.getUnitPrice());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private Order findOrder(Connection connection, long orderId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_ORDER_SQL)) {
            statement.setLong(1, orderId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) throw new SQLException("The new order could not be read.");
                return mapOrder(resultSet);
            }
        }
    }

    private Order mapOrder(ResultSet resultSet) throws SQLException {
        Order order = new Order();
        order.setOrderId(resultSet.getLong(1));
        order.setUserId(resultSet.getInt(2));
        order.setTotalPrice(resultSet.getBigDecimal(3));
        order.setStatus(resultSet.getString(4));
        order.setShippingAddress(resultSet.getString(5));
        order.setOrderDate(resultSet.getTimestamp(6));
        return order;
    }
}
