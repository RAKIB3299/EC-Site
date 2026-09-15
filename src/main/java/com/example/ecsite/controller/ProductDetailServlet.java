package com.example.ecsite.controller;

import com.example.ecsite.dao.ProductDAO;
import com.example.ecsite.dao.ProductImageDAO;
import com.example.ecsite.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * HTTPリクエストを受け取り、ProductDetailServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/product")
public class ProductDetailServlet extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();

    /**
     * GETリクエストを処理し、必要なデータを準備して画面を表示します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer productId = readProductId(request.getParameter("id"));
        if (productId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Please provide a valid positive product ID.");
            return;
        }

        try {
            Product product = productDAO.findById(productId);
            if (product == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "The requested product was not found.");
                return;
            }

            request.setAttribute("product", product);
            request.setAttribute("productImages", productImageDAO.findByProductId(productId));
            List<Product> relatedProducts = Collections.emptyList();
            List<Product> otherProducts = Collections.emptyList();
            try {
                relatedProducts = productDAO.findRelatedProducts(
                        productId, product.getCategoryId(), 4);
                otherProducts = productDAO.findOtherProducts(
                        productId, product.getCategoryId(), 4);
            } catch (SQLException e) {
                getServletContext().log(
                        "Could not load recommendations for product " + productId, e);
            }
            request.setAttribute("relatedProducts", relatedProducts);
            request.setAttribute("otherProducts", otherProducts);
            request.getRequestDispatcher("/WEB-INF/views/product-detail.jsp")
                    .forward(request, response);
        } catch (SQLException e) {
            getServletContext().log("Could not load product " + productId, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "The product could not be loaded. Please try again later.");
        }
    }

    private Integer readProductId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            int productId = Integer.parseInt(value);
            return productId > 0 ? productId : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
