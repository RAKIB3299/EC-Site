package com.example.ecsite.util;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;
import java.util.UUID;

/**
 * ECサイト内で共通利用する BannerImageStorage のユーティリティクラスです。
 *
 * <p>重複しやすい検証や設定読込、セッション・ファイル操作を一か所にまとめます。</p>
 */
public final class BannerImageStorage {
    private static final String PREFIX="/banner-images/";
    private static final Path DIRECTORY=loadDirectory();
    private BannerImageStorage(){ }
    /**
     * 検証済みの値をPreparedStatementへ設定し、新しいデータを安全に保存します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static String save(ProductImageStorage.UploadData upload)throws IOException{
        Files.createDirectories(DIRECTORY);String name="banner-"+UUID.randomUUID()+"."+upload.extension();
        Path target=safeFile(name);Files.write(target,upload.bytes(),StandardOpenOption.CREATE_NEW);return PREFIX+name;
    }
    /**
     * このクラスが公開する処理を実行し、呼び出し元へ結果を返します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static Path resolveForRead(String name){
        if(name==null||!name.matches("banner-[0-9a-fA-F-]+\\.(jpg|png)"))return null;
        Path file=safeFile(name);return Files.isRegularFile(file)?file:null;
    }
    /**
     * 対象IDと所有条件を確認し、許可されたデータだけを削除または解除します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static boolean deleteManagedPath(String path)throws IOException{
        if(path==null||!path.startsWith(PREFIX))return false;Path file=resolveForRead(path.substring(PREFIX.length()));return file!=null&&Files.deleteIfExists(file);
    }
    private static Path safeFile(String name){Path result=DIRECTORY.resolve(name).normalize();if(!result.startsWith(DIRECTORY)||!result.getParent().equals(DIRECTORY))throw new IllegalArgumentException("Invalid banner filename.");return result;}
    private static Path loadDirectory(){Properties p=new Properties();try(InputStream in=BannerImageStorage.class.getClassLoader().getResourceAsStream("database.properties")){if(in==null)throw new IllegalStateException("database.properties was not found.");p.load(in);String v=p.getProperty("banner.upload.directory");if(v==null||v.isBlank())throw new IllegalStateException("banner.upload.directory is missing.");return Paths.get(v.trim()).toAbsolutePath().normalize();}catch(IOException e){throw new IllegalStateException("Could not load banner upload configuration.",e);}}
}
