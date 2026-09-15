package com.example.ecsite.controller;

import com.example.ecsite.util.BannerImageStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;

/**
 * HTTPリクエストを受け取り、BannerImageFileServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/banner-images/*")
public class BannerImageFileServlet extends HttpServlet {
    @Override /**
 * GETリクエストを処理し、必要なデータを準備して画面を表示します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doGet(HttpServletRequest request,HttpServletResponse response)throws IOException{
        String path=request.getPathInfo();if(path==null||path.length()<2||path.substring(1).contains("/")||path.contains("\\")||path.contains("..")){response.sendError(404);return;}
        Path file=BannerImageStorage.resolveForRead(path.substring(1));if(file==null){response.sendError(404);return;}
        String lower=file.getFileName().toString().toLowerCase();response.setContentType(lower.endsWith(".png")?"image/png":"image/jpeg");response.setContentLengthLong(Files.size(file));response.setHeader("Cache-Control","public, max-age=86400");
        try(InputStream in=Files.newInputStream(file)){in.transferTo(response.getOutputStream());}
    }
}
