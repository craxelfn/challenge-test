package com.marketingconfort.challenge.dto;

import com.marketingconfort.challenge.enums.ChallengeStatus;
import com.marketingconfort.challenge.enums.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeListDTO {
    private String uuid;
    private String nom;
    private String description;
    private ChallengeStatus statut;
    private LocalDateTime dateCreation;
    private LocalDateTime datePublication;
    private LocalDateTime dateMiseAJour;
    private Difficulty difficulte;
    private int participantsCount;
    private int questionsCount;
    private int timer;
    private int nbTentatives;
    private Boolean isRandomQuestions;
    private ScoreConfigurationDTO scoreConfiguration;
    private String messageSucces;
    private String messageEchec;
    private NiveauDTO niveau;
    private List<MultimediaDTO> multimedias;
    private PrerequisDTO prerequis;
    private Boolean active;
} 