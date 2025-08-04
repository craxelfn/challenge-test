package com.marketingconfort.challenge.service.impl;

import com.marketingconfort.challenge.dto.QuestionDTO;
import com.marketingconfort.challenge.dto.request.AnswerUpdateRequestDTO;
import com.marketingconfort.challenge.models.Answer;
import com.marketingconfort.challenge.models.Question;
import com.marketingconfort.challenge.repository.QuestionRepository;
import com.marketingconfort.challenge.service.AnswerService;
import com.marketingconfort.challenge.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    @Override
    @Transactional
    public QuestionDTO updateMultipleAnswers(String questionUuid, List<AnswerUpdateRequestDTO> answers) {
        Question question = questionRepository.findByUuid(questionUuid);
        if (question == null) {
            throw new RuntimeException("Question not found with uuid: " + questionUuid);
        }
        Map<String, Answer> answerMap = new HashMap<>();
        if (question.getAnswers() != null) {
            for (Answer a : question.getAnswers()) {
                answerMap.put(a.getUuid(), a);
            }
        }
        for (AnswerUpdateRequestDTO dto : answers) {
            Answer answer = answerMap.get(dto.getUuid());
            if (answer == null) {
                throw new RuntimeException("Answer with uuid " + dto.getUuid() + " does not belong to question " + questionUuid);
            }
            answer.setTexte(dto.getTexte());
            answer.setIsCorrect(dto.isEstCorrecte());
        }
        questionRepository.save(question);
        return questionMapper.toDto(question);
    }
} 