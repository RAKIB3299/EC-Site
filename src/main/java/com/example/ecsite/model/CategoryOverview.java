package com.example.ecsite.model;

/** Oracleで集計した管理画面用カテゴリ統計を保持するモデルです。 */
public class CategoryOverview {
    private final int totalCategories;
    private final int activeCategories;
    private final int inactiveCategories;
    private final int totalProducts;
    private final int uncategorizedProducts;

    public CategoryOverview(int totalCategories, int activeCategories,
                            int inactiveCategories, int totalProducts,
                            int uncategorizedProducts) {
        this.totalCategories = totalCategories;
        this.activeCategories = activeCategories;
        this.inactiveCategories = inactiveCategories;
        this.totalProducts = totalProducts;
        this.uncategorizedProducts = uncategorizedProducts;
    }

    public int getTotalCategories() { return totalCategories; }
    public int getActiveCategories() { return activeCategories; }
    public int getInactiveCategories() { return inactiveCategories; }
    public int getTotalProducts() { return totalProducts; }
    public int getUncategorizedProducts() { return uncategorizedProducts; }
}
