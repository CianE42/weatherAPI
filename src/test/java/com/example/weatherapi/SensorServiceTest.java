package com.example.weatherapi;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SensorServiceTest {

    private SensorDataRepository repository;
    private MongoTemplate mongoTemplate;
    private SensorService service;

    @BeforeEach
    void setUp() {
        repository = mock(SensorDataRepository.class);
        mongoTemplate = mock(MongoTemplate.class);
        service = new SensorService(repository, mongoTemplate);
    }

    @Test
    void saveSensorData_mapsAndPersists() {
        SensorDataRequest req = new SensorDataRequest();
        req.setSensorId("1");
        req.setMetric("temperature");
        req.setValue(23.4);
        req.setTimestamp(Instant.parse("2025-08-20T10:00:00Z"));

        SensorData saved = new SensorData();
        saved.setId("abc123");
        saved.setSensorId("1");
        saved.setMetric("temperature");
        saved.setValue(23.4);
        saved.setTimestamp(Instant.parse("2025-08-20T10:00:00Z"));

        when(repository.save(any(SensorData.class))).thenReturn(saved);

        SensorData out = service.saveSensorData(req);

        ArgumentCaptor<SensorData> captor = ArgumentCaptor.forClass(SensorData.class);
        verify(repository).save(captor.capture());
        SensorData written = captor.getValue();

        assertEquals("1", written.getSensorId());
        assertEquals("temperature", written.getMetric());
        assertEquals(23.4, written.getValue());
        assertEquals(Instant.parse("2025-08-20T10:00:00Z"), written.getTimestamp());
        assertEquals("abc123", out.getId());
    }

    @Test
    void queryData_returnsAggValuesFromMongoTemplate() {
        // Mock Mongo aggregation results
        List<Document> docs = Arrays.asList(
            new Document("metric", "temperature").append("value", 22.0),
            new Document("metric", "humidity").append("value", 60.0)
        );
        AggregationResults<Document> results = TestAggResults.docs(docs);

        // Use typed matcher for Aggregation, and the same collection name your service uses
        // If you added a collection constant in SensorService, prefer that:
        // when(mongoTemplate.aggregate(any(Aggregation.class), eq(SensorService.COLLECTION), eq(Document.class)))
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("sensor_data"), eq(Document.class)))
            .thenReturn(results);

        Instant from = Instant.parse("2025-08-01T00:00:00Z");
        Instant to   = Instant.parse("2025-08-03T23:59:59Z");

        QueryResult qr = service.queryData(
            List.of("1"),
            List.of("temperature", "humidity"),
            Statistic.AVG,
            from, to
        );

        assertEquals(List.of("1"), qr.getSensorIds());
        assertEquals(List.of("temperature", "humidity"), qr.getMetrics());
        assertEquals("avg", qr.getStatistic());
        assertEquals(22.0, qr.getResultsByMetric().get("temperature"));
        assertEquals(60.0, qr.getResultsByMetric().get("humidity"));

        verify(mongoTemplate, times(1))
            .aggregate(any(Aggregation.class), eq("sensor_data"), eq(Document.class));
        verifyNoInteractions(repository);
    }

    @Test
    void queryData_throwsWhenRangeTooShort() {
        Instant from = Instant.parse("2025-08-01T00:00:00Z");
        Instant to   = from.plus(Duration.ofHours(12)); // < 1 day

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.queryData(List.of("1"), List.of("temperature"), Statistic.MAX, from, to)
        );

        assertTrue(ex.getMessage().contains("Date range must be between 1 and 31 days"));
        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void queryData_throwsWhenRangeTooLong() {
        Instant from = Instant.parse("2025-08-01T00:00:00Z");
        Instant to   = from.plus(Duration.ofDays(32)); // > 31 days

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.queryData(List.of("1"), List.of("temperature"), Statistic.SUM, from, to)
        );

        assertTrue(ex.getMessage().contains("Date range must be between 1 and 31 days"));
        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void saveSensorData_normalizesMetricBeforePersist() {
        SensorDataRequest req = new SensorDataRequest();
        req.setSensorId("9");
        req.setMetric("Wind-Speed"); // mixed/hyphen
        req.setValue(12.3);
        req.setTimestamp(Instant.parse("2025-08-20T10:00:00Z"));

        when(repository.save(any(SensorData.class))).thenAnswer(inv -> inv.getArgument(0));

        SensorData out = service.saveSensorData(req);

        ArgumentCaptor<SensorData> captor = ArgumentCaptor.forClass(SensorData.class);
        verify(repository).save(captor.capture());
        SensorData written = captor.getValue();

        // Should be normalized to "wind_speed"
        assertEquals("wind_speed", written.getMetric());
        assertEquals("9", written.getSensorId());
        assertEquals(12.3, written.getValue());
        assertEquals(Instant.parse("2025-08-20T10:00:00Z"), written.getTimestamp());

        // Return value is the same entity (since we echoed in the stub)
        assertEquals("wind_speed", out.getMetric());
    }

}
