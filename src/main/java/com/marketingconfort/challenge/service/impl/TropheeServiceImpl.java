package com.marketingconfort.challenge.service.impl;

import com.marketingconfort.challenge.dto.TropheeDTO;
import com.marketingconfort.challenge.dto.MultimediaInfo;
import com.marketingconfort.challenge.enums.TropheeType;
import com.marketingconfort.challenge.mapper.TropheeMapper;
import com.marketingconfort.challenge.models.Challenge;
import com.marketingconfort.challenge.models.Trophee;
import com.marketingconfort.challenge.repository.ChallengeRepository;
import com.marketingconfort.challenge.repository.TropheeRepository;
import com.marketingconfort.challenge.service.TropheeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class TropheeServiceImpl implements TropheeService {
    private final TropheeRepository tropheeRepository;
    private final ChallengeRepository challengeRepository;
    private final TropheeMapper tropheeMapper;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    public TropheeDTO createTrophee(String titre, TropheeType type, String description, double scoreMin, int tempsMaximum, int tentativeMaximum, boolean allQuestionsNeedToValide, java.util.List<String> challengeUuids, MultipartFile icone) {
        String iconeUuid = null;
        try {
            java.util.List<Challenge> challenges = challengeRepository.findAllByUuidIn(challengeUuids);
            // Validation: scoreMin <= sum of all challenge question points
            double totalScore = challenges.stream()
                .flatMap(c -> c.getQuestions().stream())
                .mapToDouble(q -> q.getPoints())
                .sum();
            if (scoreMin > totalScore) {
                throw new RuntimeException("scoreMin cannot be greater than the total score of all assigned challenges (" + totalScore + ")");
            }
            // Validation: tentativeMaximum <= min of all challenge attemptCounts
            int minTentative = challenges.stream().mapToInt(Challenge::getAttemptCount).min().orElse(0);
            if (tentativeMaximum > minTentative) {
                throw new RuntimeException("tentativeMaximum cannot be greater than the minimum attemptCount of all assigned challenges (" + minTentative + ")");
            }
            MultimediaInfo iconInfo = uploadIconAndGetInfo(icone);
            iconeUuid = iconInfo != null ? iconInfo.getUuid() : null;
            Trophee trophee = new Trophee();
            trophee.setTitre(titre);
            trophee.setType(type);
            trophee.setDescription(description);
            trophee.setScoreMin(scoreMin);
            trophee.setTempsMaximum(tempsMaximum);
            trophee.setTentativeMaximum(tentativeMaximum);
            trophee.setAllQuestionsNeedToValide(allQuestionsNeedToValide);
            trophee.setChallenges(challenges);
            if (iconInfo != null) {
                com.marketingconfort.challenge.models.MultimediaInfo multimediaInfo = 
                    new com.marketingconfort.challenge.models.MultimediaInfo();
                multimediaInfo.setUuid(iconInfo.getUuid());
                multimediaInfo.setUrl(iconInfo.getUrl());
                trophee.setIconeMultimediaInfo(multimediaInfo);
            }
            Trophee saved = tropheeRepository.save(trophee);
            return tropheeMapper.toDto(saved);
        } catch (Exception ex) {
            // Custom rollback: delete uploaded icon if any
            if (iconeUuid != null) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    Map<String, Object> body = new java.util.HashMap<>();
                    body.put("multimediaUuids", java.util.Collections.singletonList(iconeUuid));
                    body.put("action", "delete");
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                    restTemplate.postForEntity("http://localhost:7077/api/multimedia/bulk-operations", request, Void.class);
                } catch (Exception rollbackEx) {
                    // Optionally log rollback failure
                }
            }
            throw ex;
        }
    }

    @Override
    public TropheeDTO updateTrophee(String uuid, String titre, TropheeType type, String description, double scoreMin, int tempsMaximum, int tentativeMaximum, boolean allQuestionsNeedToValide, java.util.List<String> challengeUuids, MultipartFile icone) {
        Trophee trophee = tropheeRepository.findByUuid(uuid);
        if (trophee == null) throw new RuntimeException("Trophee not found with uuid: " + uuid);
        String oldIconeUuid = trophee.getIconeMultimediaInfo() != null ? trophee.getIconeMultimediaInfo().getUuid() : null;
        String newIconeUuid = null;
        boolean iconUpdated = false;
        try {
            java.util.List<Challenge> challenges = challengeRepository.findAllByUuidIn(challengeUuids);
            // Validation: scoreMin <= sum of all challenge question points
            double totalScore = challenges.stream()
                .flatMap(c -> c.getQuestions().stream())
                .mapToDouble(q -> q.getPoints())
                .sum();
            if (scoreMin > totalScore) {
                throw new RuntimeException("scoreMin cannot be greater than the total score of all assigned challenges (" + totalScore + ")");
            }
            // Validation: tentativeMaximum <= min of all challenge attemptCounts
            int minTentative = challenges.stream().mapToInt(Challenge::getAttemptCount).min().orElse(0);
            if (tentativeMaximum > minTentative) {
                throw new RuntimeException("tentativeMaximum cannot be greater than the minimum attemptCount of all assigned challenges (" + minTentative + ")");
            }
            if (icone != null && !icone.isEmpty()) {
                // Upload new icon
                MultimediaInfo iconInfo = uploadIconAndGetInfo(icone);
                newIconeUuid = iconInfo != null ? iconInfo.getUuid() : null;
                if (iconInfo != null) {
                    com.marketingconfort.challenge.models.MultimediaInfo multimediaInfo = 
                        new com.marketingconfort.challenge.models.MultimediaInfo();
                    multimediaInfo.setUuid(iconInfo.getUuid());
                    multimediaInfo.setUrl(iconInfo.getUrl());
                    trophee.setIconeMultimediaInfo(multimediaInfo);
                }
                iconUpdated = true;
            }
            // Update other fields
            trophee.setTitre(titre);
            trophee.setType(type);
            trophee.setDescription(description);
            trophee.setScoreMin(scoreMin);
            trophee.setTempsMaximum(tempsMaximum);
            trophee.setTentativeMaximum(tentativeMaximum);
            trophee.setAllQuestionsNeedToValide(allQuestionsNeedToValide);
            trophee.setChallenges(challenges);
            Trophee saved = tropheeRepository.save(trophee);
            // If icon was updated and update succeeded, delete old icon
            if (iconUpdated && oldIconeUuid != null) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    Map<String, Object> body = new java.util.HashMap<>();
                    body.put("multimediaUuids", java.util.Collections.singletonList(oldIconeUuid));
                    body.put("action", "delete");
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                    restTemplate.postForEntity("http://localhost:7077/api/multimedia/bulk-operations", request, Void.class);
                } catch (Exception ignore) {}
            }
            return tropheeMapper.toDto(saved);
        } catch (Exception ex) {
            // Rollback new icon if uploaded
            if (iconUpdated && newIconeUuid != null) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    Map<String, Object> body = new java.util.HashMap<>();
                    body.put("multimediaUuids", java.util.Collections.singletonList(newIconeUuid));
                    body.put("action", "delete");
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                    restTemplate.postForEntity("http://localhost:7077/api/multimedia/bulk-operations", request, Void.class);
                } catch (Exception ignore) {}
            }
            throw ex;
        }
    }

    @Override
    public void deleteTrophee(String uuid) {
        Trophee trophee = tropheeRepository.findByUuid(uuid);
        if (trophee == null) throw new RuntimeException("Trophee not found with uuid: " + uuid);
        String iconeUuid = trophee.getIconeMultimediaInfo() != null ? trophee.getIconeMultimediaInfo().getUuid() : null;
        tropheeRepository.delete(trophee);
        if (iconeUuid != null) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                Map<String, Object> body = new java.util.HashMap<>();
                body.put("multimediaUuids", java.util.Collections.singletonList(iconeUuid));
                body.put("action", "delete");
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                restTemplate.postForEntity("http://localhost:7077/api/multimedia/bulk-operations", request, Void.class);
            } catch (Exception ignore) {}
        }
    }

    @Override
    public org.springframework.data.domain.Page<TropheeDTO> getTropheesByChallengeUuid(String challengeUuid, int page, int size) {
        org.springframework.data.domain.Page<Trophee> tropheePage = tropheeRepository.findByChallenges_Uuid(challengeUuid, org.springframework.data.domain.PageRequest.of(page, size));
        java.util.List<TropheeDTO> dtos = new java.util.ArrayList<>();
        for (Trophee t : tropheePage.getContent()) {
            dtos.add(tropheeMapper.toDto(t));
        }
        return new org.springframework.data.domain.PageImpl<>(dtos, tropheePage.getPageable(), tropheePage.getTotalElements());
    }

    @Override
    public TropheeDTO getTropheeByUuid(String uuid) {
        Trophee trophee = tropheeRepository.findByUuid(uuid);
        if (trophee == null) throw new RuntimeException("Trophee not found with uuid: " + uuid);
        return tropheeMapper.toDto(trophee);
    }

    private MultimediaInfo uploadIconAndGetInfo(MultipartFile icone) {
        try {
            byte[] iconeBytes = icone.getBytes();
            org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("file", new org.springframework.core.io.ByteArrayResource(iconeBytes) {
                @Override
                public String getFilename() {
                    return icone.getOriginalFilename();
                }
            });
            body.add("service", "challenge");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://localhost:7077/api/multimedia/upload",
                    requestEntity,
                    Map.class
            );
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody() != null ? (Map<String, Object>) response.getBody() : null;
            if (responseBody != null && responseBody.get("uuid") != null) {
                String uuid = responseBody.get("uuid").toString();
                String url = responseBody.get("url") != null ? responseBody.get("url").toString() : null;
                return new MultimediaInfo(uuid, url);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload icon", e);
        }
    }
} 