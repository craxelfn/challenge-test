package com.marketingconfort.challenge.service.impl;

import com.marketingconfort.challenge.dto.ChallengeDTO;
import com.marketingconfort.challenge.dto.MultimediaInfo;
import com.marketingconfort.challenge.dto.request.QuestionCreateRequestDTO;
import com.marketingconfort.challenge.mapper.ChallengeMapper;
import com.marketingconfort.challenge.mapper.QuestionMapper;
import com.marketingconfort.challenge.models.Challenge;
import com.marketingconfort.challenge.models.Question;
import com.marketingconfort.challenge.repository.ChallengeRepository;
import com.marketingconfort.challenge.repository.QuestionRepository;
import com.marketingconfort.challenge.service.QuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.*;
import org.springframework.http.ResponseEntity;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl implements QuestionService {
    private final ChallengeRepository challengeRepository;
    private final QuestionRepository questionRepository;
    private final ChallengeMapper challengeMapper;
    private final QuestionMapper questionMapper;
    @Autowired
    private RestTemplate restTemplate;

    @Override
    @Transactional
    public ChallengeDTO addQuestionsToChallenge(String challengeUuid, List<QuestionCreateRequestDTO> questions, List<MultipartFile> multimedias) {
        Challenge challenge = challengeRepository.findByUuid(challengeUuid);
        if (challenge == null) throw new RuntimeException("Challenge not found with uuid: " + challengeUuid);
        List<Question> newQuestions = new ArrayList<>();
        Map<String, MultimediaInfo> fileNameToMultimediaInfo = new HashMap<>();
        List<MultipartFile> filesToUpload = new ArrayList<>();
        if (multimedias != null) {
            for (MultipartFile file : multimedias) {
                if (file != null && !file.isEmpty()) {
                    String filename = file.getOriginalFilename();
                    if (filename != null) {
                        filesToUpload.add(file);
                    }
                }
            }
        }
        List<String> uploadedUuids = new ArrayList<>();
        try {
            if (!filesToUpload.isEmpty()) {
                fileNameToMultimediaInfo = uploadFilesAndMapByName(filesToUpload);
                uploadedUuids.addAll(fileNameToMultimediaInfo.values().stream().map(MultimediaInfo::getUuid).collect(java.util.stream.Collectors.toList()));
            }
            for (int i = 0; i < questions.size(); i++) {
                QuestionCreateRequestDTO dto = questions.get(i);
                MultipartFile file = null;
                if (multimedias != null) {
                    for (MultipartFile f : multimedias) {
                        String filename = f.getOriginalFilename();
                        if (filename != null && filename.startsWith("question" + i + "_")) {
                            file = f;
                            break;
                        }
                    }
                }
                String multimediaUuid = null;
                String multimediaUrl = null;
                String filename = file != null ? file.getOriginalFilename() : null;
                if (filename != null) {
                    if (fileNameToMultimediaInfo.containsKey(filename)) {
                        MultimediaInfo info = fileNameToMultimediaInfo.get(filename);
                        multimediaUuid = info.getUuid();
                        multimediaUrl = info.getUrl();
                    }
                }
                Question question = questionMapper.toEntity(dto);
                if (multimediaUuid != null) {
                    com.marketingconfort.challenge.models.MultimediaInfo multimediaInfo = 
                        new com.marketingconfort.challenge.models.MultimediaInfo();
                    multimediaInfo.setUuid(multimediaUuid);
                    multimediaInfo.setUrl(multimediaUrl);
                    question.setMultimediaInfo(multimediaInfo);
                }
                question.setChallenge(challenge);
                // Map answers if present
                if (dto.getReponses() != null) {
                    List<com.marketingconfort.challenge.models.Answer> answers = new ArrayList<>();
                    for (com.marketingconfort.challenge.dto.request.AnswerCreateRequestDTO answerDto : dto.getReponses()) {
                        com.marketingconfort.challenge.models.Answer answer = new com.marketingconfort.challenge.models.Answer();
                        answer.setTexte(answerDto.getTexte());
                        answer.setIsCorrect(answerDto.isEstCorrecte());
                        answer.setQuestion(question);
                        answers.add(answer);
                    }
                    question.setAnswers(answers);
                }
                newQuestions.add(question);
            }
            // Save all new questions
            questionRepository.saveAll(newQuestions);
            // Add to challenge's question list
            if (challenge.getQuestions() == null) {
                challenge.setQuestions(new ArrayList<>());
            }
            challenge.getQuestions().addAll(newQuestions);
            challengeRepository.save(challenge);
            return challengeMapper.toDto(challenge);
        } catch (Exception ex) {
            if (!uploadedUuids.isEmpty()) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    Map<String, Object> body = new HashMap<>();
                    body.put("multimediaUuids", uploadedUuids);
                    body.put("action", "delete");
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                    restTemplate.postForEntity("http://localhost:7077/api/multimedia/bulk-operations", request, Void.class);
                } catch (Exception rollbackEx) {
                    log.error("Failed to rollback uploaded media: {}", rollbackEx.getMessage());
                }
            }
            throw ex;
        }
    }

    @Override
    public void deleteMultipleQuestions(String challengeUuid, List<String> questionUuids) {
        Challenge challenge = challengeRepository.findByUuid(challengeUuid);
        if (challenge == null) throw new RuntimeException("Challenge not found with uuid: " + challengeUuid);
        List<Question> questionsToDelete = new ArrayList<>();
        for (String questionUuid : questionUuids) {
            Question q = questionRepository.findByUuid(questionUuid);
            if (q == null) throw new RuntimeException("Question not found with uuid: " + questionUuid);
            if (q.getChallenge() == null || !q.getChallenge().getUuid().equals(challengeUuid)) {
                throw new RuntimeException("Question with uuid " + q.getUuid() + " does not belong to challenge " + challengeUuid);
            }
            questionsToDelete.add(q);
        }
        List<String> multimediaUuids = new ArrayList<>();
        for (Question q : questionsToDelete) {
            if (q.getMultimediaInfo() != null && q.getMultimediaInfo().getUuid() != null) {
                multimediaUuids.add(q.getMultimediaInfo().getUuid());
            }
        }
        questionRepository.deleteAll(questionsToDelete);
        if (!multimediaUuids.isEmpty()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                java.util.Map<String, Object> body = new java.util.HashMap<>();
                body.put("multimediaUuids", multimediaUuids);
                body.put("action", "delete");
                HttpEntity<java.util.Map<String, Object>> request = new HttpEntity<>(body, headers);
                restTemplate.postForEntity("http://localhost:7077/api/multimedia/bulk-operations", request, Void.class);
            } catch (Exception ex) {
                // Rollback: restore deleted questions
                questionRepository.saveAll(questionsToDelete);
                throw new RuntimeException("Failed to delete question multimedia, rollback questions. Error: " + ex.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public ChallengeDTO updateMultipleQuestions(String challengeUuid, List<com.marketingconfort.challenge.dto.request.QuestionUpdateRequestDTO> questions, List<org.springframework.web.multipart.MultipartFile> multimedias) {
        List<String> newUploadedUuids = new ArrayList<>();
        List<String> oldMultimediaUuidsToDelete = new ArrayList<>();
        List<Question> updatedQuestions = new ArrayList<>();
        Map<String, MultimediaInfo> fileNameToMultimediaInfo = new HashMap<>();
        
        try {
            // 1. Upload all new files
            if (!multimedias.isEmpty()) {
                fileNameToMultimediaInfo = uploadFilesAndMapByName(multimedias);
                newUploadedUuids.addAll(fileNameToMultimediaInfo.values().stream().map(MultimediaInfo::getUuid).collect(java.util.stream.Collectors.toList()));
            }
            // 2. For each question update
            for (com.marketingconfort.challenge.dto.request.QuestionUpdateRequestDTO dto : questions) {
                Question q = questionRepository.findByUuid(dto.getUuid());
                if (q == null) throw new RuntimeException("Question not found with uuid: " + dto.getUuid());
                if (q.getChallenge() == null || !q.getChallenge().getUuid().equals(challengeUuid)) {
                    throw new RuntimeException("Question with uuid " + q.getUuid() + " does not belong to challenge " + challengeUuid);
                }
                // If new multimedia, store old uuid and set new
                if (dto.getMultimedia() != null && dto.getMultimedia().getOriginalFilename() != null && 
                    fileNameToMultimediaInfo.containsKey(dto.getMultimedia().getOriginalFilename())) {
                    if (q.getMultimediaInfo() != null && q.getMultimediaInfo().getUuid() != null) {
                        oldMultimediaUuidsToDelete.add(q.getMultimediaInfo().getUuid());
                    }
                    MultimediaInfo info = fileNameToMultimediaInfo.get(dto.getMultimedia().getOriginalFilename());
                    com.marketingconfort.challenge.models.MultimediaInfo multimediaInfo = 
                        new com.marketingconfort.challenge.models.MultimediaInfo();
                    multimediaInfo.setUuid(info.getUuid());
                    multimediaInfo.setUrl(info.getUrl());
                    q.setMultimediaInfo(multimediaInfo);
                }
                // Update other fields
                q.setTitle(dto.getTexte());
                q.setType(dto.getType() != null ? com.marketingconfort.challenge.enums.QuestionTypeChallenge.valueOf(dto.getType()) : null);
                q.setOrdre(dto.getOrdre());
                q.setPoints(dto.getPoints());
                q.setDuree(dto.getDuree());
                q.setIsRequired(dto.isRequired());
                // Note: QuestionUpdateRequestDTO doesn't include answers, so we don't update them
                updatedQuestions.add(q);
            }
            // 3. Save all updated questions
            questionRepository.saveAll(updatedQuestions);
            // 4. Delete old multimedia in one request
            if (!oldMultimediaUuidsToDelete.isEmpty()) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    Map<String, Object> body = new HashMap<>();
                    body.put("multimediaUuids", oldMultimediaUuidsToDelete);
                    body.put("action", "delete");
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                    restTemplate.postForEntity("http://localhost:7077/api/multimedia/bulk-operations", request, Void.class);
                } catch (Exception ex) {
                    // Optionally log but do not rollback DB changes
                    log.error("Failed to delete old multimedia: {}", ex.getMessage());
                }
            }
            return challengeMapper.toDto(challengeRepository.findByUuid(challengeUuid));
        } catch (Exception ex) {
            // Rollback: delete all newly uploaded media
            if (!newUploadedUuids.isEmpty()) {
                try {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    Map<String, Object> body = new HashMap<>();
                    body.put("multimediaUuids", newUploadedUuids);
                    body.put("action", "delete");
                    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
                    restTemplate.postForEntity("http://localhost:7077/api/multimedia/bulk-operations", request, Void.class);
                } catch (Exception rollbackEx) {
                    log.error("Failed to rollback uploaded media: {}", rollbackEx.getMessage());
                }
            }
            throw ex;
        }
    }

    private Map<String, MultimediaInfo> uploadFilesAndMapByName(List<MultipartFile> files) {
        Map<String, MultimediaInfo> fileNameToMultimediaInfo = new HashMap<>();
        if (files == null || files.isEmpty()) return fileNameToMultimediaInfo;
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (MultipartFile file : files) {
            try {
                body.add("files", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                });
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to read file bytes", e);
            }
        }
        body.add("service", "challenge");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Object> response = restTemplate.postForEntity(
                "http://localhost:7077/api/multimedia/upload-multiple",
                requestEntity,
                Object.class
        );
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> responseBody = response.getBody() instanceof List ? (List<Map<String, Object>>) response.getBody() : null;
        if (responseBody != null) {
            for (Map<String, Object> map : responseBody) {
                if (map.get("uuid") != null && map.get("name") != null) {
                    String filename = map.get("name").toString();
                    String uuid = map.get("uuid").toString();
                    String url = map.get("url") != null ? map.get("url").toString() : null;
                    fileNameToMultimediaInfo.put(filename, new MultimediaInfo(uuid, url));
                }
            }
        }
        return fileNameToMultimediaInfo;
    }
} 