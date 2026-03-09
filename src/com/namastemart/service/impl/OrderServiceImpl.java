package com.namastemart.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.namastemart.beans.CartBean;
import com.namastemart.beans.OrderBean;
import com.namastemart.beans.OrderDetails;
import com.namastemart.beans.TransactionBean;
import com.namastemart.beans.UserBean;
import com.namastemart.service.OrderService;
import com.namastemart.utility.DBUtil;
import com.namastemart.utility.MailMessage;

public class OrderServiceImpl implements OrderService 
{

    @Override
    public String paymentSuccess(String userName, double paidAmount) 
	{
        String status = "Order Placement Failed!";
        List<CartBean> cartItems = new CartServiceImpl().getAllCartItems(userName);

        if (cartItems.size() == 0) return status;

        TransactionBean transaction = new TransactionBean(userName, paidAmount);
        boolean ordered = false;
        String transactionId = transaction.getTransactionId();

        for (CartBean item : cartItems) 
		{
            double amount = new ProductServiceImpl().getProductPrice(item.getProdId()) * item.getQuantity();
			
            OrderBean order = new OrderBean(transactionId, item.getProdId(), item.getQuantity(), amount);
            
            ordered = addOrder(order);
            if (!ordered) break;
            else ordered = new CartServiceImpl().removeAProduct(item.getUserId(), item.getProdId());
            
            if (!ordered) break;
            else ordered = new ProductServiceImpl().sellNProduct(item.getProdId(), item.getQuantity());
            
            if (!ordered) break;
            
		}	
        
		if (ordered) 
		{
            ordered = addTransaction(transaction);
            if (ordered) 
			{
                MailMessage.transactionSuccess(userName, new UserServiceImpl().getFName(userName), transaction.getTransactionId(), transaction.getTransAmount());
                status = "Order Placed Successfully!";
            }
        }
		
        return status;
    }

    @Override
    public boolean addOrder(OrderBean order) 
	{
        boolean flag = false;
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;

        try 
		{
            ps = con.prepareStatement("INSERT INTO orders (orderid, prodid, quantity, amount, shipped, order_date) VALUES (?, ?, ?, ?, ?, ?)");
			
            ps.setString(1, order.getTransactionId());
            ps.setString(2, order.getProductId());
            ps.setInt(3, order.getQuantity());
            ps.setDouble(4, order.getAmount());
            ps.setInt(5, 0); // shipped status, default 0 for not shipped
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis())); // Set order_date
            
            int k = ps.executeUpdate();
			
            if (k > 0) 
			{
                flag = true;
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        } 
		finally 
		{
            try 
			{
                if (ps != null) ps.close();
                if (con != null) con.close();
            } 
			catch (SQLException e) 
			{
                e.printStackTrace();
            }
        }
		
        return flag;
    }

    @Override
    public boolean addTransaction(TransactionBean transaction) 
	{
        boolean flag = false;
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;

        try 
		{
            ps = con.prepareStatement("INSERT INTO transactions (transid, username, time, amount, status) VALUES (?, ?, ?, ?, ?)");
            ps.setString(1, transaction.getTransactionId());
            ps.setString(2, transaction.getUserName());
            ps.setTimestamp(3, transaction.getTransDateTime());
            ps.setDouble(4, transaction.getTransAmount());
            ps.setString(5, "completed"); // Default status
            
            int k = ps.executeUpdate();
            if (k > 0) flag = true;
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        } 
		finally 
		{
            try 
			{
                if (ps != null) ps.close();
                if (con != null) con.close();
            }
			catch (SQLException e) 
			{
                e.printStackTrace();
            }
        }
        return flag;
    }

    @Override
    public int countSoldItem(String prodId) 
	{
        int count = 0;
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try 
		{
            ps = con.prepareStatement("SELECT SUM(quantity) FROM orders WHERE prodid = ?");
            ps.setString(1, prodId);
            rs = ps.executeQuery();

            if (rs.next()) count = rs.getInt(1);
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        } 
		finally 
		{
            try 
			{
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } 
			catch (SQLException e) 
			{
                e.printStackTrace();
            }
        }
        return count;
    }

    @Override
    public List<OrderBean> getAllOrders() 
	{
        List<OrderBean> orderList = new ArrayList<>();
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try 
		{
            ps = con.prepareStatement("SELECT * FROM orders");
            rs = ps.executeQuery();

            while (rs.next()) 
			{
                OrderBean order = new OrderBean(
                    rs.getString("orderid"),
                    rs.getString("prodid"),
                    rs.getInt("quantity"),
                    rs.getDouble("amount"),
                    rs.getInt("shipped")
                );
                orderList.add(order);
            }
        } 
		
		catch (SQLException e) 
		{
            e.printStackTrace();
        } 
		finally 
		{
            try 
			{
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } 
			catch (SQLException e) 
			{
                e.printStackTrace();
            }
        }
        return orderList;
    }

    @Override
    public List<OrderBean> getOrdersByUserId(String emailId) 
	{
        List<OrderBean> orderList = new ArrayList<>();
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try 
		{
            ps = con.prepareStatement(
                "SELECT o.orderid, o.prodid, o.quantity, o.amount, o.shipped " +
                "FROM orders o " +
                "INNER JOIN transactions t ON o.orderid = t.transid " +
                "WHERE t.username = ?");
            ps.setString(1, emailId);
            rs = ps.executeQuery();

            while (rs.next()) 
			{
                OrderBean order = new OrderBean(
                    rs.getString("orderid"),
                    rs.getString("prodid"),
                    rs.getInt("quantity"),
                    rs.getDouble("amount"),
                    rs.getInt("shipped")
                );
                orderList.add(order);
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        } 
		finally 
		{
            try 
			{
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } 
			catch (SQLException e) 
			{
                e.printStackTrace();
            }
        }
        return orderList;
    }

    @Override
    public List<OrderDetails> getAllOrderDetails(String userEmailId) 
	{
        List<OrderDetails> orderList = new ArrayList<>();
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try 
		{
            ps = con.prepareStatement(
                "SELECT p.pid AS prodid, o.orderid AS orderid, o.shipped AS shipped,p.image AS image, " +
				"p.pname AS pname, o.quantity AS qty, o.amount AS amount, t.time AS time " +
                "FROM orders o " +
                "INNER JOIN product p ON p.pid = o.prodid " +
                "INNER JOIN transactions t ON t.transid = o.orderid " +
                "WHERE t.username = ?");
            ps.setString(1, userEmailId);
            rs = ps.executeQuery();

            while (rs.next()) 
			{
                OrderDetails order = new OrderDetails();
                order.setOrderId(rs.getString("orderid"));
                order.setProdImage(rs.getAsciiStream("image"));
                order.setProdName(rs.getString("pname"));
                order.setQty(rs.getString("qty"));
                order.setAmount(rs.getString("amount"));
                order.setTime(rs.getTimestamp("time"));
                order.setProductId(rs.getString("prodid"));
                order.setShipped(rs.getInt("shipped"));
                orderList.add(order);
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        } 
		finally 
		{
            try 
			{
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (con != null) con.close();
            } 
			catch (SQLException e) 
			{
                e.printStackTrace();
            }
        }
        return orderList;
    }

    @Override
    public String shipNow(String orderId, String prodId) 
	{
        String status = "FAILURE";
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;

        try 
		{
            ps = con.prepareStatement("UPDATE orders SET shipped = 1 WHERE orderid = ? AND prodid = ? AND shipped = 0");
            ps.setString(1, orderId);
            ps.setString(2, prodId);
            
            int k = ps.executeUpdate();
            if (k > 0) 
			{
                status = "Order Has been shipped successfully!!";
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        } 
		finally 
		{
            try 
			{
                if (ps != null) ps.close();
                if (con != null) con.close();
            } 
			catch (SQLException e) 
			{
                e.printStackTrace();
            }
        }
        return status;
    }

    @Override
    public int getTotalOrders() 
	{
        int totalOrders = 0;
        String query = "SELECT COUNT(*) FROM orders";

        try (Connection con = DBUtil.provideConnection();
            PreparedStatement pstmt = con.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()) 
		{
            if (rs.next()) 
			{
                totalOrders = rs.getInt(1);
            }
        }
		catch (SQLException e) 
		{
            e.printStackTrace();
        }
        return totalOrders;
    }

    @Override
    public double getTotalAmount() 
	{
        double totalAmount = 0.0;
        String query = "SELECT SUM(amount) FROM transactions"; // Assumes transactions table has an 'amount' column

        try (Connection con = DBUtil.provideConnection();
            PreparedStatement pstmt = con.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()) 
		{
            if (rs.next())
                totalAmount = rs.getDouble(1);
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        }
        return totalAmount;
    }

    @Override
    public List<Double> getMonthlyProfit() 
	{
        List<Double> monthlyProfit = new ArrayList<>(Collections.nCopies(12, 0.0)); // Initialize with 12 zeros
        String query = "SELECT SUM(amount) AS total_profit, MONTH(order_date) AS month " + 
                      "FROM orders " + 
                      "WHERE YEAR(order_date) = YEAR(CURDATE()) " + 
                      "GROUP BY MONTH(order_date) " + 
                      "ORDER BY month";

        try (Connection con = DBUtil.provideConnection();
            PreparedStatement pstmt = con.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()) 
		{
            while (rs.next()) 
			{
                int month = rs.getInt("month");
                double totalProfit = rs.getDouble("total_profit");
                // Store profit in the correct month index (month - 1 because months are 1-based)
                monthlyProfit.set(month - 1, totalProfit);
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        }
        return monthlyProfit;
    }

    @Override
    public List<Integer> getMonthlySales() 
	{
        List<Integer> monthlySales = new ArrayList<>(Collections.nCopies(12, 0)); // Initialize with 12 zeros
        String query = "SELECT COUNT(*) AS total_sales, MONTH(order_date) AS month " + 
                      "FROM orders " +
                      "WHERE YEAR(order_date) = YEAR(CURDATE()) " +
                      "GROUP BY MONTH(order_date) " +
                      "ORDER BY month";

        try (Connection con = DBUtil.provideConnection();
            PreparedStatement pstmt = con.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()) 
		{
            while (rs.next()) 
			{
                int month = rs.getInt("month");
                int totalSales = rs.getInt("total_sales");
                // Store sales in the correct month index (month - 1 because months are 1-based)
                monthlySales.set(month - 1, totalSales);
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        }
        return monthlySales;
    }
    
	@Override
    public OrderDetails getOrderDetailsById(String orderId) 
	{
        OrderDetails orderDetails = null;
        String query = "SELECT * FROM orders WHERE orderid = ?";
        try (Connection connection = DBUtil.provideConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)) 
		{
            preparedStatement.setString(1, orderId);
            ResultSet resultSet = preparedStatement.executeQuery();
        
            if (resultSet.next()) 
			{
                orderDetails = new OrderDetails();
                orderDetails.setOrderId(resultSet.getString("orderid"));
                orderDetails.setProductId(resultSet.getString("prodid"));
                //orderDetails.setProdName(resultSet.getString("prodname")); //Correct field name
                orderDetails.setQty(resultSet.getString("quantity")); // Assuming qty is stored as String
                orderDetails.setAmount(resultSet.getString("amount")); // Assuming amount is stored as String
                orderDetails.setShipped(resultSet.getInt("shipped"));
                orderDetails.setTime(resultSet.getTimestamp("order_date"));
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace(); // Log the SQL exception
        }
        return orderDetails;
    }

    @Override
    public boolean cancelOrder(String orderId, String accountNumber, String bank, String ifscCode, String userEmail, String userName) 
	{
        OrderDetails orderDetails = getOrderDetailsById(orderId);
    
        if (orderDetails != null) 
		{
            double refundAmount = Double.parseDouble(orderDetails.getAmount()); // Convert amount to double
        
            // Process the refund
            boolean refundProcessed = processRefund(accountNumber, bank, ifscCode, refundAmount);
        
            if (refundProcessed) 
			{
                // Update the order status to "CANCELLED"
                boolean statusUpdated = updateOrderStatus(orderId, "CANCELLED");
            
                if (statusUpdated) 
				{
                    // Update the total amount to reflect the cancellation
                    boolean amountUpdated = updateOrderAmount(orderId, -refundAmount); // Reducing the amount
                
                    if (amountUpdated) 
					{
                        // Send cancellation email
                        if (userEmail == null) 
						{
                            userEmail = "default@example.com"; // Fallback email address
                        }
                        if (userName == null) 
						{
                            userName = "Valued Customer"; // Fallback customer name
                        }
                        MailMessage.orderCancelled(userEmail, userName, orderId, refundAmount);
                        // Return true indicating the order cancellation was successful
                        return true;
                    }
                }
            }
        }
        
		// Return false indicating the order cancellation failed
        return false;
    }
    

    
    public boolean updateOrderAmount(String orderId, double refundAmount) 
	{
        String updateOrderQuery = "UPDATE orders SET amount = amount + ? WHERE orderid = ?";
        String updateTransactionQuery = "UPDATE transactions SET amount = amount + ?, status = 'refunded' WHERE transid = ?";

        // Using try-with-resources to ensure resources are closed properly
        try (Connection con = DBUtil.provideConnection();
             PreparedStatement updateOrderStmt = con.prepareStatement(updateOrderQuery);
             PreparedStatement updateTransactionStmt = con.prepareStatement(updateTransactionQuery)) 
	    {
            
            // Update the orders table
            updateOrderStmt.setDouble(1, refundAmount);
            updateOrderStmt.setString(2, orderId);
            int rowsAffectedOrders = updateOrderStmt.executeUpdate();
            
            // Update the transactions table
            updateTransactionStmt.setDouble(1, refundAmount);
            updateTransactionStmt.setString(2, orderId);
            int rowsAffectedTransactions = updateTransactionStmt.executeUpdate();
            
            if (rowsAffectedOrders > 0 && rowsAffectedTransactions > 0) 
			{
                // Both updates were successful
                return updateProductOnCancel(orderId);
            } 
			else 
			{
                // If either update fails, indicate failure
                return false;
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace(); // Log the SQL exception
            return false;
        }
    }

    public boolean updateProductOnCancel(String orderId) 
	{
        // Query to update the product quantity based on the canceled order
        String updateProductQuery = "UPDATE product SET pquantity = pquantity + (SELECT quantity FROM orders WHERE orderid = ?) WHERE pid = (SELECT prodid FROM orders WHERE orderid = ?)";
        
        // Using try-with-resources to ensure resources are closed properly
        try (Connection con = DBUtil.provideConnection();
             PreparedStatement updateProductStmt = con.prepareStatement(updateProductQuery);
             PreparedStatement checkOrderStatusStmt = con.prepareStatement("SELECT shipped FROM orders WHERE orderid = ?")) 
	    {
            // Step 1: Check if the order is canceled (shipped = 2)
            checkOrderStatusStmt.setString(1, orderId);
            ResultSet rs = checkOrderStatusStmt.executeQuery();
            
            if (rs.next()) 
	     	{
                int shippedStatus = rs.getInt("shipped");
                
                // If the order is canceled, proceed with updating the product quantity
                if (shippedStatus == 2) 
				{
                    updateProductStmt.setString(1, orderId);
                    updateProductStmt.setString(2, orderId);
                    
                    int rowsAffectedProduct = updateProductStmt.executeUpdate();
                    if (rowsAffectedProduct > 0) 
		    		{
                        return true; // Product quantity was successfully updated
                    } 
			    	else 
					{
                        return false; // Product quantity update failed
                    }
                }
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace(); // Log the SQL exception
			return false;
        }
		return false; // Return false if the order was not canceled or any operation failed
    }
	
	@Override
    public boolean updateOrderStatus(String orderId, String status) 
	{
        String query = "UPDATE orders SET shipped = ? WHERE orderid = ?";
        
        try (Connection connection = DBUtil.provideConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) 
	    {
            
            // Map the status string to the corresponding status code
			// int statusCode;
			// switch (status) {
			//	case "CANCELLED";
			//       statusCode = 2; //Assuming 2 means cancelled
			//       break;
			//  case "SHIPPED":
			// 	     statusCode = 1; // Assuming 1 means shipped
			//		 break;
			//	default:
			//	     statusCode = 0; // Assuming 0 is for other statuses
			//		 break;
			// }
			// preparedStatement.setInt(1, statusCode);
			// preparedStatement.setString(2, orderId);
			
			// Map the status string to the corrresponding status code (e.g., 2 for "CANCELLED")
            int statusCode = status.equals(status) ? 2 : 1; // Assuming 2 means cancelled, 1 means shipped
            
            preparedStatement.setInt(1, statusCode);
            preparedStatement.setString(2, orderId);
            
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0; // Return true if the update was successful
        } 
		catch (SQLException e) 
		{
            e.printStackTrace(); // Log the SQL exception
        }
        return false; // Return false if the update failed
    }


    public boolean processRefund(String accountNumber, String bank, String ifscCode, double refundAmount) 
	{
        // Implement the actual refund logic here.
        // Currently, this is a placeholder that always returns true.
        System.out.println("Processing refund for account: " + accountNumber + ", amount: " + refundAmount);
        return true;
    }
}