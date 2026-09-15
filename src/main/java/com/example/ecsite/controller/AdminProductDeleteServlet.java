package com.example.ecsite.controller;
import com.example.ecsite.dao.ProductDAO;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.SQLException;
@WebServlet("/admin/products/delete") /**
 * HTTPリクエストを受け取り、AdminProductDeleteServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
public class AdminProductDeleteServlet extends HttpServlet{private final ProductDAO dao=new ProductDAO();/**
 * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doPost(HttpServletRequest r,HttpServletResponse p)throws IOException{try{int id=Integer.parseInt(r.getParameter("productId"));dao.delete(id);p.sendRedirect(r.getContextPath()+"/admin/products?message=deleted");}catch(SQLException e){String m=e.getErrorCode()==2292?"referenced":"error";p.sendRedirect(r.getContextPath()+"/admin/products?message="+m);}catch(Exception e){p.sendError(400,"Invalid product ID.");}}}
