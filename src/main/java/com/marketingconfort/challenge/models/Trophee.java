package com.marketingconfort.challenge.models;

import com.marketingconfort.challenge.enums.TropheeType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
    
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trophee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    @Enumerated(EnumType.STRING)
    private TropheeType type;

    private String description;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "icone_multimedia_info_id")
    private MultimediaInfo iconeMultimediaInfo;

    private double scoreMin;

    private int tempsMaximum;

    private int tentativeMaximum;

    private boolean allQuestionsNeedToValide;

    private LocalDateTime dateCreation;
    private LocalDateTime dateUpdate;

    @Column(nullable = false, unique = true)
    private String uuid;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        this.dateUpdate = LocalDateTime.now();
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateUpdate = LocalDateTime.now();
    }

    @ManyToMany
    @JsonIgnore
    private java.util.List<Challenge> challenges;
} 