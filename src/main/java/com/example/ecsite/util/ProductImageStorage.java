package com.example.ecsite.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * ECサイト内で共通利用する ProductImageStorage のユーティリティクラスです。
 *
 * <p>重複しやすい検証や設定読込、セッション・ファイル操作を一か所にまとめます。</p>
 */
public final class ProductImageStorage {
    public static final long MAX_BYTES=5L*1024*1024;
    private static final String PREFIX="/product-images/";
    private static final Path DIRECTORY=loadDirectory();

    private ProductImageStorage(){ }

    /**
     * 外部入力を利用する前に、安全性と形式が条件を満たしているか確認します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static UploadData validate(jakarta.servlet.http.Part part)throws IOException{
        if(part==null||part.getSize()==0)throw new ValidationException("Please select an image.");
        if(part.getSize()>MAX_BYTES)throw new ValidationException("Image must be 5 MB or smaller.");
        String submitted=Optional.ofNullable(part.getSubmittedFileName()).orElse("");
        String contentType=Optional.ofNullable(part.getContentType()).orElse("").toLowerCase(Locale.ROOT);
        return validate(part.getInputStream(),part.getSize(),submitted,contentType);
    }

    /**
     * 外部入力を利用する前に、安全性と形式が条件を満たしているか確認します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static UploadData validate(InputStream source,long size,String submitted,String contentType)throws IOException{
        if(size==0)throw new ValidationException("Please select an image.");
        if(size>MAX_BYTES)throw new ValidationException("Image must be 5 MB or smaller.");
        String extension=extension(Optional.ofNullable(submitted).orElse(""));
        contentType=Optional.ofNullable(contentType).orElse("").toLowerCase(Locale.ROOT);
        if(!(extension.equals("jpg")||extension.equals("jpeg")||extension.equals("png"))
                || !(contentType.equals("image/jpeg")||contentType.equals("image/png")))
            throw new ValidationException("Only JPG and PNG images are supported.");
        byte[] bytes;
        try(InputStream input=source){bytes=input.readNBytes((int)MAX_BYTES+1);}
        if(bytes.length>MAX_BYTES)throw new ValidationException("Image must be 5 MB or smaller.");
        String decoded=decodedFormat(bytes);
        boolean jpegExtension=extension.equals("jpg")||extension.equals("jpeg");
        if(decoded==null || (jpegExtension&&!decoded.equals("jpeg")) || (extension.equals("png")&&!decoded.equals("png")))
            throw new ValidationException("The uploaded file is not a valid image.");
        return new UploadData(bytes,jpegExtension?"jpg":"png");
    }

    /**
     * 検証済みの値をPreparedStatementへ設定し、新しいデータを安全に保存します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static String save(int productId,UploadData upload)throws IOException{
        Files.createDirectories(DIRECTORY);
        String filename="product-"+productId+"-"+UUID.randomUUID()+"."+upload.extension();
        Path target=safeFile(filename);
        Files.write(target,upload.bytes(),StandardOpenOption.CREATE_NEW);
        return PREFIX+filename;
    }

    /**
     * このクラスが公開する処理を実行し、呼び出し元へ結果を返します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static Path resolveForRead(String filename){
        if(filename==null||!filename.matches("product-[0-9]+-[0-9a-fA-F-]+\\.(jpg|png)"))return null;
        Path file=safeFile(filename);
        return Files.isRegularFile(file)?file:null;
    }

    /**
     * 対象IDと所有条件を確認し、許可されたデータだけを削除または解除します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static boolean deleteManagedPath(String applicationPath)throws IOException{
        if(applicationPath==null||!applicationPath.startsWith(PREFIX))return false;
        String filename=applicationPath.substring(PREFIX.length());
        Path file=resolveForRead(filename);
        return file!=null&&Files.deleteIfExists(file);
    }

    private static Path safeFile(String filename){
        Path result=DIRECTORY.resolve(filename).normalize();
        if(!result.startsWith(DIRECTORY)||!result.getParent().equals(DIRECTORY))throw new IllegalArgumentException("Invalid image filename.");
        return result;
    }

    private static String decodedFormat(byte[] bytes)throws IOException{
        try(ImageInputStream stream=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){
            if(stream==null)return null;Iterator<ImageReader> readers=ImageIO.getImageReaders(stream);
            if(!readers.hasNext())return null;ImageReader reader=readers.next();
            try{reader.setInput(stream,true,true);int width=reader.getWidth(0),height=reader.getHeight(0);if(width<=0||height<=0||((long)width*height)>40_000_000L)return null;String format=reader.getFormatName().toLowerCase(Locale.ROOT);if(reader.read(0)==null)return null;return format;}
            finally{reader.dispose();}
        }
    }

    private static String extension(String name){int dot=name.lastIndexOf('.');return dot<0?"":name.substring(dot+1).toLowerCase(Locale.ROOT);}
    private static Path loadDirectory(){
        Properties properties=new Properties();
        try(InputStream in=ProductImageStorage.class.getClassLoader().getResourceAsStream("database.properties")){
            if(in==null)throw new IllegalStateException("database.properties was not found.");properties.load(in);
            String value=properties.getProperty("product.upload.directory");if(value==null||value.isBlank())throw new IllegalStateException("product.upload.directory is missing.");
            return Paths.get(value.trim()).toAbsolutePath().normalize();
        }catch(IOException e){throw new IllegalStateException("Could not load upload configuration.",e);}
    }

    /**
     * このクラスが公開する処理を実行し、呼び出し元へ結果を返します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public record UploadData(byte[] bytes,String extension){ }
    public static class ValidationException extends IOException{public ValidationException(String message){super(message);}}
}
