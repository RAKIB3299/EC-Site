package com.example.ecsite.controller;

import com.example.ecsite.dao.BannerDAO;
import com.example.ecsite.model.Banner;
import com.example.ecsite.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/banners/upload")
@MultipartConfig(maxFileSize=5L*1024*1024,maxRequestSize=6L*1024*1024,fileSizeThreshold=1024*1024)
/**
 * HTTPリクエストを受け取り、AdminBannerUploadServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
public class AdminBannerUploadServlet extends HttpServlet {
    private final BannerDAO dao=new BannerDAO();
    @Override /**
 * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doPost(HttpServletRequest request,HttpServletResponse response)throws IOException{
        request.setCharacterEncoding("UTF-8");String title=emptyToNull(request.getParameter("title")),link=emptyToNull(request.getParameter("linkUrl"));int order=order(request.getParameter("displayOrder"));
        if(title!=null&&title.length()>120){redirect(request,response,"title");return;}if(link!=null&&link.length()>500||!BannerLinkValidator.isValid(link)){redirect(request,response,"link");return;}if(order<1||order>4){redirect(request,response,"order");return;}
        String path=null;try{
            ProductImageStorage.UploadData upload=ProductImageStorage.validate(request.getPart("imageFile"));path=BannerImageStorage.save(upload);Banner b=new Banner();b.setTitle(title);b.setImageUrl(path);b.setLinkUrl(link);b.setDisplayOrder(order);b.setActive("Y".equals(request.getParameter("isActive")));dao.create(b);redirect(request,response,"created");
        }catch(BannerDAO.BannerLimitException e){delete(path);redirect(request,response,"limit");
        }catch(ProductImageStorage.ValidationException e){delete(path);redirect(request,response,message(e.getMessage()));
        }catch(IllegalStateException e){delete(path);redirect(request,response,"large");
        }catch(ServletException e){delete(path);redirect(request,response,"invalid");
        }catch(SQLException e){delete(path);if(e.getErrorCode()==1)redirect(request,response,"orderUsed");else{getServletContext().log("Banner upload database error",e);redirect(request,response,"failed");}}
    }
    private String message(String m){if(m.startsWith("Please select"))return "empty";if(m.contains("5 MB"))return "large";if(m.startsWith("Only JPG"))return "type";return "invalid";}
    private void delete(String p){try{BannerImageStorage.deleteManagedPath(p);}catch(Exception ignored){}}
    private int order(String v){try{return Integer.parseInt(v);}catch(Exception e){return 0;}}
    private String emptyToNull(String v){return v==null||v.trim().isEmpty()?null:v.trim();}
    private void redirect(HttpServletRequest r,HttpServletResponse p,String message)throws IOException{p.sendRedirect(r.getContextPath()+"/admin/banners?message="+message);}
}
