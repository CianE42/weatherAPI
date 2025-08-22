package com.example.weatherapi;

import java.util.Locale;

public enum Metric {
    TEMPERATURE,
    HUMIDITY,
    WIND_SPEED;

    /**
     * Case- and punctuation-insensitive parser.
     * Accepts: "temperature", "Temperature", "wind-speed", "windSpeed", "WIND_SPEED", etc.
     */
    public static Metric from(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Metric is required");
        }
        String norm = raw
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace("-", "_");
        switch (norm) {
            case "temperature": return TEMPERATURE;
            case "humidity":    return HUMIDITY;
            case "wind_speed":
            case "windspeed":   return WIND_SPEED;
            default:
                throw new IllegalArgumentException(
                    "Invalid metric: " + raw + ". Allowed: temperature, humidity, wind_speed");
        }
    }

    /** How we persist/query in Mongo (lowercase). */
    public String dbValue() {
        switch (this) {
            case TEMPERATURE: return "temperature";
            case HUMIDITY:    return "humidity";
            case WIND_SPEED:  return "wind_speed";
            default: throw new IllegalStateException("Unexpected metric " + this);
        }
    }
}
