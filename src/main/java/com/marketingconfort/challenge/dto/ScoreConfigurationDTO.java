package com.marketingconfort.challenge.dto;

import com.marketingconfort.challenge.enums.ScoreMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreConfigurationDTO {
    private String uuid;
    private ScoreMethod methode;
    private String parametres;
} 