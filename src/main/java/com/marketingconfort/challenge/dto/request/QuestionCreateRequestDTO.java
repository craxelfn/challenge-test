package com.marketingconfort.challenge.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class QuestionCreateRequestDTO {
    private String uuid;
    private String texte;
    private String type;
    private int ordre;
    private int points;
    private int duree;
    private boolean isRequired;
    private List<AnswerCreateRequestDTO> reponses;
    private MultipartFile multimedia;
} 