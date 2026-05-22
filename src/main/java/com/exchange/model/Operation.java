package com.exchange.model;

import java.sql.Timestamp;

/**
 * Сущность "Операция обмена"
 * Сохраняет историю всех обменов валют пользователями
 */
public class Operation {

    // === ПОЛЯ ===
    private int id;                 // уникальный ID операции
    private int userId;            // ID пользователя (внешний ключ)
    private String userLogin;      // логин пользователя (для быстрого отображения)
    private String fromCurrency;   // из какой валюты меняли (USD, EUR)
    private String toCurrency;     // в какую валюту меняли
    private double amount;         // сколько отдали
    private double result;         // сколько получили
    private double amountRub;      // сумма в рублях
    private Timestamp operationDate; // дата и время операции

    // === КОНСТРУКТОРЫ ===
    public Operation() {}

    public Operation(int userId, String userLogin, String fromCurrency,
                     String toCurrency, double amount, double result) {
        this.userId = userId;
        this.userLogin = userLogin;
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.amount = amount;
        this.result = result;
    }

    // === ГЕТТЕРЫ И СЕТТЕРЫ ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserLogin() { return userLogin; }
    public void setUserLogin(String userLogin) { this.userLogin = userLogin; }

    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }

    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getResult() { return result; }
    public void setResult(double result) { this.result = result; }

    public Timestamp getOperationDate() { return operationDate; }
    public void setOperationDate(Timestamp operationDate) { this.operationDate = operationDate; }
}