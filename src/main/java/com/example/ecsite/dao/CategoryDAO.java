package com.example.ecsite.dao;

import com.example.ecsite.model.Category;
import com.example.ecsite.model.CategoryOverview;
import com.example.ecsite.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Oracleデータベースに対する CategoryDAO のデータアクセス処理をまとめたDAOクラスです。
 *
 * <p>SQLはPreparedStatementで実行し、try-with-resourcesによってJDBCリソースを確実に解放します。</p>
 */
public class CategoryDAO {
    private static final String OVERVIEW_SQL = """
            SELECT
                (SELECT COUNT(*) FROM ec_categories) AS total_categories,
                (SELECT COUNT(*) FROM ec_categories WHERE is_active = 'Y') AS active_categories,
                (SELECT COUNT(*) FROM ec_categories WHERE is_active <> 'Y' OR is_active IS NULL) AS inactive_categories,
                (SELECT COUNT(*) FROM products) AS total_products,
                (SELECT COUNT(*) FROM products WHERE category_id IS NULL) AS uncategorized_products
            FROM dual
            """;

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<Category> findActiveCategories() throws SQLException {
        // 顧客画面に表示する有効カテゴリだけを、管理画面で指定した順序で取得します。
        return query("SELECT category_id, category_name, display_order, is_active, 0 product_count FROM ec_categories WHERE is_active='Y' ORDER BY display_order NULLS LAST, category_name");
    }
    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public Category findById(int categoryId) throws SQLException {
        // カテゴリIDは?へ設定し、入力値をSQL文字列へ直接連結しません。
        try (Connection c=DBConnection.getConnection();
         PreparedStatement s=c.prepareStatement(
                "SELECT category_id, category_name, display_order, is_active, 0 product_count FROM ec_categories WHERE category_id=?")) {
            s.setInt(1, categoryId);
            try (ResultSet r=s.executeQuery()) { return r.next() ? map(r) : null; }
        }
    }
    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<Category> findAll() throws SQLException {
        // LEFT JOINにより商品が0件のカテゴリも残し、COUNTで所属商品数を取得します。
        return query("SELECT c.category_id, c.category_name, c.display_order, c.is_active, COUNT(p.prod_id) product_count FROM ec_categories c LEFT JOIN products p ON p.category_id=c.category_id GROUP BY c.category_id,c.category_name,c.display_order,c.is_active ORDER BY c.display_order NULLS LAST,c.category_id");
    }

    /**
     * 商品をJavaへ読み込まず、Oracle上で概要パネル用の件数を集計します。
     *
     * @return カテゴリと商品の集計値
     * @throws SQLException 集計SQLの実行に失敗した場合
     */
    public CategoryOverview findOverview() throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(OVERVIEW_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new SQLException("Category overview query returned no row.");
            }
            return new CategoryOverview(
                    resultSet.getInt(1),
                    resultSet.getInt(2),
                    resultSet.getInt(3),
                    resultSet.getInt(4),
                    resultSet.getInt(5));
        }
    }
    /**
     * 対象を限定する条件を付け、他の利用者のデータへ影響しないように更新します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean updateCategoryName(int categoryId, String categoryName) throws SQLException {
        try (Connection c=DBConnection.getConnection(); PreparedStatement s=c.prepareStatement(
                "UPDATE ec_categories SET category_name=? WHERE category_id=?")) {
            // 名前と更新対象IDをプレースホルダーへ安全に設定します。
            s.setString(1, categoryName); s.setInt(2, categoryId); return s.executeUpdate()==1;
        }
    }
    /**
     * 対象を限定する条件を付け、他の利用者のデータへ影響しないように更新します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean updateCategory(int categoryId, String categoryName, int displayOrder, boolean active) throws SQLException {
        try (Connection c=DBConnection.getConnection(); PreparedStatement s=c.prepareStatement(
                "UPDATE ec_categories SET category_name=?, display_order=?, is_active=? WHERE category_id=?")) {
            // JavaのbooleanはOracle側のY/N表現へ変換して保存します。
            s.setString(1, categoryName); s.setInt(2, displayOrder); s.setString(3, active?"Y":"N"); s.setInt(4, categoryId);
            return s.executeUpdate()==1;
        }
    }
    private List<Category> query(String sql) throws SQLException {
        // 共通の一覧取得処理をまとめ、接続・文・結果をtry-with-resourcesで解放します。
        List<Category> values=new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); PreparedStatement s=c.prepareStatement(sql); ResultSet r=s.executeQuery()) {
            while(r.next()) values.add(map(r));
        }
        return values;
    }
    private Category map(ResultSet r) throws SQLException {
        // ResultSetの現在行をCategoryへ変換し、集計した商品数も設定します。
        Category c=new Category(r.getInt("category_id"),r.getString("category_name"),r.getInt("display_order"),"Y".equals(r.getString("is_active")));
        c.setProductCount(r.getInt("product_count")); return c;
    }
}
