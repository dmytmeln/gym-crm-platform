package com.gym.crm.bdd.gymcore.hooks;

import com.gym.crm.bdd.gymcore.support.GymCoreComponentStack;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;

@SuppressWarnings("unused")
public class GymCoreComponentHooks {

    @Before("@gym-core-service")
    public void startStack() {
        GymCoreComponentStack.start();
    }

    @AfterAll
    public static void stopStack() {
        GymCoreComponentStack.stop();
    }

}
