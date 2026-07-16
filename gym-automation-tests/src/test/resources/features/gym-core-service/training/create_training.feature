@component @gym-core @create-training
Feature: Create Training

  Background:
    Given trainer is created with:
      | firstName        | lastName  | specialization |
      | TrainerComponent | Component | CARDIO         |
    And trainee is created with:
      | firstName        | lastName  | dateOfBirth | address     |
      | TraineeComponent | Component | 1995-05-10  | Test Street |
    And training request is created with name "Component Training", date offset 7 days and duration 60 minutes
    And caller is trainer

  Rule: Trainer creates valid Training

    @happy-path
    Scenario: Successful Training creation is persisted and published
      When trainer creates Training
      Then Training creation succeeds
      And Training is visible through trainer schedule
      And exactly one matching ADD workload update is published

  Rule: Creation requires authorized active trainer

    @authn
    Scenario: Anonymous creation
      When caller creates Training without token
      Then request is rejected with status 401 and error code 2805
      And response challenges Bearer authentication

    @authn
    Scenario: Invalid token
      When caller creates Training with invalid token
      Then request is rejected with status 401 and error code 2805
      And response challenges Bearer authentication

    @authn
    Scenario: Logged-out token
      And trainer token was logged out
      When trainer creates Training
      Then request is rejected with status 401 and error code 2805
      And response challenges Bearer authentication

    @authz
    Scenario: Deactivated trainer
      And trainer account is deactivated
      When trainer creates Training
      Then request is rejected with status 403 and error code 2807

    @authz
    Scenario: Trainee role
      And caller is trainee
      When trainer creates Training
      Then request is rejected with status 403 and error code 2806

    @authz
    Scenario: Trainer acts for another trainer
      And request names another trainer
      When trainer creates Training
      Then request is rejected with status 403 and error code 2806

  Rule: Invalid Training requests are rejected

    @validation
    Scenario Outline: Required field is omitted
      And required field "<field>" is omitted
      When trainer creates Training
      Then request is rejected with status 400 and error code 2760

      Examples:
        | field            |
        | traineeUsername  |
        | trainerUsername  |
        | trainingName     |
        | trainingDate     |
        | trainingDuration |

    @validation
    Scenario: Blank Training name
      And training name is blank
      When trainer creates Training
      Then request is rejected with status 400 and error code 2760

    @validation
    Scenario: Whitespace-only Training name
      And training name is whitespace only
      When trainer creates Training
      Then request is rejected with status 400 and error code 2760

    @validation
    Scenario: Oversized Training name
      And training name contains 101 characters
      When trainer creates Training
      Then request is rejected with status 400 and error code 2760

    @validation
    Scenario: Zero duration
      And training duration is zero
      When trainer creates Training
      Then request is rejected with status 400 and error code 2760

    @validation
    Scenario: Oversized username
      And trainee username contains 221 characters
      When trainer creates Training
      Then request is rejected with status 400 and error code 2760

    @validation
    Scenario: Invalid date
      And training date is invalid
      When trainer creates Training
      Then request is rejected with status 400 and error code 2760

    @validation
    Scenario: Malformed JSON
      When caller sends malformed JSON
      Then request is rejected with status 400 and error code 2760

    @validation
    Scenario: Unsupported content type
      When caller sends unsupported content type
      Then request is rejected with status 415 and error code 3200

  Rule: Referenced participants must exist

    @validation
    Scenario: Unknown trainee
      And request names unknown trainee
      When trainer creates Training
      Then request is rejected with status 404 and error code 2835
