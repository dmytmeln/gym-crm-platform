package com.gym.crm.bdd.hooks;

import com.gym.crm.bdd.support.GymCoreComponentStack;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;

public class GymCoreComponentHooks {

    @BeforeAll
    public static void startStack() {
        GymCoreComponentStack.start();
    }

    @AfterAll
    public static void stopStack() {
        GymCoreComponentStack.stop();
    }

}
