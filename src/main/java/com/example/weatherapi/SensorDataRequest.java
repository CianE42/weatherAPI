package com.example.weatherapi;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class SensorDataRequest {
    @NotBlank
    private String sensorId;

    @NotBlank(message = "metric is required (temperature, humidity, wind_speed)")
    private String metric;

    @NotNull
    private Double value;

    @NotNull
    private Instant timestamp;

    // Getters & Setters
    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

