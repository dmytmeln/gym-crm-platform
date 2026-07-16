package com.gym.crm.bdd.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainerRequest {

    @Setter
    private String firstName;
    @Setter
    private String lastName;
    @Setter
    private String specialization;

}
