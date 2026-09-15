package com.example.ecsite.dao;

import com.example.ecsite.model.Product;
import com.example.ecsite.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Oracleデータベースに対する ProductDAO のデータアクセス処理をまとめたDAOクラスです。
 *
 * <p>SQLはPreparedStatementで実行し、try-with-resourcesによってJDBCリソースを確実に解放します。</p>
 */
public class ProductDAO {
    /** 商品一覧を12件などのページ単位で取得し、主画像とカテゴリ名も同時に結合するSQLです。 */
    private static final String FIND_PAGE_SQL = """
            SELECT p.prod_id, p.prod_name, p.category_id, p.weight_class,
                   TO_CHAR(warranty_period), supplier_id, prod_status,
                   list_price, min_price, catalog_url,
                   (SELECT image_url FROM ec_product_images i WHERE i.prod_id=p.prod_id AND i.is_primary='Y') image_url,
                   c.category_name
            FROM products p LEFT JOIN ec_categories c ON c.category_id=p.category_id
            ORDER BY p.prod_id
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """;
    /** PRODUCTSテーブルの総件数を取得するSQLです。 */
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM products";
    /** 商品IDを主キーとして詳細表示用の1件を取得するSQLです。 */
    private static final String FIND_BY_ID_SQL = """
            SELECT p.prod_id, p.prod_name, p.category_id, p.weight_class,
                   TO_CHAR(warranty_period), supplier_id, prod_status,
                   list_price, min_price, catalog_url,
                   (SELECT image_url FROM ec_product_images i WHERE i.prod_id=p.prod_id AND i.is_primary='Y') image_url,
                   c.category_name
            FROM products p LEFT JOIN ec_categories c ON c.category_id=p.category_id
            WHERE p.prod_id = ?
            """;
    /** 現在の商品を除外し、同じカテゴリの商品を関連商品として取得するSQLです。 */
    private static final String FIND_RELATED_SQL = """
            SELECT p.prod_id, p.prod_name, p.category_id, p.weight_class,
                   TO_CHAR(p.warranty_period), p.supplier_id, p.prod_status,
                   p.list_price, p.min_price, p.catalog_url,
                   (SELECT image_url FROM ec_product_images i
                    WHERE i.prod_id = p.prod_id AND i.is_primary = 'Y') image_url,
                   c.category_name
            FROM products p LEFT JOIN ec_categories c ON c.category_id = p.category_id
            WHERE p.category_id = ? AND p.prod_id <> ?
            ORDER BY CASE WHEN p.prod_status = 'orderable' THEN 0 ELSE 1 END, p.prod_id
            FETCH FIRST ? ROWS ONLY
            """;
    /** 現在の商品と現在カテゴリを除外して、別カテゴリの商品を取得するSQLです。 */
    private static final String FIND_OTHER_WITH_CATEGORY_SQL = """
            SELECT p.prod_id, p.prod_name, p.category_id, p.weight_class,
                   TO_CHAR(p.warranty_period), p.supplier_id, p.prod_status,
                   p.list_price, p.min_price, p.catalog_url,
                   (SELECT image_url FROM ec_product_images i
                    WHERE i.prod_id = p.prod_id AND i.is_primary = 'Y') image_url,
                   c.category_name
            FROM products p LEFT JOIN ec_categories c ON c.category_id = p.category_id
            WHERE p.prod_id <> ? AND (p.category_id <> ? OR p.category_id IS NULL)
            ORDER BY CASE WHEN p.prod_status = 'orderable' THEN 0 ELSE 1 END, p.prod_id
            FETCH FIRST ? ROWS ONLY
            """;
    /** カテゴリが未設定の場合に、現在の商品だけを除外して他の商品を取得するSQLです。 */
    private static final String FIND_OTHER_WITHOUT_CATEGORY_SQL = """
            SELECT p.prod_id, p.prod_name, p.category_id, p.weight_class,
                   TO_CHAR(p.warranty_period), p.supplier_id, p.prod_status,
                   p.list_price, p.min_price, p.catalog_url,
                   (SELECT image_url FROM ec_product_images i
                    WHERE i.prod_id = p.prod_id AND i.is_primary = 'Y') image_url,
                   c.category_name
            FROM products p LEFT JOIN ec_categories c ON c.category_id = p.category_id
            WHERE p.prod_id <> ?
            ORDER BY CASE WHEN p.prod_status = 'orderable' THEN 0 ELSE 1 END, p.prod_id
            FETCH FIRST ? ROWS ONLY
            """;

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<Product> findPage(int page, int pageSize) throws SQLException {
        // 不正なページ番号でもOFFSETが負にならないよう、0以上へ補正します。
        if (page < 1 || pageSize < 1) {
            throw new IllegalArgumentException("Page and page size must be positive.");
        }

        int offset = (page - 1) * pageSize;
        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_PAGE_SQL)) {
            statement.setInt(1, offset);
            statement.setInt(2, pageSize);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
                }
            }
        }
        return products;
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public int countProducts() throws SQLException {
        // COUNT(*)のResultSetは必ず1行返るため、1列目を商品総数として読み取ります。
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            throw new SQLException("The product count query returned no result.");
        }
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<Product> searchProducts(String query, String status, Integer categoryId,
                                        BigDecimal minPrice, BigDecimal maxPrice,
                                        String sort, int page, int pageSize) throws SQLException {
        if (page < 1 || pageSize < 1) {
            throw new IllegalArgumentException("Page and page size must be positive.");
        }
        List<Object> parameters = new ArrayList<>();
        String where = buildWhere(query, status, categoryId, minPrice, maxPrice, parameters);
        String sql = """
                SELECT p.prod_id, p.prod_name, p.category_id, p.weight_class,
                       TO_CHAR(warranty_period), supplier_id, prod_status,
                       list_price, min_price, catalog_url,
                       (SELECT image_url FROM ec_product_images i WHERE i.prod_id=p.prod_id AND i.is_primary='Y') image_url,
                       c.category_name
                FROM products p LEFT JOIN ec_categories c ON c.category_id=p.category_id
                """ + where + " ORDER BY " + approvedOrderBy(sort)
                + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        parameters.add((page - 1) * pageSize);
        parameters.add(pageSize);

        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) products.add(mapProduct(resultSet));
            }
        }
        return products;
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public int countSearchResults(String query, String status, Integer categoryId,
                                  BigDecimal minPrice, BigDecimal maxPrice) throws SQLException {
        List<Object> parameters = new ArrayList<>();
        String sql = "SELECT COUNT(*) FROM products p "
                + buildWhere(query, status, categoryId, minPrice, maxPrice, parameters);
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<Integer> findCategoryIds() throws SQLException {
        String sql = "SELECT DISTINCT category_id FROM products "
                + "WHERE category_id IS NOT NULL ORDER BY category_id";
        List<Integer> categoryIds = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) categoryIds.add(resultSet.getInt(1));
        }
        return categoryIds;
    }

    private String buildWhere(String query, String status, Integer categoryId,
                              BigDecimal minPrice, BigDecimal maxPrice,
                              List<Object> parameters) {
        // WHERE句には?だけを追加し、実際の利用者入力はparametersへ分離して保持します。
        List<String> conditions = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            conditions.add("LOWER(p.prod_name) LIKE ?");
            parameters.add("%" + query.trim().toLowerCase(Locale.ROOT) + "%");
        }
        if (status != null && !status.isBlank()) {
            conditions.add("p.prod_status = ?");
            parameters.add(status);
        }
        if (categoryId != null) {
            conditions.add("p.category_id = ?");
            parameters.add(categoryId);
        }
        if (minPrice != null) {
            conditions.add("p.list_price >= ?");
            parameters.add(minPrice);
        }
        if (maxPrice != null) {
            conditions.add("p.list_price <= ?");
            parameters.add(maxPrice);
        }
        return conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);
    }

    private String approvedOrderBy(String sort) {
        // ORDER BYはプレースホルダー化できないため、許可済み候補だけを返します。
        if (sort == null) return "prod_id ASC";
        return switch (sort) {
            case "id_desc" -> "prod_id DESC";
            case "price_asc" -> "list_price ASC NULLS LAST, prod_id ASC";
            case "price_desc" -> "list_price DESC NULLS LAST, prod_id ASC";
            case "name_asc" -> "LOWER(prod_name) ASC NULLS LAST, prod_id ASC";
            case "name_desc" -> "LOWER(prod_name) DESC NULLS LAST, prod_id ASC";
            default -> "prod_id ASC";
        };
    }

    private void bindParameters(PreparedStatement statement, List<Object> values)
            throws SQLException {
        // Java型に応じたsetXXXを使い、検索条件を安全にPreparedStatementへ設定します。
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            if (value instanceof Integer integer) statement.setInt(i + 1, integer);
            else if (value instanceof BigDecimal decimal) statement.setBigDecimal(i + 1, decimal);
            else statement.setString(i + 1, (String) value);
        }
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public Product findById(int productId) throws SQLException {
        // 主キー条件をPreparedStatementへ設定し、存在しない場合はnullを返します。
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapProduct(resultSet) : null;
            }
        }
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<Product> findRelatedProducts(int currentProductId, Integer categoryId, int limit)
            throws SQLException {
        validateRecommendationArguments(currentProductId, limit);
        if (categoryId == null) return new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_RELATED_SQL)) {
            statement.setInt(1, categoryId);
            statement.setInt(2, currentProductId);
            statement.setInt(3, limit);
            return readProducts(statement);
        }
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<Product> findOtherProducts(int currentProductId, Integer excludedCategoryId, int limit)
            throws SQLException {
        validateRecommendationArguments(currentProductId, limit);
        String sql = excludedCategoryId == null
                ? FIND_OTHER_WITHOUT_CATEGORY_SQL : FIND_OTHER_WITH_CATEGORY_SQL;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentProductId);
            if (excludedCategoryId == null) {
                statement.setInt(2, limit);
            } else {
                statement.setInt(2, excludedCategoryId);
                statement.setInt(3, limit);
            }
            return readProducts(statement);
        }
    }

    private void validateRecommendationArguments(int currentProductId, int limit) {
        if (currentProductId < 1 || limit < 1) {
            throw new IllegalArgumentException("Product ID and limit must be positive.");
        }
    }

    private List<Product> readProducts(PreparedStatement statement) throws SQLException {
        // 同じマッピング処理を一覧・検索・関連商品の各検索で共有します。
        List<Product> products = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) products.add(mapProduct(resultSet));
        }
        return products;
    }

    /**
     * 対象を限定する条件を付け、他の利用者のデータへ影響しないように更新します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean update(Product product) throws SQLException {
        // PROD_IDはWHERE条件にのみ使い、既存商品の主キー自体は変更しません。
        String sql = """
                UPDATE products SET prod_name=?, category_id=?, weight_class=?,
                    supplier_id=?, prod_status=?, list_price=?, min_price=?, catalog_url=?
                WHERE prod_id=?
                """;
        try (Connection c=DBConnection.getConnection(); PreparedStatement s=c.prepareStatement(sql)) {
            s.setString(1, product.getProductName()); setInteger(s,2,product.getCategoryId());
            setInteger(s,3,product.getWeightClass()); setInteger(s,4,product.getSupplierId());
            s.setString(5,product.getProductStatus()); s.setBigDecimal(6,product.getListPrice());
            s.setBigDecimal(7,product.getMinimumPrice()); s.setString(8,product.getCatalogUrl());
            s.setInt(9,product.getProductId()); return s.executeUpdate()==1;
        }
    }

    /**
     * 検証済みの値をPreparedStatementへ設定し、新しいデータを安全に保存します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public int createProduct(Product product) throws SQLException {
        // IDの衝突を避けるため、OracleシーケンスのNEXTVALを同一トランザクションで利用します。
        String insertSql = """
                INSERT INTO products
                    (prod_id, prod_name, category_id, weight_class, warranty_period,
                     supplier_id, prod_status, list_price, min_price, catalog_url)
                VALUES
                    (ec_product_seq.NEXTVAL, ?, ?, ?, NULL, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                statement.setString(1, product.getProductName());
                setInteger(statement, 2, product.getCategoryId());
                setInteger(statement, 3, product.getWeightClass());
                setInteger(statement, 4, product.getSupplierId());
                statement.setString(5, product.getProductStatus());
                statement.setBigDecimal(6, product.getListPrice());
                statement.setBigDecimal(7, product.getMinimumPrice());
                statement.setString(8, product.getCatalogUrl());
                if (statement.executeUpdate() != 1) throw new SQLException("Product was not inserted.");
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT ec_product_seq.CURRVAL FROM dual");
                 ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) throw new SQLException("Product ID was not returned.");
                int productId = resultSet.getInt(1);
                connection.commit();
                return productId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    /**
     * 対象IDと所有条件を確認し、許可されたデータだけを削除または解除します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean delete(int productId) throws SQLException {
        // 注文明細から参照済みの商品は外部キーにより削除が拒否され、履歴が守られます。
        try (Connection c=DBConnection.getConnection(); PreparedStatement s=c.prepareStatement("DELETE FROM products WHERE prod_id=?")) {
            s.setInt(1,productId); return s.executeUpdate()==1;
        }
    }

    private void setInteger(PreparedStatement s,int index,Integer value)throws SQLException {
        if(value==null)s.setNull(index,java.sql.Types.INTEGER); else s.setInt(index,value);
    }

    private Product mapProduct(ResultSet resultSet) throws SQLException {
        // SELECT句の列順をProductコンストラクターと一致させ、主画像とカテゴリ名も設定します。
        Product product = new Product(
                resultSet.getInt(1),
                resultSet.getString(2),
                nullableInteger(resultSet, 3),
                nullableInteger(resultSet, 4),
                resultSet.getString(5),
                nullableInteger(resultSet, 6),
                resultSet.getString(7),
                resultSet.getBigDecimal(8),
                resultSet.getBigDecimal(9),
                resultSet.getString(10)
        );
        product.setImageUrl(resultSet.getString(11));
        product.setCategoryName(resultSet.getString(12));
        return product;
    }

    private Integer nullableInteger(ResultSet resultSet, int column) throws SQLException {
        // getIntはNULLでも0を返すため、wasNullで本当のNULLを復元します。
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }
}
