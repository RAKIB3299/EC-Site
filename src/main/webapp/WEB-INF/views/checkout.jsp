<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Checkout | NEXORA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navigation.jsp" />
<main class="page-container checkout-page">
    <div class="page-heading"><div><h1>Checkout</h1><p>Review your order before placing it.</p></div></div>
    <c:if test="${not empty error}"><div class="message error"><c:out value="${error}" /></div></c:if>

    <div class="checkout-layout">
        <section class="checkout-panel">
            <h2>Customer and shipping</h2>
            <p><strong>Name:</strong> <c:out value="${sessionScope.user.firstName}" /> <c:out value="${sessionScope.user.lastName}" /></p>
            <p><strong>Email:</strong> <c:out value="${sessionScope.user.email}" /></p>
            <form id="place-order-form" method="post" action="${pageContext.request.contextPath}/checkout">
                <label for="shippingAddress">Shipping address</label>
                <textarea id="shippingAddress" name="shippingAddress" rows="6" maxlength="500" required><c:out value="${shippingAddress}" /></textarea>
            </form>
        </section>

        <section class="checkout-panel">
            <h2>Order summary</h2>
            <div class="checkout-items">
                <c:forEach var="item" items="${cartItems}">
                    <div class="checkout-item">
                        <div><strong><c:out value="${item.product.productName}" /></strong><span>${item.quantity} &times; &yen;<fmt:formatNumber value="${item.product.listPrice}" pattern="#,##0.00" /></span></div>
                        <strong>&yen;<fmt:formatNumber value="${item.subtotal}" pattern="#,##0.00" /></strong>
                    </div>
                </c:forEach>
            </div>
            <p class="checkout-total">Final Total: <strong>&yen;<fmt:formatNumber value="${checkoutTotal}" pattern="#,##0.00" /></strong></p>
            <button class="form-button checkout-button" type="submit" form="place-order-form">Place Order</button>
        </section>
    </div>
</main>
</body>
</html>







