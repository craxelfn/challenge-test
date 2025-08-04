package com.marketingconfort.challenge.mapper;

import java.util.List;
import java.util.ArrayList;

import com.marketingconfort.challenge.dto.ChallengeDTO;
import com.marketingconfort.challenge.dto.request.ChallengeCreateRequestDTO;
import com.marketingconfort.challenge.models.Challenge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ChallengeMapper {
    @Mapping(source = "dto.nom", target = "name")
    @Mapping(source = "dto.difficulte", target = "difficulty")
    @Mapping(source = "dto.nbTentatives", target = "attemptCount")
    @Mapping(source = "dto.statut", target = "statut")
    @Mapping(source = "dto.timer", target = "timer")
    @Mapping(source = "dto.messageSucces", target = "successMessage")
    @Mapping(source = "dto.messageEchec", target = "failureMessage")
    @Mapping(source = "dto.scoreConfiguration", target = "scoreConfiguration")
    @Mapping(source = "dto.questions", target = "questions", qualifiedByName = "mapQuestions")
    @Mapping(source = "dto.description", target = "description")
    @Mapping(source = "dto.niveau", target = "level", qualifiedByName = "stringToLevel")
    @Mapping(source = "dto.datePublication", target = "publicationDate", qualifiedByName = "stringToLocalDateTime")
    @Mapping(source = "dto.methodeDeCalcule", target = "calculationMethod", qualifiedByName = "stringToCalculationMethod")
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "minPassingScore", ignore = true)
    @Mapping(target = "penaltyPerError", ignore = true)
    @Mapping(target = "prerequisite", ignore = true)
    @Mapping(target = "randomQuestions", ignore = true)
    @Mapping(target = "timeBonus", ignore = true)
    @Mapping(target = "multimediaInfo", ignore = true) // handled separately in service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "participantNumber", ignore = true)
    @Mapping(target = "trophees", ignore = true)
    Challenge toEntity(ChallengeCreateRequestDTO dto, List<String> multimediaUuids);
    
    @Mapping(source = "uuid", target = "uuid")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "statut", target = "statut")
    @Mapping(source = "calculationMethod", target = "calculationMethod")
    @Mapping(source = "penaltyPerError", target = "penaltyPerError")
    @Mapping(source = "timeBonus", target = "timeBonus")
    @Mapping(source = "difficulty", target = "difficulty")
    @Mapping(source = "timer", target = "timer")
    @Mapping(source = "attemptCount", target = "attemptCount")
    @Mapping(source = "publicationDate", target = "publicationDate")
    @Mapping(source = "successMessage", target = "successMessage")
    @Mapping(source = "minPassingScore", target = "minPassingScore")
    @Mapping(source = "failureMessage", target = "failureMessage")
    @Mapping(source = "randomQuestions", target = "randomQuestions")
    @Mapping(source = "active", target = "active")
    @Mapping(source = "prerequisite", target = "prerequisite")
    @Mapping(source = "level", target = "level")
    @Mapping(source = "scoreConfiguration", target = "scoreConfiguration")
    @Mapping(source = "multimediaInfo", target = "multimedia", qualifiedByName = "mapMultimediaInfoToDto")
    @Mapping(source = "questions", target = "questions", qualifiedByName = "mapQuestionsToDto")
    @Mapping(source = "description", target = "description")
    ChallengeDTO toDto(Challenge challenge);

    @Named("mapQuestions")
    default List<com.marketingconfort.challenge.models.Question> mapQuestions(List<com.marketingconfort.challenge.dto.request.QuestionCreateRequestDTO> dtos) {
        if (dtos == null) return null;
        List<com.marketingconfort.challenge.models.Question> entities = new ArrayList<>();
        for (com.marketingconfort.challenge.dto.request.QuestionCreateRequestDTO dto : dtos) {
            com.marketingconfort.challenge.models.Question entity = new com.marketingconfort.challenge.models.Question();
            entity.setTitle(dto.getTexte());
            entity.setType(dto.getType() != null ? com.marketingconfort.challenge.enums.QuestionTypeChallenge.valueOf(dto.getType()) : null);
            entity.setOrdre(dto.getOrdre());
            entity.setPoints(dto.getPoints());
            entity.setDuree(dto.getDuree());
            entity.setIsRequired(dto.isRequired());
            // multimediaInfo will be set separately in service
            // Map answers
            if (dto.getReponses() != null) {
                List<com.marketingconfort.challenge.models.Answer> answers = new ArrayList<>();
                for (com.marketingconfort.challenge.dto.request.AnswerCreateRequestDTO answerDto : dto.getReponses()) {
                    com.marketingconfort.challenge.models.Answer answer = new com.marketingconfort.challenge.models.Answer();
                    answer.setTexte(answerDto.getTexte());
                    answer.setIsCorrect(answerDto.isEstCorrecte());
                    answer.setQuestion(entity); // Set parent question
                    answers.add(answer);
                }
                entity.setAnswers(answers);
            }
            entities.add(entity);
        }
        return entities;
    }

    @Named("mapMultimediaInfoToDto")
    default List<com.marketingconfort.challenge.dto.MultimediaDTO> mapMultimediaInfoToDto(List<com.marketingconfort.challenge.models.MultimediaInfo> multimediaInfo) {
        if (multimediaInfo == null) return null;
        List<com.marketingconfort.challenge.dto.MultimediaDTO> dtos = new ArrayList<>();
        for (com.marketingconfort.challenge.models.MultimediaInfo info : multimediaInfo) {
            com.marketingconfort.challenge.dto.MultimediaDTO dto = new com.marketingconfort.challenge.dto.MultimediaDTO();
            dto.setUuid(info.getUuid());
            dto.setUrl(info.getUrl());
            dto.setType("image"); // Default type, can be enhanced later
            dtos.add(dto);
        }
        return dtos;
    }

    @Named("stringToLocalDateTime")
    default java.time.LocalDateTime stringToLocalDateTime(String date) {
        if (date == null) return java.time.LocalDateTime.now(); // Default to current date/time
        return java.time.LocalDateTime.parse(date);
    }

    @Named("stringToCalculationMethod")
    default com.marketingconfort.challenge.enums.CalculationMethod stringToCalculationMethod(String method) {
        if (method == null) return com.marketingconfort.challenge.enums.CalculationMethod.SUM_OF_POINTS; // Default value
        return com.marketingconfort.challenge.enums.CalculationMethod.valueOf(method);
    }

    @Named("stringToLevel")
    default String stringToLevel(String level) {
        if (level == null) return "BEGINNER"; // Default value
        return level;
    }

    @Named("mapQuestionsToDto")
    default List<com.marketingconfort.challenge.dto.QuestionDTO> mapQuestionsToDto(List<com.marketingconfort.challenge.models.Question> questions) {
        if (questions == null) return null;
        List<com.marketingconfort.challenge.dto.QuestionDTO> dtos = new ArrayList<>();
        for (com.marketingconfort.challenge.models.Question question : questions) {
            com.marketingconfort.challenge.dto.QuestionDTO dto = new com.marketingconfort.challenge.dto.QuestionDTO();
            dto.setUuid(question.getUuid());
            dto.setTitle(question.getTitle());
            dto.setContent(question.getContent());
            dto.setType(question.getType());
            dto.setOrdre(question.getOrdre());
            dto.setPoints(question.getPoints());
            dto.setDuree(question.getDuree());
            dto.setIsRequired(question.getIsRequired());
            dto.setReponseAttendue(question.getReponseAttendue());
            
            // Map multimedia info
            if (question.getMultimediaInfo() != null) {
                com.marketingconfort.challenge.dto.MultimediaDTO multimediaDto = new com.marketingconfort.challenge.dto.MultimediaDTO();
                multimediaDto.setUuid(question.getMultimediaInfo().getUuid());
                multimediaDto.setUrl(question.getMultimediaInfo().getUrl());
                multimediaDto.setType("image"); // Default type
                dto.setMultimedia(multimediaDto);
            }
            
            // Map answers
            if (question.getAnswers() != null) {
                List<com.marketingconfort.challenge.dto.AnswerDTO> answerDtos = new ArrayList<>();
                for (com.marketingconfort.challenge.models.Answer answer : question.getAnswers()) {
                    com.marketingconfort.challenge.dto.AnswerDTO answerDto = new com.marketingconfort.challenge.dto.AnswerDTO();
                    answerDto.setUuid(answer.getUuid());
                    answerDto.setTexte(answer.getTexte());
                    answerDto.setIsCorrect(answer.getIsCorrect());
                    answerDtos.add(answerDto);
                }
                dto.setAnswers(answerDtos);
            }
            dtos.add(dto);
        }
        return dtos;
    }
}
