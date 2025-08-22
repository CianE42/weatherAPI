package com.example.weatherapi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatisticTest {

    @Test
    void from_parsesValidValues_caseInsensitive() {
        assertEquals(Statistic.MIN, Statistic.from("min"));
        assertEquals(Statistic.MAX, Statistic.from("MAX"));
        assertEquals(Statistic.SUM, Statistic.from("Sum"));
        assertEquals(Statistic.AVG, Statistic.from("avg"));
        assertEquals(Statistic.AVG, Statistic.from("average")); // alias
    }

    @Test
    void from_defaultsToAvgWhenNull() {
        assertEquals(Statistic.AVG, Statistic.from(null));
    }

    @Test
    void from_throwsOnInvalid() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> Statistic.from("median")
        );
        assertTrue(ex.getMessage().toLowerCase().contains("invalid statistic"));
    }
}
