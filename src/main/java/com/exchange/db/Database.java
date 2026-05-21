package com.exchange.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// Простой класс для работы с H2
// Хранит таблицы в файле exchanger.mv.db в корне проекта

public class Database {
    private static final String DB_URL = "jdbc:h2:./exchanger;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";

    // Подключение к БД
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    // Создаем таблицу users при запуске
    public static void initTables() {
        String createUserTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                login VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(100) NOT NULL,
                full_name VARCHAR(100),
                role VARCHAR(20) DEFAULT 'USER'
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUserTable);
            System.out.println("[DB] Таблица users создана");
        } catch (SQLException e) {
            System.err.println("[DB] Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}