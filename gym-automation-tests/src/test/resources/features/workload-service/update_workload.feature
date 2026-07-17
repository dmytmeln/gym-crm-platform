@component @workload-service @update-workload
Feature: Update trainer workload

  Background:
    Given authenticated trainer

  @happy-path @add
  Scenario: ADD update is consumed and exposed as monthly workload
    Given ADD workload update for 60 minutes in JULY 2026
    When workload update is published
    Then trainer monthly workload becomes 60 minutes

  @happy-path @delete
  Scenario: DELETE update subtracts monthly workload
    Given existing workload of 90 minutes in JULY 2026
    And DELETE workload update for 30 minutes in JULY 2026
    When workload update is published
    Then trainer monthly workload becomes 60 minutes

  @negative @validation
  Scenario: Negative duration update is rejected
    Given ADD workload update for -1 minutes in JULY 2026
    When workload update is published
    Then trainer monthly workload remains 0 minutes

  @edge-case @delete
  Scenario: DELETE exceeding monthly workload removes month summary
    Given existing workload of 30 minutes in JULY 2026
    And DELETE workload update for 60 minutes in JULY 2026
    When workload update is published
    Then trainer monthly workload becomes 0 minutes
