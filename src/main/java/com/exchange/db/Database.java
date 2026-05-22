package com.exchange.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Класс для работы с H2 базой данных
 * База сохраняется в файл exchanger.mv.db в корне проекта
 */
public class Database {

    // === НАСТРОЙКИ ПОДКЛЮЧЕНИЯ ===
    private static final String DB_URL = "jdbc:h2:./exchanger;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASS = "";  // пустой пароль для H2

    /**
     * Получить соединение с БД
     * Закрывать соединение нужно в try-with-resources
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    /**
     * Создаёт таблицы, если их нет
     * Вызывается 1 раз при старте приложения
     */
    public static void initTables() {
        // SQL запрос для создания таблицы пользователей
        String createUserTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                login VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(100) NOT NULL,
                full_name VARCHAR(100),
                role VARCHAR(20) DEFAULT 'USER'
            )
        """;

        // ========== ТАБЛИЦА ДЛЯ ВАЛЮТ ==========
        String createCurrencyTable = """
            CREATE TABLE IF NOT EXISTS currencies (
                id INT AUTO_INCREMENT PRIMARY KEY,
                code VARCHAR(10) UNIQUE NOT NULL,
                name VARCHAR(100) NOT NULL,
                rate DOUBLE NOT NULL
            )
        """;
        // ========== ТАБЛИЦА ОПЕРАЦИЙ (ИСТОРИЯ ОБМЕНОВ) ==========
        String createOperationTable = """
            CREATE TABLE IF NOT EXISTS operations (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                user_login VARCHAR(50) NOT NULL,
                from_currency VARCHAR(10) NOT NULL,
                to_currency VARCHAR(10) NOT NULL,
                amount DOUBLE NOT NULL,
                result DOUBLE NOT NULL,
                amount_rub DOUBLE NOT NULL,
                operation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """;

        // try-with-resources — соединение и запрос закроются автоматически
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUserTable);
            stmt.execute(createCurrencyTable);
            stmt.execute(createOperationTable);
            System.out.println("[DB] Таблица users создана/проверена");
        } catch (SQLException e) {
            System.err.println("[DB] Ошибка при создании таблицы: " + e.getMessage());
            e.printStackTrace();
        }
    }
}