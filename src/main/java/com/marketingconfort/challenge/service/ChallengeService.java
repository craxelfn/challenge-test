package com.marketingconfort.challenge.service;


import com.marketingconfort.challenge.dto.ChallengeDTO;
import com.marketingconfort.challenge.dto.ChallengeListDTO;
import com.marketingconfort.challenge.dto.request.ChallengeCreateRequestDTO;
import com.marketingconfort.challenge.dto.request.ChallengeSearchCriteriaDTO;
import com.marketingconfort.challenge.models.Challenge;

import java.util.List;
import org.springframework.data.domain.Page;

public interface ChallengeService {
    ChallengeDTO createChallenge(ChallengeCreateRequestDTO challengeDTO);
    Page<ChallengeDTO> getChallenges(int page, int size);
    List<ChallengeListDTO> getChallengesList(int page, int size);
    ChallengeDTO getChallengeByUuid(String uuid);
    ChallengeDTO setChallengeActiveStatus(String uuid, boolean active);
    void deleteChallenge(String uuid);
    Page<ChallengeDTO> searchChallenges(ChallengeSearchCriteriaDTO criteria, int page, int size);
    Page<Challenge> searchChallengesEntities(ChallengeSearchCriteriaDTO criteria, int page, int size);
    void deleteMultipleChallenges(List<String> uuids);
    ChallengeDTO participate(String uuid);
}
