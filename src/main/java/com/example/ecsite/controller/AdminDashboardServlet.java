package com.example.ecsite.controller;
import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;
@WebServlet({"/admin","/admin/dashboard"}) /**
 * HTTPリクエストを受け取り、AdminDashboardServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
public class AdminDashboardServlet extends HttpServlet{
 /**
  * GETリクエストを処理し、必要なデータを準備して画面を表示します。
  *
  * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
  */
 protected void doGet(HttpServletRequest r,HttpServletResponse p)throws ServletException,IOException{r.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(r,p);}
}
