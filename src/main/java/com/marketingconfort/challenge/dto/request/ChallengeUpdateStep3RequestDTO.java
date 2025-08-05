package com.marketingconfort.challenge.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ChallengeUpdateStep3RequestDTO {
    private List<String> deletedQuestionUuids;
    private List<QuestionCreateRequestDTO> newQuestions;
    private Double minPassingScore;
}