package com.marketingconfort.challenge.controller;

import com.marketingconfort.challenge.dto.ChallengeDTO;
import com.marketingconfort.challenge.dto.request.QuestionCreateRequestDTO;
import com.marketingconfort.challenge.dto.request.QuestionUpdateRequestDTO;
import com.marketingconfort.challenge.dto.request.ChallengeUpdateStep3RequestDTO;
import com.marketingconfort.challenge.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/challenges/{challengeUuid}/questions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ChallengeDTO> addQuestionsToChallenge(
            @PathVariable String challengeUuid,
            @RequestPart("questions") List<QuestionCreateRequestDTO> questions,
            @RequestPart(value = "multimedias", required = false) List<MultipartFile> multimedias
    ) {
        ChallengeDTO updated = questionService.addQuestionsToChallenge(challengeUuid, questions, multimedias);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/delete-multiple")
    public ResponseEntity<Void> deleteMultipleQuestions(
            @PathVariable String challengeUuid,
            @RequestBody List<String> questionUuids
    ) {
        questionService.deleteMultipleQuestions(challengeUuid, questionUuids);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/update-multiple", consumes = {"multipart/form-data"})
    public ResponseEntity<ChallengeDTO> updateMultipleQuestions(
            @PathVariable String challengeUuid,
            @RequestPart("questions") List<QuestionUpdateRequestDTO> questions,
            @RequestPart(value = "multimedias", required = false) List<org.springframework.web.multipart.MultipartFile> multimedias
    ) {
        ChallengeDTO updated = questionService.updateMultipleQuestions(challengeUuid, questions, multimedias);
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value = "/bulk-update", consumes = {"multipart/form-data"})
    public ResponseEntity<ChallengeDTO> bulkUpdateQuestions(
            @PathVariable String challengeUuid,
            @RequestPart("data") ChallengeUpdateStep3RequestDTO data,
            @RequestPart(value = "multimedias", required = false) List<MultipartFile> multimedias
    ) {
        ChallengeDTO updated = questionService.bulkUpdateQuestions(challengeUuid, data, multimedias);
        return ResponseEntity.ok(updated);
    }
} 