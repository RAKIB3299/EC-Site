<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Products | NEXORA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23">
</head>
<body class="products-page">
<jsp:include page="/WEB-INF/views/includes/navigation.jsp" />
<main class="page-container">
    <section class="products-hero ${empty banners ? 'products-hero-empty' : ''}">
    <c:if test="${not empty banners}">
        <section class="banner-carousel" aria-label="Featured promotions" data-banner-carousel>
            <div class="banner-slides">
                <c:forEach var="banner" items="${banners}" varStatus="status">
                    <article class="banner-slide ${status.first ? 'active' : ''}" data-banner-index="${status.index}" aria-hidden="${not status.first}">
                        <c:choose>
                            <c:when test="${not empty banner.linkUrl}">
                                <a class="banner-link" href="<c:if test='${banner.internalLink}'>${pageContext.request.contextPath}</c:if><c:out value='${banner.linkUrl}'/>" <c:if test="${not banner.internalLink}">target="_blank" rel="noopener noreferrer"</c:if>>
                                    <img class="banner-slide-image" src="${pageContext.request.contextPath}<c:out value='${banner.imageUrl}'/>" alt="<c:out value='${banner.title}'/>" onerror="this.hidden=true;this.nextElementSibling.hidden=false"><div class="banner-image-placeholder" hidden>Banner unavailable</div>
                                    <c:if test="${not empty banner.title}"><span class="banner-title-overlay"><c:out value="${banner.title}"/></span></c:if>
                                </a>
                            </c:when>
                            <c:otherwise>
                                <div class="banner-link"><img class="banner-slide-image" src="${pageContext.request.contextPath}<c:out value='${banner.imageUrl}'/>" alt="<c:out value='${banner.title}'/>" onerror="this.hidden=true;this.nextElementSibling.hidden=false"><div class="banner-image-placeholder" hidden>Banner unavailable</div><c:if test="${not empty banner.title}"><span class="banner-title-overlay"><c:out value="${banner.title}"/></span></c:if></div>
                            </c:otherwise>
                        </c:choose>
                    </article>
                </c:forEach>
            </div>
            <c:if test="${not empty banners and banners.size() > 1}"><button class="banner-arrow banner-previous" type="button" data-banner-change="-1" aria-label="Previous banner">&#10094;</button><button class="banner-arrow banner-next" type="button" data-banner-change="1" aria-label="Next banner">&#10095;</button><div class="banner-dots" aria-label="Choose banner"><c:forEach var="banner" items="${banners}" varStatus="status"><button class="banner-dot ${status.first?'active':''}" type="button" data-banner-dot="${status.index}" aria-label="Show banner ${status.index+1}" aria-pressed="${status.first}"></button></c:forEach></div></c:if>
        </section>
    </c:if>
    <section class="product-filters product-filters-overlay" aria-label="Search and filter products">
        <form method="get" action="${pageContext.request.contextPath}/products">
            <div class="search-field">
                <label class="visually-hidden" for="q">Search products</label>
                <input id="q" name="q" type="search" maxlength="100" placeholder="Search products..." value="<c:out value='${q}' />">
            </div>
            <div class="status-field"><label class="visually-hidden" for="status">Status</label><select id="status" name="status">
                <option value="">All statuses</option>
                <option value="orderable" ${status == 'orderable' ? 'selected' : ''}>orderable</option>
                <option value="planned" ${status == 'planned' ? 'selected' : ''}>planned</option>
                <option value="under development" ${status == 'under development' ? 'selected' : ''}>under development</option>
                <option value="obsolete" ${status == 'obsolete' ? 'selected' : ''}>obsolete</option>
            </select></div>
            <div class="category-field"><label class="visually-hidden" for="category">Category</label><select id="category" name="category">
                <option value="">All categories</option>
                <c:forEach var="item" items="${categories}"><option value="${item.categoryId}" ${category == item.categoryId.toString() ? 'selected' : ''}><c:out value="${item.categoryName}"/></option></c:forEach>
            </select></div>
            <div class="price-field"><label class="visually-hidden" for="minPrice">Minimum price</label><input id="minPrice" name="minPrice" type="number" min="0" step="0.01" placeholder="Min price" value="<c:out value='${minPrice}' />"></div>
            <div class="price-field"><label class="visually-hidden" for="maxPrice">Maximum price</label><input id="maxPrice" name="maxPrice" type="number" min="0" step="0.01" placeholder="Max price" value="<c:out value='${maxPrice}' />"></div>
            <div class="sort-field"><label class="visually-hidden" for="sort">Sort</label><select id="sort" name="sort">
                <option value="id_asc" ${sort == 'id_asc' ? 'selected' : ''}>ID: Low</option>
                <option value="id_desc" ${sort == 'id_desc' ? 'selected' : ''}>ID: High</option>
                <option value="price_asc" ${sort == 'price_asc' ? 'selected' : ''}>Price: Low</option>
                <option value="price_desc" ${sort == 'price_desc' ? 'selected' : ''}>Price: High</option>
                <option value="name_asc" ${sort == 'name_asc' ? 'selected' : ''}>Name: A–Z</option>
                <option value="name_desc" ${sort == 'name_desc' ? 'selected' : ''}>Name: Z–A</option>
            </select></div>
            <div class="filter-actions"><button class="form-button" type="submit">Search</button><a href="${pageContext.request.contextPath}/products">Clear</a></div>
        </form>
    </section>
    </section>
    <div class="page-heading products-heading">
        <div><h1>Products</h1><p>${totalProducts} products found</p></div>
        <span>Page ${currentPage} of ${totalPages}</span>
    </div>
    <c:if test="${not empty error}"><div class="message error"><c:out value="${error}" /></div></c:if>

    <c:choose>
        <c:when test="${empty products}"><div class="empty-products">No products found.</div></c:when>
        <c:otherwise>
            <div class="product-grid">
                <c:forEach var="product" items="${products}">
                    <article class="product-card">
                        <c:choose><c:when test="${not empty product.imageUrl}"><div class="product-image-wrap"><img class="product-card-image" src="<c:if test='${product.localImage}'>${pageContext.request.contextPath}</c:if><c:out value='${product.imageUrl}' />" alt="<c:out value='${product.productName}' />" onerror="this.hidden=true;this.nextElementSibling.hidden=false"><div class="product-placeholder" hidden>Product</div></div></c:when><c:otherwise><div class="product-placeholder" aria-hidden="true">Product</div></c:otherwise></c:choose>
                        <div class="product-body">
                            <c:if test="${not empty product.productStatus}">
                                <span class="status"><c:out value="${product.productStatus}" /></span>
                            </c:if>
                            <h2><c:out value="${product.productName}" /></h2>
                            <p class="meta">Product ID: ${product.productId}<c:if test="${not empty product.categoryId}"> &middot; <c:out value="${product.displayCategoryName}"/></c:if></p>
                            <p class="price">
                                <c:choose>
                                    <c:when test="${not empty product.listPrice}">&yen;<fmt:formatNumber value="${product.listPrice}" pattern="#,##0.00" /></c:when>
                                    <c:otherwise>Price unavailable</c:otherwise>
                                </c:choose>
                            </p>
                            <c:if test="${not empty product.minimumPrice}"><p class="meta">Minimum price: &yen;<fmt:formatNumber value="${product.minimumPrice}" pattern="#,##0.00" /></p></c:if>
                            <c:if test="${not empty product.warrantyPeriod}"><p class="meta">Warranty: <c:out value="${product.warrantyPeriod}" /></p></c:if>
                            <div class="card-actions">
                                <a class="detail-link" href="${pageContext.request.contextPath}/product?id=${product.productId}">View Details</a>
                                <c:if test="${product.catalogUrlDisplayable}"><a class="catalog-link" href="<c:out value='${product.catalogUrl}' />" target="_blank" rel="noopener noreferrer">View Catalog</a></c:if>
                            </div>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </c:otherwise>
    </c:choose>

    <c:if test="${totalProducts > 0}"><nav class="pagination" aria-label="Product pages">
        <c:if test="${currentPage > 1}"><a href="?page=${currentPage - 1}${filterQuery}">Previous</a></c:if>
        <c:forEach begin="1" end="${totalPages}" var="pageNumber">
            <c:choose>
                <c:when test="${pageNumber == currentPage}"><span class="current" aria-current="page">${pageNumber}</span></c:when>
                <c:otherwise><a href="?page=${pageNumber}${filterQuery}">${pageNumber}</a></c:otherwise>
            </c:choose>
        </c:forEach>
        <c:if test="${currentPage < totalPages}"><a href="?page=${currentPage + 1}${filterQuery}">Next</a></c:if>
    </nav></c:if>
</main>
<c:if test="${not empty banners and banners.size() > 1}"><script>(function(){const root=document.querySelector('[data-banner-carousel]'),slides=[...root.querySelectorAll('.banner-slide')],dots=[...root.querySelectorAll('.banner-dot')];let current=0,timer;function show(index){current=(index+slides.length)%slides.length;slides.forEach((slide,i)=>{slide.classList.toggle('active',i===current);slide.setAttribute('aria-hidden',i!==current)});dots.forEach((dot,i)=>{dot.classList.toggle('active',i===current);dot.setAttribute('aria-pressed',i===current)});}function start(){clearInterval(timer);timer=setInterval(()=>show(current+1),5000);}root.querySelectorAll('[data-banner-change]').forEach(button=>button.addEventListener('click',()=>{show(current+Number(button.dataset.bannerChange));start()}));dots.forEach((dot,i)=>dot.addEventListener('click',()=>{show(i);start()}));root.addEventListener('mouseenter',()=>clearInterval(timer));root.addEventListener('mouseleave',start);start();})();</script></c:if>
</body>
</html>







