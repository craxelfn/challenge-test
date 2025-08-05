package com.marketingconfort.challenge.service;

import com.marketingconfort.challenge.dto.TropheeDTO;
import com.marketingconfort.challenge.enums.TropheeType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

public interface TropheeService {
    TropheeDTO createTrophee(String titre, TropheeType type, String description, double scoreMin, int tempsMaximum, int tentativeMaximum, boolean allQuestionsNeedToValide, java.util.List<String> challengeUuids, MultipartFile icone);
    TropheeDTO updateTrophee(String uuid, String titre, TropheeType type, String description, double scoreMin, int tempsMaximum, int tentativeMaximum, boolean allQuestionsNeedToValide, java.util.List<String> challengeUuids, MultipartFile icone);
    void deleteTrophee(String uuid);
    Page<TropheeDTO> getTropheesByChallengeUuid(String challengeUuid, int page, int size);
    TropheeDTO getTropheeByUuid(String uuid);
} 