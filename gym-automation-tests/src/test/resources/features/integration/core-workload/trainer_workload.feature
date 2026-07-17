@integration @core-workload @trainer-workload
Feature: Training lifecycle updates Trainer Workload

  Background:
    Given unique trainer and trainee registered through Gym Core

  @happy-path
  Scenario: Training creation increases Trainer Workload
    When trainer creates 60-minute Training through Gym Core
    Then Gym Core accepts operation
    And Trainer Workload eventually becomes 60 minutes

  @cascade-delete
  Scenario: Trainee deletion cascades Training removal and decreases Trainer Workload
    Given 60-minute Training exists and is reflected in Trainer Workload
    When trainee deletes profile through Gym Core
    Then Gym Core accepts operation
    And Trainer Workload eventually becomes 0 minutes

  @negative
  Scenario: Rejected Training creation leaves Trainer Workload unchanged
    Given 60-minute Training exists and is reflected in Trainer Workload
    When trainer creates Training for unknown trainee
    Then Gym Core rejects Training as not found
    And Trainer Workload remains 60 minutes during delivery window
