package com.marketingconfort.challenge.dto;

import lombok.Data;
import java.util.List ; 
import com.marketingconfort.challenge.enums.TropheeType;
import com.marketingconfort.challenge.dto.ChallengeShortDTO;

@Data
public class TropheeDTO {
    private String uuid;
    private String titre;
    private TropheeType type;
    private String description;
    private MultimediaDTO icone;
    private double scoreMin;
    private int tempsMaximum;
    private int tentativeMaximum;
    private boolean allQuestionsNeedToValide;
    private List<ChallengeShortDTO> challengeUuids;
} 