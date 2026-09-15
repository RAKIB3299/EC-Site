package com.example.ecsite.controller;

import com.example.ecsite.dao.ProductDAO;
import com.example.ecsite.dao.ProductImageDAO;
import com.example.ecsite.util.ProductImageStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/products/image/upload")
@MultipartConfig(maxFileSize=5L*1024*1024,maxRequestSize=6L*1024*1024,fileSizeThreshold=1024*1024)
/**
 * HTTPリクエストを受け取り、AdminProductImageUploadServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
public class AdminProductImageUploadServlet extends HttpServlet {
    private final ProductDAO productDAO=new ProductDAO();
    private final ProductImageDAO imageDAO=new ProductImageDAO();

    @Override /**
 * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doPost(HttpServletRequest request,HttpServletResponse response)throws IOException{
        Integer productId=positive(queryValue(request.getQueryString(),"productId"));
        if(productId==null){response.sendError(400,"Invalid product ID.");return;}
        try{
            if(productDAO.findById(productId)==null){response.sendError(404,"Product not found.");return;}
            ProductImageStorage.UploadData upload=ProductImageStorage.validate(request.getPart("imageFile"));
            String newPath=ProductImageStorage.save(productId,upload);
            try{
                if(imageDAO.addImage(productId,newPath)==null){
                    ProductImageStorage.deleteManagedPath(newPath);response.sendError(404,"Product not found.");return;
                }
            }catch(SQLException e){ProductImageStorage.deleteManagedPath(newPath);throw e;}
            redirect(request,response,productId,"uploaded");
        }catch(ProductImageDAO.ImageLimitException e){redirect(request,response,productId,"limit");
        }catch(ProductImageStorage.ValidationException e){redirect(request,response,productId,messageCode(e.getMessage()));
        }catch(IllegalStateException e){redirect(request,response,productId,"tooLarge");
        }catch(ServletException e){redirect(request,response,productId,"invalidFile");
        }catch(SQLException e){getServletContext().log("Product image upload database error",e);redirect(request,response,productId,"uploadFailed");
        }
    }
    private String messageCode(String message){
        if(message.startsWith("Please select"))return "empty";if(message.contains("5 MB"))return "tooLarge";
        if(message.startsWith("Only JPG"))return "type";return "invalidFile";
    }
    private void redirect(HttpServletRequest r,HttpServletResponse p,int id,String code)throws IOException{p.sendRedirect(r.getContextPath()+"/admin/products/edit?id="+id+"&image="+code);}
    private Integer positive(String v){try{int n=Integer.parseInt(v);return n>0?n:null;}catch(Exception e){return null;}}
    private String queryValue(String query,String name){
        if(query==null)return null;for(String part:query.split("&")){String[] pair=part.split("=",2);if(pair.length==2&&pair[0].equals(name))return pair[1];}return null;
    }
}
