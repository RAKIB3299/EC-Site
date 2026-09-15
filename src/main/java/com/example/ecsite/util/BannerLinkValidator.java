package com.example.ecsite.util;

import java.net.URI;

/**
 * ECサイト内で共通利用する BannerLinkValidator のユーティリティクラスです。
 *
 * <p>重複しやすい検証や設定読込、セッション・ファイル操作を一か所にまとめます。</p>
 */
public final class BannerLinkValidator {
    private BannerLinkValidator(){ }
    /**
     * 外部入力を利用する前に、安全性と形式が条件を満たしているか確認します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static boolean isValid(String value){
        if(value==null||value.isBlank())return true;
        try{
            URI uri=URI.create(value.trim());
            if(!uri.isAbsolute()){
                if(!value.startsWith("/")||value.startsWith("//")||value.contains("\\")||value.contains(".."))return false;
                String path=uri.getPath();return "/products".equals(path)||"/product".equals(path);
            }
            return "https".equalsIgnoreCase(uri.getScheme())&&uri.getHost()!=null&&!uri.getHost().isBlank()&&uri.getUserInfo()==null;
        }catch(IllegalArgumentException e){return false;}
    }
}
