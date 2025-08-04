package com.marketingconfort.challenge.repository;

import com.marketingconfort.challenge.models.Trophee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TropheeRepository extends JpaRepository<Trophee, Long> {
    List<Trophee> findByChallenges_Uuid(String challengeUuid);
    Page<Trophee> findByChallenges_Uuid(String challengeUuid, Pageable pageable);
    Trophee findByUuid(String uuid);
} 