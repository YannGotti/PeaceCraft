package ru.peacecraft.core.currencies.model;

public final class Currency {

    private final String id;
    private final String name;
    private final String displayName;
    private final String symbol;
    private final int decimalPlaces;

    public Currency(String id, String name, String displayName, String symbol, int decimalPlaces) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.symbol = symbol;
        this.decimalPlaces = decimalPlaces;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public String format(double amount) {
        if (decimalPlaces == 0) {
            return String.format("%d %s", (int) amount, symbol);
        }
        return String.format("%." + decimalPlaces + "f %s", amount, symbol);
    }
}
