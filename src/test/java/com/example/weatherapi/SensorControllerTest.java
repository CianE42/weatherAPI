package com.example.weatherapi;

import com.example.weatherapi.SensorData;
import com.example.weatherapi.SensorDataRepository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-stack integration test: Spring Boot + Controller + Repository + Testcontainers MongoDB.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SensorControllerTest {

  // Fixed tag avoids Apple Silicon/x86 surprises
  @Container
  @ServiceConnection 
  static final MongoDBContainer mongo = new MongoDBContainer("mongo:7");

  @Autowired MockMvc mockMvc;
  @Autowired SensorDataRepository repository;

  @BeforeEach
  void seed() {
    repository.deleteAll();
    // Sensor 1 temperature on Aug 1, 2, 3
    repository.save(doc("1", "temperature", 20.0, "2025-08-01T12:00:00Z"));
    repository.save(doc("1", "temperature", 22.0, "2025-08-02T12:00:00Z"));
    repository.save(doc("1", "temperature", 24.0, "2025-08-03T12:00:00Z"));
    // Sensor 1 humidity on Aug 2
    repository.save(doc("1", "humidity",    60.0, "2025-08-02T12:00:00Z"));
  }

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
    mockMvc.perform(get("/sensors/query")
        .param("sensorIds", "1")
        .param("metrics", "temperature")
        .param("stat", "sum")
        .param("from", "2025-08-01T00:00:00Z")
        .param("to",   "2025-08-03T23:59:59Z")
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      // 20 + 22 + 24 = 66
      .andExpect(jsonPath("$.resultsByMetric.temperature", is(closeTo(66.0, 1e-4))));
  }
}
