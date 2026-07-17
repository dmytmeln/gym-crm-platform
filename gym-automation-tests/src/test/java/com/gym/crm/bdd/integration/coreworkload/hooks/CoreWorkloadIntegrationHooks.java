package com.gym.crm.bdd.integration.coreworkload.hooks;

import com.gym.crm.bdd.integration.coreworkload.support.CoreWorkloadIntegrationStack;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

public class CoreWorkloadIntegrationHooks {

    @Before("@core-workload")
    public void startStack() {
        CoreWorkloadIntegrationStack.start();
    }

    @After("@core-workload")
    public void printLogsForFailure(Scenario scenario) {
        if (!scenario.isFailed()) {
            return;
        }

        CoreWorkloadIntegrationStack.printServiceLogs();
    }

    @AfterAll
    @SuppressWarnings("unused")
    public static void stopStack() {
        CoreWorkloadIntegrationStack.stop();
    }

}
