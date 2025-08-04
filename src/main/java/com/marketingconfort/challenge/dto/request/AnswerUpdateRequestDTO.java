package com.marketingconfort.challenge.dto.request;

import lombok.Data;

@Data
public class AnswerUpdateRequestDTO {
    private String uuid;
    private String texte;
    private boolean estCorrecte;
} 