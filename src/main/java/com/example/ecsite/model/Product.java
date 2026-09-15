package com.example.ecsite.model;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

/** Represents one row from the live Oracle PRODUCTS table. */
/**
 * ECサイトで使用する Product のデータを保持するモデルクラスです。
 *
 * <p>DAOと画面の間で値を受け渡すために使用し、データベース処理や画面処理は持ちません。</p>
 */
public class Product {
    private int productId;
    private String productName;
    private Integer categoryId;
    private Integer weightClass;
    private String warrantyPeriod;
    private Integer supplierId;
    private String productStatus;
    private BigDecimal listPrice;
    private BigDecimal minimumPrice;
    private String catalogUrl;
    private String imageUrl;
    private String categoryName;

    public Product() {
    }

    public Product(int productId, String productName, Integer categoryId,
                   Integer weightClass, String warrantyPeriod, Integer supplierId,
                   String productStatus, BigDecimal listPrice,
                   BigDecimal minimumPrice, String catalogUrl) {
        this.productId = productId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.weightClass = weightClass;
        this.warrantyPeriod = warrantyPeriod;
        this.supplierId = supplierId;
        this.productStatus = productStatus;
        this.listPrice = listPrice;
        this.minimumPrice = minimumPrice;
        this.catalogUrl = catalogUrl;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public Integer getWeightClass() { return weightClass; }
    public void setWeightClass(Integer weightClass) { this.weightClass = weightClass; }
    public String getWarrantyPeriod() { return warrantyPeriod; }
    public void setWarrantyPeriod(String warrantyPeriod) { this.warrantyPeriod = warrantyPeriod; }
    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }
    public String getProductStatus() { return productStatus; }
    public void setProductStatus(String productStatus) { this.productStatus = productStatus; }
    public BigDecimal getListPrice() { return listPrice; }
    public void setListPrice(BigDecimal listPrice) { this.listPrice = listPrice; }
    public BigDecimal getMinimumPrice() { return minimumPrice; }
    public void setMinimumPrice(BigDecimal minimumPrice) { this.minimumPrice = minimumPrice; }
    public String getCatalogUrl() { return catalogUrl; }
    public void setCatalogUrl(String catalogUrl) { this.catalogUrl = catalogUrl; }

    /** True only for a complete, safe web URL that a customer can click. */
    public boolean isCatalogUrlValid() {
        return isValidCatalogUrl(catalogUrl);
    }

    /** Customer pages hide retired Oracle sample supplier websites. */
    public boolean isCatalogUrlDisplayable() {
        return isValidCatalogUrl(catalogUrl) && !isLegacyOracleCatalogUrl(catalogUrl);
    }

    public static boolean isLegacyOracleCatalogUrl(String value) {
        if (!isValidCatalogUrl(value)) return false;
        try {
            String host = new URI(value).getHost();
            return host != null && host.toLowerCase().startsWith("www.supp-");
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public static boolean isValidCatalogUrl(String value) {
        if (value == null || value.isBlank()) return false;
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            return scheme != null
                    && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                    && uri.isAbsolute()
                    && uri.getHost() != null
                    && !uri.getHost().isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }
    public String getImageUrl(){return imageUrl;}
    public void setImageUrl(String imageUrl){this.imageUrl=imageUrl;}
    public boolean isLocalImage(){return imageUrl != null && (imageUrl.startsWith("/images/") || imageUrl.startsWith("/product-images/"));}
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getDisplayCategoryName() { return categoryName == null || categoryName.isBlank() ? "Other" : categoryName; }
}
