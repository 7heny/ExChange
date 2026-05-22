package com.exchange.model;

/**
 * Сущность "Валюта"
 * Соответствует строке в таблице currencies
 */
public class Currency {

    // === ПОЛЯ ===
    private int id;           // уникальный идентификатор
    private String code;      // код валюты (USD, EUR, RUB)
    private String name;      // полное название (Доллар США, Евро)
    private double rate;      // курс к базовой валюте (например к рублю)

    // === КОНСТРУКТОРЫ ===
    public Currency() {}

    public Currency(String code, String name, double rate) {
        this.code = code;
        this.name = name;
        this.rate = rate;
    }

    // === ГЕТТЕРЫ И СЕТТЕРЫ ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }

    @Override
    public String toString() {
        return "Currency{id=" + id + ", code='" + code + "', rate=" + rate + "}";
    }
}