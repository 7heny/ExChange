package com.exchange.servlet;

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Проверка прав
        if (!isAdmin(req)) {
            resp.sendRedirect("/auth");
            return;
        }

        // Получаем путь: /admin/dashboard, /admin/users, /admin/delete?id=1
        String path = req.getPathInfo();

        if (path == null || path.equals("/dashboard") || path.equals("/")) {
            showDashboard(req, resp);
        } else if (path.equals("/users")) {
            showUsers(req, resp);
        } else if (path.equals("/delete")) {
            deleteUser(req, resp);
        } else {
            resp.sendError(404);
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
        out.println("<a href='/admin/users'>🧟‍♂️🧟‍♀️ Пользователи</a> | ");
        out.println("<a href='/admin/currencies'>💵 Валюты</a> | ");
        out.println("<a href='/auth?logout=1'>👇 Выйти</a>");
        out.println("</nav><br>");

        // Статистика
        List<User> users = userDao.getAllUsers();
        out.println("<h2>Статистика</h2>");
        out.println("<p>Всего пользователей: " + users.size() + "</p>");

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

        // Форма добавления нового пользователя
        out.println("<h2>Добавить пользователя</h2>");
        out.println("<form method='post' action='/admin/users'>");
        out.println("Логин: <input type='text' name='login' required>");
        out.println("Пароль: <input type='password' name='password' required>");
        out.println("Имя: <input type='text' name='fullName'>");
        out.println("Роль: <select name='role'>");
        out.println("<option value='USER'>USER</option>");
        out.println("<option value='ADMIN'>ADMIN</option>");
        out.println("</select>");
        out.println("<button type='submit'>Добавить</button>");
        out.println("</form><br>");

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
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isAdmin(req)) {
            resp.sendRedirect("/auth");
            return;
        }

        String login = req.getParameter("login");
        String password = req.getParameter("password");
        String fullName = req.getParameter("fullName");
        String role = req.getParameter("role");

        if (login != null && password != null && !login.isEmpty()) {
            User newUser = new User();
            newUser.setLogin(login);
            newUser.setPassword(password);
            newUser.setFullName(fullName != null ? fullName : "");
            newUser.setRole(role != null ? role : "USER");
            userDao.addUser(newUser);
        }

        resp.sendRedirect("/admin/users");
    }
}