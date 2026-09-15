<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order Complete | NEXORA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navigation.jsp" />
<main class="success-page">
    <section class="success-card">
        <div class="success-mark" aria-hidden="true">&#10003;</div>
        <h1>Thank you for your order.</h1>
        <p class="order-number">Order #${order.orderId}</p>
        <dl>
            <dt>Status</dt><dd><c:out value="${order.status}" /></dd>
            <dt>Total</dt><dd>&yen;<fmt:formatNumber value="${order.totalPrice}" pattern="#,##0.00" /></dd>
        </dl>
        <div class="success-actions">
            <a class="detail-link" href="${pageContext.request.contextPath}/orders/detail?id=${order.orderId}">View This Order</a>
            <a class="primary-button" href="${pageContext.request.contextPath}/orders">View My Orders</a>
            <a href="${pageContext.request.contextPath}/products">Continue Shopping</a>
        </div>
    </section>
</main>
</body>
</html>







