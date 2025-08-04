package com.marketingconfort.challenge.service;

import com.marketingconfort.challenge.dto.ChallengeDTO;
import com.marketingconfort.challenge.dto.request.QuestionCreateRequestDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface QuestionService {
    ChallengeDTO addQuestionsToChallenge(String challengeUuid, List<QuestionCreateRequestDTO> questions, List<MultipartFile> multimedias);
    void deleteMultipleQuestions(String challengeUuid, List<String> questionUuids);
    ChallengeDTO updateMultipleQuestions(String challengeUuid, List<com.marketingconfort.challenge.dto.request.QuestionUpdateRequestDTO> questions, List<org.springframework.web.multipart.MultipartFile> multimedias);
} 