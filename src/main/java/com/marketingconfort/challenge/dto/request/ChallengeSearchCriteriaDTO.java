package com.marketingconfort.challenge.dto.request;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChallengeSearchCriteriaDTO {
    private String name;
    private String description;
    private String statut;
    private LocalDateTime datePublication;
    private LocalDateTime dateModification;
    private String difficulte;
} 