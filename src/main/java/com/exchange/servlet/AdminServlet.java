package com.exchange.servlet;

import com.exchange.dao.OperationDao;
import com.exchange.model.Operation;
import java.sql.Timestamp;
import com.exchange.dao.CurrencyDao;
import com.exchange.model.Currency;
import com.exchange.dao.UserDao;
import com.exchange.model.User;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Админ-панель для управления пользователями
 * Доступ только для пользователей с ролью ADMIN
 */
public class AdminServlet extends HttpServlet {

    private UserDao userDao = new UserDao();

    // Проверка, что пользователь админ
    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        String role = (String) session.getAttribute("role");
        return "ADMIN".equals(role);
    }

    // === ЗАЩИТА ОТ XSS ===
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Проверка прав
        if (!isAdmin(req)) {
            resp.sendRedirect("/auth");
            return;
        }
        String path = req.getPathInfo();

        if (path == null || path.equals("/dashboard") || path.equals("/")) {
            showDashboard(req, resp);
        } else if (path.equals("/users")) {
            showUsers(req, resp);
        } else if (path.equals("/currencies")) {
            showCurrencies(req, resp);
        } else if (path.equals("/delete")) {
            deleteUser(req, resp);
        } else if (path.equals("/deleteCurrency")) {
            deleteCurrency(req, resp);
        } else if (path.equals("/editCurrency")) {
            showEditCurrencyForm(req, resp);
        } else if (path.equals("/operations")) {
            showOperations(req, resp);
        } else {
            resp.sendError(404);
        }
    }


    // ==================== СТАТИСТИКА ОПЕРАЦИЙ ====================
    /**
     * Показывает все операции обмена валют
     * Доступно только для администратора
     */
    private void showOperations(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        OperationDao opDao = new OperationDao();
        List<Operation> operations = opDao.getAllOperations();

        double totalAmountRub = 0;
        for (Operation op : operations) {
            totalAmountRub += op.getAmountRub();
        }

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>ExChange - История операций</title>");
        out.println("<link rel='stylesheet' href='/css/style.css'>");
        out.println("</head><body>");
        out.println("<div class='container'>");

        out.println("<div class='header'>");
        out.println("<h1>📊 История операций</h1>");
        out.println("<nav>");
        out.println("<a href='/admin/dashboard'>🏠 Главная</a> | ");
        out.println("<a href='/admin/users'>👥 Пользователи</a> | ");
        out.println("<a href='/admin/currencies'>💵 Валюты</a> | ");
        out.println("<a href='/admin/operations'>📊 Операции</a> | ");
        out.println("<a href='/auth?logout=1'>🚪 Выйти</a>");
        out.println("</nav>");
        out.println("</div>");

        out.println("<div class='stats-grid'>");
        out.println("<div class='stat-card'><div class='stat-number'>" + operations.size() + "</div><div class='stat-label'>Всего операций</div></div>");
        out.println("<div class='stat-card'><div class='stat-number'>" + String.format("%.2f", totalAmountRub) + "</div><div class='stat-label'>Общий объём (в RUB)</div></div>");
        out.println("</div>");

        out.println("<div class='card'>");
        out.println("<h2>📋 Детали всех операций</h2>");
        out.println("<div class='table-container'>");
        out.println("<table>");
        out.println("<tr><th>ID</th><th>Пользователь</th><th>Продал</th><th>Купил</th><th>Дата</th></tr>");

        for (Operation op : operations) {
            out.println("<tr>");
            out.println("<td>" + op.getId() + "</td>");
            out.println("<td>" + escapeHtml(op.getUserLogin()) + "</td>");
            out.println("<td>" + String.format("%.2f", op.getAmount()) + " " + op.getFromCurrency() + "</td>");
            out.println("<td>" + String.format("%.2f", op.getResult()) + " " + op.getToCurrency() + "</td>");
            out.println("<td>" + op.getOperationDate() + "</td>");
            out.println("</tr>");
        }
        out.println("</table>");
        out.println("</div>");
        out.println("</div>");

        out.println("</div></body></html>");
    }

// ==================== УДАЛЕНИЕ ВАЛЮТЫ ====================
private void deleteCurrency(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {

    String idStr = req.getParameter("id");
    if (idStr != null && !idStr.isEmpty()) {
        try {
            int id = Integer.parseInt(idStr);
            CurrencyDao currencyDao = new CurrencyDao();
            currencyDao.deleteCurrency(id);
            System.out.println("[ADMIN] Удалена валюта с ID: " + id);
        } catch (NumberFormatException e) {
            System.err.println("[ADMIN] Ошибка: ID не число - " + idStr);
        }
    }
    resp.sendRedirect("/admin/currencies");
}

// ==================== ФОРМА РЕДАКТИРОВАНИЯ ВАЛЮТЫ ====================
private void showEditCurrencyForm(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {

    String idStr = req.getParameter("id");
    if (idStr == null || idStr.isEmpty()) {
        resp.sendRedirect("/admin/currencies");
        return;
    }

    try {
        int id = Integer.parseInt(idStr);
        CurrencyDao currencyDao = new CurrencyDao();
        Currency currency = currencyDao.getCurrencyById(id);

        if (currency == null) {
            resp.sendRedirect("/admin/currencies");
            return;
        }

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>Редактировать валюту</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; margin: 20px; }");
        out.println("input { padding: 5px; margin: 5px; }");
        out.println("button { background: #2196F3; color: white; border: none; padding: 6px 12px; cursor: pointer; }");
        out.println("</style>");
        out.println("</head><body>");

        out.println("<h1>✏️ Редактировать валюту</h1>");
        out.println("<form method='post' action='/admin/updateCurrency'>");
        out.println("<input type='hidden' name='id' value='" + currency.getId() + "'>");
        out.println("<label>Код:</label>");
        out.println("<input type='text' name='code' value='" + escapeHtml(currency.getCode()) + "' required><br>");
        out.println("<label>Название:</label>");
        out.println("<input type='text' name='name' value='" + escapeHtml(currency.getName()) + "' required size='30'><br>");
        out.println("<label>Курс:</label>");
        out.println("<input type='number' name='rate' step='0.01' value='" + currency.getRate() + "' required><br>");
        out.println("<button type='submit'>Сохранить</button>");
        out.println("<a href='/admin/currencies'>Отмена</a>");
        out.println("</form>");

        out.println("</body></html>");

    } catch (NumberFormatException e) {
        resp.sendRedirect("/admin/currencies");
    }
}

    // === ГЛАВНАЯ АДМИНКИ ===
    private void showDashboard(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession();
        String login = (String) session.getAttribute("login");
        List<User> users = userDao.getAllUsers();

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>ExChange - Админ-панель</title>");
        out.println("<link rel='stylesheet' href='/css/style.css'>");
        out.println("</head><body>");
        out.println("<div class='container'>");

        out.println("<div class='header'>");
        out.println("<h1>📊 ExChange - Админ-панель</h1>");
        out.println("<div class='user-info'>👋 Вы вошли как: " + login + " (ADMIN)</div>");
        out.println("<nav>");
        out.println("<a href='/admin/dashboard'>🏠 Главная</a> | ");
        out.println("<a href='/admin/users'>👥 Пользователи</a> | ");
        out.println("<a href='/admin/currencies'>💵 Валюты</a> | ");
        out.println("<a href='/admin/operations'>📊 Операции</a> | ");
        out.println("<a href='/auth?logout=1'>🚪 Выйти</a>");
        out.println("</nav>");
        out.println("</div>");

        out.println("<div class='stats-grid'>");
        out.println("<div class='stat-card'><div class='stat-number'>" + users.size() + "</div><div class='stat-label'>Всего пользователей</div></div>");
        out.println("</div>");

        out.println("</div></body></html>");
    }

    // ==================== УПРАВЛЕНИЕ ВАЛЮТАМИ ====================
    /**
     * Показывает страницу управления валютами
     * Содержит:
     *   - форму для добавления новой валюты
     *   - таблицу со всеми валютами
     *   - кнопки редактирования и удаления
     */
    private void showCurrencies(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        CurrencyDao currencyDao = new CurrencyDao();
        List<Currency> currencies = currencyDao.getAllCurrencies();

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>ExChange - Управление валютами</title>");
        out.println("<link rel='stylesheet' href='/css/style.css'>");
        out.println("</head><body>");
        out.println("<div class='container'>");

        out.println("<div class='header'>");
        out.println("<h1>💵 Управление валютами</h1>");
        out.println("<nav>");
        out.println("<a href='/admin/dashboard'>🏠 Главная</a> | ");
        out.println("<a href='/admin/users'>👥 Пользователи</a> | ");
        out.println("<a href='/admin/currencies'>💵 Валюты</a> | ");
        out.println("<a href='/admin/operations'>📊 Операции</a> | ");
        out.println("<a href='/auth?logout=1'>🚪 Выйти</a>");
        out.println("</nav>");
        out.println("</div>");

        // Форма добавления валюты
        out.println("<div class='card'>");
        out.println("<h2>➕ Добавить новую валюту</h2>");
        out.println("<form method='post' action='/admin/currencies'>");
        out.println("<div class='form-group'><input type='text' name='code' placeholder='Код (USD)' required></div>");
        out.println("<div class='form-group'><input type='text' name='name' placeholder='Название' required></div>");
        out.println("<div class='form-group'><input type='number' name='rate' step='0.01' placeholder='Курс к RUB' required></div>");
        out.println("<button type='submit'>➕ Добавить валюту</button>");
        out.println("</form>");
        out.println("</div>");

        // Таблица валют
        out.println("<div class='card'>");
        out.println("<h2>📋 Список валют</h2>");
        out.println("<div class='table-container'>");
        out.println("<table>");
        out.println("<tr><th>ID</th><th>Код</th><th>Название</th><th>Курс</th><th>Действия</th></tr>");

        for (Currency c : currencies) {
            out.println("<tr>");
            out.println("<td>" + c.getId() + "</td>");
            out.println("<td>" + escapeHtml(c.getCode()) + "</td>");
            out.println("<td>" + escapeHtml(c.getName()) + "</td>");
            out.println("<td>" + c.getRate() + " ₽</td>");
            out.println("<td>");
            out.println("<a href='/admin/editCurrency?id=" + c.getId() + "' class='btn-edit'>✏️ Редактировать</a>");
            out.println("<a href='/admin/deleteCurrency?id=" + c.getId() +
                    "' onclick='return confirm(\"Удалить валюту?\")' class='btn-delete'>🗑️ Удалить</a>");
            out.println("</td>");
            out.println("</tr>");
        }
        out.println("</table>");
        out.println("</div>");
        out.println("</div>");

        out.println("</div></body></html>");
    }

    // === СПИСОК ПОЛЬЗОВАТЕЛЕЙ (CRUD) ===
    // === СПИСОК ПОЛЬЗОВАТЕЛЕЙ ===
    private void showUsers(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        List<User> users = userDao.getAllUsers();

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>ExChange - Пользователи</title>");
        out.println("<link rel='stylesheet' href='/css/style.css'>");
        out.println("</head><body>");
        out.println("<div class='container'>");

        out.println("<div class='header'>");
        out.println("<h1>👥 Управление пользователями</h1>");
        out.println("<nav>");
        out.println("<a href='/admin/dashboard'>🏠 Главная</a> | ");
        out.println("<a href='/admin/users'>👥 Пользователи</a> | ");
        out.println("<a href='/admin/currencies'>💵 Валюты</a> | ");
        out.println("<a href='/admin/operations'>📊 Операции</a> | ");
        out.println("<a href='/auth?logout=1'>🚪 Выйти</a>");
        out.println("</nav>");
        out.println("</div>");

        // Форма создания админа
        out.println("<div class='card'>");
        out.println("<h2>➕ Создать нового администратора</h2>");
        out.println("<p style='color: #666; font-size: 14px;'>Только для создания учётных записей администраторов</p>");
        out.println("<form method='post' action='/admin/addAdmin'>");
        out.println("<div class='form-group'><input type='text' name='login' placeholder='Логин' required></div>");
        out.println("<div class='form-group'><input type='password' name='password' placeholder='Пароль' required></div>");
        out.println("<div class='form-group'><input type='text' name='fullName' placeholder='Полное имя'></div>");
        out.println("<button type='submit' style='background: #ff9800;'>➕ Создать администратора</button>");
        out.println("</form>");
        out.println("</div>");

        // Таблица пользователей
        out.println("<div class='card'>");
        out.println("<h2>📋 Список пользователей</h2>");
        out.println("<div class='table-container'>");
        out.println("<table>");
        out.println("<tr><th>ID</th><th>Логин</th><th>Имя</th><th>Роль</th><th>Действия</th></tr>");

        for (User u : users) {
            out.println("<tr>");
            out.println("<td>" + u.getId() + "</td>");
            out.println("<td>" + escapeHtml(u.getLogin()) + "</td>");
            out.println("<td>" + (u.getFullName() != null ? escapeHtml(u.getFullName()) : "") + "</td>");
            out.println("<td>" + u.getRole() + "</td>");
            out.println("<td>");
            out.println("<a href='/admin/delete?id=" + u.getId() + "' onclick='return confirm(\"Удалить?\")' class='btn-delete'>🗑️ Удалить</a>");
            out.println("</td>");
            out.println("</tr>");
        }
        out.println("</table>");
        out.println("</div>");
        out.println("</div>");

        out.println("</div></body></html>");
    }

    // === УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ===
    private void deleteUser(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String idStr = req.getParameter("id");
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr);
                // Не даём удалить самого себя (админа с id=1)
                HttpSession session = req.getSession();
                User currentUser = (User) session.getAttribute("user");
                if (currentUser.getId() != id) {
                    userDao.deleteUser(id);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        resp.sendRedirect("/admin/users");
    }
// ==================== ДОБАВЛЕНИЕ АДМИНА ====================
    /**
     * Создаёт нового администратора (только для существующих админов)
     */
    private void addAdmin(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String login = req.getParameter("login");
        String password = req.getParameter("password");
        String fullName = req.getParameter("fullName");

        if (login != null && !login.trim().isEmpty()) {
            if (userDao.userExists(login)) {
                System.out.println("[ADMIN] Ошибка: пользователь " + login + " уже существует");
            } else {
                User newAdmin = new User();
                newAdmin.setLogin(login.trim());
                newAdmin.setPassword(password != null && !password.isEmpty() ? password : "admin123");
                newAdmin.setFullName(fullName != null ? fullName : "");
                newAdmin.setRole("ADMIN");  // ← роль ADMIN
                userDao.addUser(newAdmin);
                System.out.println("[ADMIN] Создан новый администратор: " + login);
            }
        }
        resp.sendRedirect("/admin/users");
    }
    // === ДОБАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ (POST) ===
    // ==================== POST-ЗАПРОСЫ ДЛЯ ВАЛЮТ ====================

    /**
     * Обрабатывает POST запросы:
     * - /admin/currencies - добавление новой валюты
     * - /admin/updateCurrency - обновление существующей
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        if (!isAdmin(req)) {
            resp.sendRedirect("/auth");
            return;
        }

        String path = req.getPathInfo();

        // Добавление новой валюты
        if (path == null || path.equals("/currencies")) {
            addCurrency(req, resp);
        }
        // Обновление валюты
        else if (path.equals("/updateCurrency")) {
            updateCurrency(req, resp);
        }
        // Добавление нового админа
        else if (path.equals("/addAdmin")) {
            addAdmin(req, resp);
        }
    }

    // ==================== ДОБАВЛЕНИЕ ВАЛЮТЫ ====================
    /**
     * Обрабатывает POST запрос на добавление новой валюты
     * Данные приходят из формы на странице /admin/currencies
     */
    private void addCurrency(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // Получаем параметры из формы
        String code = req.getParameter("code");      // код валюты (USD, EUR)
        String name = req.getParameter("name");      // название (Доллар США)
        String rateStr = req.getParameter("rate");   // курс (строкой, т.к. из формы)

        // Проверяем, что все поля заполнены
        if (code != null && name != null && rateStr != null && !code.isEmpty()) {
            try {
                // Преобразуем строку с курсом в число
                double rate = Double.parseDouble(rateStr);

                // Создаём объект валюты и сохраняем в БД
                Currency currency = new Currency(code.toUpperCase(), name, rate);
                CurrencyDao currencyDao = new CurrencyDao();
                currencyDao.addCurrency(currency);

                System.out.println("[ADMIN] Добавлена валюта: " + code);
            } catch (NumberFormatException e) {
                // Если курс ввели буквами или с ошибкой
                System.err.println("[ADMIN] Ошибка: неверный формат курса - " + rateStr);
            }
        }
        // Возвращаемся на страницу со списком валют
        resp.sendRedirect("/admin/currencies");
    }

    // ==================== ОБНОВЛЕНИЕ ВАЛЮТЫ ====================
    /**
     * Обрабатывает POST запрос на обновление существующей валюты
     * Данные приходят из формы редактирования
     */
    private void updateCurrency(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // Получаем параметры из формы
        String idStr = req.getParameter("id");       // ID валюты (скрытое поле)
        String code = req.getParameter("code");      // новый код
        String name = req.getParameter("name");      // новое название
        String rateStr = req.getParameter("rate");   // новый курс

        // Проверяем, что все поля есть
        if (idStr != null && code != null && name != null && rateStr != null) {
            try {
                // Преобразуем ID и курс в числа
                int id = Integer.parseInt(idStr);
                double rate = Double.parseDouble(rateStr);

                // Создаём объект с новыми данными и сохраняем
                Currency currency = new Currency(code.toUpperCase(), name, rate);
                currency.setId(id);   // ID важен, чтобы понять КАКУЮ валюту обновлять

                CurrencyDao currencyDao = new CurrencyDao();
                currencyDao.updateCurrency(currency);

                System.out.println("[ADMIN] Обновлена валюта ID: " + id);
            } catch (NumberFormatException e) {
                System.err.println("[ADMIN] Ошибка при обновлении валюты");
            }
        }
        // Возвращаемся на страницу со списком валют
        resp.sendRedirect("/admin/currencies");
    }
}