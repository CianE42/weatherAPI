package com.example.weatherapi;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for SensorData documents.
 *
 * Provides CRUD methods (save, findAll, delete, etc.)
 * without requiring explicit implementation.
 *
 * Uses:
 * - Entity type: SensorData
 * - ID type: String (MongoDB _id)
 */
@Repository
public interface SensorDataRepository extends MongoRepository<SensorData, String> {
    
}
