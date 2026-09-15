package com.example.ecsite.controller;

import com.example.ecsite.dao.ProductImageDAO;
import com.example.ecsite.model.ProductImage;
import com.example.ecsite.util.ImageUrlValidator;
import com.example.ecsite.util.ProductImageStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * HTTPリクエストを受け取り、AdminProductImageServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/admin/products/image")
public class AdminProductImageServlet extends HttpServlet {
    private final ProductImageDAO imageDAO=new ProductImageDAO();
    @Override /**
 * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doPost(HttpServletRequest request,HttpServletResponse response)throws IOException{
        request.setCharacterEncoding("UTF-8");Integer productId=positive(request.getParameter("productId"));
        if(productId==null){response.sendError(400,"Invalid product ID.");return;}
        try{
            if("remove".equals(request.getParameter("action"))){
                Long imageId=positiveLong(request.getParameter("imageId"));if(imageId==null){response.sendError(400,"Invalid image ID.");return;}
                ProductImage old=imageDAO.deleteImage(productId,imageId);if(old==null){response.sendError(404,"Image not found for this product.");return;}
                try{ProductImageStorage.deleteManagedPath(old.getImageUrl());}catch(IOException e){getServletContext().log("Managed product image file could not be removed",e);}
                redirect(request,response,productId,"removed");return;
            }
            String imageUrl=trim(request.getParameter("imageUrl"));
            if(!ImageUrlValidator.isValid(imageUrl)){redirect(request,response,productId,"invalid");return;}
            if(imageDAO.addImage(productId,imageUrl)==null){response.sendError(404,"Product not found.");return;}
            redirect(request,response,productId,"saved");
        }catch(ProductImageDAO.ImageLimitException e){redirect(request,response,productId,"limit");
        }catch(SQLException e){getServletContext().log("Product image update failed",e);response.sendError(500,"Product image could not be updated.");}
    }
    private void redirect(HttpServletRequest r,HttpServletResponse p,int id,String code)throws IOException{p.sendRedirect(r.getContextPath()+"/admin/products/edit?id="+id+"&image="+code);}
    private Integer positive(String v){try{int n=Integer.parseInt(v);return n>0?n:null;}catch(Exception e){return null;}}
    private Long positiveLong(String v){try{long n=Long.parseLong(v);return n>0?n:null;}catch(Exception e){return null;}}
    private String trim(String v){return v==null?"":v.trim();}
}
