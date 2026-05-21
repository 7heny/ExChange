package com.exchange.dao;

import com.exchange.db.Database;
import com.exchange.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object для пользователей
 * Содержит все CRUD операции с таблицей users
 */
public class UserDao {

    // ==================== CREATE ====================

    /**
     * Добавить нового пользователя в БД
     */
    public void addUser(User user) {
        String sql = "INSERT INTO users (login, password, full_name, role) VALUES (?, ?, ?, ?)";

        // PreparedStatement защищает от SQL-инъекций
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getLogin());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== READ ====================

    /**
     * Получить всех пользователей
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Проходим по всем строкам результата запроса
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setLogin(rs.getString("login"));
                u.setPassword(rs.getString("password"));
                u.setFullName(rs.getString("full_name"));
                u.setRole(rs.getString("role"));
                users.add(u);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Поиск пользователя по логину (для авторизации)
     * Возвращает null, если пользователь не найден
     */
    public User getUserByLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, login);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setLogin(rs.getString("login"));
                u.setPassword(rs.getString("password"));
                u.setFullName(rs.getString("full_name"));
                u.setRole(rs.getString("role"));
                return u;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // пользователь не найден
    }

    // ==================== UPDATE ====================

    /**
     * Обновление данных пользователя (кроме пароля)
     */
    public void updateUser(User user) {
        String sql = "UPDATE users SET login=?, full_name=?, role=? WHERE id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getLogin());
            pstmt.setString(2, user.getFullName());
            pstmt.setString(3, user.getRole());
            pstmt.setInt(4, user.getId());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ==================== DELETE ====================

    /**
     * Удалить пользователя по id
     */
    public void deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}