package com.marketingconfort.challenge.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class QuestionUpdateRequestDTO {
    private String uuid;
    private String texte;
    private String type;
    private int ordre;
    private int points;
    private int duree;
    private boolean isRequired;
    private MultipartFile multimedia; // Name of the new multimedia file, if any
} 