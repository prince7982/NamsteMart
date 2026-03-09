<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="ISO-8859-1" %>
<%@ page import="com.namastemart.service.impl.UserServiceImpl" %>
<%@ page import="com.namastemart.beans.UserBean" %>
<%@ page import="java.util.List" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>
<%@ page import="javax.servlet.http.HttpSession" %>
<!DOCTYPE html>
<html>
<head>
    <title>Admin User Management</title>
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
        .table-container {
            background-color: #f1cdf6;
            padding: 20px;
        }
        .footer {
            position: fixed;
            left: 250px;
            bottom: 0;
            width: calc(100% - 250px);
            background-color: #333;
            color: white;
            text-align: center;
            padding: 10px;
            transition: left 0.3s, width 0.3s;
        }
        .sidebar.collapsed ~ .footer {
            left: 60px;
            width: calc(100% - 60px);
        }
        .btn {
            margin: 5px;
        }
        .user-image {
            width: 50px;
            height: 50px;
            border-radius: 50%;
        }
        .table-bordered>tbody>tr>td,
        .table-bordered>tbody>tr>th,
        .table-bordered>tfoot>tr>td,
        .table-bordered>tfoot>tr>th,
        .table-bordered>thead>tr>td,
        .table-bordered>thead>tr>th {
            border: 1px solid black !important;
        }
    </style>
</head>
<body>

<%
    String userType = (String) session.getAttribute("usertype");
    String userName = (String) session.getAttribute("username");
    String password = (String) session.getAttribute("password");

    if (userType == null || !userType.equals("admin")) {
        response.sendRedirect("login.jsp?message=Access Denied, Login as admin!!");
        return;
    } else if (userName == null || password == null) {
        response.sendRedirect("login.jsp?message=Session Expired, Login Again!!");
        return;
    }

    UserServiceImpl userService = new UserServiceImpl();
    List<UserBean> userList = userService.getAllUsers();

    // Handle user actions
    String action = request.getParameter("action");
    String userId = request.getParameter("userId");

    if ("activate".equals(action)) {
        userService.activateUser(userId);
    } else if ("deactivate".equals(action)) {
        userService.deactivateUser(userId);
    } else if ("delete".equals(action)) {
    userService.deleteUser(userId);
    }
 
    // Refresh user list after action
    userList = userService.getAllUsers();

%>

<%@ include file="adminheader.jsp" %>
<div class="content">
    <div class="table-container">
        <h2 class="text-center" style="color: black; font-size: 24px; font-weight: bold;">Manage Users</h2>
        <!-- User Table -->
        <div class="table-responsive">
            <table class="table table-bordered table-hover">
                <thead style="background-color: #b341ab; color: white; font-size: 18px;">
                    <tr>
                        <th>Image</th>
                        <th>Name</th>
                        <th>Mobile</th>
                        <th>Email</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody style="background-color: #f1cdf6; font-size: 16px;">
                    <%
                        for (UserBean user : userList) 
						{
                            String status = user.isActive() ? "Active" : "Inactive";
                            String actionStatus = user.isActive() ? "Deactivate" : "Activate";
                            String imageSrc = user.getUserImage() != null ? 
                            "ImageServlet?email=" + user.getEmail() : "images/userprofile.png";
                    %>
                        <tr>
                            <td><img src="<%= imageSrc %>" class="user-image" alt="User Image"></td>
                            <td><%= user.getName() %></td>
                            <td><%= user.getMobile() %></td>
                            <td><%= user.getEmail() %></td>
                            <td><%= status %></td>
                            <td>
                                <form action="" method="get" style="display:inline;">
                                    <input type="hidden" name="userId" value="<%= user.getEmail() %>">
                                    <button type="submit" name="action" value="<%= actionStatus.toLowerCase() %>"
                                        class="btn <%= user.isActive() ? "btn-warning" : "btn-success" %>">
                                        <%= actionStatus %>
                                    </button>
                                </form>
                                <form action="" method="get" style="display:inline;">
                                    <input type="hidden" name="userId" value="<%= user.getEmail() %>">
                                    <button type="submit" name="action" value="delete" class="btn btn-danger"
                                        onclick="return confirm('Are you sure you want to delete this user?');">
                                        Remove
                                    </button>
                                </form>
                            </td>
                        </tr>
					<% } %>
                    <%
                    if (userList.size() == 0) {
                    %>
                        <tr style="background-color: rgba(255, 0, 0, 0.704); color: white;">
                            <td colspan="6" style="text-align: center;">No Users Available</td>
                        </tr>
                    <%
                    }
                    %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<div class="footer">
    <p>NamasteMart &copy; 2024. All Rights Reserved.</p>
</div>

</body>
</html>