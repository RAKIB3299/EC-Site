<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %><%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>My Orders | NEXORA</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23"></head><body><jsp:include page="/WEB-INF/views/includes/navigation.jsp"/>
<main class="page-container"><div class="page-heading"><div><h1>My Orders</h1><p>Your NEXORA order history.</p></div></div><c:choose><c:when test="${empty orders}"><section class="empty-cart"><h2>You have no orders yet.</h2><p>Browse the catalog to place your first order.</p><a class="primary-button" href="${pageContext.request.contextPath}/products">Browse Products</a></section></c:when><c:otherwise><div class="order-list"><c:forEach var="order" items="${orders}"><article class="order-card"><div class="my-order-card-content">
<c:choose><c:when test="${not empty order.previewImageUrl}"><div class="ec-product-thumbnail-wrapper"><img class="ec-product-thumbnail" src="<c:if test='${order.localPreviewImage}'>${pageContext.request.contextPath}</c:if><c:out value='${order.previewImageUrl}'/>" alt="Preview for Order ${order.orderId}" onerror="this.hidden=true;this.nextElementSibling.hidden=false"><div class="ec-product-thumbnail-placeholder" hidden>No image</div></div></c:when><c:otherwise><div class="ec-product-thumbnail-wrapper"><div class="ec-product-thumbnail-placeholder" aria-label="No image available for Order ${order.orderId}">No image</div></div></c:otherwise></c:choose>
<div class="my-order-info"><div class="order-card-heading"><div><h2>Order #${order.orderId}</h2><p><fmt:formatDate value="${order.orderDate}" pattern="yyyy-MM-dd HH:mm"/></p></div><span class="status"><c:out value="${order.status}"/></span></div><dl><dt>Total</dt><dd>&yen;<fmt:formatNumber value="${order.totalPrice}" pattern="#,##0.00"/></dd><dt>Shipping address</dt><dd><c:out value="${order.shippingAddress}"/></dd></dl><c:if test="${order.itemCount > 1}"><p class="order-more-items">+ ${order.itemCount - 1} more item<c:if test="${order.itemCount > 2}">s</c:if></p></c:if><a class="detail-link" href="${pageContext.request.contextPath}/orders/detail?id=${order.orderId}">View Details</a></div>
</div></article></c:forEach></div></c:otherwise></c:choose></main></body></html>







