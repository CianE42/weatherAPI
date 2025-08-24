package com.example.weatherapi;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class SensorService {

    private final SensorDataRepository repository;
    private final MongoTemplate mongoTemplate;

    public SensorService(SensorDataRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    public SensorData saveSensorData(SensorDataRequest request) {
        // Validate/normalize metric
        Metric metric = Metric.from(request.getMetric());

        SensorData data = new SensorData();
        data.setSensorId(request.getSensorId());
        // Persist normalized lowercase string (stable querying)
        data.setMetric(metric.dbValue());
        data.setValue(request.getValue());
        data.setTimestamp(request.getTimestamp());

        return repository.save(data);
    }

    public QueryResult queryData(List<String> sensorIds,
                                 List<String> metrics,
                                 Statistic statistic,
                                 Instant from,
                                 Instant to) {
        // Default date window: last 24 hours if none provided
        if (from == null && to == null) {
            to = Instant.now();
            from = to.minus(Duration.ofDays(1));
        } else if (from != null && to == null) {
            to = from.plus(Duration.ofDays(1));
        } else if (from == null) { // to != null
            from = to.minus(Duration.ofDays(1));
        }

        // Validate window (1 day to 31 days)
        long days = Duration.between(from, to).toDays();
        if (days < 1 || days > 31) {
            throw new IllegalArgumentException("Date range must be between 1 and 31 days.");
        }

        // Build match criteria dynamically
        List<Criteria> and = new ArrayList<>();
        and.add(Criteria.where("timestamp").gte(from).lte(to));
        if (sensorIds != null && !sensorIds.isEmpty()) {
            and.add(Criteria.where("sensorId").in(sensorIds));
        }
        if (metrics != null && !metrics.isEmpty()) {
            and.add(Criteria.where("metric").in(metrics));
        }
        Criteria matchCriteria = new Criteria().andOperator(and.toArray(new Criteria[0]));

        // Build aggregation
        List<AggregationOperation> ops = new ArrayList<>();
        ops.add(match(matchCriteria));

        GroupOperation group = group("metric");
        switch (statistic) {
            case MIN: group = group.min("value").as("value"); break;
            case MAX: group = group.max("value").as("value"); break;
            case SUM: group = group.sum("value").as("value"); break;
            case AVG: group = group.avg("value").as("value"); break;
        }

        ops.add(group);
        ops.add(project("value")
                .and("_id").as("metric")
                .andExclude("_id"));

        Aggregation agg = newAggregation(ops);
        AggregationResults<Document> aggResults =
                mongoTemplate.aggregate(agg, "sensor_data", Document.class);

        Map<String, Double> resultsByMetric = new LinkedHashMap<>();
        for (Document d : aggResults) {
            String metric = d.getString("metric");
            Number val = (Number) d.get("value");
            if (metric != null && val != null) {
                resultsByMetric.put(metric, val.doubleValue());
            }
        }

        //Return empty result set with 200 OK 
        return new QueryResult(
                (sensorIds == null || sensorIds.isEmpty()) ? null : sensorIds,
                (metrics == null || metrics.isEmpty()) ? null : metrics,
                statistic.name().toLowerCase(),
                from, to,
                resultsByMetric // can be empty
        );
    }
}
