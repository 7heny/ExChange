package com.exchange.servlet;

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

        if (path == null || path.equals("/rates") || path.equals("/")) {
            showRates(req, resp);      // страница с курсами валют
        } else {
            resp.sendError(404);
        }
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
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }");
        out.println(".container { max-width: 800px; margin: 0 auto; }");
        out.println(".header { background: #333; color: white; padding: 15px; border-radius: 10px; margin-bottom: 20px; }");
        out.println(".header a { color: white; text-decoration: none; margin-left: 20px; }");
        out.println("table { width: 100%; border-collapse: collapse; background: white; border-radius: 10px; overflow: hidden; }");
        out.println("th { background: #4CAF50; color: white; padding: 12px; }");
        out.println("td { padding: 10px; border-bottom: 1px solid #ddd; text-align: center; }");
        out.println("tr:hover { background: #f1f1f1; }");
        out.println(".rate { font-weight: bold; color: #2196F3; }");
        out.println("</style>");
        out.println("</head><body>");
        out.println("<div class='container'>");

        // Шапка
        out.println("<div class='header'>");
        out.println("<h1>💱 ExChange - Курсы валют</h1>");
        out.println("<p>👋 Здравствуйте, " + login + "!</p>");
        out.println("<a href='/user/rates'>📊 Курсы</a> | ");
        out.println("<a href='/user/profile'>🧟‍♂️ Профиль</a> | ");
        out.println("<a href='/auth?logout=1'>👇 Выйти</a>");
        out.println("</div>");

        // Таблица курсов
        out.println("<h2>🪙 Актуальные курсы</h2>");
        out.println("<table border='1'>");
        out.println("<tr><th>Код</th><th>Валюта</th><th>Курс (к RUB)</th></tr>");

        for (Currency c : currencies) {
            out.println("<tr>");
            out.println("<td><strong>" + c.getCode() + "</strong></td>");
            out.println("<td>" + c.getName() + "</td>");
            out.println("<td class='rate'>" + c.getRate() + " ₽</td>");
            out.println("</tr>");
        }
        out.println("</table>");

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
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }");
        out.println(".container { max-width: 600px; margin: 0 auto; }");
        out.println(".header { background: #333; color: white; padding: 15px; border-radius: 10px; margin-bottom: 20px; }");
        out.println(".header a { color: white; text-decoration: none; margin-left: 20px; }");
        out.println(".card { background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
        out.println(".info-row { margin: 15px 0; padding: 10px; border-bottom: 1px solid #eee; }");
        out.println(".label { font-weight: bold; color: #555; }");
        out.println("</style>");
        out.println("</head><body>");
        out.println("<div class='container'>");

        // Шапка
        out.println("<div class='header'>");
        out.println("<h1>🧟‍♂️ Мой профиль</h1>");
        out.println("<a href='/user/rates'>📊 Курсы</a> | ");
        out.println("<a href='/user/profile'>🧟‍♂️ Профиль</a> | ");
        out.println("<a href='/auth?logout=1'>👇 Выйти</a>");
        out.println("</div>");

        // Данные профиля
        out.println("<div class='card'>");
        out.println("<h2>Личная информация</h2>");
        out.println("<div class='info-row'><span class='label'>ID:</span> " + user.getId() + "</div>");
        out.println("<div class='info-row'><span class='label'>Логин:</span> " + user.getLogin() + "</div>");
        out.println("<div class='info-row'><span class='label'>Полное имя:</span> " +
                (user.getFullName() != null && !user.getFullName().isEmpty() ? user.getFullName() : "Не указано") + "</div>");
        out.println("<div class='info-row'><span class='label'>Роль:</span> " + user.getRole() + "</div>");
        out.println("</div>");

        out.println("</div></body></html>");
    }
}