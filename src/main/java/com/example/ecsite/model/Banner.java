package com.example.ecsite.model;

import java.sql.Timestamp;

/**
 * ECサイトで使用する Banner のデータを保持するモデルクラスです。
 *
 * <p>DAOと画面の間で値を受け渡すために使用し、データベース処理や画面処理は持ちません。</p>
 */
public class Banner {
    private long bannerId;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private int displayOrder;
    private boolean active;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Banner() { }
    public long getBannerId(){return bannerId;} public void setBannerId(long v){bannerId=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
    public String getLinkUrl(){return linkUrl;} public void setLinkUrl(String v){linkUrl=v;}
    public int getDisplayOrder(){return displayOrder;} public void setDisplayOrder(int v){displayOrder=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
    public Timestamp getCreatedAt(){return createdAt;} public void setCreatedAt(Timestamp v){createdAt=v;}
    public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp v){updatedAt=v;}
    public boolean isInternalLink(){return linkUrl!=null&&linkUrl.startsWith("/");}
}
