package com.namastemart.service.impl;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.namastemart.beans.DemandBean;
import com.namastemart.beans.ProductBean;
import com.namastemart.service.ProductService;
import com.namastemart.utility.DBUtil;
import com.namastemart.utility.IDUtil;
import com.namastemart.utility.MailMessage;

public class ProductServiceImpl implements ProductService 
{

    @Override
    public String addProduct(String prodName, String prodType, String prodInfo, double prodPrice, int prodQuantity,InputStream prodImage) 
	{
        String status = null;
        String prodId = IDUtil.generateId();
        ProductBean product = new ProductBean(prodId, prodName, prodType, prodInfo, prodPrice, prodQuantity, prodImage);
        status = addProduct(product);
        return status;
    }

    @Override
    public String addProduct(ProductBean product) 
	{
        String status = "Product Registration Failed!";
        // Generate a new ID if none is provided
        if (product.getProdId() == null) 
		{
            product.setProdId(IDUtil.generateId());
        }

        // Use try-with-resources to ensure proper resource management
        try (Connection con = DBUtil.provideConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO product VALUES (?, ?, ?, ?, ?, ?, ?)")) 
		{
            
            ps.setString(1, product.getProdId());
            ps.setString(2, product.getProdName());
            ps.setString(3, product.getProdType());
            ps.setString(4, product.getProdInfo());
            ps.setDouble(5, product.getProdPrice());
            ps.setInt(6, product.getProdQuantity());
            ps.setBlob(7, product.getProdImage()); // Ensure this is an InputStream

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) 
			{
                status = "Product Added Successfully with Product Id: " + product.getProdId();
            } 
			else 
			{
                status = "Product Addition Failed!";
            }
        }
		catch (SQLException e) 
		{
            status = "Error: " + e.getMessage();
            e.printStackTrace();
        }
        return status;
    }

    @Override
    public String removeProduct(String prodId) 
	{
        String status = "Product Removal Failed!";

        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;

        try 
		{
            ps = con.prepareStatement("delete from product where pid=?");
            ps.setString(1, prodId);

            int k = ps.executeUpdate();

            if (k > 0) 
			{
                status = "Product Removed Successfully!";
                ps2 = con.prepareStatement("delete from usercart where prodid=?");
                ps2.setString(1, prodId);
                ps2.executeUpdate();
            }
        } 
		catch (SQLException e) 
		{
            status = "Error: " + e.getMessage();
            e.printStackTrace();
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(ps2);

        return status;
    }

    @Override
    public String updateProduct(ProductBean prevProduct, ProductBean updatedProduct) 
	{
        String status = "Product Updation Failed!";

        if (!prevProduct.getProdId().equals(updatedProduct.getProdId())) 
		{
            status = "Both Products are Different, Updation Failed!";
            return status;
        }

        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;

        try 
		{
            ps = con.prepareStatement("update product set pname=?, ptype=?, pinfo=?, pprice=?, pquantity=?, image=? where pid=?");
            
            ps.setString(1, updatedProduct.getProdName());
            ps.setString(2, updatedProduct.getProdType());
            ps.setString(3, updatedProduct.getProdInfo());
            ps.setDouble(4, updatedProduct.getProdPrice());
            ps.setInt(5, updatedProduct.getProdQuantity());
            ps.setBlob(6, updatedProduct.getProdImage());
            ps.setString(7, prevProduct.getProdId());

            int k = ps.executeUpdate();

            if (k > 0) 
			{
                status = "Product Updated Successfully!";
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);

        return status;
    }

    @Override
    public String updateProductPrice(String prodId, double updatedPrice) 
	{
        String status = "Price Updation Failed!";
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;
        try 
		{
            ps = con.prepareStatement("update product set pprice=? where pid=?");
            ps.setDouble(1, updatedPrice);
            ps.setString(2, prodId);
            int k = ps.executeUpdate();
            if (k > 0) 
			    status = "Price Updated Successfully!";  
        } 
		catch (SQLException e)
		{
            status = "Error: " + e.getMessage();
            e.printStackTrace();
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);

        return status;
    }

    @Override
    public List<ProductBean> getAllProducts() 
	{
        List<ProductBean> products = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try 
		{
            con = DBUtil.provideConnection();
            if (con == null) 
			{
                throw new SQLException("Failed to obtain database connection");
            }

            ps = con.prepareStatement("SELECT * FROM product");
            rs = ps.executeQuery();

            if (rs != null) 
			{
                while (rs.next()) 
				{
                    ProductBean product = new ProductBean();
                    product.setProdId(rs.getString(1));
                    product.setProdName(rs.getString(2));
                    product.setProdType(rs.getString(3));
                    product.setProdInfo(rs.getString(4));
                    product.setProdPrice(rs.getDouble(5));
                    product.setProdQuantity(rs.getInt(6));
                    product.setProdImage(rs.getBinaryStream(7));
                    products.add(product);
                }
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace(); // Replace with proper logging in production code
        } 
		finally 
		{
            // Ensure resources are closed to avoid leaks
            try
			{
				if (rs != null) rs.close();
				if (ps != null) ps.close();
				if (con != null) con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace(); //Replace with proper logging in production code
			}
        }

        return products;
    }

    /*
    @Override
    public List<ProductBean> getAllProductsByType(String type) 
	{
        if (type == null) 
		{
            throw new IllegalArgumentException("Type cannot be null");
        }

        List<ProductBean> products = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try 
		{
            con = DBUtil.provideConnection();
            if (con == null) 
			{
                throw new IllegalStateException("Failed to establish database connection");
            }

            String query = "SELECT * FROM product WHERE LOWER(ptype) LIKE ?";
            ps = con.prepareStatement(query);
            ps.setString(1, "%" + type.toLowerCase() + "%");
            rs = ps.executeQuery();

            while (rs.next()) 
			{
                ProductBean product = new ProductBean();
                product.setProdId(rs.getString(1));
                product.setProdName(rs.getString(2));
                product.setProdType(rs.getString(3));
                product.setProdInfo(rs.getString(4));
                product.setProdPrice(rs.getDouble(5));
                product.setProdQuantity(rs.getInt(6));
                product.setProdImage(rs.getAsciiStream(7));
                products.add(product);
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        } 
		finally 
		{
            DBUtil.closeConnection(rs);
            DBUtil.closeConnection(ps);
            DBUtil.closeConnection(con);
        }

        return products;
    }
	*/

    @Override
    public List<ProductBean> getAllProductsByType(String types) 
	{
        if (types == null || types.isEmpty()) 
		{
            throw new IllegalArgumentException("Types cannot be null or empty");
        }

        List<ProductBean> products = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try 
		{
            con = DBUtil.provideConnection();
            if (con == null) 
			{
                throw new IllegalStateException("Failed to establish database connection");
            }

            // Split the types by comma
            String[] typeArray = types.split(",");

            // Create a dynamic query to handle multiple types
            StringBuilder query = new StringBuilder("SELECT * FROM product WHERE LOWER(ptype) IN (");
            for (int i = 0; i < typeArray.length; i++) 
			{
                query.append("?");
                if (i < typeArray.length - 1) 
				{
                    query.append(",");
                }
            }
            query.append(")");

            ps = con.prepareStatement(query.toString());
            // Set parameters for the query
            for (int i = 0; i < typeArray.length; i++) 
			{
                ps.setString(i + 1, typeArray[i].toLowerCase().trim());
            }

            rs = ps.executeQuery();

            while (rs.next()) 
			{
                ProductBean product = new ProductBean();
                product.setProdId(rs.getString(1));
                product.setProdName(rs.getString(2));
                product.setProdType(rs.getString(3));
                product.setProdInfo(rs.getString(4));
                product.setProdPrice(rs.getDouble(5));
                product.setProdQuantity(rs.getInt(6));
                product.setProdImage(rs.getBinaryStream(7));
                products.add(product);
            }

        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        } 
		finally 
		{
            DBUtil.closeConnection(rs);
            DBUtil.closeConnection(ps);
            DBUtil.closeConnection(con);
        }

        return products;
    }

    @Override
    public List<ProductBean> searchAllProducts(String search) 
	{
        List<ProductBean> products = new ArrayList<>();

        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try 
		{
            ps = con.prepareStatement("SELECT * FROM product where lower(ptype) like ? or lower(pname) like ? or lower(pinfo) like ?");
            search = "%" + search + "%";
            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            rs = ps.executeQuery();

            while (rs.next()) 
			{
                ProductBean product = new ProductBean();
                product.setProdId(rs.getString(1));
                product.setProdName(rs.getString(2));
                product.setProdType(rs.getString(3));
                product.setProdInfo(rs.getString(4));
                product.setProdPrice(rs.getDouble(5));
                product.setProdQuantity(rs.getInt(6));
                product.setProdImage(rs.getAsciiStream(7));
                products.add(product);
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return products;
    }

    @Override
    public byte[] getImage(String prodId) 
	{
        byte[] image = null;

        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try 
		{
            ps = con.prepareStatement("select image from product where pid=?");
            ps.setString(1, prodId);
            rs = ps.executeQuery();

            if (rs.next()) 
                image = rs.getBytes("image");
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);
        DBUtil.closeConnection(rs);

        return image;
    }

    @Override
    public ProductBean getProductDetails(String prodId) 
	{
        ProductBean product = null;

        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try 
		{
            ps = con.prepareStatement("select * from product where pid=?");
            ps.setString(1, prodId);
            rs = ps.executeQuery();

            if (rs.next()) 
			{
                product = new ProductBean();
                product.setProdId(rs.getString(1));
                product.setProdName(rs.getString(2));
                product.setProdType(rs.getString(3));
                product.setProdInfo(rs.getString(4));
                product.setProdPrice(rs.getDouble(5));
                product.setProdQuantity(rs.getInt(6));
                product.setProdImage(rs.getAsciiStream(7));
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);

        return product;
    }

    @Override
    public String updateProductWithoutImage(String prevProductId, ProductBean updatedProduct) 
	{
        String status = "Product Updation Failed!";
		
		if(!prevProductId.equals(updatedProduct.getProdId()))
		{
			status = "Both Products are Different, Updation Failed!";
			return status;
		}
		
        int prevQuantity = new ProductServiceImpl().getProductQuantity(prevProductId);
        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;

        try 
		{
            ps = con.prepareStatement("update product set pname=?, ptype=?, pinfo=?, pprice=?, pquantity=? where pid=?");
            
            ps.setString(1, updatedProduct.getProdName());
            ps.setString(2, updatedProduct.getProdType());
            ps.setString(3, updatedProduct.getProdInfo());
            ps.setDouble(4, updatedProduct.getProdPrice());
            ps.setInt(5, updatedProduct.getProdQuantity());
            ps.setString(6, prevProductId);

            int k = ps.executeUpdate();
            if ((k > 0) && (prevQuantity < updatedProduct.getProdQuantity())) 
			{
                status = "Product Updated Successfully!";
                List<DemandBean> demandList = new DemandServiceImpl().haveDemanded(prevProductId);

                for (DemandBean demand : demandList) 
				{
                    String userFName = new UserServiceImpl().getFName(demand.getUserName());
                    try 
					{
                        MailMessage.productAvailableNow(demand.getUserName(), userFName, updatedProduct.getProdName(), prevProductId);
                    } 
					catch (Exception e) 
					{
                        System.out.println("Mail Sending Failed: " + e.getMessage());
                    }
                    boolean flag = new DemandServiceImpl().removeProduct(demand.getUserName(), prevProductId);

                    if (flag) 
					{
                        status += " And Mail Send to the customers who were waiting for this product!";
                    }
                }
            } 
			else if (k > 0) 
			{
                status = "Product Updated Successfully!";
            } 
			else 
			{
                status = "Product Not available in the store!";
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);

        return status;
    }
	
    @Override
    public double getProductPrice(String prodId) 
	{
        double price = 0;

        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try 
		{
            ps = con.prepareStatement("select * from product where pid=?");
            ps.setString(1, prodId);
            rs = ps.executeQuery();

            if (rs.next()) 
			{
                price = rs.getDouble("pprice");
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);

        return price;
    }

    @Override
    public boolean sellNProduct(String prodId, int n) 
	{
        boolean flag = false;

        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;

        try 
		{
            ps = con.prepareStatement("update product set pquantity=(pquantity - ?) where pid=?");
            ps.setInt(1, n);
            ps.setString(2, prodId);

            int k = ps.executeUpdate();

            if (k > 0) 
			{
                flag = true;
            }
        }
		catch (SQLException e) 
		{
            flag = false;
            e.printStackTrace();
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);

        return flag;
    }

    @Override
    public int getProductQuantity(String prodId) 
	{
        int quantity = 0;

        Connection con = DBUtil.provideConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try 
		{
            ps = con.prepareStatement("select * from product where pid=?");
            ps.setString(1, prodId);
            rs = ps.executeQuery();
            if (rs.next()) 
			{
                quantity = rs.getInt("pquantity");
            }
        } 
		catch (SQLException e) 
		{
            e.printStackTrace();
        }

        DBUtil.closeConnection(con);
        DBUtil.closeConnection(ps);

        return quantity;
    }

    @Override
    public int getTotalProducts() 
	{
        int totalProducts = 0;
        String query = "SELECT COUNT(*) FROM product";

        try (Connection con = DBUtil.provideConnection();
             PreparedStatement pstmt = con.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) 
	    {
            if (rs.next()) 
		    {
                totalProducts = rs.getInt(1);
            }
        } 
		catch (SQLException e)
		{
            e.printStackTrace();
        }
        return totalProducts;
    }
}