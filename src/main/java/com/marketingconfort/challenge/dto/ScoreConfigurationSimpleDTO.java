package com.marketingconfort.challenge.dto;

import lombok.Data;
import com.marketingconfort.challenge.enums.ScoreMethod;
import java.util.List;

@Data
public class ScoreConfigurationSimpleDTO {
    private ScoreMethod method;
    private List<String> parameters;
} 