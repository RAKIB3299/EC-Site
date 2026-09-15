package com.example.ecsite.dao;

import com.example.ecsite.model.ProductImage;
import com.example.ecsite.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Oracleデータベースに対する ProductImageDAO のデータアクセス処理をまとめたDAOクラスです。
 *
 * <p>SQLはPreparedStatementで実行し、try-with-resourcesによってJDBCリソースを確実に解放します。</p>
 */
public class ProductImageDAO {
    /** 1商品に登録できる画像数の業務上限です。 */
    public static final int MAX_IMAGES = 4;
    /** 画像検索で共通利用する列を、mapメソッドが読む順番で定義しています。 */
    private static final String COLUMNS="image_id,prod_id,image_url,is_primary,display_order,created_at";

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public List<ProductImage> findByProductId(int productId)throws SQLException{
        // 主画像を先頭にし、その後は表示順とIDで安定した順序に並べます。
        List<ProductImage> images=new ArrayList<>();
        String sql="SELECT "+COLUMNS+" FROM ec_product_images WHERE prod_id=? ORDER BY CASE WHEN is_primary='Y' THEN 0 ELSE 1 END,display_order,image_id";
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){
            s.setInt(1,productId);try(ResultSet r=s.executeQuery()){while(r.next())images.add(map(r));}
        }return images;
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public ProductImage findPrimaryByProductId(int productId)throws SQLException{
        // 商品カードと詳細ページに表示するis_primary='Y'の画像だけを取得します。
        String sql="SELECT "+COLUMNS+" FROM ec_product_images WHERE prod_id=? AND is_primary='Y'";
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){
            s.setInt(1,productId);try(ResultSet r=s.executeQuery()){return r.next()?map(r):null;}
        }
    }

    /**
     * 指定条件をPreparedStatementへ設定し、Oracleデータベースから必要な情報を取得します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public int countByProductId(int productId)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("SELECT COUNT(*) FROM ec_product_images WHERE prod_id=?")){
            s.setInt(1,productId);try(ResultSet r=s.executeQuery()){r.next();return r.getInt(1);}
        }
    }

    /**
     * このクラスが公開する処理を実行し、呼び出し元へ結果を返します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public ProductImage addImage(int productId,String imageUrl)throws SQLException{
        // 商品確認・空き表示順の決定・画像登録を一つのトランザクションで行います。
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);try{
                // FOR UPDATEで同じ商品への同時画像追加を直列化し、上限超過を防ぎます。
                try(PreparedStatement s=c.prepareStatement("SELECT prod_id FROM products WHERE prod_id=? FOR UPDATE")){s.setInt(1,productId);try(ResultSet r=s.executeQuery()){if(!r.next()){c.rollback();return null;}}}
                boolean[] used=new boolean[MAX_IMAGES+1];int count=0;
                try(PreparedStatement s=c.prepareStatement("SELECT display_order FROM ec_product_images WHERE prod_id=?")){s.setInt(1,productId);try(ResultSet r=s.executeQuery()){while(r.next()){int n=r.getInt(1);if(n>=1&&n<=MAX_IMAGES)used[n]=true;count++;}}}
                if(count>=MAX_IMAGES)throw new ImageLimitException();
                int order=1;while(order<=MAX_IMAGES&&used[order])order++;
                long id;
                try(PreparedStatement s=c.prepareStatement("INSERT INTO ec_product_images(prod_id,image_url,is_primary,display_order) VALUES(?,?,?,?)",new String[]{"IMAGE_ID"})){
                    s.setInt(1,productId);s.setString(2,imageUrl);s.setString(3,count==0?"Y":"N");s.setInt(4,order);s.executeUpdate();
                    try(ResultSet keys=s.getGeneratedKeys()){if(!keys.next())throw new SQLException("Image ID was not returned.");id=keys.getLong(1);}
                }
                c.commit();return findById(c,productId,id);
            }catch(SQLException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }

    /**
     * このクラスが公開する処理を実行し、呼び出し元へ結果を返します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean makePrimary(int productId,long imageId)throws SQLException{
        // 対象の存在確認と主画像の切替を同時に確定し、主画像が複数になるのを防ぎます。
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);try{
                if(findById(c,productId,imageId)==null){c.rollback();return false;}
                try(PreparedStatement s=c.prepareStatement("UPDATE ec_product_images SET is_primary='N' WHERE prod_id=?")){s.setInt(1,productId);s.executeUpdate();}
                try(PreparedStatement s=c.prepareStatement("UPDATE ec_product_images SET is_primary='Y' WHERE prod_id=? AND image_id=?")){s.setInt(1,productId);s.setLong(2,imageId);if(s.executeUpdate()!=1){c.rollback();return false;}}
                c.commit();return true;
            }catch(SQLException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }

    /**
     * 対象IDと所有条件を確認し、許可されたデータだけを削除または解除します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public ProductImage deleteImage(int productId,long imageId)throws SQLException{
        // 削除した画像が主画像なら、残った先頭画像を新しい主画像へ昇格させます。
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);try{
                ProductImage old=findById(c,productId,imageId);if(old==null){c.rollback();return null;}
                try(PreparedStatement s=c.prepareStatement("DELETE FROM ec_product_images WHERE prod_id=? AND image_id=?")){s.setInt(1,productId);s.setLong(2,imageId);s.executeUpdate();}
                if(old.isPrimary())try(PreparedStatement s=c.prepareStatement("UPDATE ec_product_images SET is_primary='Y' WHERE image_id=(SELECT image_id FROM ec_product_images WHERE prod_id=? ORDER BY display_order,image_id FETCH FIRST 1 ROW ONLY)")){s.setInt(1,productId);s.executeUpdate();}
                c.commit();return old;
            }catch(SQLException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }

    private ProductImage findById(Connection c,int productId,long imageId)throws SQLException{
        try(PreparedStatement s=c.prepareStatement("SELECT "+COLUMNS+" FROM ec_product_images WHERE prod_id=? AND image_id=?")){s.setInt(1,productId);s.setLong(2,imageId);try(ResultSet r=s.executeQuery()){return r.next()?map(r):null;}}
    }
    private ProductImage map(ResultSet r)throws SQLException{return new ProductImage(r.getLong(1),r.getInt(2),r.getString(3),"Y".equals(r.getString(4)),r.getInt(5),r.getTimestamp(6));}
    public static class ImageLimitException extends SQLException{public ImageLimitException(){super("Maximum 4 images per product.");}}
}
