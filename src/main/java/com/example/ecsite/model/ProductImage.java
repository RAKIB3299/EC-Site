package com.example.ecsite.model;

import java.sql.Timestamp;

/**
 * ECサイトで使用する ProductImage のデータを保持するモデルクラスです。
 *
 * <p>DAOと画面の間で値を受け渡すために使用し、データベース処理や画面処理は持ちません。</p>
 */
public class ProductImage {
    private long imageId;
    private int prodId;
    private String imageUrl;
    private boolean primary;
    private int displayOrder;
    private Timestamp createdAt;

    public ProductImage() { }
    public ProductImage(long imageId, int prodId, String imageUrl,
                        boolean primary, int displayOrder, Timestamp createdAt) {
        this.imageId=imageId; this.prodId=prodId; this.imageUrl=imageUrl;
        this.primary=primary; this.displayOrder=displayOrder; this.createdAt=createdAt;
    }
    public long getImageId(){return imageId;} public void setImageId(long v){imageId=v;}
    public int getProdId(){return prodId;} public void setProdId(int v){prodId=v;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
    public boolean isPrimary(){return primary;} public void setPrimary(boolean v){primary=v;}
    public int getDisplayOrder(){return displayOrder;} public void setDisplayOrder(int v){displayOrder=v;}
    public Timestamp getCreatedAt(){return createdAt;} public void setCreatedAt(Timestamp v){createdAt=v;}
    public boolean isLocalImage(){return imageUrl!=null&&(imageUrl.startsWith("/images/")||imageUrl.startsWith("/product-images/"));}
}
