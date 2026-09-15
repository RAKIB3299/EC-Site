package com.example.ecsite.controller;

import com.example.ecsite.dao.ProductDAO;
import com.example.ecsite.dao.CategoryDAO;
import com.example.ecsite.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Set;

/**
 * HTTPリクエストを受け取り、AdminProductCreateServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/admin/products/new")
public class AdminProductCreateServlet extends HttpServlet {
    private static final Set<String> STATUSES = Set.of(
            "orderable", "planned", "under development", "obsolete");
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    /**
     * GETリクエストを処理し、必要なデータを準備して画面を表示します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        showForm(request, response, new Product(), null);
    }

    /**
     * POSTリクエストの入力値を検証し、更新処理後に安全な画面へ遷移します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Product product = new Product();
        try {
            product.setProductName(trim(request.getParameter("productName")));
            product.setCategoryId(optionalInteger(request.getParameter("categoryId")));
            product.setWeightClass(optionalInteger(request.getParameter("weightClass")));
            product.setSupplierId(optionalInteger(request.getParameter("supplierId")));
            product.setProductStatus(trim(request.getParameter("productStatus")));
            product.setListPrice(optionalDecimal(request.getParameter("listPrice")));
            product.setMinimumPrice(optionalDecimal(request.getParameter("minimumPrice")));
            product.setCatalogUrl(emptyToNull(request.getParameter("catalogUrl")));
        } catch (NumberFormatException e) {
            showForm(request, response, product, "Numeric fields must contain valid numbers.");
            return;
        }

        String error = validate(product);
        if (error != null) {
            showForm(request, response, product, error);
            return;
        }
        try {
            int productId = productDAO.createProduct(product);
            response.sendRedirect(request.getContextPath() + "/product?id=" + productId);
        } catch (SQLException e) {
            getServletContext().log("Admin product creation failed", e);
            showForm(request, response, product,
                    "The product could not be created. Please check the values and try again.");
        }
    }

    private String validate(Product product) {
        if (product.getProductName().isEmpty()) return "Product name is required.";
        if (product.getProductName().length() > 25) return "Product name must not exceed 25 characters.";
        if (!STATUSES.contains(product.getProductStatus())) return "Please choose a valid product status.";
        if (product.getCategoryId() != null && (product.getCategoryId() < 0 || product.getCategoryId() > 99))
            return "Category ID must be between 0 and 99.";
        if (product.getWeightClass() != null && (product.getWeightClass() < 0 || product.getWeightClass() > 9))
            return "Weight class must be between 0 and 9.";
        if (product.getSupplierId() != null && (product.getSupplierId() < 0 || product.getSupplierId() > 999999))
            return "Supplier ID must be between 0 and 999999.";
        if (product.getListPrice() != null && product.getListPrice().signum() < 0)
            return "List price cannot be negative.";
        if (product.getMinimumPrice() != null && product.getMinimumPrice().signum() < 0)
            return "Minimum price cannot be negative.";
        if (product.getListPrice() != null && product.getMinimumPrice() != null
                && product.getMinimumPrice().compareTo(product.getListPrice()) > 0)
            return "Minimum price cannot be greater than list price.";
        if (product.getCatalogUrl() != null && product.getCatalogUrl().length() > 50)
            return "Catalog URL must not exceed 50 characters.";
        if (product.getCatalogUrl() != null && Product.isLegacyOracleCatalogUrl(product.getCatalogUrl()))
            return "This legacy Oracle demo catalog domain is no longer supported.";
        if (product.getCatalogUrl() != null && !Product.isValidCatalogUrl(product.getCatalogUrl()))
            return "Catalog URL must be a complete http:// or https:// address.";
        return null;
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Product product, String error) throws ServletException, IOException {
        request.setAttribute("product", product);
        request.setAttribute("error", error);
        try { request.setAttribute("categories", categoryDAO.findActiveCategories()); }
        catch (SQLException e) { throw new ServletException("Could not load categories.", e); }
        request.getRequestDispatcher("/WEB-INF/views/admin/product-create.jsp").forward(request, response);
    }

    private Integer optionalInteger(String value) {
        String clean = trim(value);
        return clean.isEmpty() ? null : Integer.valueOf(clean);
    }
    private BigDecimal optionalDecimal(String value) {
        String clean = trim(value);
        return clean.isEmpty() ? null : new BigDecimal(clean);
    }
    private String emptyToNull(String value) {
        String clean = trim(value);
        return clean.isEmpty() ? null : clean;
    }
    private String trim(String value) { return value == null ? "" : value.trim(); }
}
