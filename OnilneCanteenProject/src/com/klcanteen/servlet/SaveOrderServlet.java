package com.klcanteen.servlet;

import com.klcanteen.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@WebServlet("/saveOrder")
public class SaveOrderServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");

        String totalAmountStr = request.getParameter("totalAmount");
        String itemsJson      = request.getParameter("itemsJson");

        double totalAmount = 0.0;
        try {
            if (totalAmountStr != null && !totalAmountStr.trim().isEmpty()) {
                totalAmount = Double.parseDouble(totalAmountStr);
            }
        } catch (NumberFormatException e) {
            // keep default 0.0
        }

        // Simple random order code for reference (e.g., ORD-20251122-1234)
        String orderCode = generateOrderCode();

        int rows = 0;
        try (Connection con = DBUtil.getConnection()) {
            String sql = "INSERT INTO orders (order_code, total_amount, items_json, created_at) VALUES (?,?,?,NOW())";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, orderCode);
                ps.setDouble(2, totalAmount);
                ps.setString(3, itemsJson);
                rows = ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PrintWriter out = response.getWriter()) {
            if (rows > 0) {
                out.println("Order saved successfully with code: " + orderCode);
            } else {
                out.println("Failed to save order.");
            }
        }
    }

    private String generateOrderCode() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String ts = LocalDateTime.now().format(fmt);
        int rand = new Random().nextInt(9000) + 1000;
        return "ORD-" + ts + "-" + rand;
    }
}