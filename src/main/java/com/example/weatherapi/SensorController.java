package com.example.weatherapi;

import com.example.weatherapi.SensorDataRequest;
import com.example.weatherapi.SensorData;
import com.example.weatherapi.SensorService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
