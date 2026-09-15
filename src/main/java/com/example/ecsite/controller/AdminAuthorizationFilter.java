package com.example.ecsite.controller;

import com.example.ecsite.model.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * 認証・認可や共通データの準備を行うフィルターです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebFilter("/admin/*")
public class AdminAuthorizationFilter implements Filter {
 /**
  * リクエストを次の処理へ渡す前に、セッション情報とアクセス条件を確認します。
  *
  * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
  */
 public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain)throws IOException,ServletException{
  HttpServletRequest r=(HttpServletRequest)req;HttpServletResponse p=(HttpServletResponse)res;
  HttpSession session=r.getSession(false);Object value=session==null?null:session.getAttribute("user");
  if(!(value instanceof User)){p.sendRedirect(r.getContextPath()+"/login");return;}
  if(!"ADMIN".equals(((User)value).getRole())){p.sendError(403,"Administrator access is required.");return;}
  chain.doFilter(req,res);
 }
}
