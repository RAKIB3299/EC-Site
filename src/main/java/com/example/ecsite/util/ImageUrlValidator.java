package com.example.ecsite.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * ECサイト内で共通利用する ImageUrlValidator のユーティリティクラスです。
 *
 * <p>重複しやすい検証や設定読込、セッション・ファイル操作を一か所にまとめます。</p>
 */
public final class ImageUrlValidator {
    private ImageUrlValidator() { }

    /**
     * 外部入力を利用する前に、安全性と形式が条件を満たしているか確認します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static boolean isValid(String value) {
        if (value == null || value.isBlank() || value.length() > 500 || value.contains("..")) return false;
        if (value.startsWith("/images/") || value.startsWith("/product-images/")) return true;
        try {
            URI uri = new URI(value);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.isAbsolute() && uri.getHost() != null && !uri.getHost().isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * 外部入力を利用する前に、安全性と形式が条件を満たしているか確認します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static boolean isLocal(String value) {
        return isValid(value) && (value.startsWith("/images/") || value.startsWith("/product-images/"));
    }
}
