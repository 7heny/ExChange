package com.exchange.servlet;

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
        } else {
            resp.sendError(404);
        }
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

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'><title>Admin Panel</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; margin: 20px; }");
        out.println("nav a { margin-right: 15px; }");
        out.println("table { border-collapse: collapse; width: 100%; }");
        out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        out.println("th { background-color: #4CAF50; color: white; }");
        out.println(".delete { color: red; cursor: pointer; }");
        out.println("</style></head><body>");

        out.println("<h1>Админ-панель ExChange</h1>");
        out.println("<p>Вы вошли как: <b>" + login + "</b> (ADMIN)</p>");
        out.println("<nav>");
        out.println("<a href='/admin/dashboard'>🏕️ Главная</a> | ");
        out.println("<a href='/admin/users'>🧟‍♂️ Пользователи</a> | ");
        out.println("<a href='/admin/currencies'>💵 Валюты</a> | ");
        out.println("<a href='/auth?logout=1'>👇 Выйти</a>");
        out.println("</nav><br>");

        // Статистика
        List<User> users = userDao.getAllUsers();
        out.println("<h2>Статистика</h2>");
        out.println("<p>Всего пользователей: " + users.size() + "</p>");

        out.println("</body></html>");
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
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; }");
        out.println("nav { margin: 20px 0; padding: 10px; background: #333; color: white; border-radius: 5px; }");
        out.println("nav a { margin-right: 20px; color: white; text-decoration: none; }");
        out.println("table { border-collapse: collapse; width: 100%; margin-top: 20px; }");
        out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        out.println("th { background-color: #2196F3; color: white; }");
        out.println("tr:nth-child(even) { background-color: #f9f9f9; }");
        out.println(".edit-btn { color: blue; text-decoration: none; margin-right: 10px; }");
        out.println(".delete-btn { color: red; text-decoration: none; }");
        out.println("input, select { padding: 5px; margin: 5px; }");
        out.println("button { background: #2196F3; color: white; border: none; padding: 6px 12px; cursor: pointer; }");
        out.println("</style>");
        out.println("</head><body>");

        // Заголовок и навигация
        out.println("<h1>💵 Управление валютами</h1>");
        out.println("<nav>");
        out.println("<a href='/admin/dashboard'>👈 Назад к главной</a> | ");
        out.println("<a href='/admin/users'>🧟‍♀️ Пользователи</a> | ");
        out.println("<a href='/admin/currencies'>💵 Валюты</a> | ");
        out.println("<a href='/auth?logout=1'>Выйти</a>");
        out.println("</nav>");

        // ==================== ФОРМА ДОБАВЛЕНИЯ ====================
        out.println("<h2>➕ Добавить новую валюту</h2>");
        out.println("<form method='post' action='/admin/currencies'>");
        out.println("<label>Код:</label>");
        out.println("<input type='text' name='code' required placeholder='USD' size='5'>");
        out.println("<label>Название:</label>");
        out.println("<input type='text' name='name' required placeholder='Доллар США' size='20'>");
        out.println("<label>Курс:</label>");
        out.println("<input type='number' name='rate' step='0.01' required placeholder='95.50'>");
        out.println("<button type='submit'>Добавить валюту</button>");
        out.println("</form>");

        // ==================== ТАБЛИЦА ВАЛЮТ ====================
        out.println("<h2>📋 Список валют</h2>");
        out.println("<table border='1'>");
        out.println("<tr>");
        out.println("<th>ID</th>");
        out.println("<th>Код</th>");
        out.println("<th>Название</th>");
        out.println("<th>Курс</th>");
        out.println("<th>Действия</th>");
        out.println("</tr>");

        for (Currency c : currencies) {
            out.println("<tr>");
            out.println("<td>" + c.getId() + "</td>");
            out.println("<td>" + escapeHtml(c.getCode()) + "</td>");
            out.println("<td>" + escapeHtml(c.getName()) + "</td>");
            out.println("<td>" + c.getRate() + "</td>");
            out.println("<td>");
            out.println("<a href='/admin/editCurrency?id=" + c.getId() + "' class='edit-btn'>✏️ Редактировать</a>");
            out.println("<a href='/admin/deleteCurrency?id=" + c.getId() +
                    "' onclick='return confirm(\"Удалить валюту " + escapeHtml(c.getCode()) + "?\")' class='delete-btn'>🗑️ Удалить</a>");
            out.println("</td>");
            out.println("</tr>");
        }
        out.println("</table>");

        out.println("</body></html>");
    }

    // === СПИСОК ПОЛЬЗОВАТЕЛЕЙ (CRUD) ===
    private void showUsers(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        List<User> users = userDao.getAllUsers();

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html>");
        out.println("<html><head><meta charset='UTF-8'><title>Пользователи</title>");
        out.println("<style>");
        out.println("body { font-family: Arial; margin: 20px; }");
        out.println("nav a { margin-right: 15px; }");
        out.println("table { border-collapse: collapse; width: 100%; }");
        out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        out.println("th { background-color: #4CAF50; color: white; }");
        out.println(".delete-btn { color: red; text-decoration: none; }");
        out.println(".add-btn { background: green; color: white; padding: 5px 10px; text-decoration: none; }");
        out.println("</style></head><body>");

        out.println("<h1>Управление пользователями</h1>");
        out.println("<a href='/admin/dashboard'>Назад</a> | ");
        out.println("<a href='/auth?logout=1'>Выйти</a>");
        out.println("<hr>");

        // Таблица пользователей
        out.println("<h2>Список пользователей</h2>");
        out.println("<table>");
        out.println("<tr><th>ID</th><th>Логин</th><th>Имя</th><th>Роль</th><th>Действия</th></tr>");

        for (User u : users) {
            out.println("<tr>");
            out.println("<td>" + u.getId() + "</td>");
            out.println("<td>" + u.getLogin() + "</td>");
            out.println("<td>" + (u.getFullName() != null ? u.getFullName() : "") + "</td>");
            out.println("<td>" + u.getRole() + "</td>");
            out.println("<td>");
            out.println("<a href='/admin/delete?id=" + u.getId() + "' onclick='return confirm(\"Удалить?\")' class='delete-btn'>Удалить</a>");
            out.println("</td>");
            out.println("</tr>");
        }
        out.println("</table>");

        out.println("</body></html>");
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