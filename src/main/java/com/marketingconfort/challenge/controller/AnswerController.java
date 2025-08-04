package com.marketingconfort.challenge.controller;

import com.marketingconfort.challenge.dto.QuestionDTO;
import com.marketingconfort.challenge.dto.request.AnswerUpdateRequestDTO;
import com.marketingconfort.challenge.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/questions/{questionUuid}/answers")
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;

    @PostMapping("/update-multiple")
    public ResponseEntity<QuestionDTO> updateMultipleAnswers(
            @PathVariable String questionUuid,
            @RequestBody List<AnswerUpdateRequestDTO> answers
    ) {
        QuestionDTO updated = answerService.updateMultipleAnswers(questionUuid, answers);
        return ResponseEntity.ok(updated);
    }
} 