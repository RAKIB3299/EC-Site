<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register | NEXORA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23">
</head>
<body>
<jsp:include page="/WEB-INF/views/includes/navigation.jsp" />
<main class="form-page">
    <section class="form-card form-card-wide">
        <h1>Create an account</h1>
        <p>Fields marked with * are required.</p>
        <c:if test="${not empty error}">
            <div class="message error"><c:out value="${error}" /></div>
        </c:if>
        <form method="post" action="${pageContext.request.contextPath}/register">
            <div class="form-grid">
                <div>
                    <label for="firstName">First name *</label>
                    <input id="firstName" name="firstName" value="<c:out value='${firstName}' />" required maxlength="100" autocomplete="given-name">
                </div>
                <div>
                    <label for="lastName">Last name *</label>
                    <input id="lastName" name="lastName" value="<c:out value='${lastName}' />" required maxlength="100" autocomplete="family-name">
                </div>
            </div>

            <label for="email">Email *</label>
            <input id="email" name="email" type="email" value="<c:out value='${email}' />" required maxlength="255" autocomplete="email">

            <div class="form-grid">
                <div>
                    <label for="password">Password *</label>
                    <input id="password" name="password" type="password" required minlength="8" autocomplete="new-password">
                </div>
                <div>
                    <label for="confirmPassword">Confirm password *</label>
                    <input id="confirmPassword" name="confirmPassword" type="password" required minlength="8" autocomplete="new-password">
                </div>
            </div>

            <label for="phone">Phone</label>
            <input id="phone" name="phone" value="<c:out value='${phone}' />" maxlength="30" autocomplete="tel">

            <label for="address">Address</label>
            <textarea id="address" name="address" rows="4" maxlength="500" autocomplete="street-address"><c:out value="${address}" /></textarea>

            <button class="form-button" type="submit">Register</button>
        </form>
        <p class="form-help">Already registered? <a href="${pageContext.request.contextPath}/login">Login here</a>.</p>
    </section>
</main>
</body>
</html>







