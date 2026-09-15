package com.example.ecsite.controller;

import com.example.ecsite.dao.NotificationDAO;
import com.example.ecsite.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * 認証・認可や共通データの準備を行うフィルターです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebFilter("/*")
public class NotificationContextFilter implements Filter {
    private final NotificationDAO dao=new NotificationDAO();
    /**
     * リクエストを次の処理へ渡す前に、セッション情報とアクセス条件を確認します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)throws IOException,ServletException{
        HttpServletRequest r=(HttpServletRequest)request;String path=r.getRequestURI().substring(r.getContextPath().length());
        if(path.startsWith("/css/")||path.startsWith("/images/")||path.startsWith("/product-images/")||path.startsWith("/banner-images/")){chain.doFilter(request,response);return;}
        HttpSession session=r.getSession(false);Object value=session==null?null:session.getAttribute("user");
        if(value instanceof User user){try{r.setAttribute("headerNotifications",dao.findRecentByUserId(user.getUserId(),6));r.setAttribute("unreadNotificationCount",dao.countUnreadByUserId(user.getUserId()));}catch(SQLException e){r.getServletContext().log("Header notifications could not be loaded",e);r.setAttribute("headerNotificationError",true);}}
        chain.doFilter(request,response);
    }
}
