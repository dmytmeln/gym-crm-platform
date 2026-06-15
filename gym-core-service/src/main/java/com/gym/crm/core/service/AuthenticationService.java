package com.gym.crm.core.service;

import com.gym.crm.core.dto.LoginChangeDto;
import com.gym.crm.core.dto.LoginRequestDto;

public interface AuthenticationService {

    String login(LoginRequestDto loginRequestDto);

    void changePassword(LoginChangeDto loginChangeDto);

    void logout(String token);

}
