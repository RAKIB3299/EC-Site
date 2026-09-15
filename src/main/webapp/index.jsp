<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NEXORA</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css?v=23">
</head>
<body class="splash-page">
    <main class="nexora-splash">
        <img src="${pageContext.request.contextPath}/images/nexora-logo.png?v=2"
             alt="NEXORA"
             class="nexora-splash-logo">
        <p class="nexora-splash-tagline">SMART SHOPPING, BETTER LIFE</p>
        <noscript>
            <p class="splash-noscript"><a href="${pageContext.request.contextPath}/products">Continue to Products</a></p>
        </noscript>
    </main>
    <script>
        setTimeout(function () {
            window.location.replace('${pageContext.request.contextPath}/products');
        }, 2500);
    </script>
</body>
</html>




