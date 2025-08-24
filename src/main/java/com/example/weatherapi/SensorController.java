package com.example.weatherapi;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST controller exposing sensor endpoints:
 * - POST /sensors/data : write sensor readings
 * - GET  /sensors/query: query aggregated stats
 */
@RestController
@RequestMapping("/sensors")
public class SensorController {

    private final SensorService service;

    public SensorController(SensorService service) {
        this.service = service;
    }

    /**
     * Ingest a new sensor reading.
     * Example: POST /sensors/data
     */
    @PostMapping("/data")
    public ResponseEntity<SensorData> addSensorData(@Valid @RequestBody SensorDataRequest request) {
        SensorData saved = service.saveSensorData(request);
        return ResponseEntity.ok(saved);
    }

    /**
     * Query sensor data with filters and aggregation.
     * Example: GET /sensors/query?sensorIds=1&metrics=temperature&stat=avg&from=...&to=...
     */
    @GetMapping("/query")
    public ResponseEntity<QueryResult> query(
            @RequestParam(required = false) List<String> sensorIds, // optional: which sensors
            @RequestParam(required = false) List<String> metrics,   // optional: which metrics
            @RequestParam(defaultValue = "avg") String stat,        // aggregation (default avg)
            @RequestParam(required = false) Instant from,           // start of window
            @RequestParam(required = false) Instant to              // end of window
    ) {
        // Validate and normalize statistic param
        Statistic statistic = Statistic.from(stat);

        // Validate and normalize metrics (null/empty = all)
        List<String> normalizedMetrics = null;
        if (metrics != null && !metrics.isEmpty()) {
            normalizedMetrics = metrics.stream()
                    .map(Metric::from)       // validate against enum
                    .map(Metric::dbValue)    // normalize for DB storage/querying
                    .toList();
        }

        // Delegate to service
        QueryResult result = service.queryData(sensorIds, normalizedMetrics, statistic, from, to);
        return ResponseEntity.ok(result);
    }
}
