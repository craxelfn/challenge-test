package com.marketingconfort.challenge.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity to store multimedia information (UUID and URL)
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultimediaInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String uuid;
    private String url;
}