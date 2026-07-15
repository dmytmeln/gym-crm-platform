@component @gym-core @create-training
Feature: Create Training

  Rule: A trainer creates a valid Training

    @happy-path
    Scenario: Successful Training creation is persisted and published
      Given an active trainer and trainee exist
      When the trainer creates the Training
      Then Training creation succeeds
      And the Training is visible through the trainer schedule
      And exactly one matching ADD workload update is published

  Rule: Creation requires an authorized active trainer

    @authn
    Scenario: Anonymous creation
      Given an active trainer and trainee exist
      When the caller creates the Training without a token
      Then the request is rejected with status 401 and error code 2805
      And the response challenges Bearer authentication

    @authn
    Scenario: Invalid token
      Given an active trainer and trainee exist
      When the caller creates the Training with an invalid token
      Then the request is rejected with status 401 and error code 2805
      And the response challenges Bearer authentication

    @authn
    Scenario: Logged-out token
      Given an active trainer and trainee exist
      And the trainer token was logged out
      When the trainer creates the Training
      Then the request is rejected with status 401 and error code 2805
      And the response challenges Bearer authentication

    @authz
    Scenario: Deactivated trainer
      Given an active trainer and trainee exist
      And the trainer account is deactivated
      When the trainer creates the Training
      Then the request is rejected with status 403 and error code 2807

    @authz
    Scenario: Trainee role
      Given an active trainer and trainee exist
      And the caller is the trainee
      When the trainer creates the Training
      Then the request is rejected with status 403 and error code 2806

    @authz
    Scenario: Trainer acts for another trainer
      Given an active trainer and trainee exist
      And the request names another trainer
      When the trainer creates the Training
      Then the request is rejected with status 403 and error code 2806

  Rule: Invalid Training requests are rejected

    @validation
    Scenario Outline: Required field is omitted
      Given an active trainer and trainee exist
      And the required field "<field>" is omitted
      When the trainer creates the Training
      Then the request is rejected with status 400 and error code 2760

      Examples:
        | field              |
        | traineeUsername    |
        | trainerUsername    |
        | trainingName       |
        | trainingDate       |
        | trainingDuration   |

    @validation
    Scenario: Blank Training name
      Given an active trainer and trainee exist
      And the training name is blank
      When the trainer creates the Training
      Then the request is rejected with status 400 and error code 2760

    @validation
    Scenario: Whitespace-only Training name
      Given an active trainer and trainee exist
      And the training name is whitespace only
      When the trainer creates the Training
      Then the request is rejected with status 400 and error code 2760

    @validation
    Scenario: Oversized Training name
      Given an active trainer and trainee exist
      And the training name contains 101 characters
      When the trainer creates the Training
      Then the request is rejected with status 400 and error code 2760

    @validation
    Scenario: Zero duration
      Given an active trainer and trainee exist
      And the training duration is zero
      When the trainer creates the Training
      Then the request is rejected with status 400 and error code 2760

    @validation
    Scenario: Oversized username
      Given an active trainer and trainee exist
      And the trainee username contains 221 characters
      When the trainer creates the Training
      Then the request is rejected with status 400 and error code 2760

    @validation
    Scenario: Invalid date
      Given an active trainer and trainee exist
      And the training date is invalid
      When the trainer creates the Training
      Then the request is rejected with status 400 and error code 2760

    @validation
    Scenario: Malformed JSON
      Given an active trainer and trainee exist
      When the caller sends malformed JSON
      Then the request is rejected with status 400 and error code 2760

    @validation
    Scenario: Unsupported content type
      Given an active trainer and trainee exist
      When the caller sends an unsupported content type
      Then the request is rejected with status 415 and error code 3200

  Rule: Referenced participants must exist

    @validation
    Scenario: Unknown trainee
      Given an active trainer and trainee exist
      And the request names an unknown trainee
      When the trainer creates the Training
      Then the request is rejected with status 404 and error code 2835
