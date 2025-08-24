package com.example.weatherapi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Metric enum validation & normalization.
 */
class MetricTest {

    @Test
    void from_acceptsCommonFormats_caseAndPunctuationInsensitive() {
        // Accepts multiple case/punctuation variants of valid metrics
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
        // Internal dbValue() is always lowercase + underscored
        assertEquals("temperature", Metric.TEMPERATURE.dbValue());
        assertEquals("humidity",    Metric.HUMIDITY.dbValue());
        assertEquals("wind_speed",  Metric.WIND_SPEED.dbValue());
    }

    @Test
    void from_throwsForUnknownMetric() {
        // Invalid input exception with helpful message
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class, () -> Metric.from("humidty") // typo
        );
        assertTrue(ex.getMessage().toLowerCase().contains("invalid metric"));
    }

    @Test
    void from_throwsForBlank() {
        // Blank or null input exception
        assertThrows(IllegalArgumentException.class, () -> Metric.from("  "));
        assertThrows(IllegalArgumentException.class, () -> Metric.from(null));
    }
}
