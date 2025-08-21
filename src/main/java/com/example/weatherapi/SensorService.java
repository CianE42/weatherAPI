package com.example.weatherapi;

import com.example.weatherapi.SensorDataRequest;
import com.example.weatherapi.SensorData;
import com.example.weatherapi.SensorDataRepository;
import org.springframework.stereotype.Service;

@Service
public class SensorService {

    private final SensorDataRepository repository;

    public SensorService(SensorDataRepository repository) {
        this.repository = repository;
    }

    public SensorData saveSensorData(SensorDataRequest request) {
        SensorData data = new SensorData();
        data.setSensorId(request.getSensorId());
        data.setMetric(request.getMetric());
        data.setValue(request.getValue());
        data.setTimestamp(request.getTimestamp());

        return repository.save(data);
    }
}
