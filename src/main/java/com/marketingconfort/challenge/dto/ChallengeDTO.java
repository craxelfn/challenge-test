package com.marketingconfort.challenge.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.marketingconfort.challenge.enums.ChallengeStatus;
import com.marketingconfort.challenge.enums.Difficulty;
import com.marketingconfort.challenge.enums.CalculationMethod;
import com.marketingconfort.challenge.models.ScoreConfiguration;
import com.marketingconfort.challenge.models.Prerequisite;
import lombok.Data;

@Data
public class ChallengeDTO {
    private String uuid;
    private String name;
    private ChallengeStatus statut;
    private CalculationMethod calculationMethod;
    private double penaltyPerError;
    private double timeBonus;
    private Difficulty difficulty;
    private int timer;
    private int attemptCount;
    private LocalDateTime publicationDate;
    private String successMessage;
    private double minPassingScore;
    private String failureMessage;
    private Boolean randomQuestions;
    private Boolean active;
    private Prerequisite prerequisite;
    private String level;
    private ScoreConfiguration scoreConfiguration;
    private List<MultimediaDTO> multimedia;
    private List<QuestionDTO> questions;
    private String description;
    private double maxScore;
}
