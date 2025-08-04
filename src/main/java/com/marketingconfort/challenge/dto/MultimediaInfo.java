package com.marketingconfort.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Helper class to store multimedia information (UUID and URL)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultimediaInfo {
    private String uuid;
    private String url;
} 