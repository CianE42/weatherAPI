package com.example.weatherapi;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration tests:
 * - Runs Spring Boot app with Testcontainers MongoDB
 * - Seeds test data before each test
 * - Calls /sensors/query endpoints via MockMvc
 * - Asserts on full JSON response
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SensorControllerTest {

  // Temporary MongoDB container
  @Container
  @ServiceConnection 
  static final MongoDBContainer mongo = new MongoDBContainer("mongo:7");

  @Autowired MockMvc mockMvc;
  @Autowired SensorDataRepository repository;

  @BeforeEach
  void seed() {
    // Reset and seed small dataset for each test
    repository.deleteAll();
    repository.save(doc("1", "temperature", 20.0, "2025-08-01T12:00:00Z"));
    repository.save(doc("1", "temperature", 22.0, "2025-08-02T12:00:00Z"));
    repository.save(doc("1", "temperature", 24.0, "2025-08-03T12:00:00Z"));
    repository.save(doc("1", "humidity",    60.0, "2025-08-02T12:00:00Z"));
  }

  // Helper to build SensorData docs
  private static SensorData doc(String sensorId, String metric, double value, String isoInstant) {
    SensorData d = new SensorData();
    d.setSensorId(sensorId);
    d.setMetric(metric);
    d.setValue(value);
    d.setTimestamp(Instant.parse(isoInstant));
    return d;
  }

  @Test
  void queryAvgTempAndHumidity_windowAug1to3() throws Exception {
    // Expect avg temp = (20+22+24)/3 = 22, humidity = 60
    mockMvc.perform(get("/sensors/query")
        .param("sensorIds", "1")
        .param("metrics", "temperature,humidity")
        .param("stat", "avg")
        .param("from", "2025-08-01T00:00:00Z")
        .param("to",   "2025-08-03T23:59:59Z")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.sensorIds[0]", is("1")))
      .andExpect(jsonPath("$.metrics", containsInAnyOrder("temperature","humidity")))
      .andExpect(jsonPath("$.statistic", is("avg")))
      .andExpect(jsonPath("$.resultsByMetric.temperature", is(closeTo(22.0, 1e-4))))
      .andExpect(jsonPath("$.resultsByMetric.humidity",    is(closeTo(60.0, 1e-4))));
  }

  @Test
  void queryMaxTemp_windowAug1to3() throws Exception {
    // Expect max temp = 24
    mockMvc.perform(get("/sensors/query")
        .param("sensorIds", "1")
        .param("metrics", "temperature")
        .param("stat", "max")
        .param("from", "2025-08-01T00:00:00Z")
        .param("to",   "2025-08-03T23:59:59Z")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.resultsByMetric.temperature", is(closeTo(24.0, 1e-4))));
  }

  @Test
  void querySumTemp_windowAug1to3() throws Exception {
    // Expect sum temp = 20+22+24 = 66
    mockMvc.perform(get("/sensors/query")
        .param("sensorIds", "1")
        .param("metrics", "temperature")
        .param("stat", "sum")
        .param("from", "2025-08-01T00:00:00Z")
        .param("to",   "2025-08-03T23:59:59Z")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.resultsByMetric.temperature", is(closeTo(66.0, 1e-4))));
  }

  @Test
  void query_noData_returnsEmptyResult() throws Exception {
    // Window far in past no results, expect empty map not error
    mockMvc.perform(get("/sensors/query")
                      .param("sensorIds", "1")
                      .param("metrics", "temperature")
                      .param("stat", "avg")
                      .param("from", "2000-01-01T00:00:00Z")
                      .param("to",   "2000-01-02T00:00:00Z")
                      .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.resultsByMetric").isMap())
            .andExpect(jsonPath("$.resultsByMetric", anEmptyMap()));
  }
}
