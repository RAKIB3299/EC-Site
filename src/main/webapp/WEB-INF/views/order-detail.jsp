<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order #${order.orderId} | NEXORA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navigation.jsp" />
<main class="page-container">
    <a class="back-link" href="${pageContext.request.contextPath}/orders">&larr; Back to My Orders</a>
    <section class="order-detail-header">
        <div><h1>Order #${order.orderId}</h1><p><fmt:formatDate value="${order.orderDate}" pattern="yyyy-MM-dd HH:mm" /></p></div>
        <span class="status"><c:out value="${order.status}" /></span>
        <dl>
            <dt>Shipping address</dt><dd><c:out value="${order.shippingAddress}" /></dd>
            <dt>Final total</dt><dd>&yen;<fmt:formatNumber value="${order.totalPrice}" pattern="#,##0.00" /></dd>
        </dl>
    </section>
    <c:if test="${not empty tracking}">
        <section class="tracking-card"><h2>Shipping &amp; Tracking</h2><dl>
            <c:if test="${not empty tracking.carrier}"><dt>Carrier</dt><dd><c:out value="${tracking.carrier}" /></dd></c:if>
            <c:if test="${not empty tracking.trackingNumber}"><dt>Tracking Number</dt><dd><c:out value="${tracking.trackingNumber}" /></dd></c:if>
            <c:if test="${not empty tracking.shipmentStatus}"><dt>Shipment Status</dt><dd><c:out value="${tracking.shipmentStatus}" /></dd></c:if>
            <c:if test="${not empty tracking.trackingInformation}"><dt>Tracking Information</dt><dd><c:out value="${tracking.trackingInformation}" /></dd></c:if>
            <dt>Last Updated</dt><dd><fmt:formatDate value="${tracking.updatedAt}" pattern="yyyy-MM-dd HH:mm" /></dd>
        </dl></section>
    </c:if>

    <h2>Order items</h2>
    <div class="cart-table-wrap">
        <table class="cart-table order-items-table">
            <thead><tr><th>Product</th><th>Unit price at purchase</th><th>Quantity</th><th>Subtotal</th></tr></thead>
            <tbody>
            <c:forEach var="item" items="${orderItems}">
                <tr>
                    <td><div class="order-product"><c:choose><c:when test="${not empty item.imageUrl}"><div class="ec-product-thumbnail-wrapper"><img class="ec-product-thumbnail" src="<c:if test='${item.localImage}'>${pageContext.request.contextPath}</c:if><c:out value='${item.imageUrl}'/>" alt="<c:out value='${item.productName}'/>" onerror="this.hidden=true;this.nextElementSibling.hidden=false"><div class="ec-product-thumbnail-placeholder" hidden>No image</div></div></c:when><c:otherwise><div class="ec-product-thumbnail-wrapper"><div class="ec-product-thumbnail-placeholder" aria-label="No image available for ${item.productName}">No image</div></div></c:otherwise></c:choose><div><strong><c:choose><c:when test="${not empty item.productName}"><c:out value="${item.productName}" /></c:when><c:otherwise>Not available</c:otherwise></c:choose></strong><span>Product ID: ${item.prodId}</span></div></div></td>
                    <td>&yen;<fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.00" /></td>
                    <td>${item.quantity}</td>
                    <td>&yen;<fmt:formatNumber value="${item.subtotal}" pattern="#,##0.00" /></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</main>
</body>
</html>






