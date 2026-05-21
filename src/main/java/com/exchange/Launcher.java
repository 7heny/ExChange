package com.exchange;

import com.exchange.db.Database;
import com.exchange.dao.UserDao;
import com.exchange.model.User;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Точка входа в приложение
 * Запускает базу данных и веб-сервер
 */
public class Launcher {

    public static void main(String[] args) throws Exception {

        // === НАСТРОЙКА КОДИРОВКИ ===
        System.setProperty("file.encoding", "UTF-8");

        // === ИНИЦИАЛИЗАЦИЯ БАЗЫ ДАННЫХ ===
        // Создаёт таблицы, если их нет
        Database.initTables();

        // === ДОБАВЛЕНИЕ ТЕСТОВОГО АДМИНА ===
        UserDao userDao = new UserDao();

        // Если в БД нет ни одного пользователя — создаём админа
        if (userDao.getAllUsers().isEmpty()) {
            User admin = new User();
            admin.setLogin("admin");
            admin.setPassword("admin123");
            admin.setFullName("Administrator");
            admin.setRole("ADMIN");
            userDao.addUser(admin);
            System.out.println("[DB] Добавлен тестовый админ: login=admin, pass=admin123");
        }

        // === ВЫВОД СПИСКА ПОЛЬЗОВАТЕЛЕЙ (для проверки) ===
        System.out.println("[DB] Пользователи в БД:");
        for (User u : userDao.getAllUsers()) {
            System.out.println("  - " + u);
        }

        // === ЗАПУСК ВЕБ-СЕРВЕРА ===
        Server server = new Server(8080);

        WebAppContext context = new WebAppContext();
        context.setContextPath("/");                      // приложение доступно по localhost:8080/
        context.setResourceBase("src/main/webapp");       // папка с html/jsp файлами

        server.setHandler(context);
        server.start();

        System.out.println("ExChange запущен на http://localhost:8080");

        // Оставляем сервер работать до принудительной остановки
        server.join();
    }
}