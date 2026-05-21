package com.exchange.model;

/**
 * Сущность пользователя (POJO)
 * Соответствует строке в таблице users
 */
public class User {

    // === ПОЛЯ (соответствуют колонкам в БД) ===
    private int id;           // уникальный идентификатор
    private String login;     // логин для входа (уникальный)
    private String password;  // пароль (пока храним как есть, позже захешируем)
    private String fullName;  // полное имя пользователя
    private String role;      // USER или ADMIN

    // === КОНСТРУКТОРЫ ===

    // Пустой конструктор нужен для JDBC (он создаёт объект, потом заполняет поля)
    public User() {}

    // Конструктор для быстрого создания нового пользователя
    public User(String login, String password, String fullName, String role) {
        this.login = login;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // === ГЕТТЕРЫ И СЕТТЕРЫ ===
    // Нужны для доступа к приватным полям из других классов

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // === ДЛЯ ОТЛАДКИ ===
    // Выводит краткую информацию о пользователе (пароль не показывается)
    @Override
    public String toString() {
        return "User{id=" + id + ", login='" + login + "', role='" + role + "'}";
    }
}