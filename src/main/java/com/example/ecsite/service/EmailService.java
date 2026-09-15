package com.example.ecsite.service;

import com.example.ecsite.model.Order;
import com.example.ecsite.model.OrderTracking;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Sends optional SMTP email. Missing/broken SMTP never changes database results. */
/**
 * 複数の処理を安全に連携させる EmailService のサービスクラスです。
 *
 * <p>データの整合性を守りながらDAOを組み合わせ、画面から独立した業務処理を担当します。</p>
 */
public class EmailService {
    private static final Logger LOG=Logger.getLogger(EmailService.class.getName());
    private final Properties config=load();

    /**
     * 外部入力を利用する前に、安全性と形式が条件を満たしているか確認します。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public boolean isConfigured(){return value("SMTP_HOST")!=null&&value("SMTP_PORT")!=null&&value("SMTP_USERNAME")!=null&&value("SMTP_PASSWORD")!=null&&value("SMTP_FROM")!=null;}

    /**
     * SMTP設定を利用してメールを送信し、失敗時はデータベース更新と分離して扱います。
     *
     * <p>日本語メソッド説明: 引数は呼び出し元で準備された値を使用し、処理結果または例外を呼び出し元へ返します。</p>
     */
    public DeliveryResult sendOrderEvent(Order order,String type,OrderTracking tracking){
        if(order==null||order.getCustomerEmail()==null){LOG.warning("Order email skipped because the customer email was unavailable.");return DeliveryResult.FAILED;}
        if(!isConfigured()){LOG.warning("Order email skipped for order #"+order.getOrderId()+" because SMTP_HOST, SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, or SMTP_FROM is missing.");return DeliveryResult.NOT_CONFIGURED;}
        String subject=subject(type,order.getOrderId());
        String body=body(order,type,tracking);
        try{send(order.getCustomerEmail(),subject,body);LOG.info("Order email sent successfully for order #"+order.getOrderId()+" ("+type+").");return DeliveryResult.SENT;}catch(Exception e){LOG.log(Level.WARNING,"Order email could not be sent for order #"+order.getOrderId()+". The database update was kept.",e);return DeliveryResult.FAILED;}
    }

    private void send(String to,String subject,String body)throws MessagingException{
        Properties p=new Properties();p.put("mail.smtp.host",value("SMTP_HOST"));p.put("mail.smtp.port",or(value("SMTP_PORT"),"587"));
        boolean auth=value("SMTP_USERNAME")!=null;p.put("mail.smtp.auth",String.valueOf(auth));p.put("mail.smtp.starttls.enable",or(value("SMTP_STARTTLS"),"true"));
        Session session=Session.getInstance(p,auth?new Authenticator(){protected PasswordAuthentication getPasswordAuthentication(){return new PasswordAuthentication(value("SMTP_USERNAME"),value("SMTP_PASSWORD"));}}:null);
        MimeMessage message=new MimeMessage(session);message.setFrom(new InternetAddress(value("SMTP_FROM")));message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(to,false));message.setSubject(subject,StandardCharsets.UTF_8.name());message.setText(body,StandardCharsets.UTF_8.name());Transport.send(message);
    }

    private String subject(String type,long id){return switch(type){case"ORDER_CONFIRMED"->"Order #"+id+" Confirmed";case"ORDER_PROCESSING"->"Order #"+id+" Is Being Processed";case"ORDER_SHIPPED"->"Your Order #"+id+" Has Been Shipped";case"TRACKING_UPDATED"->"Tracking Update for Order #"+id;case"ORDER_DELIVERED"->"Order #"+id+" Delivered";case"ORDER_CANCELLED"->"Order #"+id+" Cancelled";default->"Order #"+id+" Update";};}
    private String body(Order o,String type,OrderTracking t){String name=o.getCustomerName()==null?"Customer":o.getCustomerName();String action=switch(type){case"ORDER_CONFIRMED"->"has been confirmed.";case"ORDER_PROCESSING"->"is now being processed.";case"ORDER_SHIPPED"->"has been shipped.";case"TRACKING_UPDATED"->"has new tracking information.";case"ORDER_DELIVERED"->"has been delivered.";case"ORDER_CANCELLED"->"has been cancelled.";default->"has been updated.";};StringBuilder b=new StringBuilder("Hello ").append(name).append(",\n\nYour order #").append(o.getOrderId()).append(' ').append(action).append('\n');if(t!=null){if(t.getCarrier()!=null)b.append("\nCarrier: ").append(t.getCarrier());if(t.getTrackingNumber()!=null)b.append("\nTracking Number: ").append(t.getTrackingNumber());if(t.getShipmentStatus()!=null)b.append("\nShipping Status: ").append(t.getShipmentStatus());}return b.append("\n\nYou can view the latest order information from your account.\n\nThank you for shopping with NEXORA.").toString();}
    private String value(String key){String env=System.getenv(key);if(env!=null&&!env.isBlank())return env.trim();String prop=config.getProperty(key);return prop==null||prop.isBlank()?null:prop.trim();}
    private String or(String value,String fallback){return value==null?fallback:value;}
    private Properties load(){Properties p=new Properties();try(InputStream in=EmailService.class.getClassLoader().getResourceAsStream("email.properties")){if(in!=null)p.load(in);}catch(Exception e){LOG.log(Level.WARNING,"Optional email.properties could not be read.",e);}return p;}
    public enum DeliveryResult { SENT, NOT_CONFIGURED, FAILED }
}
