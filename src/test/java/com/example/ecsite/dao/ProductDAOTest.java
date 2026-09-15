package com.example.ecsite.dao;

import com.example.ecsite.model.Product;

/** Simple command-line test for ProductDAO.findById. */
/**
 * Oracleデータベースに対する ProductDAOTest のデータアクセス処理をまとめたDAOクラスです。
 *
 * <p>SQLはPreparedStatementで実行し、try-with-resourcesによってJDBCリソースを確実に解放します。</p>
 */
public class ProductDAOTest {
    /**
     * このクラスが公開する処理を実行し、呼び出し元へ結果を返します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public static void main(String[] args) throws Exception {
        ProductDAO productDAO = new ProductDAO();

        Product existingProduct = productDAO.findById(1726);
        if (existingProduct == null) {
            throw new AssertionError("Expected product 1726 to exist.");
        }

        Product missingProduct = productDAO.findById(Integer.MAX_VALUE);
        if (missingProduct != null) {
            throw new AssertionError("Expected an unknown product ID to return null.");
        }

        System.out.println("Product 1726 found: " + existingProduct.getProductName());
        System.out.println("Unknown product correctly returned null.");
    }
}
