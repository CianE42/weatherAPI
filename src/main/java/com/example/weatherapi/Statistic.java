package com.example.weatherapi;

public enum Statistic {
    MIN, MAX, SUM, AVG;

    public static Statistic from(String raw) {
        if (raw == null) return AVG;
        switch (raw.trim().toLowerCase()) {
            case "min": return MIN;
            case "max": return MAX;
            case "sum": return SUM;
            case "avg":
            case "average": return AVG;
            default: throw new IllegalArgumentException(
                "Invalid statistic: " + raw + ". Must be one of: min, max, sum, avg");
        }
    }
}
