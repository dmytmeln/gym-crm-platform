package com.gym.crm.bdd.gymcore.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTraineeRequest {

    @Setter
    private String firstName;
    @Setter
    private String lastName;
    @Setter
    private String username;
    @Setter
    private String password;
    @Setter
    private String dateOfBirth;
    @Setter
    private String address;

}
