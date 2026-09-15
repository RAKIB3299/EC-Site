package com.example.ecsite.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Opens Oracle connections using settings from database.properties. */
/**
 * ECサイト内で共通利用する DBConnection のユーティリティクラスです。
 *
 * <p>重複しやすい検証や設定読込、セッション・ファイル操作を一か所にまとめます。</p>
 */
public final class DBConnection {
    private static final String CONFIG_FILE = "database.properties";
    private static final Properties PROPERTIES = loadProperties();

    static {
        try {
            // Explicit loading is reliable when this class runs inside Tomcat.
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "Oracle JDBC driver was not found in the application: " + e.getMessage()
            );
        }
    }

    private DBConnection() {
        // Utility class: objects of this class are not needed.
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                requiredProperty("db.url"),
                requiredProperty("db.user"),
                requiredProperty("db.password")
        );
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();

        try (InputStream input = DBConnection.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IllegalStateException(
                        CONFIG_FILE + " was not found in src/main/resources."
                );
            }
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not read " + CONFIG_FILE + ".", e
            );
        }
    }

    private static String requiredProperty(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required setting '" + key + "' is missing from " + CONFIG_FILE + "."
            );
        }
        return value.trim();
    }
}
