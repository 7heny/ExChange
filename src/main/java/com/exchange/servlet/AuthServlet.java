package com.exchange.servlet;

import com.exchange.dao.UserDao;
import com.exchange.model.User;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Сервлет для входа и выхода из системы
 * URL: /auth (GET - показать форму, POST - обработать логин)
 */
public class AuthServlet extends HttpServlet {

    private UserDao userDao = new UserDao();

    // ==================== ПОКАЗАТЬ СТРАНИЦУ ЛОГИНА ====================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Если есть параметр logout — удаляем сессию
        if (req.getParameter("logout") != null) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();  // удаляем сессию
            }
            resp.sendRedirect("/auth");
            return;
        }

        // Показываем страницу login.jsp
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    // ==================== ОБРАБОТАТЬ ФОРМУ ЛОГИНА ====================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Берём логин и пароль из формы
        String login = req.getParameter("login");
        String password = req.getParameter("password");

        // Проверяем, что поля не пустые
        if (login == null || password == null || login.isEmpty() || password.isEmpty()) {
            req.setAttribute("error", "Заполните все поля");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        // Ищем пользователя в БД по логину
        User user = userDao.getUserByLogin(login);

        // Проверяем пароль
        if (user != null && user.getPassword().equals(password)) {
            // Вход разрешён — создаём сессию
            HttpSession session = req.getSession();
            session.setAttribute("user", user);        // сохраняем пользователя
            session.setAttribute("login", user.getLogin());
            session.setAttribute("role", user.getRole());

            // Если админ → в админку, если обычный пользователь → на главную
            if ("ADMIN".equals(user.getRole())) {
                resp.sendRedirect("/admin/dashboard");
            } else {
                resp.sendRedirect("/");
            }
        } else {
            // Неверный логин или пароль
            req.setAttribute("error", "Неверный логин или пароль");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }
}