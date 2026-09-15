<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cart | NEXORA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navigation.jsp" />
<main class="page-container">
    <div class="page-heading"><div><h1>Shopping Cart</h1><p>Review and update your products.</p></div></div>
    <c:if test="${param['empty'] == 'true'}">
        <div class="message error">Add at least one product before checkout.</div>
    </c:if>

    <c:choose>
        <c:when test="${empty cartItems}">
            <section class="empty-cart">
                <h2>Your cart is empty.</h2>
                <p>Add a product to begin shopping.</p>
                <a class="primary-button" href="${pageContext.request.contextPath}/products">Browse Products</a>
            </section>
        </c:when>
        <c:otherwise>
            <div class="cart-table-wrap">
                <table class="cart-table">
                    <thead><tr><th>Product</th><th>Unit price</th><th>Quantity</th><th>Subtotal</th><th>Remove</th></tr></thead>
                    <tbody>
                    <c:forEach var="item" items="${cartItems}">
                        <tr>
                            <td><div class="cart-product"><c:choose><c:when test="${not empty item.product.imageUrl}"><div class="ec-product-thumbnail-wrapper"><img class="ec-product-thumbnail" src="<c:if test='${item.product.localImage}'>${pageContext.request.contextPath}</c:if><c:out value='${item.product.imageUrl}'/>" alt="<c:out value='${item.product.productName}'/>" onerror="this.hidden=true;this.nextElementSibling.hidden=false"><div class="ec-product-thumbnail-placeholder" hidden aria-label="No image available for ${item.product.productName}">No image</div></div></c:when><c:otherwise><div class="ec-product-thumbnail-wrapper"><div class="ec-product-thumbnail-placeholder" aria-label="No image available for ${item.product.productName}">No image</div></div></c:otherwise></c:choose><div><strong><c:out value="${item.product.productName}" /></strong><span>Product ID: ${item.product.productId}</span></div></div></td>
                            <td><c:choose><c:when test="${not empty item.product.listPrice}">&yen;<fmt:formatNumber value="${item.product.listPrice}" pattern="#,##0.00" /></c:when><c:otherwise>Not available</c:otherwise></c:choose></td>
                            <td>
                                <form class="quantity-form" method="post" action="${pageContext.request.contextPath}/cart/update">
                                    <input type="hidden" name="productId" value="${item.product.productId}">
                                    <input aria-label="Quantity for ${item.product.productName}" name="quantity" type="number" value="${item.quantity}" min="0" required>
                                    <button class="small-button" type="submit">Update</button>
                                </form>
                            </td>
                            <td>&yen;<fmt:formatNumber value="${item.subtotal}" pattern="#,##0.00" /></td>
                            <td>
                                <form method="post" action="${pageContext.request.contextPath}/cart/remove">
                                    <input type="hidden" name="productId" value="${item.product.productId}">
                                    <button class="danger-button" type="submit">Remove</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <section class="cart-summary">
                <form method="post" action="${pageContext.request.contextPath}/cart/clear">
                    <button class="danger-button" type="submit">Clear Cart</button>
                </form>
                <div class="cart-checkout">
                    <p>Cart Total: <strong>&yen;<fmt:formatNumber value="${cartTotal}" pattern="#,##0.00" /></strong></p>
                    <c:choose>
                        <c:when test="${not empty sessionScope.user}"><a class="primary-button" href="${pageContext.request.contextPath}/checkout">Proceed to Checkout</a></c:when>
                        <c:otherwise><a class="primary-button" href="${pageContext.request.contextPath}/login?returnTo=/checkout">Login to Checkout</a></c:otherwise>
                    </c:choose>
                </div>
            </section>
        </c:otherwise>
    </c:choose>
</main>
</body>
</html>







