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

        // Пока только заглушка
        resp.setContentType("text/html; charset=UTF-8");
        resp.getWriter().println("<h1>User panel works!</h1>");
        resp.getWriter().println("<p>Path: " + path + "</p>");
        resp.getWriter().println("<a href='/auth?logout=1'>Выйти</a>");
    }
}