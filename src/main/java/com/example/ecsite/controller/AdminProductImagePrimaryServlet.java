package com.example.ecsite.controller;

import com.example.ecsite.dao.ProductImageDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * HTTPリクエストを受け取り、AdminProductImagePrimaryServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/admin/products/image/primary")
public class AdminProductImagePrimaryServlet extends HttpServlet {
    private final ProductImageDAO imageDAO=new ProductImageDAO();
    @Override /**
 * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doPost(HttpServletRequest request,HttpServletResponse response)throws IOException{
        Integer productId=positiveInt(request.getParameter("productId"));Long imageId=positiveLong(request.getParameter("imageId"));
        if(productId==null||imageId==null){response.sendError(400,"Invalid product or image ID.");return;}
        try{
            if(!imageDAO.makePrimary(productId,imageId)){response.sendError(404,"Image not found for this product.");return;}
            response.sendRedirect(request.getContextPath()+"/admin/products/edit?id="+productId+"&image=primary");
        }catch(SQLException e){getServletContext().log("Could not change primary product image",e);response.sendError(500,"Primary image could not be changed.");}
    }
    private Integer positiveInt(String v){try{int n=Integer.parseInt(v);return n>0?n:null;}catch(Exception e){return null;}}
    private Long positiveLong(String v){try{long n=Long.parseLong(v);return n>0?n:null;}catch(Exception e){return null;}}
}
