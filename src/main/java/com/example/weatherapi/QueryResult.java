package com.example.weatherapi;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class QueryResult {
    private List<String> sensorIds; // null or empty means "all"
    private List<String> metrics;   // null or empty means "all"
    private String statistic;       // min/max/sum/avg
    private Instant from;
    private Instant to;
    private Map<String, Double> resultsByMetric;

    public QueryResult(List<String> sensorIds, List<String> metrics, String statistic,
                       Instant from, Instant to, Map<String, Double> resultsByMetric) {
        this.sensorIds = sensorIds;
        this.metrics = metrics;
        this.statistic = statistic;
        this.from = from;
        this.to = to;
        this.resultsByMetric = resultsByMetric;
    }

    public List<String> getSensorIds() { return sensorIds; }
    public List<String> getMetrics() { return metrics; }
    public String getStatistic() { return statistic; }
    public Instant getFrom() { return from; }
    public Instant getTo() { return to; }
    public Map<String, Double> getResultsByMetric() { return resultsByMetric; }
}

