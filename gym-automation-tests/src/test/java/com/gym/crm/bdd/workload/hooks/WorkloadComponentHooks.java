package com.gym.crm.bdd.workload.hooks;

import com.gym.crm.bdd.workload.support.WorkloadComponentStack;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;

public class WorkloadComponentHooks {

    @Before("@workload-service")
    public void startStack() {
        WorkloadComponentStack.start();
    }

    @AfterAll
    public static void stopStack() {
        WorkloadComponentStack.stop();
    }

}
