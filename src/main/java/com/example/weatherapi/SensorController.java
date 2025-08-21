package com.example.weatherapi;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/sensors")
public class SensorController {

    private final SensorService service;

    public SensorController(SensorService service) {
        this.service = service;
    }

    @PostMapping("/data")
    public ResponseEntity<SensorData> addSensorData(@Valid @RequestBody SensorDataRequest request) {
        SensorData saved = service.saveSensorData(request);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/query")
    public ResponseEntity<QueryResult> query(
            @RequestParam(required = false) List<String> sensorIds,
            @RequestParam(required = false) List<String> metrics,
            @RequestParam(defaultValue = "avg") String stat,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to
    ) {
        Statistic statistic = Statistic.from(stat);
        QueryResult result = service.queryData(sensorIds, metrics, statistic, from, to);
        return ResponseEntity.ok(result);
    }
}
