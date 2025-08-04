package com.marketingconfort.challenge.repository;

import com.marketingconfort.challenge.models.ScoreConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface ScoreConfigurationRepository extends JpaRepository<ScoreConfiguration, Long> {
    ScoreConfiguration findByUuid(String uuid);
} 