<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="com.namastemart.service.impl.*,
                 com.namastemart.service.*,
                 com.namastemart.beans.*,
                 java.util.*,
                 javax.servlet.ServletOutputStream,
                 java.io.*"%>
<!DOCTYPE html>
<html>
<head>
    <title>View Products</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/css/bootstrap.min.css">
    <link rel="stylesheet" href="css/changes.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.0/js/bootstrap.min.js"></script>
    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
        }
		
        .content {
            margin-left: 250px; /* Adjust this margin based on sidebar width */
            padding: 20px;
            transition: margin-left 0.3s; /* Smooth transition for margin change */
        }
		
        .sidebar.collapsed ~ .content {
            margin-left: 100px; /* Adjust margin when sidebar is collapsed */
        }
		
        .product-container {
            background-color: #f1cdf6;
            min-height: 100vh;
            padding-bottom: 20px;
        }
		
        .product-name {
            font-weight: bold;
        }
		
        .thumbnail {
            height: 350px;
            padding: 15px;
            margin-bottom: 30px;
        }
		
        .footer {
            position: relative;
            left: 250px; /* Adjust this based on sidebar width */
            bottom: 0;
            width: calc(100% - 250px); /* Adjust width based on sidebar width */
            background-color: #333;
            color: white;
            text-align: center;
            padding: 10px;
            margin-top: 20px;
            transition: left 0.3s, width 0.3s; /* Smooth transition for footer adjustments */
        }
		
        .sidebar.collapsed ~ .footer {
            left: 60px;  /* Adjust position when sidebar is collapsed */
            width: calc(100% - 60px); /* Adjust width when sidebar is collapsed */
        }
    </style>
</head>
<body>

    <%@ include file="adminheader.jsp" %>
        
    <div class="content">
        <div class="text-center" style="color: black; font-size: 14px; font-weight: bold;">
		    <%
                String userName = (String) session.getAttribute("username");
                String password = (String) session.getAttribute("password");
                if (userName == null || password == null) {
                    response.sendRedirect("login.jsp?message=Session Expired, Login Again!!");
                }
            
                ProductServiceImpl prodDao = new ProductServiceImpl();
                List<ProductBean> products = new ArrayList<ProductBean>();
                String search = request.getParameter("search");
                String type = request.getParameter("type");
                String message = "All Products";
                
                if (search != null) {
                    products = prodDao.searchAllProducts(search);
                    message = "Showing Results for '" + search + "'";
                } else if (type != null) {
                    products = prodDao.getAllProductsByType(type);
                    message = "Showing Results for '" + type + "'";
                } else {
                    products = prodDao.getAllProducts();
                }
                
                if (products.isEmpty()) {
                    message = "No items found for the search '" + (search != null ? search : type) + "'";
                    products = prodDao.getAllProducts();
                }
            %>
            <%= message %>
        </div>

        <!-- Start of Product Items List -->
        <div class="container product-container">
            <div class="row text-center">
                <% for (ProductBean product : products) { %>
                    <div class="col-sm-4">
                        <div class="thumbnail">
                            <img src="./ShowImage?pid=<%= product.getProdId() %>" alt="Product" style="height: 150px; max-width: 180px;">
                            <p class="productname"><%= product.getProdName() %> (<%= product.getProdId() %>)</p>
                            <p class="productinfo"><%= product.getProdInfo() %></p>
                            <p class="price">Rs <%= product.getProdPrice() %></p>
                            <form method="post">
                                <button type="submit" formaction="./RemoveProductSrv?prodid=<%= product.getProdId() %>" class="btn btn-danger">
                                    Remove Product
                                </button>
                                <button type="submit" formaction="updateProduct.jsp?prodid=<%= product.getProdId() %>" class="btn btn-primary">
                                    Update Product
                                </button>
                            </form>
                        </div>
                    </div>
                <% } %>
            </div>
        </div>
    
	    <!-- End of Product Items List -->
    </div>
</body>
</html>