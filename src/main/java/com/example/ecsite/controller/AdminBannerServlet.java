package com.example.ecsite.controller;

import com.example.ecsite.dao.BannerDAO;
import com.example.ecsite.model.Banner;
import com.example.ecsite.util.BannerImageStorage;
import com.example.ecsite.util.BannerLinkValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * HTTPリクエストを受け取り、AdminBannerServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/admin/banners")
public class AdminBannerServlet extends HttpServlet {
    private final BannerDAO dao=new BannerDAO();
    @Override /**
 * GETリクエストを処理し、必要なデータを準備して画面を表示します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        try{request.setAttribute("banners",dao.findAll());request.setAttribute("bannerCount",dao.count());request.getRequestDispatcher("/WEB-INF/views/admin/banners.jsp").forward(request,response);}
        catch(SQLException e){getServletContext().log("Banner management load failed",e);response.sendError(500,"Banners could not be loaded.");}
    }
    @Override /**
 * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doPost(HttpServletRequest request,HttpServletResponse response)throws IOException{
        request.setCharacterEncoding("UTF-8");Long id=positiveLong(request.getParameter("bannerId"));if(id==null){response.sendError(400,"Invalid banner ID.");return;}
        try{
            if("remove".equals(request.getParameter("action"))){Banner old=dao.delete(id);if(old==null){response.sendError(404,"Banner not found.");return;}try{BannerImageStorage.deleteManagedPath(old.getImageUrl());}catch(IOException e){getServletContext().log("Banner file could not be removed",e);}redirect(request,response,"removed");return;}
            Banner b=new Banner();b.setBannerId(id);b.setTitle(emptyToNull(request.getParameter("title")));b.setLinkUrl(emptyToNull(request.getParameter("linkUrl")));b.setDisplayOrder(order(request.getParameter("displayOrder")));b.setActive("Y".equals(request.getParameter("isActive")));
            String error=validate(b);if(error!=null){redirect(request,response,error);return;}try{if(!dao.update(b)){response.sendError(404,"Banner not found.");return;}}catch(SQLException e){if(e.getErrorCode()==1){redirect(request,response,"orderUsed");return;}throw e;}redirect(request,response,"updated");
        }catch(SQLException e){getServletContext().log("Banner update failed",e);response.sendError(500,"Banner could not be updated.");}
    }
    private String validate(Banner b){if(b.getTitle()!=null&&b.getTitle().length()>120)return "title";if(b.getLinkUrl()!=null&&b.getLinkUrl().length()>500)return "link";if(!BannerLinkValidator.isValid(b.getLinkUrl()))return "link";if(b.getDisplayOrder()<1||b.getDisplayOrder()>4)return "order";return null;}
    private int order(String v){try{return Integer.parseInt(v);}catch(Exception e){return 0;}}
    private Long positiveLong(String v){try{long n=Long.parseLong(v);return n>0?n:null;}catch(Exception e){return null;}}
    private String emptyToNull(String v){return v==null||v.trim().isEmpty()?null:v.trim();}
    private void redirect(HttpServletRequest r,HttpServletResponse p,String message)throws IOException{p.sendRedirect(r.getContextPath()+"/admin/banners?message="+message);}
}
