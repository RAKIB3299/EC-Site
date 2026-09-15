package com.example.ecsite.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Simple command-line test for the Oracle database connection. */
/**
 * ECサイト内で共通利用する DBConnectionTest のユーティリティクラスです。
 *
 * <p>重複しやすい検証や設定読込、セッション・ファイル操作を一か所にまとめます。</p>
 */
public class DBConnectionTest {
    private static final String COUNT_PRODUCTS_SQL =
            "SELECT COUNT(*) FROM products";

    /**
     * このクラスが公開する処理を実行し、呼び出し元へ結果を返します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static void main(String[] args) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(COUNT_PRODUCTS_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                System.out.println("Oracle connection succeeded.");
                System.out.println("Number of products: " + resultSet.getInt(1));
            } else {
                System.err.println("The count query returned no result.");
            }
        } catch (SQLException e) {
            System.err.println("Oracle connection test failed.");
            System.err.println("Message: " + e.getMessage());
            System.err.println("Oracle error code: " + e.getErrorCode());
            System.err.println("SQL state: " + e.getSQLState());
            e.printStackTrace(System.err);
        } catch (RuntimeException e) {
            System.err.println("Database configuration could not be loaded.");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
