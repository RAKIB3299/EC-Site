<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Categories | NEXORA</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23"></head>
<body><jsp:include page="/WEB-INF/views/includes/navigation.jsp"/>
<main class="page-container">
<div class="page-heading"><div><h1>Category Management</h1><p>Friendly customer-facing names for live product categories</p></div><a href="${pageContext.request.contextPath}/admin">Admin Dashboard</a></div>
<c:if test="${param.message=='updated'}"><div class="message success">Category updated successfully.</div></c:if>
<c:if test="${param.message=='invalid'}"><div class="message error">Enter a category name up to 100 characters and a valid display order.</div></c:if>
<c:if test="${param.message=='failed'}"><div class="message error">The category could not be updated.</div></c:if>
<div class="table-wrap"><table><thead><tr><th>ID</th><th>Category Name</th><th>Products</th><th>Order</th><th>Active</th><th>Update</th></tr></thead><tbody>
<c:forEach var="category" items="${categories}"><tr>
<td><form id="category-${category.categoryId}" method="post" action="${pageContext.request.contextPath}/admin/categories"><input type="hidden" name="categoryId" value="${category.categoryId}"></form><strong>${category.categoryId}</strong></td>
<td><input form="category-${category.categoryId}" name="categoryName" maxlength="100" required value="<c:out value='${category.categoryName}'/>"></td>
<td>${category.productCount} products</td>
<td><input form="category-${category.categoryId}" class="small-number-input" name="displayOrder" type="number" min="0" max="999" required value="${category.displayOrder}"></td>
<td><label><input form="category-${category.categoryId}" name="active" type="checkbox" value="Y" ${category.active?'checked':''}> Active</label></td>
<td><button form="category-${category.categoryId}" class="small-button" type="submit">Save</button></td>
</tr></c:forEach>
</tbody></table></div></main></body></html>




