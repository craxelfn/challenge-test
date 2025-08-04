package com.marketingconfort.challenge.dto;

import com.marketingconfort.challenge.enums.QuestionTypeChallenge;
import java.util.List;
import lombok.Data;

@Data
public class QuestionDTO {
    private String uuid;
    private String title;
    private String content;
    private QuestionTypeChallenge type;
    private int ordre;
    private int points;
    private int duree;
    private Boolean isRequired;
    private String reponseAttendue;
    private List<AnswerDTO> answers;
    private MultimediaDTO multimedia;
} 