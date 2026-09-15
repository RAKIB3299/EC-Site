package com.example.ecsite.dao;

import com.example.ecsite.model.User;
import com.example.ecsite.util.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Oracleデータベースに対する UserDAO のデータアクセス処理をまとめたDAOクラスです。
 *
 * <p>SQLはPreparedStatementで実行し、try-with-resourcesによってJDBCリソースを確実に解放します。</p>
 */
public class UserDAO {
    /** メールアドレスを小文字へ正規化して、一致する利用者を取得するSQLです。 */
    private static final String FIND_BY_EMAIL_SQL = """
            SELECT user_id, first_name, last_name, email, password_hash,
                   phone, address, role, created_at
            FROM app_users
            WHERE LOWER(email) = ?
            """;
    /** 同じメールアドレスが既に登録されているか件数で確認するSQLです。 */
    private static final String EMAIL_EXISTS_SQL = """
            SELECT COUNT(*) FROM app_users WHERE LOWER(email) = ?
            """;
    /** 新規利用者をAPP_USERSへ保存するSQLです。パスワードにはBCryptハッシュを渡します。 */
    private static final String REGISTER_SQL = """
            INSERT INTO app_users
                (first_name, last_name, email, password_hash, phone, address)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    /** セッションに保存された利用者IDから最新のアカウント情報を取得するSQLです。 */
    private static final String FIND_BY_ID_SQL = """
            SELECT user_id, first_name, last_name, email, password_hash,
                   phone, address, role, created_at
            FROM app_users
            WHERE user_id = ?
            """;
    /** 利用者が編集できるプロフィール項目だけを更新し、ROLEとEMAILは変更しないSQLです。 */
    private static final String UPDATE_PROFILE_SQL = """
            UPDATE app_users
            SET first_name = ?, last_name = ?, phone = ?, address = ?
            WHERE user_id = ?
            """;
    /** BCryptで生成済みの新しいハッシュだけを更新するSQLです。 */
    private static final String UPDATE_PASSWORD_SQL = """
            UPDATE app_users SET password_hash = ? WHERE user_id = ?
            """;

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public User findById(int userId) throws SQLException {
        // userIdは画面入力ではなく、認証済みセッションの利用者IDを呼び出し元が渡します。
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        }
    }

    /**
     * 対象を限定する条件を付け、他の利用者のデータへ影響しないように更新します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean updateProfile(int userId, String firstName, String lastName,
                                 String phone, String address) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PROFILE_SQL)) {
            // SQLの?と同じ順番で値を設定し、文字列連結によるSQL注入を防止します。
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, emptyToNull(phone));
            statement.setString(4, emptyToNull(address));
            statement.setInt(5, userId);
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * このクラスが公開する処理を実行し、呼び出し元へ結果を返します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean verifyPassword(int userId, String plainPassword) throws SQLException {
        // DBに保存されたハッシュを取得し、平文パスワードそのものは保存しません。
        User user = findById(userId);
        if (user == null || plainPassword == null || user.getPasswordHash() == null) return false;
        try {
            return BCrypt.checkpw(plainPassword, user.getPasswordHash());
        } catch (IllegalArgumentException invalidHash) {
            return false;
        }
    }

    /**
     * 対象を限定する条件を付け、他の利用者のデータへ影響しないように更新します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean updatePassword(int userId, String newPasswordHash) throws SQLException {
        // ハッシュ化は呼び出し元で完了しており、このDAOは平文パスワードを受け取りません。
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PASSWORD_SQL)) {
            statement.setString(1, newPasswordHash);
            statement.setInt(2, userId);
            return statement.executeUpdate() == 1;
        }
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public User findByEmail(String email) throws SQLException {
        // 大文字・小文字や前後空白の違いによるログイン失敗を防ぐため正規化します。
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL_SQL)) {
            statement.setString(1, normalizeEmail(email));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        }
    }

    /**
     * 認証情報を安全に照合し、パスワードやハッシュを画面へ公開しません。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean emailExists(String email) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(EMAIL_EXISTS_SQL)) {
            statement.setString(1, normalizeEmail(email));
            try (ResultSet resultSet = statement.executeQuery()) {
                // COUNT(*)は必ず1行返すため、先頭行の件数が1以上かを判定します。
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    /**
     * 認証情報を安全に照合し、パスワードやハッシュを画面へ公開しません。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public User authenticate(String email, String plainPassword) throws SQLException {
        // メールが未登録の場合もパスワード不一致と同じnullを返し、登録状況を漏らしません。
        User user = findByEmail(email);
        if (user == null || plainPassword == null) {
            return null;
        }

        try {
            if (!BCrypt.checkpw(plainPassword, user.getPasswordHash())) {
                return null;
            }
            // セッションには表示用情報だけが必要なので、パスワードハッシュを取り除きます。
            user.setPasswordHash(null);
            return user;
        } catch (IllegalArgumentException invalidHash) {
            // 不正形式の古いハッシュを認証成功として扱わないよう、安全側で失敗させます。
            return null;
        }
    }

    /**
     * 検証済みの値をPreparedStatementへ設定し、新しいデータを安全に保存します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean register(User user, String plainPassword) throws SQLException {
        // メール表記を統一し、BCryptのcost 12でパスワードを一方向ハッシュ化します。
        String normalizedEmail = normalizeEmail(user.getEmail());
        String passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(REGISTER_SQL)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, normalizedEmail);
            statement.setString(4, passwordHash);
            statement.setString(5, emptyToNull(user.getPhone()));
            statement.setString(6, emptyToNull(user.getAddress()));
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            // ORA-00001（メールの一意制約違反）は、通常の重複登録としてfalseを返します。
            if (e.getErrorCode() == 1) {
                return false;
            }
            throw e;
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        // SELECT句の列順と同じ順番で、DBの1行をUserオブジェクトへ詰め替えます。
        User user = new User();
        user.setUserId(resultSet.getInt(1));
        user.setFirstName(resultSet.getString(2));
        user.setLastName(resultSet.getString(3));
        user.setEmail(resultSet.getString(4));
        user.setPasswordHash(resultSet.getString(5));
        user.setPhone(resultSet.getString(6));
        user.setAddress(resultSet.getString(7));
        user.setRole(resultSet.getString(8));
        user.setCreatedAt(resultSet.getTimestamp(9));
        return user;
    }

    private String normalizeEmail(String email) {
        // Locale.ROOTを使い、実行環境の言語設定に依存しない小文字変換を行います。
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String emptyToNull(String value) {
        // 任意項目の空文字はOracle上で意味が曖昧にならないようnullへ統一します。
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
