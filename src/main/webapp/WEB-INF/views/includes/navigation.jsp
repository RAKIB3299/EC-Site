<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="navPath" value="${not empty requestScope['jakarta.servlet.forward.request_uri'] ? requestScope['jakarta.servlet.forward.request_uri'] : pageContext.request.requestURI}"/>
<c:set var="navHome" value="${navPath == pageContext.request.contextPath.concat('/') || fn:endsWith(navPath,'/index.jsp')}"/>
<c:set var="navProducts" value="${fn:startsWith(navPath,pageContext.request.contextPath.concat('/product'))}"/>
<c:set var="navCart" value="${fn:startsWith(navPath,pageContext.request.contextPath.concat('/cart')) || fn:startsWith(navPath,pageContext.request.contextPath.concat('/checkout'))}"/>
<c:set var="navOrders" value="${fn:startsWith(navPath,pageContext.request.contextPath.concat('/orders'))}"/>
<c:set var="navAccount" value="${fn:startsWith(navPath,pageContext.request.contextPath.concat('/account'))}"/>
<c:set var="navNotifications" value="${fn:startsWith(navPath,pageContext.request.contextPath.concat('/notifications'))}"/>
<header class="nexora-header">
  <div class="nexora-header-inner">
    <a class="nexora-brand" href="${pageContext.request.contextPath}/" aria-label="NEXORA home">
      <img class="nexora-brand-logo" src="${pageContext.request.contextPath}/images/nexora-logo.png?v=2" alt="NEXORA">
    </a>
    <nav class="nexora-main-nav" aria-label="Main navigation">
      <a class="nexora-nav-item ${navHome?'nexora-nav-item-active':''}" href="${pageContext.request.contextPath}/"><svg class="nexora-nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 10.5 12 3l9 7.5v9a1.5 1.5 0 0 1-1.5 1.5h-5v-6h-5v6h-5A1.5 1.5 0 0 1 3 19.5z"/></svg><span>Home</span></a>
      <a class="nexora-nav-item ${navProducts?'nexora-nav-item-active':''}" href="${pageContext.request.contextPath}/products"><svg class="nexora-nav-icon" viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg><span>Products</span></a>
      <a class="nexora-nav-item ${navCart?'nexora-nav-item-active':''}" href="${pageContext.request.contextPath}/cart"><svg class="nexora-nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 4h2l2.2 10.2a2 2 0 0 0 2 1.6h7.9a2 2 0 0 0 2-1.5L21 7H6"/><circle cx="10" cy="20" r="1.4"/><circle cx="18" cy="20" r="1.4"/></svg><span>Cart</span><c:if test="${sessionScope.cartItemCount > 0}"><span class="nexora-cart-badge">${sessionScope.cartItemCount}</span></c:if></a>
      <c:if test="${not empty sessionScope.user}">
        <a class="nexora-nav-item ${navOrders?'nexora-nav-item-active':''}" href="${pageContext.request.contextPath}/orders"><svg class="nexora-nav-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M6 3h12v18l-3-2-3 2-3-2-3 2z"/><path d="M9 8h6M9 12h6"/></svg><span>My Orders</span></a>
        <a class="nexora-nav-item ${navAccount?'nexora-nav-item-active':''}" href="${pageContext.request.contextPath}/account"><svg class="nexora-nav-icon" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4.5 21a7.5 7.5 0 0 1 15 0"/></svg><span>My Account</span></a>
      </c:if>
    </nav>
    <div class="nexora-header-actions">
      <c:choose><c:when test="${not empty sessionScope.user}">
        <button class="nexora-icon-action nexora-notification-trigger" type="button" aria-label="Notifications${unreadNotificationCount>0?' with unread items':''}" aria-expanded="false" aria-controls="nexora-notification-panel"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/><path d="M10 21h4"/></svg><c:if test="${unreadNotificationCount>0}"><span class="nexora-notification-badge">${unreadNotificationCount>9?'9+':unreadNotificationCount}</span></c:if></button>
        <div class="nexora-profile">
          <button class="nexora-profile-trigger" type="button" aria-label="Open profile menu" aria-expanded="false" aria-controls="nexora-profile-menu"><span class="nexora-avatar"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="4"/><path d="M4.5 21a7.5 7.5 0 0 1 15 0"/></svg></span><svg class="nexora-chevron" viewBox="0 0 24 24" aria-hidden="true"><path d="m7 10 5 5 5-5"/></svg></button>
          <div class="nexora-profile-menu" id="nexora-profile-menu" hidden>
            <div class="nexora-profile-summary"><strong><c:out value="${sessionScope.user.firstName}"/> <c:out value="${sessionScope.user.lastName}"/></strong><span><c:out value="${sessionScope.user.email}"/></span></div>
            <a href="${pageContext.request.contextPath}/account">My Account</a><a href="${pageContext.request.contextPath}/orders">My Orders</a>
            <c:if test="${sessionScope.user.role == 'ADMIN'}"><a href="${pageContext.request.contextPath}/admin">Admin Dashboard</a></c:if>
            <div class="nexora-menu-divider"></div><a href="${pageContext.request.contextPath}/logout">Logout</a>
          </div>
        </div>
      </c:when><c:otherwise>
        <a class="nexora-auth-link" href="${pageContext.request.contextPath}/login">Login</a><a class="nexora-register-link" href="${pageContext.request.contextPath}/register">Register</a>
      </c:otherwise></c:choose>
    </div>
    <div class="nexora-mobile-actions">
      <c:if test="${not empty sessionScope.user}"><button class="nexora-mobile-notification nexora-notification-trigger" type="button" aria-label="Notifications${unreadNotificationCount>0?' with unread items':''}" aria-expanded="false" aria-controls="nexora-notification-panel"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/><path d="M10 21h4"/></svg><c:if test="${unreadNotificationCount>0}"><span class="nexora-notification-badge">${unreadNotificationCount>9?'9+':unreadNotificationCount}</span></c:if></button></c:if>
      <a class="nexora-mobile-cart" href="${pageContext.request.contextPath}/cart" aria-label="Cart"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3 4h2l2.2 10.2a2 2 0 0 0 2 1.6h7.9a2 2 0 0 0 2-1.5L21 7H6"/><circle cx="10" cy="20" r="1.4"/><circle cx="18" cy="20" r="1.4"/></svg><c:if test="${sessionScope.cartItemCount > 0}"><span class="nexora-cart-badge">${sessionScope.cartItemCount}</span></c:if></a>
      <button class="nexora-menu-toggle" type="button" aria-label="Open navigation menu" aria-expanded="false" aria-controls="nexora-mobile-menu"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M4 12h16M4 17h16"/></svg></button>
    </div>
    <c:if test="${not empty sessionScope.user}"><section class="nexora-notification-panel" id="nexora-notification-panel" aria-label="Notifications" hidden><header><h2>Notifications</h2><c:if test="${unreadNotificationCount>0}"><form method="post" action="${pageContext.request.contextPath}/notifications/read-all"><button type="submit">Mark all read</button></form></c:if></header><div class="nexora-notification-list"><c:choose><c:when test="${empty headerNotifications}"><p class="nexora-notification-empty">No notifications yet.</p></c:when><c:otherwise><c:forEach var="notification" items="${headerNotifications}"><a class="nexora-notification-item ${notification.read?'':'notification-unread'}" href="${pageContext.request.contextPath}/notifications/open?id=${notification.notificationId}"><span class="notification-indicator" aria-hidden="true"></span><span class="notification-copy"><strong><c:out value="${notification.title}"/></strong><span><c:out value="${notification.message}"/></span><small><c:out value="${notification.relativeTime}"/></small></span></a></c:forEach></c:otherwise></c:choose></div><footer><a href="${pageContext.request.contextPath}/notifications">View all notifications</a></footer></section></c:if>
  </div>
  <nav class="nexora-mobile-menu" id="nexora-mobile-menu" aria-label="Mobile navigation" hidden>
    <a href="${pageContext.request.contextPath}/">Home</a><a href="${pageContext.request.contextPath}/products">Products</a><a href="${pageContext.request.contextPath}/cart">Cart</a>
    <c:choose><c:when test="${not empty sessionScope.user}"><a href="${pageContext.request.contextPath}/orders">My Orders</a><a href="${pageContext.request.contextPath}/account">My Account</a><c:if test="${sessionScope.user.role == 'ADMIN'}"><a href="${pageContext.request.contextPath}/admin">Admin Dashboard</a></c:if><a href="${pageContext.request.contextPath}/logout">Logout</a></c:when><c:otherwise><a href="${pageContext.request.contextPath}/login">Login</a><a href="${pageContext.request.contextPath}/register">Register</a></c:otherwise></c:choose>
  </nav>
</header>
<script>(function(){const profile=document.querySelector('.nexora-profile-trigger'),menu=document.getElementById('nexora-profile-menu'),toggle=document.querySelector('.nexora-menu-toggle'),mobile=document.getElementById('nexora-mobile-menu'),notificationPanel=document.getElementById('nexora-notification-panel'),notificationTriggers=[...document.querySelectorAll('.nexora-notification-trigger')];function setOpen(button,panel,open){if(!button||!panel)return;button.setAttribute('aria-expanded',String(open));panel.hidden=!open;}function setNotifications(open){if(!notificationPanel)return;notificationPanel.hidden=!open;notificationTriggers.forEach(b=>b.setAttribute('aria-expanded',String(open)));}profile?.addEventListener('click',e=>{e.stopPropagation();setNotifications(false);setOpen(profile,menu,menu.hidden)});toggle?.addEventListener('click',()=>{setNotifications(false);setOpen(toggle,mobile,mobile.hidden)});notificationTriggers.forEach(button=>button.addEventListener('click',e=>{e.stopPropagation();setOpen(profile,menu,false);setNotifications(notificationPanel.hidden)}));notificationPanel?.addEventListener('click',e=>e.stopPropagation());document.addEventListener('click',e=>{if(menu&&!e.target.closest('.nexora-profile'))setOpen(profile,menu,false);if(notificationPanel&&!e.target.closest('.nexora-notification-panel'))setNotifications(false)});document.addEventListener('keydown',e=>{if(e.key==='Escape'){setOpen(profile,menu,false);setOpen(toggle,mobile,false);setNotifications(false)}});})();</script>
