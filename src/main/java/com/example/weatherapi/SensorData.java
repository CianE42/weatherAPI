package com.example.weatherapi;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representing a single sensor reading.
 * Stored in collection "sensor_data".
 *
 * A compound index on (sensorId, metric, timestamp)
 * supports efficient range queries and aggregations.
 */
@Document(collection = "sensor_data")
@CompoundIndex(name = "sensor_metric_time_idx", def = "{'sensorId': 1, 'metric': 1, 'timestamp': 1}")
public class SensorData {
    @Id
    private String id;

    private String sensorId;
    private String metric;     // e.g. temperature, humidity
    private double value;
    private Instant timestamp;

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
