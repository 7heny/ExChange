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

        // ========== НОВАЯ ТАБЛИЦА ДЛЯ ВАЛЮТ ==========
        String createCurrencyTable = """
            CREATE TABLE IF NOT EXISTS currencies (
                id INT AUTO_INCREMENT PRIMARY KEY,
                code VARCHAR(10) UNIQUE NOT NULL,
                name VARCHAR(100) NOT NULL,
                rate DOUBLE NOT NULL
            )
        """;

        // try-with-resources — соединение и запрос закроются автоматически
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createUserTable);
            stmt.execute(createCurrencyTable);
            System.out.println("[DB] Таблица users создана/проверена");
        } catch (SQLException e) {
            System.err.println("[DB] Ошибка при создании таблицы: " + e.getMessage());
            e.printStackTrace();
        }
    }
}