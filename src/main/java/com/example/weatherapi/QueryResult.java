package com.example.weatherapi;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO for returning aggregated query results to the client.
 */
public class QueryResult {
    private List<String> sensorIds; // sensors included in the query (null/empty = all)
    private List<String> metrics;   // metrics included in the query (null/empty = all)
    private String statistic;       // aggregation type: min/max/sum/avg
    private Instant from;           // start of query window
    private Instant to;             // end of query window
    private Map<String, Double> resultsByMetric; // metric aggregated value

    /**
     * Constructs a QueryResult response.
     */
    public QueryResult(List<String> sensorIds, List<String> metrics, String statistic,
                       Instant from, Instant to, Map<String, Double> resultsByMetric) {
        this.sensorIds = sensorIds;
        this.metrics = metrics;
        this.statistic = statistic;
        this.from = from;
        this.to = to;
        this.resultsByMetric = resultsByMetric;
    }

    // Getters
    public List<String> getSensorIds() { return sensorIds; }
    public List<String> getMetrics() { return metrics; }
    public String getStatistic() { return statistic; }
    public Instant getFrom() { return from; }
    public Instant getTo() { return to; }
    public Map<String, Double> getResultsByMetric() { return resultsByMetric; }
}
