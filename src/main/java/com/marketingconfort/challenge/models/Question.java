package com.marketingconfort.challenge.models;

import com.marketingconfort.challenge.enums.QuestionTypeChallenge;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    private String title ; 

    private String content ; 

    @Enumerated(EnumType.STRING)
    private QuestionTypeChallenge type ; 

    private int ordre  ; 

    private int points ; 

    private int duree ; 

    private Boolean isRequired ; 

    private String reponseAttendue = "0"; 

    @Column(nullable = false, unique = true)
    private String uuid;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<Answer> answers;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
    @JoinColumn(name = "multimedia_info_id")
    private MultimediaInfo multimediaInfo;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    @JsonIgnore
    private Challenge challenge;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }

    
}
