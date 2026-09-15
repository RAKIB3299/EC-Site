package com.example.ecsite.controller;

import com.example.ecsite.dao.CategoryDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/**
 * HTTPリクエストを受け取り、AdminCategoryServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/admin/categories")
public class AdminCategoryServlet extends HttpServlet {
    private final CategoryDAO categoryDAO=new CategoryDAO();
    /**
     * GETリクエストを処理し、必要なデータを準備して画面を表示します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        try { request.setAttribute("categories",categoryDAO.findAll()); request.getRequestDispatcher("/WEB-INF/views/admin/categories.jsp").forward(request,response); }
        catch(SQLException e){getServletContext().log("Category list failed",e);response.sendError(500,"Categories could not be loaded.");}
    }
    /**
     * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            int id=Integer.parseInt(request.getParameter("categoryId"));
            int order=Integer.parseInt(request.getParameter("displayOrder"));
            String name=request.getParameter("categoryName")==null?"":request.getParameter("categoryName").trim();
            if(id<=0||order<0||order>999||name.isEmpty()||name.length()>100){response.sendRedirect(request.getContextPath()+"/admin/categories?message=invalid");return;}
            if(!categoryDAO.updateCategory(id,name,order,"Y".equals(request.getParameter("active")))) { response.sendError(404,"Category not found."); return; }
            response.sendRedirect(request.getContextPath()+"/admin/categories?message=updated");
        } catch(NumberFormatException e){response.sendRedirect(request.getContextPath()+"/admin/categories?message=invalid");}
        catch(SQLException e){getServletContext().log("Category update failed",e);response.sendRedirect(request.getContextPath()+"/admin/categories?message=failed");}
    }
}
