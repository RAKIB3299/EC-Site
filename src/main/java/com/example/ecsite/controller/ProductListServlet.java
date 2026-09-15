package com.example.ecsite.controller;

import com.example.ecsite.dao.ProductDAO;
import com.example.ecsite.dao.BannerDAO;
import com.example.ecsite.dao.CategoryDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * HTTPリクエストを受け取り、ProductListServlet に対応する画面処理を行うサーブレットです。
 *
 * <p>入力値とログイン状態を確認し、必要なDAO・サービスを呼び出して、安全な画面遷移を行います。</p>
 */
@WebServlet("/products")
public class ProductListServlet extends HttpServlet {
    private static final int PAGE_SIZE = 12;
    private static final Set<String> STATUSES = Set.of(
            "orderable", "planned", "under development", "obsolete");
    private static final Set<String> SORTS = Set.of(
            "id_asc", "id_desc", "price_asc", "price_desc", "name_asc", "name_desc");
    private final ProductDAO productDAO = new ProductDAO();
    private final BannerDAO bannerDAO = new BannerDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    /**
     * GETリクエストを処理し、必要なデータを準備して画面を表示します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int requestedPage = readPageNumber(request.getParameter("page"));
        String query = trim(request.getParameter("q"));
        String status = trim(request.getParameter("status"));
        String categoryValue = trim(request.getParameter("category"));
        String minValue = trim(request.getParameter("minPrice"));
        String maxValue = trim(request.getParameter("maxPrice"));
        String sort = trim(request.getParameter("sort"));
        if (sort.isEmpty()) sort = "id_asc";

        String error = null;
        Integer categoryId = null;
        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        if (!status.isEmpty() && !STATUSES.contains(status)) {
            error = "Please choose a valid product status.";
            status = "";
        }
        if (!SORTS.contains(sort)) {
            error = error == null ? "Please choose a valid sorting option." : error;
            sort = "id_asc";
        }
        try {
            if (!categoryValue.isEmpty()) {
                categoryId = Integer.valueOf(categoryValue);
                if (categoryId < 0) throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            error = error == null ? "Category must be a valid number." : error;
            categoryValue = "";
            categoryId = null;
        }
        try {
            if (!minValue.isEmpty()) minPrice = new BigDecimal(minValue);
            if (!maxValue.isEmpty()) maxPrice = new BigDecimal(maxValue);
            if ((minPrice != null && minPrice.signum() < 0)
                    || (maxPrice != null && maxPrice.signum() < 0)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            error = error == null ? "Prices must be valid non-negative numbers." : error;
            minPrice = null;
            maxPrice = null;
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            error = error == null ? "Minimum price cannot be greater than maximum price." : error;
            minPrice = null;
            maxPrice = null;
        }

        try {
            int totalProducts = productDAO.countSearchResults(
                    query, status, categoryId, minPrice, maxPrice);
            int totalPages = Math.max(1, (int) Math.ceil((double) totalProducts / PAGE_SIZE));
            int currentPage = Math.min(requestedPage, totalPages);

            request.setAttribute("products", productDAO.searchProducts(
                    query, status, categoryId, minPrice, maxPrice, sort, currentPage, PAGE_SIZE));
            request.setAttribute("banners", bannerDAO.findActiveBanners());
            request.setAttribute("categories", categoryDAO.findActiveCategories());
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalProducts", totalProducts);
            request.setAttribute("q", query);
            request.setAttribute("status", status);
            request.setAttribute("category", categoryValue);
            request.setAttribute("minPrice", minValue);
            request.setAttribute("maxPrice", maxValue);
            request.setAttribute("sort", sort);
            request.setAttribute("filterQuery", buildFilterQuery(
                    query, status, categoryValue, minValue, maxValue, sort));
            request.setAttribute("error", error);
            request.getRequestDispatcher("/WEB-INF/views/products.jsp")
                    .forward(request, response);
        } catch (SQLException e) {
            getServletContext().log("Product search failed", e);
            request.setAttribute("error", "Products could not be loaded. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/products.jsp").forward(request, response);
        }
    }

    private int readPageNumber(String value) {
        if (value == null || value.isBlank()) {
            return 1;
        }
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private String buildFilterQuery(String query, String status, String category,
                                    String minPrice, String maxPrice, String sort) {
        StringBuilder result = new StringBuilder();
        append(result, "q", query);
        append(result, "status", status);
        append(result, "category", category);
        append(result, "minPrice", minPrice);
        append(result, "maxPrice", maxPrice);
        if (!"id_asc".equals(sort)) append(result, "sort", sort);
        return result.toString();
    }

    private void append(StringBuilder result, String name, String value) {
        if (value == null || value.isBlank()) return;
        result.append('&').append(name).append('=')
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }
}
