package com.exchange.servlet;

import com.exchange.dao.UserDao;
import com.exchange.model.User;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * Сервлет для регистрации новых пользователей
 * URL: /register
 */
public class RegisterServlet extends HttpServlet {

    private UserDao userDao = new UserDao();

    /**
     * Показывает страницу регистрации
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendRedirect("/register.html");
    }

    /**
     * Обрабатывает форму регистрации
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        // Получаем данные из формы
        String login = req.getParameter("login");
        String password = req.getParameter("password");
        String fullName = req.getParameter("fullName");

        // Проверяем, что логин и пароль не пустые
        if (login == null || login.trim().isEmpty()) {
            String error = URLEncoder.encode("Логин не может быть пустым", "UTF-8");
            resp.sendRedirect("/register.html?error=" + error);
            return;
        }

        if (password == null || password.isEmpty()) {
            String error = URLEncoder.encode("Пароль не может быть пустым", "UTF-8");
            resp.sendRedirect("/register.html?error=" + error);
            return;
        }

        // Проверяем, существует ли уже такой пользователь
        if (userDao.userExists(login)) {
            String error = URLEncoder.encode("Пользователь с таким логином уже существует", "UTF-8");
            resp.sendRedirect("/register.html?error=" + error);
            return;
        }

        // Создаём нового пользователя (по умолчанию роль USER)
        User newUser = new User();
        newUser.setLogin(login.trim());
        newUser.setPassword(password);
        newUser.setFullName(fullName != null ? fullName : "");
        newUser.setRole("USER");

        userDao.addUser(newUser);

        String success = URLEncoder.encode("Регистрация успешна! Теперь войдите в систему", "UTF-8");
        resp.sendRedirect("/auth?success=" + success);
    }
}