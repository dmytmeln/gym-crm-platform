package com.gym.crm.core.controller;

import com.gia.openapi.model.ActivationStatusRequest;
import com.gia.openapi.model.AssignedTrainerResponse;
import com.gia.openapi.model.GetTraineeTrainingResponse;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateRequest;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateResponse;
import com.gia.openapi.model.TraineeCreateRequest;
import com.gia.openapi.model.TraineeCreateResponse;
import com.gia.openapi.model.TraineeGetResponse;
import com.gia.openapi.model.TraineeUpdateRequest;
import com.gia.openapi.model.TraineeUpdateResponse;
import com.gym.crm.core.dto.filter.TraineeTrainingSearchFilter;
import com.gym.crm.core.facade.GymFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("${app.api.base-path}/trainees")
@RequiredArgsConstructor
public class TraineeRestController {

    private final GymFacade facade;

    @PostMapping("/register")
    public ResponseEntity<TraineeCreateResponse> registerTrainee(@Valid @RequestBody TraineeCreateRequest traineeCreateRequest) {
        return ResponseEntity.ok(facade.createTrainee(traineeCreateRequest));
    }

    @GetMapping("/{username}")
    public ResponseEntity<TraineeGetResponse> getTraineeProfile(@PathVariable String username) {
        return ResponseEntity.ok(facade.getTraineeByUsername(username));
    }

    @GetMapping("/{username}/available-trainers")
    public ResponseEntity<List<AssignedTrainerResponse>> getAvailableTrainers(@PathVariable String username) {
        return ResponseEntity.ok(facade.getAvailableTrainersForTrainee(username));
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<GetTraineeTrainingResponse>> getTraineeTrainings(@PathVariable String username,
                                                                                @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                                                @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                                                                @RequestParam(value = "trainerName", required = false) String trainerName,
                                                                                @RequestParam(value = "trainingType", required = false) String trainingType) {
        TraineeTrainingSearchFilter filter = TraineeTrainingSearchFilter.builder()
                .username(username)
                .fromDate(fromDate)
                .toDate(toDate)
                .trainerName(trainerName)
                .trainingTypeName(trainingType)
                .build();

        return ResponseEntity.ok(facade.getTraineeTrainings(filter));
    }

    @PutMapping("/{username}")
    public ResponseEntity<TraineeUpdateResponse> updateTraineeProfile(@PathVariable String username,
                                                                      @Valid @RequestBody TraineeUpdateRequest traineeUpdateRequest) {
        return ResponseEntity.ok(facade.updateTrainee(username, traineeUpdateRequest));
    }

    @PutMapping("/{username}/trainers")
    public ResponseEntity<TraineeAssignedTrainersUpdateResponse> updateTraineeTrainers(@PathVariable String username,
                                                                                       @Valid @RequestBody TraineeAssignedTrainersUpdateRequest traineeAssignedTrainersUpdateRequest) {
        return ResponseEntity.ok(facade.updateTraineeTrainers(username, traineeAssignedTrainersUpdateRequest));
    }

    @PatchMapping("/{username}/activation")
    public ResponseEntity<Void> changeTraineeActivationStatus(@PathVariable String username,
                                                              @Valid @RequestBody ActivationStatusRequest activationStatusRequest) {
        facade.updateTraineeActivationStatus(username, activationStatusRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable String username) {
        facade.deleteTraineeByUsername(username);
        return ResponseEntity.ok().build();
    }

}
