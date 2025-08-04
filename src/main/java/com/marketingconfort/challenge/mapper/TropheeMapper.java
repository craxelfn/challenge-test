package com.marketingconfort.challenge.mapper;

import com.marketingconfort.challenge.dto.TropheeDTO;
import com.marketingconfort.challenge.models.Trophee;
import com.marketingconfort.challenge.models.Challenge;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TropheeMapper {
    @Mapping(target = "challengeUuids", expression = "java(toChallengeUuids(entity.getChallenges()))")
    @Mapping(source = "uuid", target = "uuid")
    @Mapping(source = "iconeMultimediaInfo", target = "icone")
    TropheeDTO toDto(Trophee entity);

    @Mapping(target = "challenges", expression = "java(toChallenges(dto.getChallengeUuids()))")
    @Mapping(source = "uuid", target = "uuid")
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateUpdate", ignore = true)
    @Mapping(target = "iconeMultimediaInfo", ignore = true) // handled separately in service
    @Mapping(target = "id", ignore = true)
    Trophee toEntity(TropheeDTO dto);

    default List<String> toChallengeUuids(List<Challenge> challenges) {
        if (challenges == null) return null;
        return challenges.stream().map(Challenge::getUuid).collect(Collectors.toList());
    }

    default List<Challenge> toChallenges(List<String> challengeUuids) {
        if (challengeUuids == null) return null;
        return challengeUuids.stream().map(uuid -> {
            Challenge c = new Challenge();
            c.setUuid(uuid);
            return c;
        }).collect(Collectors.toList());
    }
} 