package com.example.weatherapi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Demo-only seeder: ALWAYS starts with a clean collection, then seeds a week of data.
 * Runs only when profile "demo" is active.
 */
@Component
@Profile("demo")
public class DemoDataSeeder implements CommandLineRunner {

    private final SensorDataRepository repo;
    private final MongoTemplate mongoTemplate;

    public DemoDataSeeder(SensorDataRepository repo, MongoTemplate mongoTemplate) {
        this.repo = repo;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        // 1) Drop collection to ensure a fresh dataset every time
        if (mongoTemplate.collectionExists(SensorData.class)) {
            mongoTemplate.dropCollection(SensorData.class);
        }

        // 2) Recreate indexes (compound index for query efficiency)
        mongoTemplate.indexOps(SensorData.class).createIndex(
            new Index()
                .on("sensorId", Sort.Direction.ASC)
                .on("metric", Sort.Direction.ASC)
                .on("timestamp", Sort.Direction.ASC)
                .named("idx_sensor_metric_ts")
        );

        // 3) Generate 7 days of data, every 6 hours, for 3 sensors x 3 metrics
        List<String> sensors = List.of("1", "2", "3");

        Instant now = Instant.now().truncatedTo(ChronoUnit.HOURS);
        Instant start = now.minus(7, ChronoUnit.DAYS);

        List<SensorData> batch = new ArrayList<>();
        for (String s : sensors) {
            for (Instant t = start; t.isBefore(now); t = t.plus(6, ChronoUnit.HOURS)) { // ← every 6h
                batch.add(doc(s, "temperature", rnd(14, 27), t));
                batch.add(doc(s, "humidity",    rnd(40, 80), t));
                batch.add(doc(s, "wind_speed",  rnd(2, 18),  t));
            }
        }

        repo.saveAll(batch);
        System.out.println("DemoDataSeeder: fresh seed complete; inserted " + batch.size() + " docs");
    }

    private static double rnd(double min, double max) {
        return Math.round((ThreadLocalRandom.current().nextDouble(min, max)) * 10.0) / 10.0;
    }

    private static SensorData doc(String sensorId, String metric, double value, Instant ts) {
        SensorData d = new SensorData();
        d.setSensorId(sensorId);
        d.setMetric(metric); // already normalized strings
        d.setValue(value);
        d.setTimestamp(ts);
        return d;
    }
}
