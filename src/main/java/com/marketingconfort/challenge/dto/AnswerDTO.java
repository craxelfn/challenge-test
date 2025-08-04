package com.marketingconfort.challenge.dto;

import lombok.Data;

@Data
public class AnswerDTO {
    private String uuid;
    private String texte;
    private Boolean isCorrect;
} 