package com.gym.crm.core.controller;

import com.gia.openapi.model.ActivationStatusRequest;
import com.gia.openapi.model.GetTrainerTrainingResponse;
import com.gia.openapi.model.TrainerCreateRequest;
import com.gia.openapi.model.TrainerCreateResponse;
import com.gia.openapi.model.TrainerGetResponse;
import com.gia.openapi.model.TrainerUpdateRequest;
import com.gia.openapi.model.TrainerUpdateResponse;
import com.gym.crm.core.dto.filter.TrainerTrainingSearchFilter;
import com.gym.crm.core.facade.GymFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${app.api.base-path}/trainers")
@RequiredArgsConstructor
public class TrainerRestController {

    private final GymFacade facade;

    @PostMapping("/register")
    public ResponseEntity<TrainerCreateResponse> registerTrainer(@Valid @RequestBody TrainerCreateRequest trainerCreateRequest) {
        return ResponseEntity.ok(facade.createTrainer(trainerCreateRequest));
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerGetResponse> getTrainerProfile(@PathVariable String username) {
        return ResponseEntity.ok(facade.getTrainerByUsername(username));
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<GetTrainerTrainingResponse>> getTrainerTrainings(@PathVariable String username,
                                                                                @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                                @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                                                @RequestParam(value = "traineeName", required = false) String traineeName) {
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder()
                .username(username)
                .fromDate(fromDate)
                .toDate(toDate)
                .traineeName(traineeName)
                .build();

        return ResponseEntity.ok(facade.getTrainerTrainings(filter));
    }

    @PutMapping("/{username}")
    public ResponseEntity<TrainerUpdateResponse> updateTrainerProfile(@PathVariable String username,
                                                                      @Valid @RequestBody TrainerUpdateRequest trainerUpdateRequest) {
        return ResponseEntity.ok(facade.updateTrainer(username, trainerUpdateRequest));
    }

    @PatchMapping("/{username}/activation")
    public ResponseEntity<Void> changeTrainerActivationStatus(@PathVariable String username,
                                                              @Valid @RequestBody ActivationStatusRequest activationStatusRequest) {
        facade.updateTrainerActivationStatus(username, activationStatusRequest.getIsActive());
        return ResponseEntity.ok().build();
    }

}
