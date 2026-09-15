<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Edit Product | NEXORA</title><link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23"></head>
<body><jsp:include page="/WEB-INF/views/includes/navigation.jsp"/>
<main class="form-page"><section class="form-card form-card-wide product-edit-card">
<a class="back-link" href="${pageContext.request.contextPath}/admin/products">&larr; Back to Product Management</a>
<div class="product-edit-heading"><div><h1>Edit Product</h1><p>Product ID <strong>#${product.productId}</strong> is read-only.</p></div></div>
<c:if test="${param.message=='updated'}"><div class="message success">Product updated successfully.</div></c:if>
<c:if test="${not empty error}"><div class="message error"><c:out value="${error}"/></div></c:if>
<c:if test="${legacyCatalogUrl}"><div class="message info">Legacy Oracle catalog URL is no longer available. You can leave it unchanged, leave it blank, or replace it with a current URL.</div></c:if>
<form class="product-edit-form" method="post" action="${pageContext.request.contextPath}/admin/products/edit">
<input type="hidden" name="productId" value="${product.productId}">
<div class="form-section"><h2>Basic Information</h2>
<label for="productName">Product Name</label><input id="productName" name="productName" maxlength="25" required value="<c:out value='${product.productName}'/>">
<div class="form-grid"><div><label for="productStatus">Status</label><select id="productStatus" name="productStatus" required><c:forEach var="status" items="${['orderable','planned','under development','obsolete']}"><option value="${status}" ${product.productStatus==status?'selected':''}>${status}</option></c:forEach></select></div>
<div><label for="categoryId">Category</label><select id="categoryId" name="categoryId"><option value="">No category</option><c:forEach var="category" items="${categories}"><option value="${category.categoryId}" ${product.categoryId==category.categoryId?'selected':''}><c:out value="${category.categoryName}"/></option></c:forEach></select></div></div></div>
<div class="form-section"><h2>Product Specifications</h2><div class="form-grid">
<div><label for="weightClass">Weight Class</label><input id="weightClass" name="weightClass" type="number" min="0" max="9" step="1" value="${product.weightClass}"><small class="field-note">Optional whole number from 0 to 9.</small></div>
<div><label for="supplierId">Supplier ID</label><input id="supplierId" name="supplierId" type="number" min="0" max="999999" step="1" value="${product.supplierId}"><small class="field-note">Optional.</small></div>
</div><p class="read-only-field"><strong>Warranty:</strong> <c:choose><c:when test="${not empty product.warrantyPeriod}"><c:out value="${product.warrantyPeriod}"/></c:when><c:otherwise>Not available</c:otherwise></c:choose> <span>Read-only</span></p></div>
<div class="form-section"><h2>Pricing</h2><div class="form-grid">
<div><label for="listPrice">List Price</label><input id="listPrice" name="listPrice" type="number" min="0" max="999999.99" step="0.01" required value="${product.listPrice}"></div>
<div><label for="minimumPrice">Minimum Price</label><input id="minimumPrice" name="minimumPrice" type="number" min="0" max="999999.99" step="0.01" value="${product.minimumPrice}"><small class="field-note">Optional; cannot exceed list price.</small></div>
</div></div>
<div class="form-section"><h2>Catalog</h2><label for="catalogUrl">Catalog URL</label><input id="catalogUrl" name="catalogUrl" maxlength="50" value="<c:out value='${product.catalogUrl}'/>"><small class="field-note">Use a current http/https URL. Leave blank to remove the catalog URL.</small></div>
<button class="form-button product-save-button" type="submit">Save Changes</button>
</form><section class="image-management"><h2>Product Images</h2><p><strong>${imageCount} / 4 images</strong></p><c:if test="${param.image=='saved'}"><div class="message success">Product image added.</div></c:if><c:if test="${param.image=='uploaded'}"><div class="message success">Product image uploaded successfully.</div></c:if><c:if test="${param.image=='removed'}"><div class="message success">Product image removed.</div></c:if><c:if test="${param.image=='primary'}"><div class="message success">Primary product image updated.</div></c:if><c:if test="${param.image=='limit'}"><div class="message error">Maximum 4 images per product.</div></c:if><c:if test="${param.image=='invalid'}"><div class="message error">Use an HTTPS URL, /images/ path, or /product-images/ path. Paths cannot contain ..</div></c:if><c:if test="${param.image=='empty'}"><div class="message error">Please select an image.</div></c:if><c:if test="${param.image=='type'}"><div class="message error">Only JPG and PNG images are supported.</div></c:if><c:if test="${param.image=='tooLarge'}"><div class="message error">Image must be 5 MB or smaller.</div></c:if><c:if test="${param.image=='invalidFile'}"><div class="message error">The uploaded file is not a valid image.</div></c:if><c:if test="${param.image=='uploadFailed'}"><div class="message error">The image could not be uploaded. Please try again.</div></c:if>
<div class="admin-image-gallery"><c:forEach var="image" items="${productImages}" varStatus="status"><article class="admin-gallery-item"><h3>Image ${status.index + 1} <c:if test="${image.primary}"><span class="status">Primary</span></c:if></h3><div class="admin-gallery-thumbnail"><img src="<c:if test='${image.localImage}'>${pageContext.request.contextPath}</c:if><c:out value='${image.imageUrl}'/>" alt="Image ${status.index + 1} for ${product.productName}" onerror="this.hidden=true;this.nextElementSibling.hidden=false"><div class="product-placeholder" hidden>Unavailable</div></div><div class="admin-gallery-actions"><c:if test="${not image.primary}"><form method="post" action="${pageContext.request.contextPath}/admin/products/image/primary"><input type="hidden" name="productId" value="${product.productId}"><input type="hidden" name="imageId" value="${image.imageId}"><button class="small-button" type="submit">Make Primary</button></form></c:if><form method="post" action="${pageContext.request.contextPath}/admin/products/image"><input type="hidden" name="productId" value="${product.productId}"><input type="hidden" name="imageId" value="${image.imageId}"><button class="danger-button" name="action" value="remove" type="submit">Remove</button></form></div></article></c:forEach></div>
<c:choose><c:when test="${imageCount >= 4}"><div class="message error">Maximum 4 images per product.</div></c:when><c:otherwise><h3>Upload New Image</h3><form method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/admin/products/image/upload?productId=${product.productId}"><label for="imageFile">Choose JPG or PNG (maximum 5 MB)</label><input id="imageFile" name="imageFile" type="file" accept="image/jpeg,image/png,.jpg,.jpeg,.png"><button class="form-button" type="submit">Upload New Image</button></form><h3>Or add Image URL / Path</h3><form method="post" action="${pageContext.request.contextPath}/admin/products/image"><input type="hidden" name="productId" value="${product.productId}"><label for="imageUrl">Image URL / Path</label><input id="imageUrl" name="imageUrl" maxlength="500" placeholder="/images/products/1726.svg"><button class="form-button" name="action" value="save">Add Image</button></form></c:otherwise></c:choose></section>
</section></main></body></html>








