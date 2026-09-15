package com.example.ecsite.dao;

import com.example.ecsite.model.Banner;
import com.example.ecsite.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Oracleデータベースに対する BannerDAO のデータアクセス処理をまとめたDAOクラスです。
 *
 * <p>SQLはPreparedStatementで実行し、try-with-resourcesによってJDBCリソースを確実に解放します。</p>
 */
public class BannerDAO {
    /** 管理画面から登録できるバナーの上限数です。 */
    public static final int MAX_BANNERS = 4;

    /** SELECT句で共通利用する列を、ResultSetのマッピング順に並べています。 */
    private static final String COLS =
            "banner_id,title,image_url,link_url,display_order,is_active,created_at,updated_at";
    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<Banner> findActiveBanners() throws SQLException {
        // 公開対象だけを表示順で取得し、同順位の場合はIDで順序を安定させます。
        return find("SELECT " + COLS
                + " FROM ec_site_banners WHERE is_active='Y' ORDER BY display_order,banner_id");
    }
    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<Banner> findAll() throws SQLException {
        // 管理画面用なので、有効・無効を問わずすべてのバナーを取得します。
        return find("SELECT " + COLS + " FROM ec_site_banners ORDER BY display_order,banner_id");
    }

    /**
     * 引数で受け取った固定SQLを実行し、取得行をBannerへ変換します。
     *
     * @param sql 呼び出し元で組み立て済みの、利用者入力を含まないSELECT文
     * @return SQLの並び順を保ったバナー一覧
     * @throws SQLException 接続または検索に失敗した場合
     */
    private List<Banner> find(String sql) throws SQLException {
        List<Banner> list = new ArrayList<>();
        // try-with-resourcesにより、検索完了時に接続・文・結果を逆順で自動解放します。
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            // ResultSetの各行を画面で扱いやすいBannerオブジェクトへ変換します。
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }
    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public int count() throws SQLException {
        // COUNT(*)は必ず1行返すため、最初の列から登録件数を読み取ります。
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM ec_site_banners");
             ResultSet r = s.executeQuery()) {
            r.next();
            return r.getInt(1);
        }
    }
    /**
     * 検証済みの値をPreparedStatementへ設定し、新しいデータを安全に保存します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean create(Banner banner) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            // 上限確認とINSERTを一つの処理として確定するため、自動コミットを停止します。
            c.setAutoCommit(false);
            try {
                // 同時登録による上限超過を防ぐため、件数確認の間だけ表を排他ロックします。
                try (PreparedStatement lock = c.prepareStatement(
                        "LOCK TABLE ec_site_banners IN EXCLUSIVE MODE")) {
                    lock.execute();
                }
                // 現在件数を調べ、4件以上なら業務上の専用例外を返します。
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT COUNT(*) FROM ec_site_banners");
                     ResultSet r = s.executeQuery()) {
                    r.next();
                    if (r.getInt(1) >= MAX_BANNERS) {
                        throw new BannerLimitException();
                    }
                }
                // 値はプレースホルダーへ設定し、文字列連結によるSQL注入を防ぎます。
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO ec_site_banners(title,image_url,link_url,display_order,is_active) VALUES(?,?,?,?,?)")) {
                    bind(s, banner, 1);
                    s.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLException e) {
                // 途中で失敗した場合は、件数確認後の変更をまとめて取り消します。
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }
    /**
     * 対象を限定する条件を付け、他の利用者のデータへ影響しないように更新します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean update(Banner banner) throws SQLException {
        String sql = "UPDATE ec_site_banners SET title=?,link_url=?,display_order=?,is_active=?,updated_at=SYSTIMESTAMP WHERE banner_id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            // UPDATE文の?と同じ順番で、編集可能な値と対象IDを設定します。
            s.setString(1, banner.getTitle());
            s.setString(2, banner.getLinkUrl());
            s.setInt(3, banner.getDisplayOrder());
            s.setString(4, banner.isActive() ? "Y" : "N");
            s.setLong(5, banner.getBannerId());
            return s.executeUpdate() == 1;
        }
    }
    /**
     * 対象IDと所有条件を確認し、許可されたデータだけを削除または解除します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public Banner delete(long id) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // 削除前の情報は、画像ファイルの後処理などを呼び出し元が行えるよう保持します。
                Banner old = findById(c, id);
                if (old == null) {
                    c.rollback();
                    return null;
                }
                try (PreparedStatement s = c.prepareStatement(
                        "DELETE FROM ec_site_banners WHERE banner_id=?")) {
                    s.setLong(1, id);
                    s.executeUpdate();
                }
                c.commit();
                return old;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** 同じ接続を利用して、指定IDのバナーを検索します。 */
    private Banner findById(Connection c, long id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "SELECT " + COLS + " FROM ec_site_banners WHERE banner_id=?")) {
            s.setLong(1, id);
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    /** Bannerの値をINSERT文の連続したプレースホルダーへ設定します。 */
    private void bind(PreparedStatement s, Banner b, int n) throws SQLException {
        s.setString(n, b.getTitle());
        s.setString(n + 1, b.getImageUrl());
        s.setString(n + 2, b.getLinkUrl());
        s.setInt(n + 3, b.getDisplayOrder());
        s.setString(n + 4, b.isActive() ? "Y" : "N");
    }

    /** ResultSetの現在行をBannerへ変換します。 */
    private Banner map(ResultSet r) throws SQLException {
        Banner b = new Banner();
        b.setBannerId(r.getLong(1));
        b.setTitle(r.getString(2));
        b.setImageUrl(r.getString(3));
        b.setLinkUrl(r.getString(4));
        b.setDisplayOrder(r.getInt(5));
        b.setActive("Y".equals(r.getString(6)));
        b.setCreatedAt(r.getTimestamp(7));
        b.setUpdatedAt(r.getTimestamp(8));
        return b;
    }

    /** バナー上限超過を呼び出し元へ区別して知らせる例外です。 */
    public static class BannerLimitException extends SQLException {
        public BannerLimitException() {
            super("Maximum 4 banners.");
        }
    }
}
