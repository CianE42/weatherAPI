package com.example.weatherapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetricTest {

    @Test
    void from_acceptsCommonFormats_caseAndPunctuationInsensitive() {
        assertEquals(Metric.TEMPERATURE, Metric.from("temperature"));
        assertEquals(Metric.TEMPERATURE, Metric.from("Temperature"));
        assertEquals(Metric.HUMIDITY,    Metric.from("HUMIDITY"));
        assertEquals(Metric.WIND_SPEED,  Metric.from("wind-speed"));
        assertEquals(Metric.WIND_SPEED,  Metric.from("wind_speed"));
        assertEquals(Metric.WIND_SPEED,  Metric.from("windSpeed"));
        assertEquals(Metric.WIND_SPEED,  Metric.from("WindSpeed"));
        assertEquals(Metric.WIND_SPEED,  Metric.from("windspeed"));
    }

    @Test
    void dbValue_isNormalizedLowercase() {
        assertEquals("temperature", Metric.TEMPERATURE.dbValue());
        assertEquals("humidity",    Metric.HUMIDITY.dbValue());
        assertEquals("wind_speed",  Metric.WIND_SPEED.dbValue());
    }

    @Test
    void from_throwsForUnknownMetric() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> Metric.from("humidty") // typo
        );
        assertTrue(ex.getMessage().toLowerCase().contains("invalid metric"));
    }

    @Test
    void from_throwsForBlank() {
        assertThrows(IllegalArgumentException.class, () -> Metric.from("  "));
        assertThrows(IllegalArgumentException.class, () -> Metric.from(null));
    }
}
