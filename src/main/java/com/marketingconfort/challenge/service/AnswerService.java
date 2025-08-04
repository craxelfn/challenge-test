package com.marketingconfort.challenge.service;

import com.marketingconfort.challenge.dto.QuestionDTO;
import com.marketingconfort.challenge.dto.request.AnswerUpdateRequestDTO;
import java.util.List;

public interface AnswerService {
    QuestionDTO updateMultipleAnswers(String questionUuid, List<AnswerUpdateRequestDTO> answers);
}
