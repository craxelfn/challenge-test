package com.marketingconfort.challenge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.marketingconfort.challenge.models.Challenge;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> , JpaSpecificationExecutor<Challenge > {
    Challenge findByUuid(String uuid);


    Challenge findByUuid(String uuid);
    
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.multimediaInfo LEFT JOIN FETCH q.answers WHERE q.challenge.uuid = :challengeUuid")
    List<com.marketingconfort.challenge.models.Question> findQuestionsByChallengeUuid(@Param("challengeUuid") String challengeUuid);

    @Query("SELECT c FROM Challenge c LEFT JOIN FETCH c.multimediaInfo WHERE c.uuid IN :uuids")
    List<Challenge> findAllByUuidIn(@Param("uuids") List<String> uuids);
}
