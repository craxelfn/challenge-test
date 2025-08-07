package com.marketingconfort.challenge.service.impl;

import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import com.marketingconfort.challenge.dto.ChallengeDTO;
import com.marketingconfort.challenge.dto.ChallengeListDTO;
import com.marketingconfort.challenge.dto.ScoreConfigurationDTO;
import com.marketingconfort.challenge.dto.NiveauDTO;
import com.marketingconfort.challenge.dto.MultimediaInfo;
import com.marketingconfort.challenge.dto.request.ChallengeCreateRequestDTO;
import com.marketingconfort.challenge.dto.request.ChallengeSearchCriteriaDTO;

import com.marketingconfort.challenge.dto.request.QuestionCreateRequestDTO;
import com.marketingconfort.challenge.mapper.ChallengeMapper;
import com.marketingconfort.challenge.models.Challenge;
import com.marketingconfort.challenge.repository.ChallengeRepository;
import com.marketingconfort.challenge.service.ChallengeService;

import java.util.List;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.HashMap;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import com.marketingconfort.challenge.repository.ScoreConfigurationRepository;
import com.marketingconfort.challenge.models.ScoreConfiguration;
import com.marketingconfort.challenge.exception.ChallengeValidationException;
import com.marketingconfort.challenge.enums.QuestionTypeChallenge;
import com.marketingconfort.challenge.enums.ChallengeStatus;
import com.marketingconfort.challenge.enums.Difficulty;
import com.marketingconfort.challenge.enums.CalculationMethod;
import org.springframework.web.client.ResourceAccessException;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChallengeServiceImpl  implements ChallengeService{
    private final ChallengeRepository challengeRepository;
    private final ChallengeMapper challengeMapper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ScoreConfigurationRepository scoreConfigurationRepository;

    @Override
    public ChallengeDTO createChallenge(ChallengeCreateRequestDTO challengeDTO) {
        // Validate enum values before processing
        validateChallengeData(challengeDTO);
        
        List<MultipartFile> allFiles = new ArrayList<>();
        Map<String, String> fileContext = new HashMap<>();
        List<String> uploadedUuids = new ArrayList<>(); // Track uploaded UUIDs for cleanup
        
        try {
            // 1. Collect all files
            if (challengeDTO.getMultimedias() != null) {
                for (MultipartFile file : challengeDTO.getMultimedias()) {
                    if (file != null && !file.isEmpty()) {
                        String filename = file.getOriginalFilename();
                        if (filename != null && filename.startsWith("challenge_")) {
                            allFiles.add(file);
                            fileContext.put(filename, "challenge");
                        }
                    }
                }
            }
            if (challengeDTO.getQuestions() != null) {
                for (int i = 0; i < challengeDTO.getQuestions().size(); i++) {
                    QuestionCreateRequestDTO question = challengeDTO.getQuestions().get(i);
                    MultipartFile file = question.getMultimedia();
                    if (file != null && !file.isEmpty()) {
                        String filename = file.getOriginalFilename();
                        if (filename != null && filename.startsWith("question" + i + "_")) {
                            allFiles.add(file);
                            fileContext.put(filename, "question:" + i);
                        }
                    }
                }
            }
            
            // 2. Upload all files and track UUIDs and URLs
            Map<String, MultimediaInfo> fileNameToMultimediaInfo = uploadFilesAndMapByName(allFiles);
            uploadedUuids.addAll(fileNameToMultimediaInfo.values().stream().map(MultimediaInfo::getUuid).collect(Collectors.toList())); // Track for cleanup
            
            // 3. Assign UUIDs and URLs to DTO fields
            List<com.marketingconfort.challenge.models.MultimediaInfo> challengeMultimediaInfo = new ArrayList<>();
            if (challengeDTO.getMultimedias() != null) {
                for (MultipartFile file : challengeDTO.getMultimedias()) {
                    if (file != null && !file.isEmpty()) {
                        String filename = file.getOriginalFilename();
                        if (filename != null && filename.startsWith("challenge_")) {
                            MultimediaInfo info = fileNameToMultimediaInfo.get(filename);
                            if (info != null) {
                                com.marketingconfort.challenge.models.MultimediaInfo multimediaInfo = 
                                    new com.marketingconfort.challenge.models.MultimediaInfo();
                                multimediaInfo.setUuid(info.getUuid());
                                multimediaInfo.setUrl(info.getUrl());
                                challengeMultimediaInfo.add(multimediaInfo);
                            }
                        }
                    }
                }
            }
            // Create a map to store multimedia info for each question
            Map<Integer, com.marketingconfort.challenge.models.MultimediaInfo> questionMultimediaMap = new HashMap<>();
            
            if (challengeDTO.getQuestions() != null) {
                for (int i = 0; i < challengeDTO.getQuestions().size(); i++) {
                    QuestionCreateRequestDTO question = challengeDTO.getQuestions().get(i);
                    MultipartFile file = question.getMultimedia();
                    if (file != null && !file.isEmpty()) {
                        String filename = file.getOriginalFilename();
                        if (filename != null && filename.startsWith("question" + i + "_")) {
                            MultimediaInfo info = fileNameToMultimediaInfo.get(filename);
                            if (info != null) {
                                com.marketingconfort.challenge.models.MultimediaInfo multimediaInfo = 
                                    new com.marketingconfort.challenge.models.MultimediaInfo();
                                multimediaInfo.setUuid(info.getUuid());
                                multimediaInfo.setUrl(info.getUrl());
                                questionMultimediaMap.put(i, multimediaInfo);
                            }
                        }
                    }
                }
            }
            
            // 4. Create and save challenge
            var challenge = challengeMapper.toEntity(challengeDTO, challengeMultimediaInfo.stream()
                .map(com.marketingconfort.challenge.models.MultimediaInfo::getUuid)
                .collect(Collectors.toList()));
            challenge.setMultimediaInfo(challengeMultimediaInfo);
            // Set maxScore as sum of all question points
            if (challenge.getQuestions() != null) {
                double maxScore = challenge.getQuestions().stream().mapToDouble(q -> q.getPoints()).sum();
                challenge.setMaxScore(maxScore);
            } else {
                challenge.setMaxScore(0.0);
            }
            
            // Handle ScoreConfiguration
            if (challengeDTO.getScoreConfiguration() != null) {
                ScoreConfiguration config = challengeDTO.getScoreConfiguration();
                
                if (config.getId() != null) {
                    // Try to find existing ScoreConfiguration by ID
                    ScoreConfiguration existingConfig = scoreConfigurationRepository.findById(config.getId()).orElse(null);
                    if (existingConfig != null) {
                        challenge.setScoreConfiguration(existingConfig);
                    } else {
                        // If not found, create new one
                        config.setId(null); // Ensure it's treated as new
                        ScoreConfiguration savedConfig = scoreConfigurationRepository.save(config);
                        challenge.setScoreConfiguration(savedConfig);
                    }
                } else {
                    // Create new ScoreConfiguration
                    ScoreConfiguration savedConfig = scoreConfigurationRepository.save(config);
                    challenge.setScoreConfiguration(savedConfig);
                }
            }
            
            if (challenge.getQuestions() != null) {
                for (int i = 0; i < challenge.getQuestions().size(); i++) {
                    var question = challenge.getQuestions().get(i);
                    question.setChallenge(challenge);
                    
                    // Assign multimedia info to the question if it exists
                    com.marketingconfort.challenge.models.MultimediaInfo multimediaInfo = questionMultimediaMap.get(i);
                    if (multimediaInfo != null) {
                        question.setMultimediaInfo(multimediaInfo);
                    }
                }
            }
            
            var saved = challengeRepository.save(challenge);
            log.info("Challenge created successfully with UUID: {}", saved.getUuid());
            return challengeMapper.toDto(saved);
            
        } catch (ResourceAccessException ex) {
            log.error("Multimedia service is unreachable: {}", ex.getMessage());
            cleanupUploadedFiles(uploadedUuids);
            throw new RuntimeException("The multimedia service is currently unavailable. Please try again later.");
        } catch (Exception ex) {
            log.error("Failed to create challenge: {}", ex.getMessage(), ex);
            cleanupUploadedFiles(uploadedUuids);
            throw new RuntimeException("Failed to create challenge: " + ex.getMessage());
        }
    }
    
    /**
     * Cleanup uploaded files from multimedia service
     * @param uuids List of UUIDs to delete
     */
    private void cleanupUploadedFiles(List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return;
        }
        
        try {
            log.info("Cleaning up {} uploaded files: {}", uuids.size(), uuids);
            
            String action = "permanent-delete";
            validateMultimediaAction(action);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = new HashMap<>();
            body.put("multimediaUuids", uuids);  // Send as List, not Set
            body.put("action", action);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<Void> response = restTemplate.postForEntity(
                "http://localhost:7077/api/multimedia/bulk", 
                request, 
                Void.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully cleaned up {} uploaded files", uuids.size());
            } else {
                log.warn("Failed to cleanup uploaded files. Response status: {}", response.getStatusCode());
            }
            
        } catch (Exception ex) {
            log.error("Failed to cleanup uploaded media files: {}", ex.getMessage(), ex);
            // Don't throw the exception as this is cleanup code
        }
    }

    @Override
    public Page<ChallengeDTO> getChallenges(int page, int size) {
        Page<Challenge> challengePage = challengeRepository.findAll(PageRequest.of(page, size));
        return challengePage.map(challengeMapper::toDto);
    }

    @Override
    public List<ChallengeListDTO> getChallengesList(int page, int size) {
        Page<Challenge> challengePage = challengeRepository.findAll(PageRequest.of(page, size));
        
        return challengePage.getContent().stream()
            .map(this::mapToChallengeListDTO)
            .toList();
    }
    
    private ChallengeListDTO mapToChallengeListDTO(Challenge challenge) {
        ChallengeListDTO dto = new ChallengeListDTO();
        dto.setUuid(challenge.getUuid());
        dto.setNom(challenge.getName());
        dto.setDescription(challenge.getDescription());
        dto.setStatut(challenge.getStatut());
        dto.setDateCreation(LocalDateTime.now()); // Use current time as creation date
        dto.setDatePublication(challenge.getPublicationDate());
        dto.setDateMiseAJour(LocalDateTime.now()); // Use current time as update date
        dto.setDifficulte(challenge.getDifficulty());
        dto.setParticipantsCount(challenge.getParticipantNumber());
        dto.setQuestionsCount(challenge.getQuestions() != null ? challenge.getQuestions().size() : 0);
        dto.setTimer(challenge.getTimer());
        dto.setNbTentatives(challenge.getAttemptCount());
        dto.setIsRandomQuestions(challenge.getRandomQuestions());
        dto.setMessageSucces(challenge.getSuccessMessage());
        dto.setMessageEchec(challenge.getFailureMessage());
        dto.setActive(challenge.getActive());
        
        // Map score configuration
        if (challenge.getScoreConfiguration() != null) {
            ScoreConfigurationDTO scoreConfig = new ScoreConfigurationDTO();
            scoreConfig.setUuid(challenge.getScoreConfiguration().getUuid());
            scoreConfig.setMethode(challenge.getScoreConfiguration().getMethod());
            scoreConfig.setParametres("{\"pointsParBonneReponse\": 10}"); // Default or from config
            dto.setScoreConfiguration(scoreConfig);
        }
        
        // Map niveau (level)
        NiveauDTO niveau = new NiveauDTO();
        niveau.setUuid("1"); // Default or from challenge
        niveau.setNom(challenge.getLevel());
        dto.setNiveau(niveau);
        
        // Map multimedias (empty for now, would need multimedia service integration)
        dto.setMultimedias(new ArrayList<>());
        
        // Map prerequis (null for now, would need prerequisite logic)
        dto.setPrerequis(null);
        
        return dto;
    }

    @Override
    public ChallengeDTO getChallengeByUuid(String uuid) {
        var challenge = challengeRepository.findByUuid(uuid);
        if (challenge == null) throw new RuntimeException("Challenge not found with uuid: " + uuid);
        
        // Load multimedia info for challenge
        if (challenge.getMultimediaInfo() != null) {
            challenge.getMultimediaInfo().size(); // Force load
        }
        
        // Load questions with their multimedia info and answers using separate query
        var questions = challengeRepository.findQuestionsByChallengeUuid(uuid);
        challenge.setQuestions(questions);
        
        return challengeMapper.toDto(challenge);
    }

    @Override
    public ChallengeDTO setChallengeActiveStatus(String uuid, boolean active) {
        var challenge = challengeRepository.findByUuid(uuid);
        if (challenge == null) throw new RuntimeException("Challenge not found with uuid: " + uuid);
        challenge.setActive(active);
        var saved = challengeRepository.save(challenge);
        return challengeMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteChallenge(String uuid) {
        var challenge = challengeRepository.findByUuid(uuid);
        if (challenge == null) throw new RuntimeException("Challenge not found with uuid: " + uuid);
        // Collect all multimedia UUIDs: challenge-level and question-level
        java.util.List<String> allMediaUuids = new java.util.ArrayList<>();
        if (challenge.getMultimediaInfo() != null) {
            allMediaUuids.addAll(challenge.getMultimediaInfo().stream()
                .map(com.marketingconfort.challenge.models.MultimediaInfo::getUuid)
                .collect(Collectors.toList()));
        }
        if (challenge.getQuestions() != null) {
            for (var question : challenge.getQuestions()) {
                if (question.getMultimediaInfo() != null && question.getMultimediaInfo().getUuid() != null) {
                    allMediaUuids.add(question.getMultimediaInfo().getUuid());
                }
            }
        }
        // Send delete request to multimedia service
        if (!allMediaUuids.isEmpty()) {
            try {
                String action = "permanent-delete";
                validateMultimediaAction(action);
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                java.util.Map<String, Object> body = new java.util.HashMap<>();
                body.put("multimediaUuids", allMediaUuids);
                body.put("action", action);
                HttpEntity<java.util.Map<String, Object>> request = new HttpEntity<>(body, headers);
                restTemplate.postForEntity("http://localhost:7077/api/multimedia/bulk", request, Void.class);
            } catch (Exception ex) {
                log.error("Failed to delete challenge/question media: {}", ex.getMessage());
            }
        }
        // Delete the challenge (cascade will handle questions/answers)
        challengeRepository.delete(challenge);
    }

    @Override
    public Page<ChallengeDTO> searchChallenges(ChallengeSearchCriteriaDTO criteria, int page, int size) {
        Page<Challenge> challengePage = searchChallengesEntities(criteria, page, size);
        return challengePage.map(challengeMapper::toDto);
    }

    @Override
    public Page<Challenge> searchChallengesEntities(ChallengeSearchCriteriaDTO criteria, int page, int size) {
        Specification<Challenge> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("name")), "%" + criteria.getName().toLowerCase() + "%"));
            }
            if (criteria.getDescription() != null && !criteria.getDescription().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + criteria.getDescription().toLowerCase() + "%"));
            }
            if (criteria.getStatut() != null && !criteria.getStatut().isEmpty()) {
                predicates.add(cb.equal(root.get("statut"), criteria.getStatut()));
            }
            if (criteria.getDatePublication() != null) {
                predicates.add(cb.equal(root.get("publicationDate"), criteria.getDatePublication()));
            }
            if (criteria.getDateModification() != null) {
                predicates.add(cb.equal(root.get("lastModifiedAt"), criteria.getDateModification()));
            }
            if (criteria.getDifficulte() != null && !criteria.getDifficulte().isEmpty()) {
                predicates.add(cb.equal(root.get("difficulty"), criteria.getDifficulte()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return challengeRepository.findAll(spec, PageRequest.of(page, size));
    }

    @Override
    @Transactional
    public void deleteMultipleChallenges(List<String> uuids) {
        Map<String, String> results = new HashMap<>();
        
        List<Challenge> challenges = challengeRepository.findAllByUuidIn(uuids);
        
        for (Challenge challenge : challenges) {
            try {
                List<String> multimediaUuids = new ArrayList<>();
                if (challenge.getMultimediaInfo() != null) {
                    multimediaUuids = challenge.getMultimediaInfo().stream()
                        .map(com.marketingconfort.challenge.models.MultimediaInfo::getUuid)
                        .collect(Collectors.toList());
                }
                
                if (!multimediaUuids.isEmpty()) {
                    Map<String, Object> body = new HashMap<>();
                    body.put("multimediaUuids", multimediaUuids);
                    body.put("action", "permanent-delete");
                    
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                    
                    restTemplate.postForEntity("http://localhost:7077/api/multimedia/bulk", request, Void.class);
                }
                
                challengeRepository.delete(challenge);
                results.put(challenge.getUuid(), "SUCCESS");
                
            } catch (Exception e) {
                log.error("Failed to delete challenge {}: {}", challenge.getUuid(), e.getMessage());
                results.put(challenge.getUuid(), "FAILED - " + e.getMessage());
            }
        }

        if (results.values().stream().anyMatch(status -> status.startsWith("FAILED"))) {
            String errors = results.entrySet().stream()
                .filter(e -> e.getValue().startsWith("FAILED"))
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));
            throw new RuntimeException("Some deletions failed: " + errors);
        }
    }
    @Override
    public ChallengeDTO participate(String uuid) {
        var challenge = challengeRepository.findByUuid(uuid);
        if (challenge == null) throw new RuntimeException("Challenge not found with uuid: " + uuid);
        challenge.setParticipantNumber(challenge.getParticipantNumber() + 1);
        var saved = challengeRepository.save(challenge);
        return challengeMapper.toDto(saved);
    }

    @Override
    public ChallengeDTO updateChallengeStep1(String uuid, com.marketingconfort.challenge.dto.request.ChallengeUpdateStep1RequestDTO dto, java.util.List<org.springframework.web.multipart.MultipartFile> multimedias) {
        Challenge challenge = challengeRepository.findByUuid(uuid);
        if (challenge == null) throw new RuntimeException("Challenge not found");
        if (dto.getNom() != null) challenge.setName(dto.getNom());
        if (dto.getStatut() != null) challenge.setStatut(com.marketingconfort.challenge.enums.ChallengeStatus.valueOf(dto.getStatut()));
        if (dto.getDescription() != null) challenge.setDescription(dto.getDescription());
        if (dto.getDifficulte() != null) challenge.setDifficulty(com.marketingconfort.challenge.enums.Difficulty.valueOf(dto.getDifficulte()));
        if (dto.getNiveau() != null) challenge.setLevel(dto.getNiveau());
        if (dto.getDatePublication() != null) challenge.setPublicationDate(java.time.LocalDateTime.parse(dto.getDatePublication()));
        // Handle images: delete old, add new
        if (multimedias != null && !multimedias.isEmpty()) {
            // Delete old images (call multimedia service with old UUIDs)
            java.util.List<String> oldUuids = new java.util.ArrayList<>();
            if (challenge.getMultimediaInfo() != null) {
                for (com.marketingconfort.challenge.models.MultimediaInfo info : challenge.getMultimediaInfo()) {
                    if (info.getUuid() != null) oldUuids.add(info.getUuid());
                }
            }
            if (!oldUuids.isEmpty()) {
                try {
                    org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                    headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
                    java.util.Map<String, Object> body = new java.util.HashMap<>();
                    body.put("multimediaUuids", oldUuids);
                    body.put("action", "delete");
                    org.springframework.http.HttpEntity<java.util.Map<String, Object>> request = new org.springframework.http.HttpEntity<>(body, headers);
                    restTemplate.postForEntity("http://localhost:7077/api/multimedia/bulk-operations", request, Void.class);
                } catch (Exception ignore) {}
            }
            // Upload new images
            java.util.List<com.marketingconfort.challenge.models.MultimediaInfo> newInfos = new java.util.ArrayList<>();
            java.util.Map<String, com.marketingconfort.challenge.dto.MultimediaInfo> fileNameToInfo = uploadFilesAndMapByName(multimedias);
            for (org.springframework.web.multipart.MultipartFile file : multimedias) {
                if (file != null && !file.isEmpty()) {
                    String filename = file.getOriginalFilename();
                    com.marketingconfort.challenge.dto.MultimediaInfo info = fileNameToInfo.get(filename);
                    if (info != null) {
                        com.marketingconfort.challenge.models.MultimediaInfo m = new com.marketingconfort.challenge.models.MultimediaInfo();
                        m.setUuid(info.getUuid());
                        m.setUrl(info.getUrl());
                        newInfos.add(m);
                    }
                }
            }
            challenge.setMultimediaInfo(newInfos);
        }
        Challenge saved = challengeRepository.save(challenge);
        return challengeMapper.toDto(saved);
    }

    @Override
    public ChallengeDTO updateChallengeStep2(String uuid, com.marketingconfort.challenge.dto.request.ChallengeUpdateStep2RequestDTO dto) {
        Challenge challenge = challengeRepository.findByUuid(uuid);
        if (challenge == null) throw new RuntimeException("Challenge not found");
        if (dto.getNbTentatives() != null) challenge.setAttemptCount(dto.getNbTentatives());
        if (dto.getRandomQuestions() != null) challenge.setRandomQuestions(dto.getRandomQuestions());
        if (dto.getMethodeDeCalcule() != null) challenge.setCalculationMethod(com.marketingconfort.challenge.enums.CalculationMethod.valueOf(dto.getMethodeDeCalcule()));
        if (dto.getPrerequisUuid() != null) {
            // You may need to fetch and set the prerequisite entity here
            // challenge.setPrerequisite(...)
        }
        if (dto.getMessageSucces() != null) challenge.setSuccessMessage(dto.getMessageSucces());
        if (dto.getMessageEchec() != null) challenge.setFailureMessage(dto.getMessageEchec());
        Challenge saved = challengeRepository.save(challenge);
        return challengeMapper.toDto(saved);
    }

    private Map<String, MultimediaInfo> uploadFilesAndMapByName(List<MultipartFile> files) {
        Map<String, MultimediaInfo> fileNameToMultimediaInfo = new HashMap<>();
        if (files == null || files.isEmpty()) {
            log.info("No files to upload");
            return fileNameToMultimediaInfo;
        }
        
        log.info("Uploading {} files to multimedia service", files.size());
        
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            for (MultipartFile file : files) {
                try {
                    body.add("files", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                        @Override
                        public String getFilename() {
                            return file.getOriginalFilename();
                        }
                    });
                    log.debug("Added file to upload: {}", file.getOriginalFilename());
                } catch (java.io.IOException e) {
                    log.error("Failed to read file bytes for: {}", file.getOriginalFilename(), e);
                    throw new RuntimeException("Failed to read file: " + file.getOriginalFilename(), e);
                }
            }
            body.add("service", "challenge");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            log.debug("Sending upload request to multimedia service");
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.postForEntity(
                    "http://localhost:7077/api/multimedia/upload-multiple",
                    requestEntity,
                    (Class<List<Map<String, Object>>>) (Class<?>) List.class
            );
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> responseBody = response.getBody() != null ? (List<Map<String, Object>>) response.getBody() : null;
            
            if (responseBody != null) {
                for (Object obj : responseBody) {
                    if (obj instanceof Map map && map.get("uuid") != null && map.get("name") != null) {
                        String filename = map.get("name").toString();
                        String uuid = map.get("uuid").toString();
                        String url = map.get("url") != null ? map.get("url").toString() : null;
                        fileNameToMultimediaInfo.put(filename, new MultimediaInfo(uuid, url));
                        log.debug("File uploaded successfully: {} -> UUID: {}, URL: {}", filename, uuid, url);
                    }
                }
                log.info("Successfully uploaded {} files", fileNameToMultimediaInfo.size());
            } else {
                log.warn("Upload response body is null");
            }
            
        } catch (ResourceAccessException ex) {
            log.error("Multimedia service is unreachable during file upload: {}", ex.getMessage());
            throw new RuntimeException("Multimedia service is unavailable. Cannot upload files.", ex);
        } catch (Exception ex) {
            log.error("Failed to upload files to multimedia service: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to upload files: " + ex.getMessage(), ex);
        }
        
        return fileNameToMultimediaInfo;
    }
    
    /**
     * Validate challenge data including enum values
     */
    private void validateChallengeData(ChallengeCreateRequestDTO challengeDTO) {
        // Validate challenge status
        if (challengeDTO.getStatut() != null) {
            try {
                ChallengeStatus.valueOf(challengeDTO.getStatut());
            } catch (IllegalArgumentException e) {
                throw new ChallengeValidationException(
                    "Invalid challenge status: " + challengeDTO.getStatut() + ". Valid values are: " + 
                    java.util.Arrays.toString(ChallengeStatus.values()),
                    "INVALID_CHALLENGE_STATUS",
                    "statut"
                );
            }
        }
        
        // Validate difficulty
        if (challengeDTO.getDifficulte() != null) {
            try {
                Difficulty.valueOf(challengeDTO.getDifficulte());
            } catch (IllegalArgumentException e) {
                throw new ChallengeValidationException(
                    "Invalid difficulty: " + challengeDTO.getDifficulte() + ". Valid values are: " + 
                    java.util.Arrays.toString(Difficulty.values()),
                    "INVALID_DIFFICULTY",
                    "difficulte"
                );
            }
        }
        
        // Validate calculation method
        if (challengeDTO.getMethodeDeCalcule() != null) {
            try {
                CalculationMethod.valueOf(challengeDTO.getMethodeDeCalcule());
            } catch (IllegalArgumentException e) {
                throw new ChallengeValidationException(
                    "Invalid calculation method: " + challengeDTO.getMethodeDeCalcule() + ". Valid values are: " + 
                    java.util.Arrays.toString(CalculationMethod.values()),
                    "INVALID_CALCULATION_METHOD",
                    "methodeDeCalcule"
                );
            }
        }
        
        // Validate questions
        if (challengeDTO.getQuestions() != null) {
            for (int i = 0; i < challengeDTO.getQuestions().size(); i++) {
                var question = challengeDTO.getQuestions().get(i);
                if (question.getType() != null) {
                    try {
                        QuestionTypeChallenge.valueOf(question.getType());
                    } catch (IllegalArgumentException e) {
                        throw new ChallengeValidationException(
                            "Invalid question type for question " + (i + 1) + ": " + question.getType() + 
                            ". Valid values are: " + java.util.Arrays.toString(QuestionTypeChallenge.values()),
                            "INVALID_QUESTION_TYPE",
                            "questions[" + i + "].type"
                        );
                    }
                }
            }
        }
    }
    
    /**
     * Validate multimedia action
     */
    private void validateMultimediaAction(String action) {
        String[] validActions = {"delete", "permanent-delete", "activate", "deactivate", "move"};
        boolean isValid = false;
        for (String validAction : validActions) {
            if (validAction.equals(action)) {
                isValid = true;
                break;
            }
        }
        
        if (!isValid) {
            throw new ChallengeValidationException(
                "Invalid multimedia action: " + action + ". Valid actions are: " + 
                java.util.Arrays.toString(validActions),
                "INVALID_MULTIMEDIA_ACTION",
                "action"
            );
        }
    }
}

