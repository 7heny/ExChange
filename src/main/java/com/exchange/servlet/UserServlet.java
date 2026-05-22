package com.exchange.servlet;

import com.exchange.dao.OperationDao;
import com.exchange.model.Operation;
import java.text.DecimalFormat;
import com.exchange.dao.CurrencyDao;
import com.exchange.model.Currency;
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
 * Сервлет для обычных пользователей
 * Показывает курсы валют и профиль
 */
public class UserServlet extends HttpServlet {

    private CurrencyDao currencyDao = new CurrencyDao();

    // ==================== ПРОВЕРКА АВТОРИЗАЦИИ ====================
    /**
     * Проверяет, авторизован ли пользователь
     */
    private boolean isLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute("user") != null;
    }

    // ==================== GET-ЗАПРОСЫ ====================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Если не авторизован - отправляем на страницу входа
        if (!isLoggedIn(req)) {
            resp.sendRedirect("/auth");
            return;
        }

        String path = req.getPathInfo();

        System.out.println("[DEBUG] UserServlet path: " + path);

        if (path == null || path.equals("/rates") || path.equals("/")) {
            showRates(req, resp);
        } else if (path.equals("/profile")) {
            showProfile(req, resp);
        } else if (path.equals("/exchange")) {
            showExchange(req, resp);
        } else {
            resp.sendError(404);
        }
    }

    // ==================== POST-ЗАПРОСЫ ====================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        if (!isLoggedIn(req)) {
            resp.sendRedirect("/auth");
            return;
        }

        String path = req.getPathInfo();

        if (path.equals("/doExchange")) {
            doExchange(req, resp);
        } else {
            resp.sendError(404);
        }
    }
    // ==================== РАСЧЁТ ОБМЕНА ВАЛЮТ ====================
    /**
     * Рассчитывает обмен из одной валюты в другую
     * Формула: количество * (курс_исходной / курс_целевой)
     *
     * @param fromCode код валюты, из которой меняем (USD, EUR)
     * @param toCode код валюты, в которую меняем
     * @param amount количество исходной валюты
     * @return строка с отформатированным результатом или null при ошибке
     */
    private String calculateExchange(String fromCode, String toCode, double amount) {
        List<Currency> currencies = currencyDao.getAllCurrencies();

        Currency fromCurrency = null;
        Currency toCurrency = null;

        // Находим валюты по коду
        for (Currency c : currencies) {
            if (c.getCode().equalsIgnoreCase(fromCode)) {
                fromCurrency = c;
            }
            if (c.getCode().equalsIgnoreCase(toCode)) {
                toCurrency = c;
            }
        }

        if (fromCurrency == null || toCurrency == null) {
            return null;
        }

        // Расчёт: amount * (курс_откуда / курс_куда)
        double result = amount * (fromCurrency.getRate() / toCurrency.getRate());

        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(result);
    }

    // ==================== ОБРАБОТКА ОБМЕНА ====================
    /**
     * Обрабатывает POST запрос на обмен валют
     * Рассчитывает результат и сохраняет в сессию
     */
    private void doExchange(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        HttpSession session = req.getSession();

        String amountStr = req.getParameter("amount");
        String fromCurrency = req.getParameter("fromCurrency");
        String toCurrency = req.getParameter("toCurrency");

        if (amountStr == null || fromCurrency == null || toCurrency == null) {
            session.setAttribute("exchangeResult", "❌ Ошибка: все поля обязательны");
            resp.sendRedirect("/user/exchange");
            return;
        }

        try {
            String normalizedAmount = amountStr.replace(',', '.');
            double amount = Double.parseDouble(normalizedAmount);

            if (amount <= 0) {
                session.setAttribute("exchangeResult", "❌ Ошибка: сумма должна быть больше 0");
                resp.sendRedirect("/user/exchange");
                return;
            }

            String resultStr = calculateExchange(fromCurrency, toCurrency, amount);

            if (resultStr == null) {
                session.setAttribute("exchangeResult", "❌ Ошибка: валюта не найдена");
                resp.sendRedirect("/user/exchange");
                return;
            }

            double result = Double.parseDouble(resultStr.replace(',', '.'));

            // Рассчитываем сумму в рублях (нормализованная стоимость)
            List<Currency> currencies = currencyDao.getAllCurrencies();
            double rubRate = 1.0;
            for (Currency c : currencies) {
                if (c.getCode().equals("RUB")) {
                    rubRate = c.getRate();
                    break;
                }
            }
            // Сумма в рублях = amount * курс_исходной_валюты
            double amountInRub = 0;
            for (Currency c : currencies) {
                if (c.getCode().equals(fromCurrency)) {
                    amountInRub = amount * c.getRate();
                    break;
                }
            }

            User currentUser = (User) session.getAttribute("user");
            OperationDao opDao = new OperationDao();
            Operation operation = new Operation(
                    currentUser.getId(),
                    currentUser.getLogin(),
                    fromCurrency,
                    toCurrency,
                    amount,
                    result,
                    amountInRub
            );
            opDao.addOperation(operation);

            session.setAttribute("exchangeResult",
                    "💰 " + amount + " " + fromCurrency.toUpperCase() + " = " + resultStr + " " + toCurrency.toUpperCase());

        } catch (NumberFormatException e) {
            session.setAttribute("exchangeResult", "❌ Ошибка: неверный формат суммы");
        }

        resp.sendRedirect("/user/exchange");
    }
    // ==================== СТРАНИЦА С КУРСАМИ ВАЛЮТ ====================
    /**
     * Показывает таблицу со всеми валютами и их курсами
     */
    private void showRates(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        HttpSession session = req.getSession();
        String login = (String) session.getAttribute("login");
        List<Currency> currencies = currencyDao.getAllCurrencies();

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>ExChange - Курсы валют</title>");
        out.println("<link rel='stylesheet' href='/css/style.css'>");
        out.println("</head><body>");
        out.println("<div class='container'>");

        // Шапка
        out.println("<div class='header'>");
        out.println("<h1>💱 ExChange - Курсы валют</h1>");
        out.println("<div class='user-info'>👋 Здравствуйте, " + login + "!</div>");
        out.println("<nav>");
        out.println("<a href='/user/rates'>📊 Курсы</a> | ");
        out.println("<a href='/user/profile'>👤 Профиль</a> | ");
        out.println("<a href='/user/exchange'>💱 Обмен</a> | ");
        out.println("<a href='/auth?logout=1'>🚪 Выйти</a>");
        out.println("</nav>");
        out.println("</div>");

        // Таблица курсов
        out.println("<div class='card'>");
        out.println("<h2>💰 Актуальные курсы</h2>");
        out.println("<div class='table-container'>");
        out.println("<table>");
        out.println("<tr><th>Код</th><th>Валюта</th><th>Курс (к RUB)</th></tr>");

        for (Currency c : currencies) {
            out.println("<tr>");
            out.println("<td><strong>" + c.getCode() + "</strong></td>");
            out.println("<td>" + c.getName() + "</td>");
            out.println("<td>" + c.getRate() + " ₽</td>");
            out.println("</tr>");
        }
        out.println("</table>");
        out.println("</div>");
        out.println("</div>");

        out.println("</div></body></html>");
    }

    // ==================== СТРАНИЦА ОБМЕНА ВАЛЮТ ====================
    /**
     * Показывает форму для обмена валют
     */
    private void showExchange(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        HttpSession session = req.getSession();
        String login = (String) session.getAttribute("login");
        List<Currency> currencies = currencyDao.getAllCurrencies();

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>ExChange - Обмен валют</title>");
        out.println("<link rel='stylesheet' href='/css/style.css'>");
        out.println("</head><body>");
        out.println("<div class='container'>");

        // Шапка
        out.println("<div class='header'>");
        out.println("<h1>💱 Обмен валют</h1>");
        out.println("<div class='user-info'>👋 Здравствуйте, " + login + "!</div>");
        out.println("<nav>");
        out.println("<a href='/user/rates'>📊 Курсы</a> | ");
        out.println("<a href='/user/profile'>👤 Профиль</a> | ");
        out.println("<a href='/user/exchange'>💱 Обмен</a> | ");
        out.println("<a href='/auth?logout=1'>🚪 Выйти</a>");
        out.println("</nav>");
        out.println("</div>");

        // Форма обмена
        out.println("<div class='card'>");
        out.println("<h2>💰 Конвертер валют</h2>");
        out.println("<form method='post' action='/user/doExchange'>");

        out.println("<div class='form-group'>");
        out.println("<label>💰 Сумма:</label>");
        out.println("<input type='number' name='amount' step='0.01' placeholder='Введите сумму' required>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<label>🔄 Из валюты:</label>");
        out.println("<select name='fromCurrency'>");
        for (Currency c : currencies) {
            out.println("<option value='" + c.getCode() + "'>" + c.getCode() + " - " + c.getName() + "</option>");
        }
        out.println("</select>");
        out.println("</div>");

        out.println("<div class='form-group'>");
        out.println("<label>🔄 В валюту:</label>");
        out.println("<select name='toCurrency'>");
        for (Currency c : currencies) {
            out.println("<option value='" + c.getCode() + "'>" + c.getCode() + " - " + c.getName() + "</option>");
        }
        out.println("</select>");
        out.println("</div>");

        out.println("<button type='submit'>💱 Рассчитать</button>");
        out.println("</form>");

        // Показываем результат
        String result = (String) session.getAttribute("exchangeResult");
        if (result != null) {
            out.println("<div class='success-message'>✅ " + result + "</div>");
            session.removeAttribute("exchangeResult");
        }

        out.println("</div>");
        out.println("</div></body></html>");
    }


    // ==================== СТРАНИЦА ПРОФИЛЯ ====================
    /**
     * Показывает информацию о текущем пользователе
     */
    private void showProfile(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        HttpSession session = req.getSession();
        User user = (User) session.getAttribute("user");

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html><head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<title>ExChange - Мой профиль</title>");
        out.println("<link rel='stylesheet' href='/css/style.css'>");
        out.println("</head><body>");
        out.println("<div class='container'>");

        // Шапка
        out.println("<div class='header'>");
        out.println("<h1>👤 Мой профиль</h1>");
        out.println("<div class='user-info'>👋 Здравствуйте, " + user.getLogin() + "!</div>");
        out.println("<nav>");
        out.println("<a href='/user/rates'>📊 Курсы</a> | ");
        out.println("<a href='/user/profile'>👤 Профиль</a> | ");
        out.println("<a href='/user/exchange'>💱 Обмен</a> | ");
        out.println("<a href='/auth?logout=1'>🚪 Выйти</a>");
        out.println("</nav>");
        out.println("</div>");

        // Данные профиля
        out.println("<div class='card'>");
        out.println("<h2>Личная информация</h2>");
        out.println("<div class='form-group'><label>ID:</label> <span>" + user.getId() + "</span></div>");
        out.println("<div class='form-group'><label>Логин:</label> <span>" + user.getLogin() + "</span></div>");
        out.println("<div class='form-group'><label>Полное имя:</label> <span>" +
                (user.getFullName() != null && !user.getFullName().isEmpty() ? user.getFullName() : "Не указано") + "</span></div>");
        out.println("<div class='form-group'><label>Роль:</label> <span>" + user.getRole() + "</span></div>");
        out.println("</div>");

        out.println("</div></body></html>");
    }
}