<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login | NEXORA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navigation.jsp" />
<main class="form-page">
    <section class="form-card">
        <h1>Login</h1>
        <p>Sign in to your NEXORA account.</p>
        <c:if test="${param.registered == 'true'}">
            <div class="message success">Registration successful. Please log in.</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="message error"><c:out value="${error}" /></div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/login">
            <c:if test="${param.returnTo == '/checkout'}">
                <input type="hidden" name="returnTo" value="/checkout">
            </c:if>
            <label for="email">Email</label>
            <input id="email" name="email" type="email" value="<c:out value='${email}' />" required autocomplete="email">

            <label for="password">Password</label>
            <input id="password" name="password" type="password" required autocomplete="current-password">

            <button class="form-button" type="submit">Login</button>
        </form>
        <p class="form-help">No account? <a href="${pageContext.request.contextPath}/register">Register here</a>.</p>
    </section>
</main>
</body>
</html>







