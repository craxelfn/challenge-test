package com.marketingconfort.challenge.dto.request;

import lombok.Data;

@Data
public class AnswerCreateRequestDTO {
    private String uuid;
    private String texte;
    private boolean estCorrecte;
} 