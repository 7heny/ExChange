package com.exchange.servlet;

import com.exchange.dao.UserDao;
import com.exchange.model.User;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthServlet extends HttpServlet {

    private UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (req.getParameter("logout") != null) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect("/login.html");
            return;
        }

        resp.sendRedirect("/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String login = req.getParameter("login");
        String password = req.getParameter("password");

        if (login == null || password == null || login.isEmpty() || password.isEmpty()) {
            resp.sendRedirect("/login.html?error=" + java.net.URLEncoder.encode("Заполните все поля", "UTF-8"));
            return;
        }

        User user = userDao.getUserByLogin(login);

        if (user != null && user.getPassword().equals(password)) {
            HttpSession session = req.getSession();
            session.setAttribute("user", user);
            session.setAttribute("login", user.getLogin());
            session.setAttribute("role", user.getRole());

            if ("ADMIN".equals(user.getRole())) {
                resp.sendRedirect("/admin/dashboard");
            } else {
                resp.sendRedirect("/");
            }
        } else {
            resp.sendRedirect("/login.html?error=" + java.net.URLEncoder.encode("Неверный логин или пароль", "UTF-8"));
        }
    }
}