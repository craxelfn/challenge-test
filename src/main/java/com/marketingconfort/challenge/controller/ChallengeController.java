package com.marketingconfort.challenge.controller;

import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.marketingconfort.challenge.dto.ChallengeDTO;
import com.marketingconfort.challenge.dto.ChallengeListDTO;
import com.marketingconfort.challenge.dto.ChallengeSimpleDTO;
import com.marketingconfort.challenge.dto.ScoreConfigurationSimpleDTO;
import com.marketingconfort.challenge.dto.request.ChallengeCreateRequestDTO;
import com.marketingconfort.challenge.dto.request.ChallengeSearchCriteriaDTO;
import com.marketingconfort.challenge.models.Challenge;
import com.marketingconfort.challenge.repository.ChallengeRepository;
import com.marketingconfort.challenge.service.ChallengeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges")
public class ChallengeController {
    private final ChallengeService challengeService;
    private final ChallengeRepository challengeRepository;
    

    @PostMapping("/challenges")
    public ResponseEntity<ChallengeDTO> createChallenge(
        @RequestPart("challenge") ChallengeCreateRequestDTO challengeDTO,
        @RequestPart(value = "multimedias", required = false) List<MultipartFile> multimedias
    ) {
        System.out.println("[DEBUG] Received files: " + (multimedias != null ? multimedias.size() : 0));
        if (multimedias != null) {
            for (MultipartFile file : multimedias) {
                if (file != null && file.getOriginalFilename() != null) {
                    String filename = file.getOriginalFilename();
                    System.out.println("[DEBUG] File name: " + filename);
                }
            }
        }
        challengeDTO.setMultimedias(multimedias);
        if (challengeDTO.getQuestions() != null && multimedias != null) {
            for (int i = 0; i < challengeDTO.getQuestions().size(); i++) {
                for (MultipartFile file : multimedias) {
                    String filename = file.getOriginalFilename();
                    if (filename != null && filename.startsWith("question" + i + "_")) {
                        challengeDTO.getQuestions().get(i).setMultimedia(file);
                    }
                }
            }
        }
        ChallengeDTO created = challengeService.createChallenge(challengeDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<ChallengeSimpleDTO>> getChallenges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // Get paginated data
        Page<Challenge> challengePage = challengeRepository.findAll(PageRequest.of(page, size));
        List<ChallengeSimpleDTO> challenges = challengePage.getContent().stream()
            .map(this::mapToChallengeSimpleDTO)
            .toList();
        
        // Return with pagination headers
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(challengePage.getTotalElements()))
            .header("X-Page", String.valueOf(page + 1))
            .header("X-Limit", String.valueOf(size))
            .header("X-Total-Pages", String.valueOf(challengePage.getTotalPages()))
            .body(challenges);
    }

    @GetMapping("/list")
    public ResponseEntity<List<ChallengeListDTO>> getChallengesList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // Get paginated data
        Page<Challenge> challengePage = challengeRepository.findAll(PageRequest.of(page, size));
        List<ChallengeListDTO> challenges = challengeService.getChallengesList(page, size);
        
        // Return with pagination headers
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(challengePage.getTotalElements()))
            .header("X-Page", String.valueOf(page + 1))
            .header("X-Limit", String.valueOf(size))
            .header("X-Total-Pages", String.valueOf(challengePage.getTotalPages()))
            .body(challenges);
    }

    /**
     * Get a challenge by UUID with all details including:
     * - Challenge information (name, description, status, etc.)
     * - Multimedia information (UUID, URL, type) for challenge-level files
     * - Questions with their multimedia information
     * - Answers for each question
     * - Score configuration and other settings
     * 
     * @param uuid The UUID of the challenge
     * @return ChallengeDTO with complete challenge details
     */
    @GetMapping("/{uuid}")
    public ResponseEntity<ChallengeDTO> getChallengeById(@PathVariable String uuid) {
        ChallengeDTO dto = challengeService.getChallengeByUuid(uuid);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{uuid}/activate")
    public ResponseEntity<ChallengeDTO> activateChallenge(@PathVariable String uuid) {
        ChallengeDTO dto = challengeService.setChallengeActiveStatus(uuid, true);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{uuid}/deactivate")
    public ResponseEntity<ChallengeDTO> deactivateChallenge(@PathVariable String uuid) {
        ChallengeDTO dto = challengeService.setChallengeActiveStatus(uuid, false);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/search")
    public ResponseEntity<List<ChallengeSimpleDTO>> searchChallenges(
            @RequestBody ChallengeSearchCriteriaDTO criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Challenge> challengePage = challengeService.searchChallengesEntities(criteria, page, size);
        List<ChallengeSimpleDTO> challenges = challengePage.getContent().stream()
            .map(this::mapToChallengeSimpleDTO)
            .toList();
        
        // Return with pagination headers
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(challengePage.getTotalElements()))
            .header("X-Page", String.valueOf(page + 1))
            .header("X-Limit", String.valueOf(size))
            .header("X-Total-Pages", String.valueOf(challengePage.getTotalPages()))
            .body(challenges);
    }

    @PostMapping("/{uuid}/participate")
    public ResponseEntity<ChallengeDTO> participate(@PathVariable String uuid) {
        ChallengeDTO dto = challengeService.participate(uuid);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteChallenge(@PathVariable String uuid) {
        challengeService.deleteChallenge(uuid);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/delete-multiple")
    public ResponseEntity<Void> deleteMultipleChallenges(@RequestBody List<String> challengeUuids) {
        challengeService.deleteMultipleChallenges(challengeUuids);
        return ResponseEntity.noContent().build();
    }
    
    private ChallengeSimpleDTO mapToChallengeSimpleDTO(Challenge challenge) {
        ChallengeSimpleDTO dto = new ChallengeSimpleDTO();
        dto.setUuid(challenge.getUuid());
        dto.setNom(challenge.getName());
        dto.setDescription(challenge.getDescription());
        dto.setStatut(challenge.getStatut());
        dto.setNiveau(challenge.getLevel());
        dto.setDifficulte(challenge.getDifficulty());
        dto.setDatePublication(challenge.getPublicationDate());
        dto.setTimer(challenge.getTimer());
        dto.setNbTentatives(challenge.getAttemptCount());
        dto.setMessageSucces(challenge.getSuccessMessage());
        dto.setMessageEchec(challenge.getFailureMessage());
        dto.setQuestionsCount(challenge.getQuestions() != null ? challenge.getQuestions().size() : 0);
        dto.setParticipantsCount(challenge.getParticipantNumber());
        dto.setDateCreation(java.time.LocalDateTime.now());
        dto.setDateMiseAJour(java.time.LocalDateTime.now());
        dto.setActive(challenge.getActive() != null ? challenge.getActive() : false);
        dto.setCalculationMethod(challenge.getCalculationMethod());
        dto.setPenaltyPerError(challenge.getPenaltyPerError());
        dto.setTimeBonus(challenge.getTimeBonus());
        dto.setMinPassingScore(challenge.getMinPassingScore());
        dto.setRandomQuestions(challenge.getRandomQuestions() != null ? challenge.getRandomQuestions() : false);
        // Map score configuration with null check
        dto.setScoreConfiguration(null);
        if (challenge.getScoreConfiguration() != null) {
            ScoreConfigurationSimpleDTO scoreConfig = new ScoreConfigurationSimpleDTO();
            scoreConfig.setMethod(challenge.getScoreConfiguration().getMethod());
            scoreConfig.setParameters(challenge.getScoreConfiguration().getParameters());
            dto.setScoreConfiguration(scoreConfig);
        }
        return dto;
    }
}
