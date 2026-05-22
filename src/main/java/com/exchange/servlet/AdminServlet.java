package com.exchange.servlet;

import com.exchange.dao.UserDao;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Админ-панель для управления пользователями
 * Доступ только для роли ADMIN
 */
public class AdminServlet extends HttpServlet {

    private UserDao userDao = new UserDao();

    // === ПРОВЕРКА ПРАВ ===
    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        String role = (String) session.getAttribute("role");
        return "ADMIN".equals(role);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // Если не админ — на страницу входа
        if (!isAdmin(req)) {
            resp.sendRedirect("/auth");
            return;
        }

        // Пока просто заглушка
        resp.getWriter().println("Admin panel works!");
    }
}