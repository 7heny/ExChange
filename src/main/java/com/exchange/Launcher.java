package com.exchange;

import com.exchange.db.Database;
import com.exchange.dao.UserDao;
import com.exchange.dao.CurrencyDao;
import com.exchange.model.User;
import com.exchange.model.Currency;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
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

        // === ДОБАВЛЕНИЕ ТЕСТОВЫХ ВАЛЮТ ===
        CurrencyDao currencyDao = new CurrencyDao();
        if (currencyDao.getAllCurrencies().isEmpty()) {
            Currency usd = new Currency("USD", "Доллар США", 70.79);
            Currency eur = new Currency("EUR", "Евро", 83.27);
            Currency rub = new Currency("RUB", "Российский рубль", 1.00);
            currencyDao.addCurrency(usd);
            currencyDao.addCurrency(eur);
            currencyDao.addCurrency(rub);
            System.out.println("[DB] Добавлены тестовые валюты: USD, EUR, RUB");
        }

        // === ВЫВОД СПИСКА ПОЛЬЗОВАТЕЛЕЙ (для проверки) ===
        System.out.println("[DB] Пользователи в БД:");
        for (User u : userDao.getAllUsers()) {
            System.out.println("  - " + u);
        }

        // === ВЫВОД СПИСКА ВАЛЮТ (для проверки) ===
        System.out.println("[DB] Валюты в БД:");
        for (Currency c : currencyDao.getAllCurrencies()) {
            System.out.println("  - " + c);
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