package com.marketingconfort.challenge.mapper;

import com.marketingconfort.challenge.dto.TropheeDTO;
import com.marketingconfort.challenge.dto.ChallengeSimpleDTO;
import com.marketingconfort.challenge.dto.ChallengeShortDTO;
import com.marketingconfort.challenge.dto.MultimediaDTO;
import com.marketingconfort.challenge.models.Trophee;
import com.marketingconfort.challenge.models.Challenge;
import com.marketingconfort.challenge.models.MultimediaInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TropheeMapper {
    @Mapping(target = "challengeUuids", expression = "java(toChallengeShortDTOs(entity.getChallenges()))")
    @Mapping(source = "uuid", target = "uuid")
    @Mapping(target = "icone", expression = "java(toMultimediaDTO(entity.getIconeMultimediaInfo()))")
    TropheeDTO toDto(Trophee entity);

    @Mapping(target = "challenges", expression = "java(toChallengesFromShortDTOs(dto.getChallengeUuids()))")
    @Mapping(source = "uuid", target = "uuid")
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(target = "iconeMultimediaInfo", ignore = true) // handled separately in service
    @Mapping(target = "id", ignore = true)
    Trophee toEntity(TropheeDTO dto);

    default List<ChallengeSimpleDTO> toChallengeSimpleDTOs(List<Challenge> challenges) {
        if (challenges == null) return null;
        return challenges.stream().map(challenge -> {
            ChallengeSimpleDTO dto = new ChallengeSimpleDTO();
            dto.setUuid(challenge.getUuid());
            dto.setNom(challenge.getName());
            return dto;
        }).collect(Collectors.toList());
    }

    default List<Challenge> toChallenges(List<String> challengeUuids) {
        if (challengeUuids == null) return null;
        return challengeUuids.stream().map(uuid -> {
            Challenge c = new Challenge();
            c.setUuid(uuid);
            return c;
        }).collect(Collectors.toList());
    }

    // Distinctly named method to avoid type-erasure clash with toChallenges(List<String>)
    default List<Challenge> toChallengesFromShortDTOs(List<ChallengeShortDTO> challengeShortDTOs) {
        if (challengeShortDTOs == null) return null;
        return challengeShortDTOs.stream().map(shortDto -> {
            Challenge c = new Challenge();
            c.setUuid(shortDto.getUuid());
            return c;
        }).collect(Collectors.toList());
    }

    // Explicit mapping for nested multimedia to help MapStruct
    default MultimediaDTO toMultimediaDTO(MultimediaInfo info) {
        if (info == null) return null;
        MultimediaDTO dto = new MultimediaDTO();
        dto.setUuid(info.getUuid());
        dto.setUrl(info.getUrl());
        dto.setType("image");
        return dto;
    }

    default List<ChallengeShortDTO> toChallengeShortDTOs(List<Challenge> challenges) {
        if (challenges == null) return null;
        return challenges.stream().map(challenge -> {
            ChallengeShortDTO dto = new ChallengeShortDTO();
            dto.setUuid(challenge.getUuid());
            dto.setName(challenge.getName());
            return dto;
        }).collect(Collectors.toList());
    }
} 