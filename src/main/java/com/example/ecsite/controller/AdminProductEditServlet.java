package com.example.ecsite.controller;

import com.example.ecsite.dao.CategoryDAO;
import com.example.ecsite.dao.ProductDAO;
import com.example.ecsite.dao.ProductImageDAO;
import com.example.ecsite.model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;

/**
 * HTTPリクエストを受け取り、AdminProductEditServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/admin/products/edit")
public class AdminProductEditServlet extends HttpServlet {
    private static final Set<String> STATUSES = Set.of(
            "orderable", "planned", "under development", "obsolete");
    private final ProductDAO productDAO = new ProductDAO();
    private final ProductImageDAO productImageDAO = new ProductImageDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    /**
     * GETリクエストを処理し、必要なデータを準備して画面を表示します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer id = positiveInteger(request.getParameter("id"));
        if (id == null) { response.sendError(400, "Invalid product ID."); return; }
        try {
            Product product = productDAO.findById(id);
            if (product == null) { response.sendError(404, "Product not found."); return; }
            showForm(request, response, product, null);
        } catch (SQLException e) {
            getServletContext().log("Admin product load failed", e);
            response.sendError(500, "Product cannot be loaded.");
        }
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
        Integer id = positiveInteger(request.getParameter("productId"));
        if (id == null) { response.sendError(400, "Invalid product ID."); return; }
        try {
            Product product = productDAO.findById(id);
            if (product == null) { response.sendError(404, "Product not found."); return; }
            String originalCatalogUrl = product.getCatalogUrl();
            try {
                product.setProductName(trim(request.getParameter("productName")));
                product.setCategoryId(optionalInteger(request.getParameter("categoryId")));
                product.setWeightClass(optionalInteger(request.getParameter("weightClass")));
                product.setSupplierId(optionalInteger(request.getParameter("supplierId")));
                product.setProductStatus(trim(request.getParameter("productStatus")));
                product.setListPrice(requiredDecimal(request.getParameter("listPrice")));
                product.setMinimumPrice(optionalDecimal(request.getParameter("minimumPrice")));
                product.setCatalogUrl(emptyToNull(request.getParameter("catalogUrl")));
            } catch (NumberFormatException e) {
                showForm(request, response, product,
                        "Weight class, supplier ID, and prices must contain valid numbers.");
                return;
            }

            String error = validate(product, originalCatalogUrl);
            if (error != null) { showForm(request, response, product, error); return; }
            if (!productDAO.update(product)) {
                response.sendError(404, "Product not found.");
                return;
            }
            response.sendRedirect(request.getContextPath()
                    + "/admin/products/edit?id=" + id + "&message=updated");
        } catch (SQLException e) {
            getServletContext().log("Admin product update failed", e);
            response.sendError(500, "Product could not be updated.");
        }
    }

    private String validate(Product product, String originalCatalogUrl) throws SQLException {
        if (product.getProductName().isEmpty()) return "Product name is required.";
        if (product.getProductName().length() > 25)
            return "Product name must not exceed 25 characters.";
        if (!STATUSES.contains(product.getProductStatus()))
            return "Please choose a valid product status.";
        if (product.getCategoryId() != null && categoryDAO.findById(product.getCategoryId()) == null)
            return "Please choose a valid category.";
        if (product.getWeightClass() != null
                && (product.getWeightClass() < 0 || product.getWeightClass() > 9))
            return "Weight class must be a whole number from 0 to 9.";
        if (product.getSupplierId() != null
                && (product.getSupplierId() < 0 || product.getSupplierId() > 999999))
            return "Supplier ID must be a whole number from 0 to 999999.";
        if (!validPrice(product.getListPrice()))
            return "List price must be non-negative, with up to 6 whole digits and 2 decimal places.";
        if (product.getMinimumPrice() != null && !validPrice(product.getMinimumPrice()))
            return "Minimum price must be non-negative, with up to 6 whole digits and 2 decimal places.";
        if (product.getMinimumPrice() != null
                && product.getMinimumPrice().compareTo(product.getListPrice()) > 0)
            return "Minimum price cannot be greater than list price.";

        String catalogUrl = product.getCatalogUrl();
        if (catalogUrl != null && catalogUrl.length() > 50)
            return "Catalog URL must not exceed 50 characters.";
        boolean catalogChanged = !Objects.equals(originalCatalogUrl, catalogUrl);
        if (catalogChanged && Product.isLegacyOracleCatalogUrl(catalogUrl))
            return "This legacy Oracle demo catalog domain is no longer supported.";
        if (catalogChanged && catalogUrl != null && !Product.isValidCatalogUrl(catalogUrl))
            return "Catalog URL must be a complete http:// or https:// address.";
        return null;
    }

    private boolean validPrice(BigDecimal value) {
        if (value == null || value.signum() < 0 || value.scale() > 2) return false;
        return value.precision() - value.scale() <= 6;
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response,
                          Product product, String error) throws ServletException, IOException {
        request.setAttribute("product", product);
        request.setAttribute("legacyCatalogUrl", Product.isLegacyOracleCatalogUrl(product.getCatalogUrl()));
        try {
            request.setAttribute("productImages", productImageDAO.findByProductId(product.getProductId()));
            request.setAttribute("imageCount", productImageDAO.countByProductId(product.getProductId()));
            request.setAttribute("categories", categoryDAO.findAll());
        } catch (SQLException e) {
            throw new ServletException("Could not load product edit data.", e);
        }
        if (error != null) request.setAttribute("error", error);
        request.getRequestDispatcher("/WEB-INF/views/admin/product-edit.jsp").forward(request, response);
    }

    private Integer positiveInteger(String value) {
        try { int number = Integer.parseInt(value); return number > 0 ? number : null; }
        catch (Exception e) { return null; }
    }
    private Integer optionalInteger(String value) {
        String clean = trim(value); return clean.isEmpty() ? null : Integer.valueOf(clean);
    }
    private BigDecimal optionalDecimal(String value) {
        String clean = trim(value); return clean.isEmpty() ? null : new BigDecimal(clean);
    }
    private BigDecimal requiredDecimal(String value) {
        String clean = trim(value); if (clean.isEmpty()) throw new NumberFormatException();
        return new BigDecimal(clean);
    }
    private String emptyToNull(String value) {
        String clean = trim(value); return clean.isEmpty() ? null : clean;
    }
    private String trim(String value) { return value == null ? "" : value.trim(); }
}
