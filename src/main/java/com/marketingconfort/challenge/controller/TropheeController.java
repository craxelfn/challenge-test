package com.marketingconfort.challenge.controller;

import com.marketingconfort.challenge.dto.TropheeDTO;
import com.marketingconfort.challenge.enums.TropheeType;
import com.marketingconfort.challenge.service.TropheeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/trophees")
@RequiredArgsConstructor
public class TropheeController {
    private final TropheeService tropheeService;

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<TropheeDTO> createTrophee(
            @RequestParam("titre") String titre,
            @RequestParam("type") TropheeType type,
            @RequestParam("description") String description,
            @RequestParam("scoreMin") double scoreMin,
            @RequestParam("tempsMaximum") int tempsMaximum,
            @RequestParam("tentativeMaximum") int tentativeMaximum,
            @RequestParam("allQuestionsNeedToValide") boolean allQuestionsNeedToValide,
            @RequestParam("challengeUuids") List<String> challengeUuids,
            @RequestPart("icone") MultipartFile icone
    ) {
        TropheeDTO dto = tropheeService.createTrophee(
            titre, type, description, scoreMin, tempsMaximum, tentativeMaximum, allQuestionsNeedToValide, challengeUuids, icone
        );
        return ResponseEntity.ok(dto);
    }

    @PutMapping(value = "/{uuid}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<TropheeDTO> updateTrophee(
            @PathVariable String uuid,
            @RequestParam("titre") String titre,
            @RequestParam("type") TropheeType type,
            @RequestParam("description") String description,
            @RequestParam("scoreMin") double scoreMin,
            @RequestParam("tempsMaximum") int tempsMaximum,
            @RequestParam("tentativeMaximum") int tentativeMaximum,
            @RequestParam("allQuestionsNeedToValide") boolean allQuestionsNeedToValide,
            @RequestParam("challengeUuids") List<String> challengeUuids,
            @RequestPart(value = "icone", required = false) MultipartFile icone
    ) {
        TropheeDTO dto = tropheeService.updateTrophee(
            uuid, titre, type, description, scoreMin, tempsMaximum, tentativeMaximum, allQuestionsNeedToValide, challengeUuids, icone
        );
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteTrophee(@PathVariable String uuid) {
        tropheeService.deleteTrophee(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<TropheeDTO> getTropheeByUuid(@PathVariable String uuid) {
        TropheeDTO dto = tropheeService.getTropheeByUuid(uuid);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/by-challenge/{challengeUuid}")
    public ResponseEntity<org.springframework.data.domain.Page<TropheeDTO>> getTropheesByChallengeUuid(
            @PathVariable String challengeUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(tropheeService.getTropheesByChallengeUuid(challengeUuid, page, size));
    }
} 