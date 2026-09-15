<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Add Product | NEXORA</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23"></head>
<body><jsp:include page="/WEB-INF/views/includes/navigation.jsp" />
<main class="form-page"><section class="form-card form-card-wide"><a class="back-link" href="${pageContext.request.contextPath}/admin/products">&larr; Back to Product Management</a>
<h1>Add New Product</h1><p>The product ID is generated safely by Oracle. Warranty is optional and will be left empty.</p>
<c:if test="${not empty error}"><div class="message error"><c:out value="${error}" /></div></c:if>
<form method="post" action="${pageContext.request.contextPath}/admin/products/new">
<label for="productName">Product name</label><input id="productName" name="productName" maxlength="25" required value="<c:out value='${product.productName}' />">
<div class="form-grid"><div><label for="categoryId">Category</label><select id="categoryId" name="categoryId"><option value="">No category</option><c:forEach var="category" items="${categories}"><option value="${category.categoryId}" ${product.categoryId==category.categoryId?'selected':''}><c:out value="${category.categoryName}"/></option></c:forEach></select></div>
<div><label for="weightClass">Weight class</label><input id="weightClass" name="weightClass" type="number" min="0" max="9" value="${product.weightClass}"></div>
<div><label for="supplierId">Supplier ID</label><input id="supplierId" name="supplierId" type="number" min="0" max="999999" value="${product.supplierId}"></div>
<div><label for="productStatus">Status</label><select id="productStatus" name="productStatus" required><option value="orderable" ${empty product.productStatus || product.productStatus == 'orderable' ? 'selected' : ''}>orderable</option><option value="planned" ${product.productStatus == 'planned' ? 'selected' : ''}>planned</option><option value="under development" ${product.productStatus == 'under development' ? 'selected' : ''}>under development</option><option value="obsolete" ${product.productStatus == 'obsolete' ? 'selected' : ''}>obsolete</option></select></div>
<div><label for="listPrice">List price</label><input id="listPrice" name="listPrice" type="number" min="0" max="999999.99" step="0.01" value="${product.listPrice}"></div>
<div><label for="minimumPrice">Minimum price</label><input id="minimumPrice" name="minimumPrice" type="number" min="0" max="999999.99" step="0.01" value="${product.minimumPrice}"></div></div>
<label for="catalogUrl">Catalog URL</label><input id="catalogUrl" name="catalogUrl" maxlength="50" value="<c:out value='${product.catalogUrl}' />"><small class="field-note">Use a current working http/https product URL.</small>
<button class="form-button" type="submit">Create Product</button></form></section></main></body></html>








