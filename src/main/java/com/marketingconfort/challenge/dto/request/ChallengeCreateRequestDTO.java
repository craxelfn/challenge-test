package com.marketingconfort.challenge.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import com.marketingconfort.challenge.models.ScoreConfiguration;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
public class ChallengeCreateRequestDTO {
    @NotBlank
    private String nom;
    @NotBlank
    private String description;
    @NotBlank
    private String statut;
    @NotBlank
    private String niveau;
    @NotBlank
    private String difficulte;
    @NotBlank
    private String datePublication;
    @NotBlank
    private String methodeDeCalcule;
    @NotBlank
    private String messageSucces;
    @NotBlank
    private String messageEchec;
    @Min(1)
    private int timer;
    @Min(1)
    private int nbTentatives;
    @NotNull
    private ScoreConfiguration scoreConfiguration;
    private List<MultipartFile> multimedias; // For challenge-level images
    @NotNull
    private List<QuestionCreateRequestDTO> questions;
    private String uuid;
    private Double minPassingScore;
} 