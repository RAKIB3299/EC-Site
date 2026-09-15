<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Admin | NEXORA</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23"></head><body><jsp:include page="/WEB-INF/views/includes/navigation.jsp"/><main class="page-container"><h1>Admin Dashboard</h1><div class="admin-grid"><a href="${pageContext.request.contextPath}/admin/products"><strong>Product Management</strong><span>View and edit live products</span></a><a href="${pageContext.request.contextPath}/admin/orders"><strong>Order Management</strong><span>View orders and update status</span></a><a href="${pageContext.request.contextPath}/admin/banners"><strong>Banner Management</strong><span>Manage Products-page cover banners</span></a><a href="${pageContext.request.contextPath}/admin/categories"><strong>Categories</strong><span>Manage friendly product category names</span></a><a href="${pageContext.request.contextPath}/products"><strong>Back to Shop</strong><span>Return to customer pages</span></a><a href="${pageContext.request.contextPath}/logout"><strong>Logout</strong><span>End the current session</span></a></div></main></body></html>









