package com.exchange.dao;

import com.exchange.db.Database;
import com.exchange.model.Operation;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object для операций обмена валют
 * Сохраняет и загружает историю обменов
 */
public class OperationDao {

    // ==================== CREATE ====================
    /**
     * Сохраняет операцию обмена в БД
     */
    public void addOperation(Operation operation) {
        String sql = "INSERT INTO operations (user_id, user_login, from_currency, to_currency, amount, result) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, operation.getUserId());
            pstmt.setString(2, operation.getUserLogin());
            pstmt.setString(3, operation.getFromCurrency());
            pstmt.setString(4, operation.getToCurrency());
            pstmt.setDouble(5, operation.getAmount());
            pstmt.setDouble(6, operation.getResult());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== READ ====================
    /**
     * Получить все операции (только для админа)
     */
    public List<Operation> getAllOperations() {
        List<Operation> operations = new ArrayList<>();
        String sql = "SELECT * FROM operations ORDER BY operation_date DESC";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Operation op = new Operation();
                op.setId(rs.getInt("id"));
                op.setUserId(rs.getInt("user_id"));
                op.setUserLogin(rs.getString("user_login"));
                op.setFromCurrency(rs.getString("from_currency"));
                op.setToCurrency(rs.getString("to_currency"));
                op.setAmount(rs.getDouble("amount"));
                op.setResult(rs.getDouble("result"));
                op.setOperationDate(rs.getTimestamp("operation_date"));
                operations.add(op);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return operations;
    }

    /**
     * Получить операции конкретного пользователя
     */
    public List<Operation> getOperationsByUser(int userId) {
        List<Operation> operations = new ArrayList<>();
        String sql = "SELECT * FROM operations WHERE user_id = ? ORDER BY operation_date DESC";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Operation op = new Operation();
                op.setId(rs.getInt("id"));
                op.setUserId(rs.getInt("user_id"));
                op.setUserLogin(rs.getString("user_login"));
                op.setFromCurrency(rs.getString("from_currency"));
                op.setToCurrency(rs.getString("to_currency"));
                op.setAmount(rs.getDouble("amount"));
                op.setResult(rs.getDouble("result"));
                op.setOperationDate(rs.getTimestamp("operation_date"));
                operations.add(op);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return operations;
    }

    // ==================== DELETE ====================
    /**
     * Удалить все операции пользователя (при удалении пользователя)
     */
    public void deleteOperationsByUser(int userId) {
        String sql = "DELETE FROM operations WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}