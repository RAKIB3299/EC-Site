<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Categories | NEXORA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=25">
</head>
<body class="category-admin-page">
<jsp:include page="/WEB-INF/views/includes/navigation.jsp"/>
<main class="page-container">
    <nav class="admin-breadcrumb" aria-label="Breadcrumb">
        <a href="${pageContext.request.contextPath}/admin">Admin Dashboard</a>
        <span aria-hidden="true">/</span><span aria-current="page">Categories</span>
    </nav>
    <div class="page-heading category-admin-heading"><div>
        <h1>Category Management</h1>
        <p>Manage friendly customer-facing names for live product categories.</p>
    </div></div>

    <c:if test="${param.message=='updated'}"><div class="message success">Category updated successfully.</div></c:if>
    <c:if test="${param.message=='invalid'}"><div class="message error">Enter a category name up to 100 characters and a valid display order.</div></c:if>
    <c:if test="${param.message=='failed'}"><div class="message error">The category could not be updated.</div></c:if>

    <div class="category-admin-layout">
        <section class="category-table-card" aria-labelledby="category-table-title">
            <div class="category-toolbar">
                <div><h2 id="category-table-title">Categories</h2><p>Edit and save each category independently.</p></div>
                <div class="category-search-wrap">
                    <label for="category-search">Search categories</label>
                    <input id="category-search" class="category-search" type="search"
                           placeholder="Search by name or ID" autocomplete="off">
                </div>
            </div>
            <div class="category-table-scroll" tabindex="0">
                <table class="category-admin-table">
                    <thead><tr><th>ID</th><th>Category Name</th><th>Products</th><th>Display Order</th><th>Status</th><th>Action</th></tr></thead>
                    <tbody>
                    <c:forEach var="category" items="${categories}">
                        <tr class="category-row ${category.active ? '' : 'category-row-inactive'}"
                            data-category-id="${category.categoryId}"
                            data-category-name="<c:out value='${category.categoryName}'/>">
                            <td>
                                <form id="category-${category.categoryId}" method="post"
                                      action="${pageContext.request.contextPath}/admin/categories">
                                    <input type="hidden" name="categoryId" value="${category.categoryId}">
                                </form>
                                <strong>${category.categoryId}</strong>
                            </td>
                            <td><input form="category-${category.categoryId}" class="category-name-input"
                                       name="categoryName" maxlength="100" required
                                       value="<c:out value='${category.categoryName}'/>"></td>
                            <td><strong>${category.productCount}</strong> products</td>
                            <td><input form="category-${category.categoryId}" class="category-order-input"
                                       name="displayOrder" type="number" min="0" max="999" required
                                       value="${category.displayOrder}"></td>
                            <td><label class="category-status ${category.active ? 'is-active' : 'is-inactive'}">
                                <input form="category-${category.categoryId}" name="active" type="checkbox"
                                       value="Y" ${category.active?'checked':''}>
                                <span>${category.active ? 'Active' : 'Inactive'}</span>
                            </label></td>
                            <td><button form="category-${category.categoryId}" class="category-save-button" type="submit">Save</button></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
            <p id="category-no-results" class="category-no-results" hidden>No matching categories.</p>
        </section>

        <aside class="category-overview" aria-labelledby="category-overview-title">
            <h2 id="category-overview-title">Category Overview</h2>
            <c:choose>
                <c:when test="${statisticsUnavailable}"><p class="category-statistics-unavailable">Statistics unavailable</p></c:when>
                <c:otherwise><dl class="category-stat-list">
                    <div class="category-stat"><dt>Total categories</dt><dd>${categoryOverview.totalCategories}</dd></div>
                    <div class="category-stat"><dt>Active categories</dt><dd>${categoryOverview.activeCategories}</dd></div>
                    <div class="category-stat"><dt>Inactive categories</dt><dd>${categoryOverview.inactiveCategories}</dd></div>
                    <div class="category-stat"><dt>Total products</dt><dd>${categoryOverview.totalProducts}</dd></div>
                    <div class="category-stat"><dt>Products without a category</dt><dd>${categoryOverview.uncategorizedProducts}</dd></div>
                </dl></c:otherwise>
            </c:choose>
        </aside>
    </div>
</main>
<script>
(function () {
    const search = document.getElementById('category-search');
    const rows = [...document.querySelectorAll('.category-row')];
    const noResults = document.getElementById('category-no-results');
    if (!search) return;
    search.addEventListener('input', function () {
        const term = search.value.trim().toLocaleLowerCase();
        let matches = 0;
        rows.forEach(function (row) {
            const name = (row.dataset.categoryName || '').toLocaleLowerCase();
            const id = row.dataset.categoryId || '';
            const visible = !term || name.includes(term) || id.includes(term);
            row.hidden = !visible;
            if (visible) matches++;
        });
        noResults.hidden = matches !== 0;
    });
    document.querySelectorAll('.category-status input').forEach(function (checkbox) {
        checkbox.addEventListener('change', function () {
            const label = checkbox.closest('.category-status');
            label.classList.toggle('is-active', checkbox.checked);
            label.classList.toggle('is-inactive', !checkbox.checked);
            label.querySelector('span').textContent = checkbox.checked ? 'Active' : 'Inactive';
        });
    });
})();
</script>
</body>
</html>
