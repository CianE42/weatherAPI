package com.example.weatherapi;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;

/**
 * Test helper for mocking MongoTemplate.aggregate() results.
 */
public final class TestAggResults {
    private TestAggResults() {}
    public static AggregationResults<Document> docs(List<Document> mapped) {
        // The second arg is raw results; not used by our service.
        return new AggregationResults<>(mapped, new Document());
    }
}
