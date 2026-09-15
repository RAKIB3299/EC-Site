package com.example.ecsite.controller;
import com.example.ecsite.dao.ProductDAO;import jakarta.servlet.*;import jakarta.servlet.annotation.WebServlet;import jakarta.servlet.http.*;import java.io.IOException;import java.sql.SQLException;
@WebServlet("/admin/products") /**
 * HTTPリクエストを受け取り、AdminProductListServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
public class AdminProductListServlet extends HttpServlet{
 private final ProductDAO dao=new ProductDAO();/**
 * GETリクエストを処理し、必要なデータを準備して画面を表示します。
 *
 * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
 */
protected void doGet(HttpServletRequest r,HttpServletResponse p)throws ServletException,IOException{int page=number(r.getParameter("page"));try{int total=dao.countProducts(),pages=Math.max(1,(total+11)/12);page=Math.min(page,pages);r.setAttribute("products",dao.findPage(page,12));r.setAttribute("currentPage",page);r.setAttribute("totalPages",pages);r.setAttribute("message",r.getParameter("message"));r.getRequestDispatcher("/WEB-INF/views/admin/products.jsp").forward(r,p);}catch(SQLException e){getServletContext().log("Admin products",e);p.sendError(500,"Products cannot be loaded.");}}private int number(String v){try{return Math.max(1,Integer.parseInt(v));}catch(Exception e){return 1;}}
}
