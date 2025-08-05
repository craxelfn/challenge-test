package com.marketingconfort.challenge.models;

import java.time.LocalDateTime;

import com.marketingconfort.challenge.enums.ChallengeStatus;
import com.marketingconfort.challenge.enums.Difficulty;
import com.marketingconfort.challenge.enums.CalculationMethod;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Challenge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String uuid;

    @Column(nullable = false)
    private String name ;  
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus statut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalculationMethod calculationMethod;

    private double penaltyPerError;
    private double timeBonus;

    

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(nullable = false)
    private int timer;

    @Column(nullable = false)
    private int attemptCount;

    @Column(nullable = false)
    private LocalDateTime publicationDate;

    @Column(nullable = false)
    private String successMessage;

    private double minPassingScore;
    private double maxScore;

    @Column(nullable = false)
    private String failureMessage;

    private Boolean randomQuestions;

    private Boolean active = true;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int participantNumber = 0;

    @OneToOne
    private Prerequisite prerequisite;

    @Column(nullable = false)
    private String level;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)
    private ScoreConfiguration scoreConfiguration;


    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
        name = "challenge_multimedia_info",
        joinColumns = @JoinColumn(name = "challenge_id"),
        inverseJoinColumns = @JoinColumn(name = "multimedia_info_id")
    )
    private List<MultimediaInfo> multimediaInfo;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL)
    private List<Question> questions;

    @ManyToMany(mappedBy = "challenges")
    @JsonIgnore
    private java.util.List<Trophee> trophees;

    @PrePersist
    protected void onCreate() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }

}
