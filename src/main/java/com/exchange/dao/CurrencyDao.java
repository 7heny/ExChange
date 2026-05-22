package com.exchange.dao;

import com.exchange.db.Database;
import com.exchange.model.Currency;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object для валют
 * Содержит все CRUD операции с таблицей currencies
 */
public class CurrencyDao {

    // ==================== Создание ====================
    public void addCurrency(Currency currency) {
        String sql = "INSERT INTO currencies (code, name, rate) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currency.getCode());
            pstmt.setString(2, currency.getName());
            pstmt.setDouble(3, currency.getRate());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== Чтение ====================
    public List<Currency> getAllCurrencies() {
        List<Currency> currencies = new ArrayList<>();
        String sql = "SELECT * FROM currencies";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Currency c = new Currency();
                c.setId(rs.getInt("id"));
                c.setCode(rs.getString("code"));
                c.setName(rs.getString("name"));
                c.setRate(rs.getDouble("rate"));
                currencies.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return currencies;
    }

    public Currency getCurrencyById(int id) {
        String sql = "SELECT * FROM currencies WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Currency c = new Currency();
                c.setId(rs.getInt("id"));
                c.setCode(rs.getString("code"));
                c.setName(rs.getString("name"));
                c.setRate(rs.getDouble("rate"));
                return c;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ==================== Обновление ====================
    public void updateCurrency(Currency currency) {
        String sql = "UPDATE currencies SET code=?, name=?, rate=? WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, currency.getCode());
            pstmt.setString(2, currency.getName());
            pstmt.setDouble(3, currency.getRate());
            pstmt.setInt(4, currency.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== Удаление ====================
    public void deleteCurrency(int id) {
        String sql = "DELETE FROM currencies WHERE id=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}