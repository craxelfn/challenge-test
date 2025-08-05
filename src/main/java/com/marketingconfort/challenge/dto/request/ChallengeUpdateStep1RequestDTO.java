package com.marketingconfort.challenge.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class ChallengeUpdateStep1RequestDTO {
    private String nom;
    private String statut;
    private String description;
    private String difficulte;
    private String niveau;
    private String datePublication;
    private List<MultipartFile> multimedias; // New images for the challenge
}