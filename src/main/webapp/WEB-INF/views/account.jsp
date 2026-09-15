<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>My Account | NEXORA</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23"></head>
<body><jsp:include page="/WEB-INF/views/includes/navigation.jsp" />
<main class="page-container account-page">
    <div class="page-heading"><div><h1>My Account</h1><p>View your account and safely update your details.</p></div></div>
    <section class="account-summary">
        <h2>Account information</h2>
        <dl><dt>First name</dt><dd><c:out value="${sessionScope.user.firstName}" /></dd>
            <dt>Last name</dt><dd><c:out value="${sessionScope.user.lastName}" /></dd>
            <dt>Email</dt><dd><c:out value="${sessionScope.user.email}" /></dd>
            <dt>Phone</dt><dd><c:out value="${empty sessionScope.user.phone ? 'Not available' : sessionScope.user.phone}" /></dd>
            <dt>Address</dt><dd><c:out value="${empty sessionScope.user.address ? 'Not available' : sessionScope.user.address}" /></dd>
            <dt>Role</dt><dd><c:out value="${sessionScope.user.role}" /></dd>
            <dt>Created</dt><dd><fmt:formatDate value="${sessionScope.user.createdAt}" pattern="yyyy-MM-dd HH:mm" /></dd></dl>
    </section>
    <div class="account-forms">
        <section class="form-card"><h2>Edit profile</h2>
            <c:if test="${param.profileUpdated == 'true'}"><div class="message success">Profile updated successfully.</div></c:if>
            <c:if test="${not empty profileError}"><div class="message error"><c:out value="${profileError}" /></div></c:if>
            <form method="post" action="${pageContext.request.contextPath}/account/update">
                <label for="firstName">First name</label><input id="firstName" name="firstName" maxlength="100" required value="<c:out value='${not empty profileError ? formFirstName : sessionScope.user.firstName}' />">
                <label for="lastName">Last name</label><input id="lastName" name="lastName" maxlength="100" required value="<c:out value='${not empty profileError ? formLastName : sessionScope.user.lastName}' />">
                <label for="phone">Phone</label><input id="phone" name="phone" maxlength="30" value="<c:out value='${not empty profileError ? formPhone : sessionScope.user.phone}' />">
                <label for="address">Address</label><textarea id="address" name="address" maxlength="500" rows="4"><c:out value="${not empty profileError ? formAddress : sessionScope.user.address}" /></textarea>
                <button class="form-button" type="submit">Save Profile</button>
            </form>
            <p class="readonly-note">Email and role cannot be changed here.</p>
        </section>
        <section class="form-card"><h2>Change password</h2>
            <c:if test="${param.passwordUpdated == 'true'}"><div class="message success">Password changed successfully.</div></c:if>
            <c:if test="${not empty passwordError}"><div class="message error"><c:out value="${passwordError}" /></div></c:if>
            <form method="post" action="${pageContext.request.contextPath}/account/password">
                <label for="currentPassword">Current password</label><input id="currentPassword" name="currentPassword" type="password" autocomplete="current-password" required>
                <label for="newPassword">New password</label><input id="newPassword" name="newPassword" type="password" minlength="8" autocomplete="new-password" required>
                <label for="confirmPassword">Confirm new password</label><input id="confirmPassword" name="confirmPassword" type="password" minlength="8" autocomplete="new-password" required>
                <button class="form-button" type="submit">Change Password</button>
            </form>
        </section>
    </div>
</main></body></html>







