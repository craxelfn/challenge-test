package com.marketingconfort.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrerequisDTO {
    private String uuid;
    private String nom;
    private int pourcentageMinimum;
} 