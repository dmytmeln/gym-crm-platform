package com.gym.crm.workload.controller;

import com.gia.openapi.model.TrainerWorkloadResponse;
import com.gia.openapi.model.TrainerWorkloadUpdateRequest;
import com.gym.crm.workload.dto.TrainerWorkloadUpdate;
import com.gym.crm.workload.dto.TrainerWorkloadSearchFilter;
import com.gym.crm.workload.mapper.TrainerWorkloadMapper;
import com.gym.crm.workload.service.TrainerWorkloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Month;

@RestController
@RequestMapping("${app.api.base-path}/trainer-workloads")
@RequiredArgsConstructor
public class TrainerWorkloadsRestController {

    private final TrainerWorkloadService service;
    private final TrainerWorkloadMapper mapper;

    @PostMapping
    public ResponseEntity<Void> updateTrainerWorkload(@Valid @RequestBody TrainerWorkloadUpdateRequest trainerWorkloadUpdateRequest) {
        TrainerWorkloadUpdate update = mapper.toDomainUpdate(trainerWorkloadUpdateRequest);
        service.updateWorkload(update);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}")
    public ResponseEntity<TrainerWorkloadResponse> getTrainerWorkload(@PathVariable String username,
                                                                      @RequestParam Integer year,
                                                                      @RequestParam Month month) {
        TrainerWorkloadSearchFilter filter = TrainerWorkloadSearchFilter.builder()
                .username(username)
                .year(year)
                .month(month)
                .build();

        Integer hours = service.getWorkingHours(filter);

        TrainerWorkloadResponse response = new TrainerWorkloadResponse(hours);
        return ResponseEntity.ok(response);
    }

}
