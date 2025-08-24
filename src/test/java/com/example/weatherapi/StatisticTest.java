package com.example.weatherapi;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Statistic enum parsing and validation.
 */
class StatisticTest {

    @Test
    void from_parsesValidValues_caseInsensitive() {
        // Accepts valid stats regardless of case, supports "average" alias
        assertEquals(Statistic.MIN, Statistic.from("min"));
        assertEquals(Statistic.MAX, Statistic.from("MAX"));
        assertEquals(Statistic.SUM, Statistic.from("Sum"));
        assertEquals(Statistic.AVG, Statistic.from("avg"));
        assertEquals(Statistic.AVG, Statistic.from("average"));
    }

    @Test
    void from_defaultsToAvgWhenNull() {
        // Null input defaults to AVG
        assertEquals(Statistic.AVG, Statistic.from(null));
    }

    @Test
    void from_throwsOnInvalid() {
        // Invalid input exception with helpful message
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> Statistic.from("median")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("invalid statistic"));
    }
}
