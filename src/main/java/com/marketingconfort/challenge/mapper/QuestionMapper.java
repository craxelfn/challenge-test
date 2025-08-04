package com.marketingconfort.challenge.mapper;

import com.marketingconfort.challenge.dto.QuestionDTO;
import com.marketingconfort.challenge.dto.request.QuestionCreateRequestDTO;
import com.marketingconfort.challenge.models.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionMapper {
    @Mapping(source = "texte", target = "title")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "ordre", target = "ordre")
    @Mapping(source = "points", target = "points")
    @Mapping(source = "duree", target = "duree")
    @Mapping(source = "required", target = "isRequired")
    @Mapping(target = "multimediaInfo", ignore = true) // handled separately in service
    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "challenge", ignore = true)
    @Mapping(target = "content", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reponseAttendue", ignore = true)
    Question toEntity(QuestionCreateRequestDTO dto);
    
    @Mapping(source = "uuid", target = "uuid")
    @Mapping(source = "multimediaInfo", target = "multimedia")
    QuestionDTO toDto(Question entity);
    
    List<QuestionDTO> toDtoList(List<Question> entities);
} 