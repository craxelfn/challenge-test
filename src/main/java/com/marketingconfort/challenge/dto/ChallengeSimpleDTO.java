package com.marketingconfort.challenge.dto;

import lombok.Data;
import java.time.LocalDateTime;
import com.marketingconfort.challenge.enums.ChallengeStatus;
import com.marketingconfort.challenge.enums.Difficulty;
import com.marketingconfort.challenge.enums.CalculationMethod;

@Data
public class ChallengeSimpleDTO {
    private String uuid;
    private String nom;
    private String description;
    private ChallengeStatus statut;
    private String niveau;
    private Difficulty difficulte;
    private LocalDateTime datePublication;
    private int timer;
    private int nbTentatives;
    private String messageSucces;
    private String messageEchec;
    private ScoreConfigurationSimpleDTO scoreConfiguration;
    private int questionsCount;
    private int participantsCount;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMiseAJour;
    private boolean active;
    private CalculationMethod calculationMethod;
    private double penaltyPerError;
    private double timeBonus;
    private double minPassingScore;
    private Boolean randomQuestions;
} 