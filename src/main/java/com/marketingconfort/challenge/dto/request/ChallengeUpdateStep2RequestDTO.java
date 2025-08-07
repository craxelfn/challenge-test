package com.marketingconfort.challenge.dto.request;

import lombok.Data;

@Data
public class ChallengeUpdateStep2RequestDTO {
    private Integer nbTentatives;
    private Boolean randomQuestions;
    private String methodeDeCalcule;
    private String prerequisUuid;
    private String prerequisMinScore;
    private String messageSucces;
    private String messageEchec;
}